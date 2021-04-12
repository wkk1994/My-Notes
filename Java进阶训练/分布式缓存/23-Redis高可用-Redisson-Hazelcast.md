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

Redisson不仅是Redis的连接驱动，还是一个分布式组件库。

基于Redis实现了大量丰富的分布式功能特性，比如JUC的线程安全集合和工具的分布式版本，分布式的基本数据类型和锁等。

> [redisson官网](https://github.com/redisson/redisson)

使用示例1：分布式锁实现

RLock能实现跨节点的锁状态，并且支持自动续期。

使用示例2：分布式Map

RMap实现全集群共享，一个机器改了，其他都会自动同步。

## 3.Hazelcast

### Hazelcast介绍

Hazelcast是基于内存的数据网格开源项目，它的基本特征：

* 分布式：数据按照某种策略尽可能的分布在集群的所有节点上。
* 节点对等：集群中的节点没有主备之分都是对等的，可以把Hazelcast内嵌到应用程序中一同发布运行，或者使用client-server模式。
* 高可用：集群的每个节点都可以提供业务查询和数据修改事务；部分节点不可用时，集群依然可以提供业务服务。
* 可扩展性：增加节点简单，新加入的节点可以自动发现集群，集群的内存存储能力和计算能力可以维持线性增加。集群内每两个节点之间都有一条TCP连接，所有的交互都通过该TCP连接。
* 面向对象：数据模型是面向对象和非关系型的。在 java 语言应用程序中引入 hazelcast client api是相当简单的。
* 低延迟：基于内存的，可以使用堆外内存。

> Hazelcast需要内网传输高

### Hazelcast部署模式

* 内嵌模式

![内嵌模式.png](https://docs.hazelcast.com/imdg/4.2/_images/Embedded.png)

在内嵌模式下，Hazelcast集群的一个节点包括应用程序、Hazelcast分区数据、Hazelcast服务三部分。这种模式的优点是数据访问延迟低。

* 客户端/服务器部署模式

![客户端/服务器部署模式.png](https://docs.hazelcast.com/imdg/4.2/_images/ClientServer.png)

Hazelcast数据和服务集中在一个或多个节点上，应用通过客户端读写数据。这种模式的优点是：可预测性高、可以提供可靠的Hazelcast服务、更容易发现问题原因、具备高可扩展性。

### Hazelcast数据分区

Hazelcast中的数据分区也叫分片，默认分为271个分区，也可以通过hazelcast.partition.count 配置修改，所有分区均匀分布在集群的所有节点上。为了保证高可用，Hazelcast默认会为每个分区创建一个副本，副本数量也可以通过配置修改，这些副本只有一个是主副本，只有主副本的节点才接收读写请求。**同一个节点不会同时包含一个分区的多个副本(副本总是 分散的以保证高可用)**

**数据分区的过程：** key被序列化转为byte数组 --> 对byte数组执行散列函数 --> Hash值和分区数取余数得到数据应该保存的分区计数。

### Hazelcast 事务支持

Hazelcast支持事务操作：

```java
HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
TransactionOptions options = new TransactionOptions()
                .setTransactionType( TransactionOptions.TransactionType.ONE_PHASE );

TransactionContext context = hazelcastInstance.newTransactionContext( options );
context.beginTransaction();

TransactionalQueue queue = context.getQueue( "myqueue" );
TransactionalMap map = context.getMap( "mymap" );
TransactionalSet set = context.getSet( "myset" );

try {
    Object obj = queue.poll();
    //process obj
    map.put( "1", "value1" );
    set.add( "value" );
    //do other things
    context.commitTransaction();
} catch ( Throwable t ) {
    context.rollbackTransaction();
}
```

支持两种事务类型：
ONE_PHASE: 只有一个提交阶段；在节点宕机等情况下可能导致系统不一致；
TWO_PHASE: 在提交前增减一个 prepare 阶段；该阶段检查提交冲突，然后将commit log 拷贝到一个本分节点；如果本节点宕机，备份节点会完成事务提交动作；

### Hazelcast数据亲密性

确保业务相关的数据在同一个集群节点上，避免操作多个数据的业务事务在执行中通过网络请求 数据，从而实现更低的事务延迟。

### Hazelcast 控制台

## Tips

* 什么是脑裂：5台机器，一主四从，发生网络分区，分成3个一组A，2个一组B，组A的master是A1，组B的master是B1，这时候两个组开始各自接收请求处理，这叫brain splitting。
* 对于一主四从的Redis集群，可以配置当从库少于2个时，主库不能写，这样再出现脑裂时，集群就不可用。
* ignite和hazelcast很相似，Gemfire是hazelcast的企业版。
* 近端缓存（一个JVM内的缓存）、远端缓存（非）。
