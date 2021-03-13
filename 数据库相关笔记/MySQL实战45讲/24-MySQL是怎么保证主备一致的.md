# 24-MySQL是怎么保证主备一致的？

MySQL基于binlog实现主备同步，高可用架构。

## MySQL主备的基本原理

客户端的读写直接访问主库A，备库B只是同步更新主库A的变化，以此保证主库和备库的数据是相同的。当需要进行主备切换到的时候，备库B成为主库，主库A成为备库。

备库一般设置为只读模式（readonly），原因：
* 有时候一些运营类的查询语句会被放到备库上去查，设置为只读可以防止误操作；
* 防止切换逻辑有bug，比如切换过程中出现双写，造成主备不一致；
* 可以用readonly状态，来判断节点的角色。

**备库设置成只读了，还怎么跟主库保持同步更新呢？**
 readonly 设置对超级 (super) 权限用户是无效的，而用于同步更新的线程，就拥有超级权限。

## 主备流程图

![主备流程图](../../../../youdaonote-images/8594FE143B644B67BCF6F98A355C028D.png)

备库 B 跟主库 A 之间维持了一个长连接。主库 A 内部有一个线程，专门用于服务备库 B 的这个长连接。一个事务日志同步的完整过程是这样的：

1.备库 B 上通过 change master 命令，设置主库 A 的 IP、端口、用户名、密码，以及要从哪个位置开始请求 binlog，这个位置包含文件名和日志偏移量。
2.在备库 B 上执行 start slave 命令，这时候备库会启动两个线程，就是图中的 io_thread 和 sql_thread。其中 io_thread 负责与主库建立连接。
3.主库 A 校验完用户名、密码后，开始按照备库 B 传过来的位置，从本地读取 binlog文件，发给 B。（如果要读取的binlog还在page cache中直接读走，否则就要去磁盘读。）
4.备库 B 拿到 binlog 后，写到本地文件，称为中转日志（relay log）。
5.sql_thread 读取中转日志，解析出日志里的命令，并执行。（sql_thread 已经演化成为了多个线程）

## binlog的三种格式对比

* statement格式：
* row格式：
* mixed格式：上面两种格式的混合。

参数`log_bin`表示当前binlog的开启关闭，值为OFF表示binlog关闭，值为ON表示binlog开启。开启方式是在my.cnf中的[mysqld]添加：

```text
server_id=1918 # server id
log_bin = mysql-bin # binlog文件名称
binlog_format = ROW # binlog格式
```

执行语句```sql delete from test24 /*comment*/  where a>=4 and t_modified<='2018-11-10' limit 1;```

**binlog为statement时日志内容：**

执行sql`show binlog events in 'mysql_bin_log.000001';`显示大致内容为：

```text
mysql_bin_log.000001	9725	Anonymous_Gtid	1	9790	SET @@SESSION.GTID_NEXT= 'ANONYMOUS'
mysql_bin_log.000001	9790	Query	1	9877	BEGIN
mysql_bin_log.000001	9877	Query	1	10038	use `test`; delete from test24 /*comment*/  where a>=4 and t_modified<='2018-11-10' limit 1
mysql_bin_log.000001	10038	Xid	1	10069	COMMIT /* xid=271 */
```

可以看到statement 格式下，记录到 binlog 里的是语句原文。这可能存在一中情况：主库执行这条SQL语句的时候用的索引和在备库用的不同，就可能出现备库执行结果和主库不一致的情况。因此MySQL认为这样写时有风险的，会给出`warning`。

**binlog为row时日志内容：**

执行sql`show binlog events in 'mysql_bin_log.000001';`显示大致内容为：

```text
mysql_bin_log.000001	10345	Anonymous_Gtid	1	10410	SET @@SESSION.GTID_NEXT= 'ANONYMOUS'
mysql_bin_log.000001	10410	Query	1	10490	BEGIN
mysql_bin_log.000001	10490	Table_map	1	10542	table_id: 172 (test.test24)
mysql_bin_log.000001	10542	Delete_rows	1	10590	table_id: 172 flags: STMT_END_F
mysql_bin_log.000001	10590	Xid	1	10621	COMMIT /* xid=345 */
```

可以看到，与 statement 格式的 binlog 相比，前后的 BEGIN 和 COMMIT 是一样的。但是，row 格式的 binlog 里没有了 SQL 语句的原文，而是替换成了两个 event：Table_map 和 Delete_rows。

Table_map event，用于说明接下来要操作的表是 test 库的表 t;
Delete_rows event，用于定义删除的行为。

