# NoSQL实践

## Spring对Mongodb的支持

* Spring Data MongoDB
  * MongoTemplae
  * Repostiory支持

**基本用法**

**使用MongodbTemplate**

* @Document 表示类对应的文档
* @Id 表示id；Mongodb的文档都有一个id，通过id查询文档，不需要知道文档在哪个分片
* MongodbTemplate
  * save/remove
  * Update/Query

**使用Repostiory**

* 在启动类上添加@EnbaleMongoRepository
* Repository实现MongoRepository
  * MongoRepository 继承了PagingAndSortingRepository和QueryByExampleExecutor
  * PagingAndSortingRepository 继承了CrudRepository 分页查询和排序查询
  * QueryByExampleExecutor  
  * CrudRepository 基本的crud
* 和JPARepository类似，Repository可以根据方法名进行解析查询

## Spring对Redis的支持

* Spring Data Redis
  * 支持的客户端 Jedis /Letture
  * RedisTemplate
  * Repository支持

**Jedis**

Jedis注意：Jedis不是线程安全的，可以通过JedisPool获取Jedis实例，直接获取Jedis使用它的方法

**Lettuce**

支持读写分离，可以只读主，只读从；优先读主，优先读从。

* LettuceClientConfiguration
* LettucePoolingClientConfiguration
* LettuceClientConfigurationBuilderCustomizer

**RedisTemplate**

redisTemplate支持JedisConnectionFactory和LettuceConnectionFactory作为连接方式，默认使用LettuceConnectionFactory。

* RedisStandaloneConfiguration 配置单个redis节点的配置类
* RedisSentinelConfiguration 配置哨兵模式的配置类
* RedisClusterConfiguration 集群模式配置类

**Repository支持**

* 在启动类上添加@EnableRedisRepositories注解
* Repository继承对应的Repository
  * CrudRepository 基本的curd
* 对应缓存的实体类上添加注解
  * 类上添加@RedisHash(value = "缓存key", timeToLive = 过期时间单位s)
  * @Id 缓存的主键id，存放的key示例："coffee:4"
  * @Indexed 缓存的二级索引，二级索引value存放主键的id，通过主键id再查询对应的缓存值。存放key示例："coffee:name:mocha"

### redis的哨兵模式和集群模式

[参考](https://www.jianshu.com/p/06ab9daf921d)

* Redis Sentinel（哨兵模式）

哨兵模式是一种特殊的模式，Redis提供了哨兵的命令，哨兵是一个独立的进程，作为进程，它会独立运行。其原理是哨兵通过发送命令，等待Redis服务器响应，从而监控运行的多个Redis实例。当哨兵监测到master宕机，会自动将slave切换成master，然后通过发布订阅模式通知其他的从服务器，修改配置文件，让它们切换主机。

在主服务宕机时，哨兵1先检测到这个结果，系统并不会马上进行failover过程，仅仅是哨兵1主观的认为主服务器不可用，这个现象成为主观下线。当后面的哨兵也检测到主服务器不可用，并且数量达到一定值时，那么哨兵之间就会进行一次投票，投票的结果由一个哨兵发起，进行failover操作。切换成功后，就会通过发布订阅模式，让各个哨兵把自己监控的从服务器实现切换主机，这个过程称为客观下线。这样对于客户端而言，一切都是透明的。

Jedis通过JedisSentinelPool配置哨兵。

* Redis Cluster（集群模式）

将数据自动分片（分成16384个Hash Slot），在部分节点失效时，有一定可用性。

Jedis通过JedisCluster配置集群，Jedis只能从Master读取数据，如果想要自动读写分离需要特殊的配置

## Spring的缓存抽象

Spring提供类一套缓存方式，用于本地缓存，可以选择使用redis做分布式缓存

* 为java方法增加缓存，缓存执行的结果
* 支持ConcurrentMap，EhCache，Caffeine，JCache（JSR-107）
* 接口
  * org.springframework.cache.Cache
  * org.springframework.cache.CacheManager

**使用方式**

* 在启动类上添加@EnableCaching，启用Spring缓存
* 注解
  * @Cacheable 缓存执行结果，如果缓存中已经有数据就直接取缓存数据
  * @CacheEvict 清除缓存
  * @CachePut 不管缓存有没有数据都缓存执行结果
  * @Caching 缓存操作的集中操作
  * @CacheConfig 缓存配置，配置缓存的key等

**Spring Cache使用Redis作为缓存**

* 配置参数

```properties

spring.redis.host=
spring.redis.password=
spring.redis.port=
# 缓存类型
spring.cache.type=redis
#缓存的名称
spring.cache.cache-names=coffee
#过期时间
spring.cache.redis.time-to-live=60000
spring.cache.redis.cache-null-values=false
```

* 注解方式使用和cache方式相同