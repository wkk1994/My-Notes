# 23-binlog和redo log的写入机制

## binlog的写入机制

事务执行过程中，先把日志写到binlog cache，事务提交的时候，再把binlog cache写到binlog文件中。

参数`binlog_cache_size`控制每个线程binlog cache的大小。系统给binlog cache分配了一片内存，每个线程一个，如果超过这个大小就要暂存到磁盘中。

一个事务的binlog不能拆分，必须要顺序完整的写入到binlog中。写入binlog后，binlog cache会被清空。binlog的写入操作分为2步：

* write操作，把日志写入到文件系统的 page cache，并没有把数据持久化到磁盘，所以速度比较快。
* fsync操作，才是将数据持久化到磁盘的操作。一般情况下，认为 fsync 才占磁盘的 IOPS。

参数`sync_binlog`控制write 和 fsync 的时机：

* `sync_binlog=0`，表示每次提交事务都只write，不fsync；
* `sync_binlog=1`，默认值，表示每次提交事务都会执行fsync；
* `sync_binlog=N(N>1)`，表示每次提交事务都会write，但累计N个事务后才fsync。

在出现IO瓶颈时，将`sync_binlog`设置一个比较大的值，可以提升性能。但是如果主机发生异常重启，会丢失最近N个事务的binlog日志。具体设置什么值要基于业务权衡，常见的设置为100~1000中的某个值。


## redo log的写入机制

 redo log可能存在的三种状态：

![redo log的三种状态](../../../../youdaonote-images/F147CD5C63D847919D48EAE84494E458.png)

* 1.存在redo log buffer中，物理上是MySQL进程内存中，上图的红色部分；
* 2.写到磁盘（write），但是没有持久化（fsync），物理上是文件系统的page cache里面，上图的黄色部分。
* 3.持久化到磁盘，对应的是hard disk，上图的绿色部分。

日志写到redo log buffer是很快的，write到page cache也差不多，但是持久化到磁盘的数据就慢很多。

参数`innodb_flush_log_at_trx_commit`控制redo log的写入策略：

* 设置为0的时候，表示每次事务提交时都只是把redo log留在redo log buffer中；
* 设置为1的时候，表示每次事务提交时都将redo log直接持久化到磁盘；
* 设置为2的时候，表示每次事务提交时只是把redo log写到page cache。

InnoDB有一个后台线程，每隔1秒，就会把redo log buffer中的日志，调用write写到文件系统的page cache，然后调用fsync持久化到磁盘。

**注意：** redo log buffer是所有线程共用的，并且事务执行中间过程的 redo log 也是直接写在 redo log buffer 中的，这些 redo log 也会被后台线程一起持久化到磁盘。也就是说，一个没有提交的事务的 redo log，也是可能已经持久化到磁盘的。

除了后台线程轮询操作外，还有两种场景会让一个没有提交的事务的 redo log 写入到磁盘中。

* 一种是，redo log buffer占用的空间即将达到innodb_log_buffer_size一半的时候，后台线程会主动写盘。注意，由于这个事务并没有提交，所以这个写盘动作只是 write，而没有调用 fsync，也就是只留在了文件系统的 page cache。
* 另一种是，并行的事务提交的时候，顺带将这个事务的 redo log buffer 持久化到磁盘。

说明：在两阶段提交的时候，时序上 redo log 先 prepare， 再写 binlog，最后再把 redo log commit。如果 `innodb_flush_log_at_trx_commit=1`，那么在redo log的prepare阶段就要持久化一次，因为有一个崩溃恢复逻辑是要依赖于prepare的redo log，再加上binlog来恢复的。后台线程的轮询刷盘，再加上崩溃恢复这个逻辑，InnoDB就认为在redo log commit的时候不需要fsync了，只write到文件系统的page cache中就够了。

当MySQL数据库是“双1配置”（sync_binlog 和 innodb_flush_log_at_trx_commit 都设置成 1）的时候，一个事务的完整提交前，需要等待两次刷盘，一次是redo log（prepare阶段），一次是binlog。

### 组提交（group commit）机制

在实际中，MySQL的TPS会高于磁盘能力，如TPS为每秒两万，每秒就会写四万磁盘，但是工具测试磁盘能为为两万左右。这是为啥子呢？？这就是组提交的原因。

**日志逻辑序列号（log sequence number，LSN）**：LSN是单调递增的，用来对应redo log的一个个写入点。每次写入长度为length的redo log，LSN的值就会加上length。LSN 也会写到 InnoDB 的数据页中，来确保数据页不会被多次执行重复的 redo log。

组提交的过程：