使用mysqlbinlog工具解析和查看binlog中的内容：
`mysqlbinlog  --no-defaults -vv /var/lib/mysql/mysql_bin_log.000001 --start-position=8900;`

```text
BEGIN
/*!*/;
# at 10490
#191125 12:53:04 server id 1  end_log_pos 10542 CRC32 0xeeb7abcc 	Table_map: `test`.`test24` mapped to number 172
# at 10542
#191125 12:53:04 server id 1  end_log_pos 10590 CRC32 0xc34cf7a0 	Delete_rows: table id 172 flags: STMT_END_F

BINLOG '
sM7bXRMBAAAANAAAAC4pAAAAAKwAAAAAAAEABHRlc3QABnRlc3QyNAADAwMRAQACzKu37g==
sM7bXSABAAAAMAAAAF4pAAAAAKwAAAAAAAEAAgAD//gEAAAABAAAAFvlrwCg90zD
'/*!*/;
### DELETE FROM `test`.`test24`
### WHERE
###   @1=4 /* INT meta=0 nullable=0 is_null=0 */
###   @2=4 /* INT meta=0 nullable=1 is_null=0 */
###   @3=1541779200 /* TIMESTAMP(0) meta=0 nullable=0 is_null=0 */
# at 10590
#191125 12:53:04 server id 1  end_log_pos 10621 CRC32 0xe5b03126 	Xid = 345
COMMIT/*!*/;
SET @@SESSION.GTID_NEXT= 'AUTOMATIC' /* added by mysqlbinlog */ /*!*/;
```

* server id 1，表示这个事务是在 server_id=1 的这个库上执行的。
* 每个 event 都有 CRC32 的值，这是因为把参数 binlog_checksum 设置成了 CRC32。
* Table_map event 显示了接下来要打开的表，map 到数字 172。每个表都有一个对应的 Table_map event、都会对应map 到一个单独的数字，用于区分对不同表的操作。现在这个数据172
* binlog_row_image 的默认配置是 FULL，因此 Delete_event 里面，包含了删掉的行的所有字段的值。如果把 binlog_row_image 设置为 MINIMAL，则只会记录必要的信息，在这个例子里，就是只会记录 id=4 这个信息。
* 最后的 Xid event，用于表示事务被正确地提交了。

当 binlog_format 使用 row 格式的时候，binlog 里面记录了真实删除行的主键 id，这样 binlog 传到备库去的时候，就肯定会删除 id=4 的行，不会有主备删除不同行的问题。但是row格式占用的空间比较大，这也是它的一个缺点，因为要存储的内容相比statement要多。

**mixed 格式的 binlog**

由于binlog只使用`statement`可能会导致主备不一致，或只使用`row`占用的空间比较大，所以就有了`mixed`格式。`mixed` 格式的意思是，MySQL 自己会判断这条 SQL 语句是否可能引起主备不一致，如果有可能，就用 `row` 格式，否则就用 `statement` 格式。

**如果线上 MySQL 设置的 binlog 格式是 statement 的话，那基本上就可以认为这是一个不合理的设置。至少应该把 binlog 的格式设置为 mixed。由于row格式的binlog会记录delete/insert/update所有字段的信息，所以可以很容易恢复误操作的数据，越来越多的场景要求把MySQL的binlog格式设置为row**

### 循环复制问题

在双M结构只是AB之间设置为互为主备，不过任何时刻只有一个节点在接受更新的。
在双M结构中可能存在一个问题，当A节点的binlog给B节点执行后，B节点产生的binlog也会被A节点执行，循环复制。

可以使用下面的逻辑解决循环复制问题：

1.规定两个库的 server id 必须不同，如果相同，则它们之间不能设定为主备关系；
2.一个备库接到 binlog 并在重放的过程中，生成与原 binlog 的 server id 相同的新的 binlog；
3.每个库在收到从自己的主库发过来的日志后，先判断 server id，如果跟自己的相同，表示这个日志是自己生成的，就直接丢弃这个日志。

这样双M结构的执行流程为：

1.从节点 A 更新的事务，binlog 里面记的都是 A 的 server id；
2.传到节点 B 执行一次以后，节点 B 生成的 binlog 的 server id 也是 A 的 server id；
3.再传回给节点 A，A 判断到这个 server id 与自己的相同，就不会再处理这个日志。所以，死循环在这里就断掉了。

**主从复制，双M架构数据比对方少的判断依据：**

一开始创建主备关系的时候， 是由备库指定从主库binlog的哪里开始同步，主库就从这个指定位置开始往后发。
而主备复制关系搭建完成以后，是主库来决定“要发数据给备库”的。所以主库有生成新的日志，就会发给备库。