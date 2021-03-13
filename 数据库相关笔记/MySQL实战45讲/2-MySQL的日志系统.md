# MySQL的日志系统

MySQL的更新流程涉及到两个重要的日志模块。一个是InnoDB特有的 redo log（重做日志），一个是Server层有的 binlog（归档日志）。

## redo log

每次更新操作，数据引擎都要频繁将修改写入磁盘效率太低，所以将当前修改先写入文件中，保存在内存里。等数据库空闲或者文件写满时，再写入磁盘中，这样大大提高效率。这种方式是MySQL的WAL技术，全称Write-Ahead Logging（预写式日志），它的关键点就是先写日志，再写磁盘。

### 简介

redo log是InnoDB才有的日志，属于物理日志，记录的是“在某个数据页上做了什么修改”，大小固定。
redo log使用循环写的方式，当文件大小不够时，会将一部分内容写到磁盘中。有了 redo log，InnoDB 就可以保证即使数据库发生异常重启，之前提交的记录都不会丢失，这个能力称为crash-safe。

redo log包括两部分： 1.内存中的日志缓存（redo log buffer），该部分是容易丢失的。2.磁盘上的重做日志文件（redo log file 这个log file并不是磁盘上的物理日志文件，而是操作系统缓存中的log file），该部分日志是持久的。

### redo log的作用

确保事务的持久性。防止在发生故障的时间点，尚有脏页未写入磁盘，在重启mysql服务的时候，根据redo log进行重做，从而达到事务的持久性这一特性。

### redo log日志文件

每个InnoDB存储引擎至少有1个重做日志文件组（group），每个文件组至少有2个重做日志文件，如默认的ib_logfile0和ib_logfile1。 

### 影响redo log参数

* `innodb_log_file_size`: 指定每个redo日志大小，默认值48MB;
* `innodb_log_files_in_group`: 指定日志文件组中redo日志文件数量，默认为2;
* `innodb_log_group_home_dir`: 指定日志文件组所在路劲，默认值./，指mysql的数据目录datadir;
* `innodb_log_buffer_size`: redo log buffer大小。

### innodb_flush_log_at_trx_commit

innodb_flush_log_at_trx_commit控制redo log buffer刷到log file中的规则。

* 1： 默认值，事务每次提交都会将log buffer中的日志写入os buffer并调用fsync()刷到log file on disk中。这种方式即使数据库崩溃也不会丢失任何数据，但是IO性能较差。

* 0： 事务提交时不会将log buffer中日志写入os buffer，而是通过master thread写入os buffer并调用fsync()写入到 log file on disk中。当数据库宕机时，会丢失1秒钟的数据。

* 2： 事务提交时将log buffer中日志写入os buffer，通过master thread调用fsync()写入到 log file on disk中。当数据库宕机而操作系统没有宕机时，这部分日志不会丢失；当操作系统宕机时，会丢失1秒钟的数据。

**master thread：** InnoDB一个在后台运行的主线程，主要工作包括但不限于：刷新日志缓冲，合并插入缓冲，刷新脏页等。master thread大致分为每秒运行一次的操作和每10秒运行一次的操作。

## undo log

undo log有两个作用：提供回滚和多个行版本控制(MVCC)。

## binlog

### 简介

binlog（归档日志）属于Server层特有的日志。binlog 是逻辑日志，记录的是这个语句的原始逻辑，比如“给 ID=2 这一行的 c 字段加 1 ”。
binlog使用追加写的方式，当一个文件写到一定大小后会切换到下一个，不会覆盖以前的日志文件。

### 影响binlog的参数

* `max_binlog_size`：指定单个binlog文件最大值。默认值为1g，最大值1g，如果超过该值，则产生新的binlog文件，后缀名+1，并记录到.index文件。

* `binlog_cache_size`：使用事务表存储引擎（如innodb存储引擎）时，所有未提交的binlog日志会被记录到一个缓存中去，等事务提交时再将缓存中的binlog写入到binlog文件中。缓存的大小由binlog_cache_size决定，默认大小为32K。

* `expire_logs_days`：表示binlog文件自动删除N天前的文件。默认值为0，表示不自动删除，最大值99.要手动删除binlog文件，可以使用purge binary logs语句。

## redo log与binlog的不同

* redo log 是 InnoDB 引擎特有的；binlog 是 MySQL 的 Server 层实现的，所有引擎都可以使用。

* redo log 是物理日志，记录的是“在某个数据页上做了什么修改”；binlog 是逻辑日志，记录的是这个语句的原始逻辑，比如“给 ID=2 这一行的 c 字段加 1 ”。

* redo log 是循环写的，空间固定会用完；binlog 是可以追加写入的。“追加写”是指 binlog 文件写到一定大小后会切换到下一个，并不会覆盖以前的日志。

## update的内部流程

```sql
mysql> update T set c=c+1 where ID=2;
```

