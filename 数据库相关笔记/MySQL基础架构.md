# MySQL基础

## MySQL的基础架构

![MySQL基础架构](/mysql基础架构.png)

大体分为`server层`和`存储器`两部分

* **server层：** 包括连接器、查询缓存、分析器、优化器、执行器等，大多数核心服务功能，包括所有的内置函数，跨存储引擎的功能比如：存储过程、触发器、视图等都是在server层实现。

* **存储引擎：** 负责数据的存储和提取。架构模式为插件式。支持InnoDB、MylSAM、Memory等多个存储引擎。从`MySQL5.5.5版本`开始InnoDB称为默认引擎（InnoDB支持事务）。也可以在建表时指定引擎：`create table T (...)engine = InnoDB`

**注意：** 不同的存储引擎共用一个Server 层

### 连接器

连接器负责根客户端建立连接、获取权限、维持和管理连接。

当用户密码认证通过后，连接器就会到权限表里查出当前用户拥有的权限。之后这个连接里的权限判断都依赖于此时读取到的权限。也就是说即使外部改变了用户权限，当前连接里的权限还是原先的权限不受影响。

通过使用`show processlist`可以查看当前连接。客户端如果太长时间没有动静，连接器就会自动将它断开。这个时间由参数`wait_timeout`控制的，默认是8小时。断开后要重连才能继续使用。`show variables like 'wait_timeout';`

#### 长连接和短连接

建立连接的过程通常比较复杂，建议使用中尽量减少建立连接，就是尽量使用长连接。
但是MySQL在执行过程中临时使用的内存是管理在连接对象里面的，当长连接比较多的时候就会产生MySQL占用的内存涨的快。一直累计下来，可能导致内存占用太大，被系统强行杀掉（OOM），从现象看就是 MySQL 异常重启了。

**解决方法：**

* 定期断开长连接。使用一段时间，或者程序里面判断执行过一个占用内存的大查询后，断开连接，之后要查询再重连。

* MySQL 5.7 或更新版本，可以在每次执行一个比较大的操作后，通过执行 mysql_reset_connection 来重新初始化连接资源。这个过程不需要重连和重新做权限验证，但是会将连接恢复到刚刚创建完时的状态。

### 查询缓存

MySQL查询时，会先到查询缓存中查看是否执行过这条语句。之前执行的语句及其结果可能会议key-value对的形式保存到查询缓存中。如果缓存命中就不需要执行后面复杂的结果直接返回就好。

但是查询缓存时效非常频繁，对一个表进行更新时这个表上的所有查询缓存都会被清空。所以查询缓存的命中率会非常低，除非你的业务就是有一张静态表，很长时间才会更新一次。

#### query_cache_type

参数`query_cache_type`控制MySQL的缓存是否开启0，OFF,缓存禁用； 1，ON,缓存所有的结果；2，DENAND,只缓存在select语句中通过SQL_CACHE指定需要缓存的查询

**注意：** MySQL8.0 版本直接将查询缓存的整块功能删掉

### 分析器

如果没有命中查询缓存，就要开始真正执行语句了。

分析器会先做“词法分析”。识别输入的的SQL语句分别是什么，代表什么。

