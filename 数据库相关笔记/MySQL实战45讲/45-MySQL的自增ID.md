# MySQL的自增ID

## 表定义自增值 id

表定义的自增值达到上限后的逻辑是：再申请下一个 id 时，得到的值保持不变。

```sql
create table t(id int unsigned auto_increment primary key) auto_increment=4294967295;
insert into t values(null);-- 成功插入一行 4294967295show create table t;
insert into t values(null);--Duplicate entry '4294967295' for key 'PRIMARY'
```

## InnoDB 系统自增 row_id

如果创建的InnoDB表没有指定主键，那么InnoDB会自动创建一个不可见，长度为6个字节的row_id。InnoDB维护了一个全局的dict_sys.row_id值，所有无主键的InnoDB表，没插入一行数据，都将当前的dict_sys.row_id值作为要插入数据点的row_id，然后再将dict_sys.row_id的值加1。

实际上，在代码实现时 row_id 是一个长度为 8 字节的无符号长整型 (bigint unsigned)。但是，InnoDB 在设计时，给 row_id 留的只是 6 个字节的长度，这样写到数据表中时只放了最后 6 个字节，所以 row_id 能写到数据表中的值，就有两个特征：

* 1.row_id写入表的范围是从0到$2^{48}$-1；
* 2.当dict_sys.row_id=$2^{48}$时，如果再有插入数据的行为要来申请 row_id，拿到以后再取最后 6 个字节的话就是 0。

也就是说，写入表的 row_id 是从 0 开始到 $2^{48}$-1。达到上限后，下一个值就是 0，然后继续循环。虽然$2^{48}$-1 这个值本身已经很大了，但是如果一个 MySQL 实例跑得足够久的话，还是可能达到这个上限的。在 InnoDB 逻辑里，申请到 row_id=N 后，就将这行数据写入表中；如果表中已经存在 row_id=N 的行，新写入的行就会覆盖原有的行。

从这个角度来看，还是应该在创建InnoDB表中创建主键，因为主键id达到上限后，再插入数据会提示主键冲突的错误，更容易被接受的。数据被覆盖，影响的数据的可靠性，一般情况下可靠性优于可用性。

## Xid

在redo log和binlog相配合的时候，有一个共同的字段Xid，它在MySQL中是用来对应事务的。

**Xid在MySQL中时如何生成的？**

MySQL内部维护了一个全局变量global_query_id，每次执行语句时都会将它赋值给Query_id，然后这个变量再加1。如果当前语句时这个事务执行的第一句，那么MySQL还会同时把Query_id赋值给这个事务的Xid。global_query_id是一个纯内存变量，重启之后就会清零。所以在同一个数据库实例中，不同事务的Xid可能是相同的。但是MySQL重启之后会生成新的binlog文件，这就保证了同一个binlog文件中Xid是惟一的。

虽然 MySQL 重启不会导致同一个 binlog 里面出现两个相同的 Xid，但是如果 global_query_id 达到上限后，就会继续从 0 开始计数。从理论上讲，还是就会出现同一个 binlog 里面出现相同 Xid 的场景。因为 global_query_id 定义的长度是 8 个字节，这个自增值的上限是 $2^{64}$-1。要出现这种情况，必须是下面这样的过程：

* 执行一个事务，假设 Xid 是 A；
* 接下来执行 2^{64}次查询语句，让 global_query_id 回到 A；
* 再启动一个事务，这个事务的 Xid 也是 A。

不过，$2^{64}$这个值太大了，大到可以认为这个可能性只会存在于理论上。

## Innodb trx_id

Xid是server层维护的，InnoDB内部使用Xid就是为了在InnoDB事务和server之间做关联。但是，InnoDB自己的trx_id是另外维护的。

这个trx_id就是在事务可见性中使用的事务id（transaction id）。

InnoDB内部维护了一个max_trx_id全局变量，每次需要申请一个新的trx_id时，就会获得当前max_trx_id的值，再将max_trx_id的值加1。

对于正在执行的事务，你可以从 information_schema.innodb_trx 表中看到事务的 trx_id。

![事务的trx_id](https://note.youdao.com/yws/api/personal/file/D2F4175708724217AB3948E3868068BF?method=download&shareKey=ae2a6c7d6767f5cbff89193529ed513c)

对于只读事务，InnoDB 并不会分配 trx_id。所以在上图中的T2时刻查询到的trx_id是一个比较大的值，如果在 select 语句后面加上 for update，这个事务也不是只读事务。

**trx_id并不是按照1递增的：**

* update 和 delete 语句除了事务本身，还涉及到标记删除旧数据，也就是要把数据放到 purge 队列里等待后续物理删除，这个操作也会把 max_trx_id+1， 因此在一个事务中至少加 2；

* InnoDB 的后台操作，比如表的索引信息统计这类操作，也是会启动内部事务的，因此你可能看到，trx_id 值并不是按照加 1 递增的。

**T2 时刻查到的这个很大的数字是怎么来的呢**

这个数字是每次查询的时候由系统临时计算出来的。它的算法是：把当前事务的 trx 变量的指针地址转成整数，再加上 $2^{48}$。加上 $2^{48}$，目的是要保证只读事务显示的 trx_id 值比较大，正常情况下就会区别于读写事务的 id。虽然在理论上还是可能出现一个读写事务与一个只读事务显示的 trx_id 相同的情况。不过这个概率很低，并且也没有什么实质危害，可以不管它。使用这个算法，就可以保证以下两点：

* 因为同一个只读事务在执行期间，它的指针地址是不会变的，所以不论是在 innodb_trx 还是在 innodb_locks 表里，同一个只读事务查出来的 trx_id 就会是一样的。
* 如果有并行的多个只读事务，每个事务的 trx 变量的指针地址肯定不同。这样，不同的并发只读事务，查出来的 trx_id 就是不同的。

**只读事务不分配 trx_id的好处**

* 一个好处是，这样做可以减小事务视图里面活跃事务数组的大小。因为当前正在运行的只读事务，是不影响数据的可见性判断的。所以，在创建事务的一致性视图时，InnoDB 就只需要拷贝读写事务的 trx_id。
* 另一个好处是，可以减少 trx_id 的申请次数。在 InnoDB 里，即使你只是执行一个普通的 select 语句，在执行过程中，也是要对应一个只读事务的。所以只读事务优化后，普通的查询语句不需要申请 trx_id，就大大减少了并发事务申请 trx_id 的锁冲突。

但是，max_trx_id 会持久化存储，重启也不会重置为 0，那么从理论上讲，只要一个 MySQL 服务跑得足够久，就可能出现 max_trx_id 达到 $2^{48}$-1 的上限，然后从 0 开始的情况。当达到这个状态后，MySQL 就会持续出现一个脏读的 bug。

## thread_id

线程id是MySQL中最常见的一种自增id，show processlist 里面的第一列，就是 thread_id。

thread_id 的逻辑：系统保存了一个全局变量 thread_id_counter，每新建一个连接，就将 thread_id_counter 赋值给这个新连接的线程变量。thread_id_counter 定义的大小是 4 个字节，因此达到 $2^{32}$-1 后，它就会重置为 0，然后继续增加。但是，在 show processlist 里不会看到两个相同的 thread_id。这，是因为 MySQL 设计了一个唯一数组的逻辑，给新线程分配 thread_id 的时候，会判断当前是否存在这个tread id了。
