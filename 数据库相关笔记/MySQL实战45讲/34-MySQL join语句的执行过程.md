# MySQL join语句的执行过程

t1 数据100行 t2数据1000行
```sql
select * from t1 straight_join t2 on (t1.a=t2.a);
````
`straight_join` 让 MySQL 使用固定的连接方式执行查询，这样优化器只会按照我们指定的方式去 join。

join有两种算法分别是`Index Nested-Loop Join`和`Simple Nested-Loop Join`

## Index Nested-Loop Join

对于可以用上被驱动表上的索引的join语句，MySQL选择使用`Index Nested-Loop Join`算法（简称NLJ）。执行流程为：

* 1.从表t1中读入一行数据R；
* 2.从数据行R中取出a字段到表t2里去查找；
* 3.取出表t2中满足条件的行，跟R组成一行，作为结果集的一部分；
* 4.重复执行步骤1到3，直到表t1的末尾循环结束。

在这个过程中，对表t1做了全表扫描，对于t2走的是树搜索，每次从表t1中读出一行只会扫描一行t2的数据，所以一共扫描了200行。

对于上面情况：驱动表是走全表扫描，而被驱动表是走树搜索。近似复杂度是N + N*2*log2M，显然应该让小表来做驱动表。

**两个结论：**
* 使用 join 语句，性能比强行拆成多个单表执行 SQL 语句的性能要好；
* 如果使用 join 语句的话，需要让小表做驱动表。

## Simple Nested-Loop Join

```sql
select * from t1 straight_join t2 on (t1.a=t2.b);
```

如果表t2上的b字段没有索引，因此再使用上面的流程时就需要对表t2做全表扫描。使用上面的流程也能得到正确的数据，这个算法称为“Simple Nested-Loop Join”。但是这样算来上面的sql就需要扫描100*1000=10万行，数量太多了。不适合没有索引的join了，MySQL使用了另一个算法“Block Nested-Loop Join”。

## Block Nested-Loop Join

当被驱动表上没有可用索引时，BNL算法的执行流程如下：

* 1.把表t1的数据读入线程内存join_buffer中，只会将需要返回的字段放入join_buffer中。（这里写的select * 因此把整个t1放入了内存）；
* 扫描表t2，把t2中的每一行取出来，跟join_buffer中的数据做对比，满足条件的作为结果集的一部分返回。

`join_buffer` 的大小是由参数 join_buffer_size 设定的，默认值是 256k。如果放不下表 t1 的所有数据话就分段放。

在这个过程中，对表 t1 和 t2 都做了一次全表扫描，因此总的扫描行数是 1100。由于 join_buffer 是以无序数组的方式组织的，因此对表 t2 中的每一行，都要做 100 次判断，总共需要在内存中做的判断次数是：100*1000=10 万次。如果使用 Simple Nested-Loop Join 算法进行查询，扫描行数也是 10 万行。因此，从时间复杂度上来说，这两个算法是一样的。但是，Block Nested-Loop Join 算法的这 10 万次判断是内存操作，速度上会快很多，性能也更好。

当表t1很大超过join_buffer大小时，BNL算法的执行过程就变成了：

* 1.扫描表t1，顺序读取数据行放入join_buffer中，放完第88行join_buffer满了，继续第2步；
* 2.扫描表t2，把t2中的每一行取出来，跟join_buffer中的数据做对比，满足join条件的作为结果集的一部分返回；
* 3.情况join_bufer；
* 4.继续扫描表t1，顺序读取最后12行数据放入join_buffer中，继续执行第2步。

在这个过程中，判断条件的次数还是10万次，但是表t2需要被扫描2次。

假设，驱动表的数据行数是 N，需要分 K 段才能完成算法流程，被驱动表的数据行数是 M。注意，这里的 K 不是常数，N 越大 K 就会越大，因此把 K 表示为λ*N，显然λ的取值范围是 (0,1)。所以，在这个算法的执行过程中：

* 扫描行数是 N+λ\*N*M；
* 内存判断 N*M 次。

显然，这种算法内存判断次数是不受选择哪个表作为驱动表影响的。而考虑到扫描行数，在 M 和 N 大小确定的情况下，N 小一些，整个算式的结果会更小。同时这种算法也受`join_buffer_size`大小的影响，join_buffer_size 越大，一次可以放入的行越多，分成的段数也就越少，对被驱动表的全表扫描次数就越少。

**能不能使用 join 语句？**

* 如果可以使用 Index Nested-Loop Join 算法，也就是说可以用上被驱动表上的索引，是没问题的；
* 如果使用 Block Nested-Loop Join 算法，扫描行数就会过多。尤其是在大表上的 join 操作，这样可能要扫描被驱动表很多次，会占用大量的系统资源。所以这种 join 尽量不要用。

在判断要不要使用 join 语句时，就是看 explain 结果里面，Extra 字段里面有没有出现“Block Nested Loop”字样。

**如果要使用 join，应该选择大表做驱动表还是选择小表做驱动表？**

* 如果是 Index Nested-Loop Join 算法，应该选择小表做驱动表；
* 如果是 Block Nested-Loop Join 算法：
    * 在 join_buffer_size 足够大的时候，是一样的；
    * 在 join_buffer_size 不够大的时候（这种情况更常见），应该选择小表做驱动表。

注意这里的小表是：在决定哪个表做驱动表的时候，应该是两个表按照各自的条件过滤，过滤完成之后，计算参与 join 的各个字段的总数据量，数据量小的那个表，就是“小表”，应该作为驱动表。