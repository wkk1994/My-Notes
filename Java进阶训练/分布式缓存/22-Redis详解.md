# 分布式缓存-Redis详解

## 1.Redis基本功能

### Redis安装方式

* 下载安装、编译源码安装，window下[安装参考](https://github.com/tporadowski/redis/releases)；
* brew、apt、yum安装；
* docker启动。

### Redis性能测试

redis提供了基准测试命令：redis-banchmark。

常用参数：

* -h: 指定服务器主机名；
* -p: 指定服务器端口；
* -c: 指定并发连接数，默认值50；
* -n: 指定请求数，默认值10000；
* -t: 仅运行以逗号分隔的测试命令列表；
* -q: 强制退出 redis。仅显示 query/sec 值；

示例：`redis-benchmark -n 100000 -c 32 -t SET,GET,INCR,HSET,LPUSH,MSET -q`

[redis-banchmark参考](https://www.runoob.com/redis/redis-benchmarks.html)

### Redis的5种基本数据结构

#### string

二进制安全的字符串，可以保存任意数据类型，最大512M，最大值和内部表示长度的值有关，内部表示字符串长度的值，最大值是2的32次方。

支持的操作：set/get/getset/del/exists/append/incr/decr/incrby/decrby

注意：

* append问题：append会使用更多的内存，因为redis string的空间预分配。
* 数据共享：整数会被共享。（如果能使用整数就尽量使用整数，但是淘汰策略LRU会使整数共享失效）
* 整数精度问题：redis大概能保证16位的大整数，17位到18位会丢失精度。

#### hash

支持的操作：hset/hget/hmset/hmget/hgetall/hdel/hincrby/hexists/hlen/hkeys/hvals

#### list

按照插入顺序排序的字符串链表，在插入数据时支持从头部或者尾部插入。在插入时，如果该键并不存在，Redis将为该键创建一个新的链表。与此相反，如果链表中所有的元素均被移除，那么该键也将会被从数据库中删除。

支持的操作：lpush/rpush/lrange/lpop/rpop

#### set

set类型是没有排序的字符串集合。支持求交集、并集、差集。

支持的操作：sadd/srem/smembers/sismember、sdiff/sinter/sunion ~ 集合求差集，求交集，求并集

#### sorted set

和set类型类似，都是字符串集合，不同的是每个成员都有一个分数与之相关联，通过分数来为集合的成员进行从小到大的排序。sortedset中分数是可以重复的。

### Redis的3种高级数据结构

#### Bigmaps

bitmaps不是一个真实的数据结构。而是String类型上的一组面向bit操作的集合。

支持的操作：setbit/getbit/bitop/bitcount/bitpos

使用bitmap可以实现计数排序。

#### Hyperloglogs

HyperLogLog 是用来做基数统计的算法，HyperLogLog 的优点是，在输入元素的数量或者体积非常大时，计算基数所需的空间总是固定的、并且是很小的。这个估算的基数并不一定准确，是一个带有 0.81% 标准错误（standard error）的近似值。

支持的操作：pfadd/pfcount/pfmerge。

#### GEO

存储地理位置。

支持的操作：geoadd/geohash/geopos/geodist/georadius/georadiusbymember

### Redis是单线程还是多线程

redis做为一个进程都是多线程。

具体分情况是：

* IO线程：在redis6之前是单线程；redis6之后多线程，NIO模型。
* 内存处理线程：单线程。历史原因内存处理线程不能改成多线程，改成多线程会有线程安全问题。

## 2.Redis六大使用场景

### 1.业务数据缓存

经典用法：

* 通用数据缓存，string，int，list，map等。
* 实时热数据缓存，比如最新500条数据。
* 会话缓存，token缓存等。

### 2.业务数据处理

对于数据量比较大，又不要求实时或严格一致性的数据可以存放到redis中。

* 非严格一致性要求的数据：评论，点击等。
* 业务数据去重：订单处理的幂等校验等，可以做跨节点的去重。
* 业务数据排序：排名，排行榜等。

### 3.全局一致性计数

* 全局流控计数
* 秒杀的库存计算
* 抢红包
* 全局id生成

### 4.高效统计计数

* id去重，记录访问ip等全局bitmap操作；
* UV、PV等访问量，非严格一致性要求。

### 5.发布订阅与Stream

* Pub-Sub 模拟队列

  订阅消息：subscribe comments；
  发送消息：publish comments hello；

* Redis Stream

  Redis 5.0 版本新增加的数据结构。 Redis Stream主要用于消息队列。

### 6.分布式锁

实现分布式锁的方式：

* 获取锁：set kekName NX PX 30000；
* 如果需要手动释放锁，可以通过lua脚本，lua脚本是一个原子操作。

  ```lua
  if redis.call("get",KEYS[1]) == ARGV[1]
  then 
    return redis.call("del",KEYS[1])
  else
    return 0 end
  ```

**redis的锁如何续期**

* 过期时间足够长；
* 增加守护线程进行续期；
* 或者直接使用redisson，redisson内部已经实现了续期。

## 3.Redis的Java客户端

### Jedis

官方客户端，类似于JDBC，可以看做是对redis命令的包装。基于BIO，线程不安全，需要配置连接池管理连接。

### Lettuce

目前主流推荐的驱动，基于Netty NIO，API线程安全。

### Redission

基于Netty NIO，API线程安全。

亮点：大量丰富的分布式功能特性，比如JUC的线程安全集合和工具的分布式版本，分布式的基本数据类型和锁等。

## 4.Redis与Spring整合

### Spring Data Redis

核心是 RedisTemplate(可以配置基于Jedis，Lettuce，Redisson)，使用方式类似于MongoDBTemplate。

封装了redis的命令。

### Spring Boot与Redis集成

spring boot提供了spring-boot-starter-data-redis，方便集成redis。

### Spring Cache与Redis集成

Spring Cache不仅可以使用ConcurrentHashMap或ehcache，还可以使用redis作为缓存。

使用redis，需要：

* 默认使用java的对象序列化，对象需要实现Serializable
* 自定义配置，可以修改为其他序列化方式

### MyBatis项目集成cache示例

见代码

## 5.Redis高级功能

### Redis事务

事务涉及的主要命令：

* multi：开始事务；
* exec：提交事务；
* discard：撤销事务。
* watch key[...]：监视一个(或多个) key ，如果在事务执行之前这个(或这些) key 被其他命令所改动，那么事务将被打断。
* unwatch：取消 WATCH 命令对所有 key 的监视。

> 在事务操作过程中，可能出现要操作的key被其他线程修改的情况，可以在事务开始之前（multi命令之前）执行watch操作，监视要修改的key，如果监视的key被修改了，事务在执行提交时（exec命令）会返回(nil)

### Redis Lua

类似于数据库的存储过程，mongodb的js脚本。

使用方式：

* 直接运行

  示例：`eval "return'hello java'" 0`
  `eval "return redis.call('set',KEYS[1],ARGV[1])" 1 lua-key lua-value`

* 预编译

  * 使用`script`命令加载脚本片段并返回一个SHA-1签名shastring：`script load script脚本片段`；
  * 根据脚本的签名进行调用：`evalsha shastring keynum [key1 key2 key3 ...] [param1 param2 param3 ...]`。

### Redis 管道技术（pipeline）

Redis 管道技术可以在服务端未响应时，客户端可以继续向服务端发送请求，并最终一次性读取所有服务端的响应。

示例：`(echo -en "PING\r\n SET runoobkey redis\r\nGET runoobkey\r\nINCR visitor\r\nINCR visitor\r\nINCR visitor\r\n"; sleep 10) | nc localhost 6379`

> `nc`相当于`telnet`命令。
> Jedis客户端对管道技术支持的比较好，可以直接使用。

### Redis 数据备份与恢复--RDB

备份：

执行`save`命令会在redis数据目录中生成数据文件dump.rdb。也可以使用`bgsave`异步生成。

恢复：

将备份文件（dump.rdb）移动到redis数据目录并启动服务即可。

> 查看redis数据文件夹`CONFIG GET dir`。
> rdb备份的方式可以看做是mysql的数据文件frm。

### Redis 数据备份与恢复--AOF

备份：

如果配置`appendonly`为yes，则以 AOF 方式备份 Redis 数据，此时Redis会根据配置在特定时机执行追加命令，用来备份数据。

```text
appendfilename "appendonly.aof"
# appendfsync always
# appendfsync everysec
# appendfsync no......
```

AOF 文件和 Redis 命令是同步频率的，假设配置为 always，其含义为当 Redis 执行 命令的时候，则同时同步到 AOF 文件，这样会使得 Redis 同步刷新 AOF 文件，造成 缓慢。而采用 evarysec 则代表每秒同步一次命令到 AOF 文件。

恢复：

Redis会根据自身的AOF配置进行自动恢复。

### Redis 性能优化

Redis进行优化的核心是内存和CPU。

* 内存优化

  Redis本身支持多个数据结构，可以通过更改配置节省内存的占用，比如`hash-max-ziplist-value 64``zset-max-ziplist-value 64`。一般建议单个Redis的内存大小在10~20G。[redis内存优化参考](https://redis.io/topics/memory-optimization)

* CPU优化

  * 因为Redis是单线程操作，所以不要有阻塞操作，特别是lua脚本。
  * 谨慎使用范围操作。
  * 可以通过命令`slowlog get 10`获取慢操作日志，默认10毫秒，默认只保留最后的128条数据。

### Redis 分区

* 容量

  容量问题是设计系统时就需要考虑清楚的。
  对于多个业务系统是使用一个redis还是应该分开使用问题，如果是使用一个redis可以根据不同的系统的key固定设置不同的前缀来进行区分，注意前缀不要太长。

* 分区

  如果缓存的数据量很大，就需要考虑分区了。

> 在大公司中，一般研发团队看不到redis的实际配置，研发团队需要申请缓存资源，根据申请到的key
/token，作为缓存的key的前缀。

### Redis 使用的一些经验

* 1.性能

  * 客户端一般设置的线程数是（4~8），redis服务的连接数默认是10000，配置`maxclients`修改。
  * 监控系统读写比和缓存命中率（N:1，90%+）；

* 2.容量：

  做好容量评估，合理使用缓存资源；监控要注重增量变化。

* 3.资源管理与分配

  * 尽量每个业务集群单独使用自己的redis，不混用。
  * 控制redis资源的申请与使用，规范环境和Key的管理（以一线互联网为例）；
  * 监控CPU 100%，优化高延迟的操作。

## Tips

* roaring bitmap原理，在计数排序时，对于10000000 ~ 11000000进行基数排序，将0~99999999压缩为一个区间，10000000~11000000分别划分区间，当有数据落到0~99999999中，再将0~99999999进行换分区间。
