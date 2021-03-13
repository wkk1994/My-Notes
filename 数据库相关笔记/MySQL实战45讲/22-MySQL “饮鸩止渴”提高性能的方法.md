# 22- MySQL “饮鸩止渴”提高性能的方法

## 短连接风暴

正常的短连接模式就是连接到数据库后，执行很少的 SQL 语句就断开，下次需要的时候再重连。如果使用的是短连接，在业务高峰期的时候，就可能出现连接数突然暴涨的情况。

参数`max_connections`用来控制MySQL的连接数，如果连接数超过这个上限，后续的连接请求会被拒绝，并且提示“Too many connections”。对于被拒绝的请求来说，从业务角度就是数据库服务不可用。如果一味提高`max_connections`的值，让更多连接进来，那么系统的负载可能会进一步加大，大量的资源耗费在权限验证等逻辑上，结果可能适得其反，已经连接的线程拿不到cpu资源去执行业务SQL。

### 解决方法一：处理掉占着连接不工作的线程

有些线程已经执行完sql命令但是还是没有主动释放连接。使用`show processlis;`命令查看连接，`kill`掉显示为sleep的线程。但是也是可能有损的，可能有些连接还没有提交事务，直接kill导致事务回滚。可以通过表`information_schema.innodb_trx`查询在事务中的连接，避免kill事务中的连接，除非确定不会影响正常业务。

从数据库端主动断开连接可能是有损的，连接被服务端主动断开后，这个客户端并不会马上知道。直到客户端在发起下一个请求的时候，才会收到这样的报错“ERROR 2013 (HY000): Lost connection to MySQL server during query”。可能导致客户端一直重试，或者认为“MySQL一直没恢复”。

### 解决方法二：减少连接过程的消耗

* 跳过权限验证
    跳过权限验证的方法是：重启数据库，并使用–skip-grant-tables 参数启动。这样，整个 MySQL 会跳过所有的权限验证阶段，包括连接过程和语句执行过程在内。但是，这种方法风险极高，是我特别不建议使用的方案。尤其你的库外网可访问的话，就更不能这么做了。在 MySQL 8.0 版本里，如果你启用–skip-grant-tables 参数，MySQL 会默认把 --skip-networking 参数打开，表示这时候数据库只能被本地的客户端连接。


除了短连接数暴增可能会带来性能问题外，实际上在线上碰到更多的是查询或者更新语句导致的性能问题。查询问题比较典型的有两类，一类是由新出现的慢查询导致的，一类是由 QPS（每秒查询数）突增导致的。

## 慢查询性能问题

在 MySQL 中，会引发性能问题的慢查询，大体有以下三种可能：

* 索引没有设计好；
* SQL 语句没写好；
* MySQL 选错了索引。

### 索引没有设计好

这样的情况一般是创建索引解决。MySQL5.6版本以后，创建索引都支持Online DDL。

比较理想的是能够在备库先执行。假设现在的服务是一主一备，主库 A、备库 B，这个方案的大致流程是这样的：

1.在备库 B 上执行 set sql_log_bin=off，也就是不写 binlog，然后执行 alter table 语句加上索引；

2.执行主备切换；

3.这时候主库是 B，备库是 A。在 A 上执行 set sql_log_bin=off，然后执行 alter table 语句加上索引。

这是一个“古老”的 DDL 方案。平时在做变更的时候，应该考虑类似 gh-ost 这样的方案，更加稳妥。但是在需要紧急处理时，上面这个方案的效率是最高的。

### SQL语句没有写好

如果是SQL语句没有写好，导致选择索引错误或者没有选择索引，可以通过改写SQL语句处理。MySQL5.7提供了query_rewrite功能，可以把输入的SQL语句改写成另一种模式。

* `install_rewriter`安装
    执行官方准备好的安装语句`install_rewriter.sql`，会自动安装插件，创建一个新的数据库`query_rewrite`其中包含一张表rewrite_rules，用于定义重写的规则，也可以用于管理规则。

* 使用重写规则
    如规则`select k, id from sbtest1 where k = ?;`改写为`select k, id from sbtest1 force index(primary) where k = ?;`

    * 先插入规则
    
        ```sql
        insert into query_rewrite.rewrite_rules(pattern, replacement, pattern_database) values (
        "select k, id from sbtest1 where k = ?",
        "select k, id from sbtest1 force index(primary)  where k = ?","sb1");
        ```

    * 然后调用存储过程: `CALL query_rewrite.flush_rewrite_rules();`
        
        该存储过程先提交当前会话的事务（如果有未提交的事务），然后调用一个UDF函数load_rewite_rules将规则加载到插件的内存中。

    * 测试：

        ```sql
        mysql> select k, id from sbtest1 where k = 19618;
        mysql> show warnings;
        *************************** 1. row ***************************
        Level: Note
        Code: 1105
        Message: Query 'select k, id from sbtest1 where k = 19618' rewritten to 'SELECT k, id from sbtest1 force index(primary)  where k = 19618' by a query rewrite plugin
        1 row in set (0.00 sec)
        ```

开源工具 pt-query-digest(https://www.percona.com/doc/percona-toolkit/3.0/pt-query-digest.html)检查所有的 SQL 语句的返回结果

### MySQL 选错了索引

如果MySQL选错了索引可以使用`force index(index_name)`指定使用的索引。在上线前做一次全量回归测试也很有必要。

## QPS 突增问题

有时候由于业务突然出现高峰，或者应用程序 bug，导致某个语句的 QPS 突然暴涨，也可能导致 MySQL 压力过大，影响服务。

解決的方案:
1.如果是全新的业务导致的bug可以下线业务，将服务器从数据库白名单里去除。这个需要协调才能解决，毕竟是全面下线业务。

2.如果新功能使用的是新的数据库用户，可以将用户删除，然后断开现有的连接。这样，这个新功能的连接不成功，由它引发的QPS就会变成0；

3.如果这个新增的功能和主题功能部署在一起，那么可以把压力最大的 SQL 语句直接重写成"select 1"返回。这可能产生误伤，以及对后续依赖这个查询业务的功能出现错误。