* 第一个事务trx1到达 redo log buffer会成为这组的leader；
* 等到第一个事务trx1开始写盘时，这是组内可能会有多个事务，这个时候LSN也变成了组内最大的LSN（160）；
* 第一个事务trx1写盘的时候，带的LSN就是新的LSN，因此等第一个事务trx1返回的时候，所有 LSN 小于等于 组内最大的LSN（160） 的 redo log，都已经被持久化到磁盘；
* 这时候其他在这个组的事务就可以直接返回了。

所以，一次组提交里面，组员越多，节约磁盘IOPS的效果越好。在并发更新场景下，第一个事务写完 redo log buffer 以后，接下来这个 fsync 越晚调用，组员可能越多，节约 IOPS 的效果就越好。

为了让一次 fsync 带的组员更多，MySQL 有一个优化：拖时间。在两阶段提交中，“写binlog”在实际上分为两步：
1.先把binlog从binlog cache中写到磁盘上的binlog文件；
2.调用fsync优化。

![组提交](../../../../youdaonote-images/698B4C9353494DB2ADF61A7CCF62938D.png)

如上图，binlog也可以使用组提交。但是 binlog 的 write 和 fsync 间的间隔时间短，导致能集合到一起持久化的 binlog 比较少，因此 binlog 的组提交的效果通常不如 redo log 的效果那么好。

通过设置参数`binlog_group_commit_sync_delay`和`binlog_group_commit_sync_no_delay_count`可以提升binlog组提交的效果。`binlog_group_commit_sync_delay`参数，表示延迟多少微秒后才调用 fsync;`binlog_group_commit_sync_no_delay_count`参数，表示累积多少次以后才调用 fsync。当 binlog_group_commit_sync_delay 设置为 0 的时候，binlog_group_commit_sync_no_delay_count 也无效了。

**参数sync_binlog，binlog_group_commit_sync_delay，binlog_group_commit_sync_no_delay_count的逻辑：**

先判断当前`binlog_group_commit_sync_delay`或`binlog_group_commit_sync_no_delay_count`是否满足其中一个，满足继续判断`sync_binlog`，如果`sync_binlog`等于0则不fsync。如果`sync_binlog`大于0，判断当前事务数是否大于等于`sync_binlog`，大于等于才fsync。

**注意：** 参数`binlog_group_commit_sync_delay`和`binlog_group_commit_sync_no_delay_count`会对事务提交时间有影响，可能导致事务执行时间变长。binlog_group_commit_sync_no_delay_count这个参数最好设置的比并发线程数小，否则的话，就只能等binlog_group_commit_sync_delay 时间到了。

**WAL的机制是减少磁盘的写，主要得益于两个方面：**

* redo log 和 binlog 都是顺序写，磁盘的顺序写比随机写速度要快；
* 组提交机制，可以大幅度降低磁盘的 IOPS 消耗。

**如果MySQL出现了性能瓶颈，并且瓶颈在IO上，可以考虑下面三种方法提升性能：**

* 1.设置 binlog_group_commit_sync_delay 和 binlog_group_commit_sync_no_delay_count 参数，减少 binlog 的写盘次数。这个方法是基于“额外的故意等待”来实现的，因此可能会增加语句的响应时间，但没有丢失数据的风险。
* 2.将 sync_binlog 设置为大于 1 的值（比较常见是 100~1000）。这样做的风险是，主机掉电时会丢 binlog 日志。
* 将 innodb_flush_log_at_trx_commit 设置为 2。这样做的风险是，主机掉电的时候会丢数据。

不建议你把 innodb_flush_log_at_trx_commit 设置成 0。因为把这个参数设置成 0，表示 redo log 只保存在内存中，这样的话 MySQL 本身异常重启也会丢数据，风险太大。而 redo log 写到文件系统的 page cache 的速度也是很快的，所以将这个参数设置成 2 跟设置成 0 其实性能差不多，但这样做 MySQL 异常重启时就不会丢数据了，相比之下风险会更小。

**数据库的 crash-safe 保证的是：**

* 如果客户端收到事务成功的消息，事务就一定持久化了；
* 如果客户端收到事务失败（比如主键冲突、回滚等）的消息，事务就一定失败了；
* 如果客户端收到“执行异常”的消息，应用需要重连后通过查询当前状态来继续后续的逻辑。此时数据库只需要保证内部（数据和日志之间，主库和备库之间）一致就可以了。
* 

**为什么 binlog cache 是每个线程自己维护的，而 redo log buffer 是全局共用的？**

* binlog 是不能“被打断的”。一个事务的 binlog 必须连续写，因此要整个事务完成后，再一起写到文件里。
* binlog存储是以statement或者row格式存储的，而redo log是以page页格式存储的。page格式，天生就是共有的，而row格式，只跟当前事务相关