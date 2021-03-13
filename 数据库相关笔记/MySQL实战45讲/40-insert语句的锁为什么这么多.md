
# insert语句的锁为什么这么多

insert语句虽然是一个很轻量的操作，但是有些特殊的insert语句，在执行过程中需要给其他资源加锁，或者无法在申请到自增 id 以后就立马释放自增锁。

## insert … select 语句

在可重复读隔离级别下，binlog_format=statement 时执行：

```sql
insert into t2(c,d) select c,d from t;
```

这个语句时，需要对表 t 的所有行和间隙加锁呢？主要原因还是日志和数据的一致性。

如果上面语句执行过程中存在其他sessio A向t表插入数据操作，并且是上面语句先执行，sessionA后执行。由于上面语句对表 t 主键索引加了 (-∞,1] 这个 next-key lock，会在语句执行完成后，才允许 session A 的 insert 语句执行。但如果没有锁的话，就可能出现 上面的语句先执行，但是后写入 binlog 的情况。于是，在 binlog_format=statement 的情况下，binlog 里面就记录了这样的语句序列：

```sql
insert into t values(-1,-1,-1);
insert into t2(c,d) select c,d from t;
```

这个语句到了备库执行，就会把 id=-1 这一行也写到表 t2 中，出现主备不一致。

## insert 循环写入

执行 insert … select 的时候，对目标表也不是锁全表，而是只锁住需要访问的资源。

语句：

```sql
insert into t2(c,d)  (select c+1, d from t force index(c) order by c desc limit 1);
```

这个语句的加锁范围，就是表 t 索引 c 上的 (3,4] 和 (4,supremum] 这两个 next-key lock，以及主键索引上 id=4 这一行。

它的执行流程也比较简单，从表 t 中按照索引 c 倒序，扫描第一行，拿到结果写入到表 t2 中。因此整条语句的扫描行数是 1。这个语句执行的慢查询日志（slow log）也可以看到 Rows_examined=1。

语句：

```sql
insert into t(c,d)  (select c+1, d from t force index(c) order by c desc limit 1);
```

这个语句的执行流程：

* 1.创建临时表，表里有两个字段 c 和 d。
* 2.按照索引 c 扫描表 t，依次取 c=4、3、2、1，然后回表，读到 c 和 d 的值写入临时表。这时，Rows_examined=4。
* 3.由于语义里面有 limit 1，所以只取了临时表的第一行，再插入到表 t 中。这时，Rows_examined 的值加 1，变成了 5。

也就是说，这个语句会导致在表 t 上做全表扫描，并且会给索引 c 上的所有间隙都加上共享的 next-key lock。所以，这个语句执行期间，其他事务不能在这个表上插入数据。

优化方式：先 insert into 到临时表 temp_t，这样就只需要扫描一行；然后再从表 temp_t 里面取出这行数据插入表 t1。

```sql
create temporary table temp_t(c int,d int) engine=memory;
insert into temp_t  (select c+1, d from t force index(c) order by c desc limit 1);
insert into t select * from temp_t;
drop table temp_t;
```

## insert 唯一键冲突

```sql
-- session A                            -- session B
insert into t values(10,10,10);     
begin;
insert into t values(11,10,10);
(Duplicate entry '10' for key 'c')
                                        insert into t values(12,9,9);
                                        (blocked)
```

可以看到 session A执行的 insert 语句，发生唯一键冲突的时候，并不只是简单地报错返回，还在冲突的索引上加了锁。这时候，session A 持有索引 c 上的 (5,10] 共享 next-key lock（读锁）。

> 这里官方文档有一个描述错误，认为如果冲突的是主键索引，就加记录锁，唯一索引才加 next-key lock。但实际上，这两类索引冲突加的都是 next-key lock。

**唯一键冲突 -- 死锁**

![唯一键冲突 -- 死锁](../../../../youdaonote-images/DF0170AC2B1045738E7B1A9954D62707.png)

在 session A 执行 rollback 语句回滚的时候，session C 几乎同时发现死锁并返回。

这个死锁产生的逻辑是这样的：

* 1.在 T1 时刻，启动 session A，并执行 insert 语句，此时在索引 c 的 c=5 上加了记录锁。注意，这个索引是唯一索引，因此退化为记录锁。
* 2.在 T2 时刻，session B 要执行相同的 insert 语句，发现了唯一键冲突，加上读锁；同样地，session C 也在索引 c 上，c=5 这一个记录上，加了读锁。
* 3.T3 时刻，session A 回滚。这时候，session B 和 session C 都试图继续执行插入操作，都要加上写锁。两个 session 都要等待对方的行锁，所以就出现了死锁。

## insert into … on duplicate key update

insert into … on duplicate key update 这个语义的逻辑是，插入一行数据，如果碰到唯一键约束，就执行后面的更新语句。

```sql
insert into t values(11,10,10) on duplicate key update d=100; 
```

>注意，如果有多个列违反了唯一性约束，就会按照索引的顺序，修改跟第一个索引冲突的行。
>执行这条语句的 affected rows 返回的是 2，很容易造成误解。实际上，真正更新的只有一行，只是在代码实现上，insert 和 update 都认为自己成功了，update 计数加了 1， insert 计数也加了 1。

## 总结

insert … select 是很常见的在两个表之间拷贝数据的方法。需要注意，在可重复读隔离级别下，这个语句会给 select 的表里扫描到的记录和间隙加读锁。

而如果 insert 和 select 的对象是同一个表，则有可能会造成循环写入。这种情况下，需要引入用户临时表来做优化。

insert 语句如果出现唯一键冲突，会在冲突的唯一值上加共享的 next-key lock(S 锁)。因此，碰到由于唯一键约束导致报错后，要尽快提交或回滚事务，避免加锁时间过长。

pt-archiver 工具
