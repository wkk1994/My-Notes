# 第 23 课 分布式缓存-Redis高可用/Redisson/Hazelcast

## 1. Redis集群与高可用

### Redis 主从复制：从单机到多节点

在从节点上使用命令`slaveof 127.0.0.1 6379`就可以建立起主从复制。

注意：从节点只读，异步复制，会有主从延迟。

### Redis Sentinel 主从切换：走向高可用

可以做到监控主从节点的在线状态，并做切换（基于raft协议）。

Redis Sentinel启动方式：`redis-sentinel sentinel.conf`或`redis-server redis.conf --sentinel`。

Redis Sentinel相关配置：

```text
#指定sentinel监控的Master节点的IP和端口，以及判断该节点故障所需要的sentinel节点个数。
sentinel monitor mymaster 127.0.0.1 6379 2
#sentinel会定时向所监控的节点发送PING命令，如果超过down-after-milliseconds还未收到某节点回复，则认为该节点下线。
sentinel down-after-milliseconds mymaster 60000

sentinel failover-timeout mymaster 180000
#parallel-syncs参数指定发生failover时，同时发起复制的salve节点个数，即如果设置为1，则所有的节点一个接着一个发起复制，
#一个复制完了另一个再发起复制。如果parallel-syncs设置为2，则Master节点同一时刻要把数据发往两个节点，造成大的网络开销。
sentinel parallel-syncs mymaster 1
```

> [redis sentinel原理介绍](http://www.redis.cn/topics/sentinel.html)
> [redis复制与高可用配置](https://www.cnblogs.com/itzhouq/p/redis5.html)

### Redis Cluster：走向分片

主从复制不能解决Redis的单机容量问题，Redis Cluster通过将数据分散到多个机器上去，可以提高Redis的整体容量。

Redis Cluster通过一致性Hash的方式，将数据分散到多个服务器节点：先设计 16384 个哈希槽，分配到多台redis-server。当需要在 Redis Cluster中存取一个 key时， Redis 客户端先对 key 使用 crc16 算法计算一个数值，然后对 16384 取模，这样每个 key 都会对应一个编号在 0-16383 之间的哈希槽，然后在 此槽对应的节点上操作。

注意：

* 节点间使用gossip通信，服务器规模最好小于1000，否则容易出现问题。
* 默认所有槽位可用，才提供服务。
* 一般会配合主从模式使用。

> [redis cluster介绍](http://redisdoc.com/topic/cluster-spec.html)
> [redis cluster原理](https://www.cnblogs.com/williamjie/p/11132211.html)
> [redis cluster详细配置](https://www.cnblogs.com/renpingsheng/p/9813959.html)

### Java中配置使用Redis Sentinel *

### Java中配置使用Redis Cluster *

## 2.Redisson介绍

## 3.Hazelcast介绍

## Tips

* 什么是脑裂：5台机器，一主四从，发生网络分区，分成3个一组A，2个一组B，组A的master是A1，组B的master是B1，这时候两个组开始各自接收请求处理，这叫brain splitting。
* 对于一主四从的Redis集群，可以配置当从库少于2个时，主库不能写，这样再出现脑裂时，集群就不可用。