1. 执行器先找引擎取 ID=2 这一行。ID 是主键，引擎直接用树搜索找到这一行。如果 ID=2 这一行所在的数据页本来就在内存中，就直接返回给执行器；否则，需要先从磁盘读入内存，然后再返回。

2. 执行器拿到引擎给的行数据，把这个值加上 1，比如原来是 N，现在就是 N+1，得到新的一行数据，再调用引擎接口写入这行新数据。

3. 引擎将这行新数据更新到内存中，同时将这个更新操作记录到 redo log 里面，此时 redo log 处于 prepare 状态。然后告知执行器执行完成了，随时可以提交事务。

4. 执行器生成这个操作的 binlog，并把 binlog 写入磁盘。

5. 执行器调用引擎的提交事务接口，引擎把刚刚写入的 redo log 改成提交（commit）状态，更新完成。

### 两阶段提交

为什么会有两阶段提交？是为了让两份日志之间的逻辑一致，如果随便先写一个日志，当写第一个日志完成后，MySQL异常重启了，那么就会导致数据库的状态和用它的日志恢复出来的库的状态不一致。

## 参考

* [redo log和binlog参数](https://blog.csdn.net/wanbin6470398/article/details/81941586) 
* [详细分析MySQL事务日志(redo log和undo log)](https://www.cnblogs.com/f-ck-need-u/archive/2018/05/08/9010872.html#auto_id_16)
* [MySQL中日志文件](https://www.linuxidc.com/Linux/2018-01/150614.htm)

## 问题

* 一天一备跟一周一备的对比？
    
    在一天一备的模式里，最坏情况下需要应用一天的 binlog。比如，你每天 0 点做一次全量备份，而要恢复出一个到昨天晚上 23 点的备份。一周一备最坏情况就要应用一周的 binlog 了。系统的对应指标就是 RTO（恢复目标时间）。当然这个是有成本的，因为更频繁全量备份需要消耗更多存储空间，所以这个 RTO 是成本换来的，就需要根据业务重要性来评估了。

1.首先客户端通过tcp/ip发送一条sql语句到server层的SQL interface
2.SQL interface接到该请求后，先对该条语句进行解析，验证权限是否匹配
3.验证通过以后，分析器会对该语句分析,是否语法有错误等
4.接下来是优化器器生成相应的执行计划，选择最优的执行计划
5.之后会是执行器根据执行计划执行这条语句。在这一步会去open table,如果该table上有MDL，则等待。
如果没有，则加在该表上加短暂的MDL(S)
(如果opend_table太大,表明open_table_cache太小。需要不停的去打开frm文件)
6.进入到引擎层，首先会去innodb_buffer_pool里的data dictionary(元数据信息)得到表信息
7.通过元数据信息,去lock info里查出是否会有相关的锁信息，并把这条update语句需要的
锁信息写入到lock info里(锁这里还有待补充)
8.然后涉及到的老数据通过快照的方式存储到innodb_buffer_pool里的undo page里,并且记录undo log修改的redo
(如果data page里有就直接载入到undo page里，如果没有，则需要去磁盘里取出相应page的数据，载入到undo page里)
9.在innodb_buffer_pool的data page做update操作。并把操作的物理数据页修改记录到redo log buffer里
由于update这个事务会涉及到多个页面的修改，所以redo log buffer里会记录多条页面的修改信息。
因为group commit的原因，这次事务所产生的redo log buffer可能会跟随其它事务一同flush并且sync到磁盘上
10.同时修改的信息，会按照event的格式,记录到binlog_cache中。(这里注意binlog_cache_size是transaction级别的,不是session级别的参数,
一旦commit之后，dump线程会从binlog_cache里把event主动发送给slave的I/O线程)
11.之后把这条sql,需要在二级索引上做的修改，写入到change buffer page，等到下次有其他sql需要读取该二级索引时，再去与二级索引做merge
(随机I/O变为顺序I/O,但是由于现在的磁盘都是SSD,所以对于寻址来说,随机I/O和顺序I/O差距不大)
12.此时update语句已经完成，需要commit或者rollback。这里讨论commit的情况，并且双1
13.commit操作，由于存储引擎层与server层之间采用的是内部XA(保证两个事务的一致性,这里主要保证redo log和binlog的原子性),
所以提交分为prepare阶段与commit阶段
14.prepare阶段,将事务的xid写入，将binlog_cache里的进行flush以及sync操作(大事务的话这步非常耗时)
15.commit阶段，由于之前该事务产生的redo log已经sync到磁盘了。所以这步只是在redo log里标记commit
16.当binlog和redo log都已经落盘以后，如果触发了刷新脏页的操作，先把该脏页复制到doublewrite buffer里，把doublewrite buffer里的刷新到共享表空间，然后才是通过page cleaner线程把脏页写入到磁盘中