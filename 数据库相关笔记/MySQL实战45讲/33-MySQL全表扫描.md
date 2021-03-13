# MySQL全表扫描

MySQL对一个数据量很大的表做全表扫描会产生什么影响？

## 全表扫描对 server 层的影响

MySQL是“边读边发的”，server层取数据发送数据流程：

* 获取一行，写到 net_buffer 中。这块内存的大小是由参数 net_buffer_length 定义的，默认是 16k。
* 重复获取行，直到net_buffer写满，调用网络接口发送出去。
* 如果发送成功就清空net_buffer，然后继续取下一行，并写入net_bufffer。
* 如果发送函数返回 EAGAIN 或 WSAEWOULDBLOCK，就表示本地网络栈（socket send buffer）写满了，进入等待。直到网络栈重新可写，再继续发送。

一个查询在发送过程过程中，占用MySQL内部的最大内存就是`net_buffer_length`。socket send buffer 也不可能达到很大（默认定义 /proc/sys/net/core/wmem_default），如果 socket send buffer 被写满，就会暂停读数据的流程。

因此一个查询很多的数据并不会把MySQL内存写满。

通过`show processlist`查询`state`显示为`Sending to client`，就表示当前服务端的网络栈写满了。

关于客户端`-quick`参数，使用`mysql_use_result`方法就表示读一行处理一行，如果客户端处理数据比较慢，就会显示`Sending to client`。对于正常的业务，如果一个查询不会返回特别多数据的话，建议使用`mysql_store_result`接口，直接将查询结果保存到本地内存。

**很多个线程都处于“Sending to client”的处理**，如果要快速减少处于这个状态的线程的话，将 net_buffer_length 参数设置为一个更大的值是一个可选方案。

`state`显示为`Sending data`，并不一定是指“正在发送数据”，而可能是处于执行器过程中的任意阶段。比如，锁等待的场景，就能看到 Sending data 状态。可以理解为“正在执行”。

## 全表扫描对 InnoDB 的影响

内存的数据页是在 Buffer Pool (BP) 中管理的，在 WAL 里 Buffer Pool 起到了加速更新的作用。而实际上，Buffer Pool 还有一个更重要的作用，就是加速查询。

由于有 WAL 机制，当事务提交的时候，磁盘上的数据页是旧的，那如果这时候马上有一个查询要来读这个数据页，不需要吧redo log应用到数据页。因为这时候内存数据页的结果是最新的，直接读内存页就可以了。直接从内存拿结果，速度是很快的。所以说，Buffer Pool 还有加速查询的作用。

Buffer Pool 对查询的加速效果，依赖于一个重要的指标，即：内存命中率。 `show engine innodb status` 结果中，`Buffer pool hit rate`显示当前db的内存命中率。一般情况下，一个稳定服务的线上系统，要保证响应时间符合要求的话，内存命中率要在 99% 以上。最好的情况是内存命中率100%，实际生产中很难做到。

InnoDB Buffer Pool 的大小是由参数 innodb_buffer_pool_size 确定的，一般建议设置成可用物理内存的 60%~80%。innodb_buffer_pool_size 小于磁盘的数据量是很常见的。如果一个 Buffer Pool 满了，而又要从磁盘读入一个数据页，那肯定是要淘汰一个旧数据页的。

### InnoDB Buffer Pool 内存管理(Least Recently Used, LRU) 算法

InnoDB 管理 Buffer Pool 的 LRU 算法，是用链表来实现的。

**基本 LRU 算法**

* 链表头部表示最近刚刚被访问过的数据，当访问一个数据在链表中时，会将数据移到链表头部。
* 当访问一个数据不在链表中时，需要在 Buffer Pool 中新申请一个数据页，加到链表头部。但是由于内存已经满了，不能申请新的内存。于是，会清空链表末尾这个数据页的内存，存入新的内容，然后放到链表头部。

基本的LRU算法有一个问题，如果做全表扫描，数据比较多时，就会把当前Buffer Pool 里的数据全部淘汰掉，存入扫描过程中访问到的数据页的内容。存入的数据可能只会访问这一次。这会导致Buffer Pool 的内存命中率急剧下降，磁盘压力增加，SQL 语句响应变慢。

**InnoDB 对 LRU 算法的改进**

![InnoDB改进后的LRU算法](https://note.youdao.com/yws/api/personal/file/C34DFC52BED54EBF983A5B1A69C13C98?method=download&shareKey=bd7781551e527a8d104447a9e855cc45)

InnoDB在实现上，按照5：3的比例把整个LRU链表分成了young区域和old区域。图中 LRU_old 指向的就是 old 区域的第一个位置，是整个链表的 5/8 处。也就是说，靠近链表头部的 5/8 是 young 区域，靠近链表尾部的 3/8 是 old 区域。

* 如果要访问的数据页young区，和优化前的 LRU 算法一样，将其移到链表头部。
* 如果访问的数据页不在链表中，这时候链表已经满了的话，将会清空链表末尾这个数据页的内存，存入新的内容，然后放到LRU_old处。
* 处于 old 区域的数据页，每次被访问的时候都要做下面这个判断：
  * 若这个数据页在 LRU 链表中存在的时间超过了 1 秒，就把它移动到链表头部；
  * 如果这个数据页在 LRU 链表中存在的时间短于 1 秒，位置保持不变。1 秒这个时间，是由参数 innodb_old_blocks_time 控制的。其默认值是 1000，单位毫秒。

这个策略，就是为了处理类似全表扫描的操作量身定制的。改进后的全表扫描逻辑：
1.扫描过程中，需要新插入的数据页，都被放到 old 区域 ;
2.一个数据页里面有多条记录，这个数据页会被多次访问到，但由于是顺序扫描，这个数据页第一次被访问和最后一次被访问的时间间隔不会超过 1 秒，因此还是会被保留在 old 区域；
3.再继续扫描后续的数据，之前的这个数据页之后也不会再被访问到，于是始终没有机会移到链表头部（也就是 young 区域），很快就会被淘汰出去。

这个策略最大的收益，就是在扫描这个大表的过程中，虽然也用到了 Buffer Pool，但是对 young 区域完全没有影响，从而保证了 Buffer Pool 响应正常业务的查询命中率。
