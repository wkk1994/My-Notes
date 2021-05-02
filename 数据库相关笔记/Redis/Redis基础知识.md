# Redis

## Redis概念

开源的高性能键值对的内存数据库，可以用作数据库、缓存、消息中间件等。属于一种NoSQL数据库(not-only sql，泛指非关系型数据库）。

性能优秀，数据在内存中，读写速度非常快，支持并发 10W QPS。单进程单线程，是线程安全的，采用 IO 多路复用机制。丰富的数据类型，支持字符串（strings）、散列（hashes）、列表（lists）、集合（sets）、有序集合（sorted sets）等。支持数据持久化。可以将内存中数据保存在磁盘中，重启时加载。主从复制，哨兵，高可用。可以用作分布式锁。可以作为消息中间件使用，支持发布订阅。

## Redis命令

[Redis Command](https://redis.io/commands)

* 连接redis
  * 本地连接：`redis-cli`
  * 远程连接：`redis-cli -h host -p port -a password`
  * `select index`: 命令用于切换到指定的数据库，数据库索引号 index 用数字值指定，以 0 作为起始索引值。
  * `ping`: 使用客户端向 Redis 服务器发送一个 PING ，如果服务器运作正常的话，会返回一个 PONG 。通常用于测试与服务器的连接是否仍然生效，或者用于测量延迟值。

* key相关命令
  * `del key`: 删除已存在的键，不存在的 key 会被忽略。
  * `dump key`: 序列化给定 key ，并返回被序列化的值。
  * `exists key`: 检查指定key是否存在，若 key 存在返回 1，否则返回 0。
  * `expire key seconds`: 为给定 key 设置过期时间，以秒计。
  * `expireat key timestamp`: 以 UNIX 时间戳(unix timestamp)格式设置 key 的过期时间。
  * `pexpire key milliseconds`: 设置 key 的过期时间以毫秒计。
  * `pexpireat key milliseconds-timestamp`: 设置 key 过期时间的时间戳(unix timestamp) 以毫秒计。
  * `keys pattern`: 用于查找所有符合给定模式 pattern 的 key。**需要占用大量CPU，生产环境慎用**
  * `move key db`: 将当前数据库的 key 移动到给定的数据库 db 当中。当key不存在/数据库存在相同key时移动失败返回0。`db`为数字0,1...
  * `persist key`: 移除 key 的过期时间，key 将持久保持。当过期时间移除成功时，返回 1 。 如果 key 不存在或 key 没有设置过期时间，返回 0 。
  * `pttl key`: 以毫秒为单位返回key的剩余过期时间。当 key 不存在时，返回 -2 。 当 key 存在但没有设置剩余生存时间时，返回 -1 。 否则，以毫秒为单位，返回 key 的剩余生存时间。
  * `ttl key`: 以秒为单位，返回key的剩余过期时间。同上
  * `randomkey`: 从当前数据库中随机返回一个 key 。
  * `rename old_key new_key`: 修改key名称。改名成功时提示 OK ，失败时候返回一个错误。当 old_key 和 new_key 相同，或者 old_key 不存在时，返回一个错误。 当 new_key 已经存在时， rename 命令将覆盖旧值。
  * `renamenx old_key new_key`: 仅当 new_key 不存在时，将 old_key 改名为 new_key 。
  * `scan cursor [MATCH pattern] [COUNT count]`: 迭代数据库中的数据库键。cursor - 游标。pattern - 匹配的模式。count - 指定从数据集里返回多少元素，默认值为 10 。
  `scan`命令是一个基于游标的迭代器，每次被调用之后，都会向用户返回一个新的游标，用户在下次迭代时需要使用这个新游标作为 `scan` 命令的游标参数， 以此来延续之前的迭代过程。
  `scan`返回一个包含两个元素的数组， 第一个元素是用于进行下一次迭代的新游标， 而第二个元素则是一个数组， 这个数组中包含了所有被迭代的元素。如果新游标返回 0 表示迭代已结束。
  * `type key`: 返回 key 所储存的值的类型。返回 key 的数据类型，数据类型有：none (key不存在) string (字符串) list (列表) set (集合) zset (有序集) hash (哈希表)
  * `object encoding key`: 返回指定键的值的编码
* redis配置

  * `config get name`: 获取指定配置属性
  * `config get *`: 获取所有属性配置
  * `config set name value`: 设置指定属性
  * `info [name]`: 获取指定redis信息，memory,cpu...

**keys和scan**

[深入理解Redis的scan命令](https://www.jianshu.com/p/be15dc89a3e8)

`keys`命令的不足之处：

* 没有limit，只能一次性获取所有符合条件的key。可能返回的结果数过大。
* `keys`命令是遍历算法，时间复杂度是O(N)。 容易导致Redis服务卡顿。

相对于`keys` `scan`命令的优势：

* `scan`命令的时间复杂度虽然也是O(N)，但它是分次进行的，不会阻塞线程。
* `scan`命令提供了limit参数，可以控制每次返回结果的最大条数。

## 支持的数据类型

Redis底层使用redisObject对象来表示所有的key和value，即在创建时会生成一个用于键名的redisObject对象和用于键值的redisObject对象。

redisObject结构体：

```c
/*
 * Redis 对象
 */
typedef struct redisObject {
    // 类型
    unsigned type:4;
    // 不使用(对齐位)
    unsigned notused:2;
    // 编码方式
    unsigned encoding:4;
    // LRU 时间（相对于 server.lruclock）
    unsigned lru:22;
    // 引用计数
    int refcount;
    // 指向对象的值
    void *ptr;
} robj;
```

* type：记录了对象的类型名称，值可选为：string，list，hash，set，zset
* encoding：记录了对象所使用的编码名称，值可选为：int，embstr，raw，hashtable， ziplist，intset，linkedlist，skiplist
* lru：记录对象最后一次被程序访问的时间
* refcount：引用计数，初始值为1

### 五种数据类型

#### String

字符串是Redis最基本的类型。Redis字符串是二进制安全的，这意味着Redis字符串可以包含任何类型的数据，例如JPEG图像或序列化的Ruby对象。字符串值的最大长度为512 MB。

字符串对象对应的编码方式是int、raw或者embstr。如果一个字符串的内容可以转换成long，那么就会被转换成long类型，对象的ptr就会指向该long，并且对象编码方式也用int类型表示；对于普通字符串，如果字符串对象的长度小于39字节，就用embstr对象。否则用传统的raw对象。

**常用命令：**

```line
# 设置key值
> set key value
OK
# 获取key值
> get key
value
# 返回 key 中字符串值的子字符
> getrange key 1 2
al
# 将给定 key 的值设为 value ，并返回 key 的旧值(old value)
> getset key newvalue
value
# 将值 value 关联到 key ，并将 key 的过期时间设为 seconds (以秒为单位)
> setex key 10 value
OK
# 只有在 key 不存在时设置 key 的值
> setnx key value
1
# 返回 key 所储存的字符串值的长度
> strlen key
5
# 将 key 中储存的数字值增一
> incr key
2
# 将 key 所储存的值加上给定的增量值（increment）
> incrby key 8
11
# 将 key 所储存的值加上给定的浮点增量值（increment）
> incrbyfloat key 8.8
19.8
# 将 key 中储存的数字值减一
> decr key
10
# key 所储存的值减去给定的减量值（decrement）
> decrby key 5
5
```

* `incr`命令
   Incr 命令将 key 中储存的数字值增一。如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作。如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。本操作的值限制在 64 位(bit)有符号数字表示之内。`incr`命令属于原子性操作。

* `incrbyfloat`命令
   为 key 中所储存的值加上浮点数增量 increment 。如果 key 不存在，那么 INCRBYFLOAT 会先将 key 的值设为 0 ，再执行加法操作。如果命令执行成功，那么 key 的值会被更新为（执行加法之后的）新值，并且新值会以字符串的形式返回给调用者。`incrbyfloat`的计算结果也最多只能表示小数点的后十七位，最后一位四舍五入。
   **注意：**使用`incrbyfloat`命令使key值变成float类型后，再使用`incr`命令/`decr`命令会提示错误：`ERR value is not an integer or out of range`。当float小数点后为0时，又可以使用`incr`命令/`decr`命令。

#### List

Redis列表只是字符串列表，按插入顺序排序。可以将元素添加到Redis列表中，从而将新元素推到列表的头部（左侧）或尾部（右侧）。一个列表最多可以包含 $2^{32} - 1$ 个元素 (4294967295)。极端的时候访问的时间复杂度是O(N)

**常用命令：**

```ssh
# 将一个或多个值插入到列表头部
> lpush list value1 value2
2
# 将一个值插入到已存在的列表头部 不存在的key会返回0
> lpushx list value3
3
# 获取列表指定范围内的元素
> lrange list 0 3
value3
value2
value1
# 获取列表长度
> llen list
3
# 移出并获取列表的第一个元素
> lpop list
value3
# 通过索引获取列表中的元素
> lindex list 1
value1
# 将值 value 插入到列表 key 当中，位于值 pivot 之前或之后。
> linsert key before|after pivot value
# 根据参数 COUNT 的值，移除列表中与参数 VALUE 相等的元素
> lrem list -1 value2
1
# 通过索引设置列表元素的值
> lset list 0 value1new
OK
# 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除
> ltrim key start stop
4
# 移除列表的最后一个元素，返回值为移除的元素
> rpop list
value2
# 在列表尾部添加一个或多个值
> rpush list value1- value2- value3-
8
# 为已存在的列表添加值
> rpushx list value5-
9
# 移出并获取列表的第一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
> BLPOP key1 [key2 ] timeout
# 移出并获取列表的最后一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
> BRPOP key1 [key2 ] timeout
```

* `lrem`命令

  根据参数 COUNT 的值，移除列表中与参数 VALUE 相等的元素。COUNT 的值可以是以下几种：
  count > 0 : 从表头开始向表尾搜索，移除与 VALUE 相等的元素，数量为 COUNT 。
  count < 0 : 从表尾开始向表头搜索，移除与 VALUE 相等的元素，数量为 COUNT 的绝对值。
  count = 0 : 移除表中所有与 VALUE 相等的值。

列表对象的编码可以是ziplist或linkedlist。

ziplist是一种压缩链表，它的好处是更能节省内存空间，因为它所存储的内容都是在连续的内存区域当中的。当列表对象元素不大，每个元素也不大的时候，就采用ziplist存储（所有字符串的长度<64字节，个数<512）。但当数据量过大时就ziplist就不是那么好用了。因为为了保证他存储内容在内存中的连续性，插入的复杂度是O(N)，即每次插入都会重新进行realloc。

linkedlist是一种双向链表。它的结构比较简单，节点中存放pre和next两个指针，还有节点相关的信息。当每增加一个node的时候，就需要重新malloc一块内存。

#### Hash

Hash是一个键值（key-value）的集合。Redis 的 Hash 是一个 String 的 Key 和 Value 的映射表。Redis 中每个 hash 可以存储$2^{32} - 1$键值对（40多亿）。

**常用命令：**

```line
# 同时将多个 field-value (域-值)对设置到哈希表 key 中
> hmset map key1 value1 key2 value2
OK
# 将哈希表 key 中的字段 field 的值设为 value
> hset map key3 value3new
0
# 只有在字段 field 不存在时，设置哈希表字段的值
> hsetnx map key6 value6
1
# 获取哈希表中所有值
> hvals map
value1
value2
value3new
value4
value6
value6
# 获取所有哈希表中的字段
> hkeys map
key1
key2
key3
key4
key5
key6
# 获取哈希表中字段的数量
> hlen map
6
# 获取存储在哈希表中指定字段的值
> hget map key2
value2
# 获取在哈希表中指定 key 的所有字段和值
> hgetall map
key1
value1
key2
value2
key3
value3new
key4
value4
key5
value6
key6
value6
# 获取所有给定字段的值
> hmget map key1 key2
value1
value2
# 查看哈希表 key 中，指定的字段是否存在
> hexists map key1
1
# 删除一个或多个哈希表字段
> hdel map key1
1
# 为哈希表 key 中的指定字段的整数值加上增量 increment 
> hincrby map key1 3
9
# 为哈希表 key 中的指定字段的浮点数值加上增量 increment
> hincrbyfloat map key1 3.1
18.1
# 迭代哈希表中的键值对
> HSCAN key cursor [MATCH pattern] [COUNT count]
```

* `hset`命令

  `hset`命令用于为哈希表中的字段赋值。如果哈希表不存在，一个新的哈希表被创建并进行 HSET 操作。如果字段已经存在于哈希表中，旧值将被覆盖。如果字段是哈希表中的一个新建字段，并且值设置成功，返回 1 。 如果哈希表中域字段已经存在且旧值已被新值覆盖，返回 0 。

* `hscan`命令

* `hincrbby`命令
  
  `hincrbby`命令用于为哈希表中的字段值加上指定增量值。增量也可以为负数，相当于对指定字段进行减法操作。如果哈希表的 key 不存在，一个新的哈希表被创建并执行 HINCRBY 命令。如果指定的字段不存在，那么在执行命令前，字段的值被初始化为 0。对一个储存字符串值的字段执行 HINCRBY 命令将造成一个错误。本操作的值被限制在 64 位(bit)有符号数字表示之内。

哈希对象的底层实现可以是ziplist或者hashtable。
ziplist中的哈希对象是按照key1,value1,key2,value2这样的顺序存放来存储的。当对象数目不多且内容不大时（所有字符串的长度<64字节，个数<512），这种方式效率是很高的。
hashtable的是由dict这个结构来实现的。

#### Set

Set 是 String 类型的无序集合。Set 中的元素是没有顺序的，而且是没有重复的。Set是通过哈希表实现的，所以添加，删除，查找的复杂度都是 O(1)。最大成员数量是$2^{32} - 1$ 个元素 (4294967295)。

**常用命令：**

```line
# 向集合添加一个或多个成员
> sadd set value1 value2
2
# 获取集合的成员数
> scard set
2
# 命令返回给定集合之间的差集。不存在的集合 key 将视为空集。
> sdiff key1 [key2]
# 返回给定所有集合的差集并存储在 destination 中
> sdiffstore destination key1 [key2]
# 返回给定所有集合的交集
> sinter key1 [key2]
# 返回给定所有集合的交集并存储在 destination 中
> sinterstore destination key1 [key2]
# 判断指定元素是否是集合 key 的成员，如果成员元素是集合的成员，返回1。如果成员元素不是集合的成员，或key不存在，返回 0 。
> sismember set value1
1
# 返回集合中的所有成员
> smembers set
value1
value3
value2
# 将 member 元素从 source 集合移动到 destination 集合
> smove source destination member
# 移除并返回集合中的count个随机元素，count 参数在 3.2+ 版本可用，默认为0。
> spop set [count]
value3
# 返回集合中一个或多个随机数
> srandmember set 3
value1
value2
value3
# 移除集合中一个或多个成员
> srem set value1
1
# 返回所有给定集合的并集
> sunion key1 [key2]
# 所有给定集合的并集存储在 destination 集合中
> sunionstore destination key1 [key2]
# 迭代集合中的元素
> sscan key cursor [MATCH pattern] [COUNT count]
```

* `sdiff`命令

  `sdiff`命令返回给定集合之间的差集。不存在的集合 key 将视为空集。差集的结果来自前面的key1与后面集合的差集。

* `srandmember`命令

  `srandmember`命令，从 Redis 2.6 版本开始， Srandmember 命令接受可选的 count 参数：如果 count 为正数，且小于集合基数，那么命令返回一个包含 count 个元素的数组，数组中的元素各不相同。如果 count 大于等于集合基数，那么返回整个集合；如果 count 为负数，那么命令返回一个数组，数组中的元素可能会重复出现多次，而数组的长度为 count 的绝对值。

* `scan`命令

set对象的编码方式可以是intset或hashtable。intset是一个整数有序集合，里面存的为某同一类型的整数，支持的长度为int16_t, int32_t,int64_t。intset查找的复杂度是O(logN)，但是插入的时间复杂度不一定是O(logN)，因为有可能涉及到升级操作。例如：当集合中的全是int16_t型的整数，这时要插入一个int32_t，那么为了维护集合中数据类型的一致，那么所有的数据都会转换成int32_t类型，涉及到内存重新分配，这时插入的时间复杂度为O(N)了。intset不支持降级操作。

#### Zset（sorted set）

Zset 和 Set 一样是 String 类型元素的集合，且不允许重复的元素。区别在于，每个元素都会关联一个double类型的分数。redis正是通过分数来为集合中的成员进行从小到大的排序。有序集合的成员是唯一的,但分数(score)却可以重复。集合是通过哈希表实现的，所以添加，删除，查找的复杂度都是O(1)。 集合中最大的成员数为$2^{32} - 1$。

**常用命令：**

```line
# 向有序集合添加一个或多个成员，或者更新已存在成员的分数
> zadd zset 4 value1 2 value2 2.9 value3
3
# 获取有序集合的成员数
> zcard zset
3
# 计算在有序集合中指定区间分数的成员数
> zcount zset 1 4
3
# 有序集合中对指定成员的分数加上增量 increment
> zincrby key increment member
# 在有序集合中计算指定字典区间内成员数量
> zlexcount key min max
# 通过索引区间返回有序集合指定区间内的成员
> zrange zset 0 -1
value2
value1
value3
> zrange zset 0 -1 withscores
value2
2
value1
4
value3
4.9000000000000004
# 通过字典区间返回有序集合的成员
> ZRANGEBYLEX key min max [LIMIT offset count]
# 通过分数返回有序集合指定区间内的成员
> ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]
# 返回有序集合中指定成员的索引
> zrank zset value2
0
> zrank zset value8
null
# 移除有序集合中的一个或多个成员
> zrem zset value1 value5
1
# 移除有序集合中给定的字典区间的所有成员
> ZREMRANGEBYLEX key min max
# 移除有序集合中给定的排名区间的所有成员
> zremrangebyrank zset 0 1
2
# 移除有序集合中给定的分数区间的所有成员
> zremrangebyscore zset 1 2
2
# 返回有序集中指定区间内的成员，通过索引，分数从高到低，具有相同分数值的成员按字典序的逆序(reverse lexicographical order)排列。
> zrevrange zset 0 -1 withscores
value5
5
value4
4
value3
3
# 返回有序集中指定分数区间内的成员，分数从高到低排序，具有相同分数值的成员按字典序的逆序(reverse lexicographical order )排列。
> ZREVRANGEBYSCORE zset 4 3 withscores
value4
4
value3
3
# 返回有序集合中指定成员的排名，有序集成员按分数值递减(从大到小)排序
> zrevrank zset value5
0
# 返回有序集中，成员的分数值
> zscore zset value5
5
# 计算给定的一个或多个有序集的交集并将结果集存储在新的有序集合 key 中
> ZINTERSTORE destination numkeys key [key ...]
# 计算给定的一个或多个有序集的并集，并存储在新的 key 中
> ZUNIONSTORE destination numkeys key [key ...]


```

* `zincrby`命令
  
  `zincrby`命令对有序集合中指定成员的分数加上增量 increment，对于浮点型数据有丢失精度问题：
  
  ```text
    > zincrby zset 5 value6
    7.5
    > zincrby zset 5.3 value6
    12.800000000000001
  ```

* `zlexcount`命令

  `zlexcount`命令在计算有序集合中指定字典区间内成员数量。字典区间就是两个参数在英文字典(lexic)中的排序位置所形成的区间,查询哪些member落在区间内。**搞不懂这个区间到底怎么计算的**

* `ZRANGEBYLEX key min max [LIMIT offset count]`命令

  通过字典区间返回有序集合的成员。

* `ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]`命令

  返回有序集合中指定分数区间的成员列表。有序集成员按分数值递增(从小到大)次序排列。具有相同分数值的成员按字典序来排列(该属性是有序集提供的，不需要额外的计算)。默认情况下，区间的取值使用闭区间 (小于等于或大于等于)，可以通过给参数前增加`(`符号来使用可选的开区间 (小于或大于)。
  如：`ZRANGEBYSCORE zset (5 (10`表示 5 < score < 10；`ZRANGEBYSCORE salary -inf +inf` 表示显示整个有序集。

zset对象的底层实现可以是ziplist或者skiplist+hashtable。

## redis淘汰策略

redis可以设置最大内存量，当超过时会执行淘汰策略。

* `maxmemory`属性，设置redis的最大内存使用量，64位系统默认为0（不限制大小），32位系统`maxmemory`会设置默认值为3GB。

* `maxmemory-policy`属性，设置redis的淘汰策略，默认配置为`noeviction`。

### 淘汰策略

* `volatile-lru`: 从已设置过期时间的数据集中挑选最近最不经常使用的数据淘汰。
* `volatile-ttl`: 从已设置过期时间的数据集中挑选将要过期的数据进行淘汰
* `volatile-random`: 从已设置过期时间的数据集中任意选择数据淘汰
* `allkeys-lru`: 从所有数据集中挑选最近最不经常使用的数据淘汰
* `allkeys-random`: 从所有数据集中任意选择数据淘汰
* `noeviction`: 在达到内存限制并且客户端尝试执行可能导致使用更多内存的命令时返回错误(no eviction)

### 淘汰策略的执行过程

淘汰策略的执行过程如下：

* 1.客户端运行新命令，从而添加更多数据；
* 2.Redis会检查内存使用情况，如果大于maxmemory限制，会根据淘汰策略删除一些key；
* 3.新命令执行成功。

如果一次需要使用很多的内存（比如一次写入一个很大的set），那么，Redis 的内存使用可能超出最大内存限制一段时间。

### 近似LRU算法

Redis的LRU算法不是严格意义上的LRU算法实现，是一种近似的LRU实现，主要是为了节约内存占用以及提升性能。Redis会根据配置项`maxmemory-samples`的数量，取出指定数目的key，然后从中选择一个最近最不经常使用的key进行置换，默认值是5。

Redis 3.0起，对该算法进行了改进，使其也可以将大量优秀候选人逐出。这提高了算法的性能，使其能够更接近地逼真的LRU算法的行为。可以通过修改配置`maxmemory-samples`的调整算法的精度，将期设置为10，那么Redis将会增加额外的 CPU开销以保证接近真正的 LRU 性能。通过`CONFIG SET maxmemory-samples <count>`动态调整样本数大小。

Redis使用的近似LRU与真实LRU比较的图形:

![Redis使用的近似LRU与真实LRU比较的图形](https://wx1.sbimg.cn/2020/05/31/lru_comparison.png)

### LFU算法

Redis4.0开始支持LFU算法，根据key的使用频率，最少使用的key会被淘汰。

支持的策略：

* `volatile-lfu`: 从已设置过期时间的数据集中挑选最少使用的数据淘汰
* `allkeys-lfu`: 从所有数据集中挑选最少使用的数据淘汰

[Redis的缓存淘汰策略LRU与LFU](https://www.jianshu.com/p/c8aeb3eee6bc)
[Redis中LFU算法的深入分析](https://www.jb51.net/article/162359.htm)

## serverCron

## 缓存问题

### 缓存穿透

缓存穿透是指对一个不存在的数据进行请求，该请求将会穿透缓存达到数据库。
解决方法：

* 对于这些不存在的数据缓存空数据
* 对于这类请求进行过滤，比如对于请求的id<=0数据直接返回null
* 布隆过滤器

### 缓存击穿

在高并发下，对一个特定的值进行查询，但是这个时候缓存正好过期了，缓存没有命中，导致大量请求直接落到数据库上。

解决方法：

* 限流
* 缓存预热，对于即将过期的数据可以通过异步线程进行更新
* 对于热点key可以设置永不过期，或者更新时加上互斥锁

### 缓存雪崩

指的是由于数据没有被加载到缓存中，或者缓存数据在同一时间大面积失效（过期），又或者缓存服务器宕机，导致大量的请求都到达数据库。在有缓存的系统中，系统非常依赖于缓存，缓存分担了很大一部分的数据请求。当发生缓存雪崩时，数据库无法处理这么大的请求，导致数据库崩溃。

解决方法：

* 对于过期key的大面积过期可能造成的缓存雪崩，在设置过期时间时加上一个随机时间解决
* 缓存预热
* 为了防止缓存服务器宕机出现的缓存雪崩，可以使用分布式缓存，分布式缓存中每一个节点只缓存部分的数据，当某个节点宕机时可以保证其它节点的缓存仍然可用。

### 缓存一致性

缓存一致性要求更新的同时缓存数据也能够实时更新

解决方法：

* 在更新数据的同时立即去更新缓存
* 在读缓存之前先判断是否是最新的，如果不是最新的先进行更新

要保证缓存一致性需要付出很大的代价，缓存数据最好是那些对一致性要求不高的数据，允许出现一些脏数据。

### 缓存 “无底洞” 现象

指的是为了满足业务要求添加了大量缓存节点，但是性能不但没有好转反而下降了的现象。

产生的原因：缓存系统通常采用hash函数将key映射到对应的缓存节点上，随着缓存节点的增加，键值分布到更多的节点上，导致客户端一次批量操作涉及多次网络操作，这意味着批量操作的耗时会随着节点的增加而增加。此外网络链接数变多对节点性能也有一定影响。

解决方案：

* 优化批量数据操作命令；
* 减少网络通信次数；
* 降低接入成本，使用长连接 / 连接池，NIO 等。

## 持久化

Redis是内存型数据库，为了保证数据在断电后不会丢失，需要将内存中的数据持久化到硬盘上。Redis的持久化策略有两，RDB和AOF

### RDB持久化

[Redis RDB 持久化方式](https://www.jianshu.com/p/c0e2c54b6519)

按指定的时间间隔以快照形式将内存中的数据保存到dump.rdb文件中。

#### RDB的创建

* `save`命令：执行一个同步保存操作，将当前 Redis 实例的所有数据快照(snapshot)以 RDB 文件的形式保存到硬盘。
  当执行`save`命令时，会阻塞 Redis 服务器进程，直到 RDB 文件创建完毕为止，在服务器进程阻塞期间，服务器不能处理任何命令请求。对应函数rdb.c/rdbSave。

* `bgsave`命令：派生出一个子进程，然后由子进程负责创建 RDB 文件，父进程继续处理命令请求。对应函数rdb.c/rdbSaveBackground
  
  子进程创建RDB文件的过程中，Redis服务器仍然可以继续处理客户端的命令，但是在`bgsave`命令执行期间，服务器处理 `save`、`bgsave`、`bgrewriteaof` 三个命令的方式会和平时有所不同。
  * 在`bgsave`命令执行期间，客户端发送的`save`命令会被服务器拒绝，服务器禁止`save`命令和`bgsave`命令同时执行是为了避免父进程和子进程同时执行两个 rdbSave 调用，防止产生竞争条件。
  * 在`bgsave`命令执行期间，客户端发送的`bgsave`命令会被服务器拒绝，因为同时执行两个`bgsave`命令也会产生竞争条件。
  * `bgrewirteaof`和`bgsave`两个命令不能同时执行：
    * `bgsave`命令在执行过程中，客户端发送的`bgrewriteaof`命令会被延迟到`bgsave`命令执行完后执行；
    * 如果`bgrewirteaof`命令正在执行，那么客户端发送的`bgsave`命令会被服务器拒绝；
    * 因为`bgrewirteaof`和`bgsave`两个命令的实际工作都由子进程执行，所以这两个命令在操作方面并没有什么冲突的地方，不能同时执行它们只是一个性能方面的考虑一一并发出两个子进程，并且这两个子进程都同时执行大量的磁盘写入操作。

#### RDB的载入

RDB文件的载入（恢复）：在redis服务启动时自动执行。如果配置`appendonly`为no（AOF持久化功能处于关闭状态），并且存在RDB文件，redis就会自动载入。**服务器在载入RDB 文件期间，会一直处于阻塞状态，直到载入工作完成为止。**

载入操作由rdb.c/rdbLoad函数执行。

#### 自动间隔性保存

Redis服务器会根据相关配置是否满足自动执行bgsave操作。

相关配置为：

```text
save 900 1
save 300 10
save 60 10000
```

上面配置是Redis的默认配置，表示为满足：1.在900秒之内，对数据库进行了至少1次修改；2.在300秒内对数据库进行了10修改；3.在60秒内对数据库进行了10000次修改。三个条件中的任意一个会执行bgsave操作。

这些配置项保存在:

```c
struct redisServer {
  // ...
  long long dirty;                /* Changes to DB from the last save */
  struct saveparam *saveparams;   /* Save points array for RDB */
  time_t lastsave;                /* Unix time of last successful save */
  // ...
}

// saveparam结构
struct saveparam {
    time_t seconds;
    int changes;
};
```

* dirty属性记录距离上次成功执行save命令或bgsave命令后，服务器对数据库状态进行了多少次修改（包括写入、删除、更新等操作）。
* lastsave属性是一个UNIX时间戳，记录服务器上一次成功执行save命令或bgsave命令的时间。

执行的大致过程为：

在函数serverCron内周期检查，如果没有正在进行的后台保存RDB或重写AOF操作，检查是否满足RDB保存配置，如果达到了给定的更改量、给定的秒数，并且最近的bgsave成功，或者在发生错误的情况下，至少已过CONFIG_BGSAVE_RETRY_DELAY秒（默认5s），则执行保存。在函数的最后也有一个检查，检查是否有因为重写AOF导致bgsave被推迟的情况，如果有并且最近的bgsave成功，或者在发生错误的情况下，至少已过CONFIG_BGSAVE_RETRY_DELAY秒（默认5s），则立即执行bgsave。

主要代码：

```c
  // ....
  /* Check if a background saving or AOF rewrite in progress terminated. */
    if (hasActiveChildProcess() || ldbPendingChildren())
    {
        checkChildrenDone();
    } else {
        /* If there is not a background saving/rewrite in progress check if
         * we have to save/rewrite now. */
        for (j = 0; j < server.saveparamslen; j++) {
            struct saveparam *sp = server.saveparams+j;

            /* Save if we reached the given amount of changes,
             * the given amount of seconds, and if the latest bgsave was
             * successful or if, in case of an error, at least
             * CONFIG_BGSAVE_RETRY_DELAY seconds already elapsed. */
            if (server.dirty >= sp->changes &&
                server.unixtime-server.lastsave > sp->seconds &&
                (server.unixtime-server.lastbgsave_try >
                 CONFIG_BGSAVE_RETRY_DELAY ||
                 server.lastbgsave_status == C_OK))
            {
                serverLog(LL_NOTICE,"%d changes in %d seconds. Saving...",
                    sp->changes, (int)sp->seconds);
                rdbSaveInfo rsi, *rsiptr;
                rsiptr = rdbPopulateSaveInfo(&rsi);
                rdbSaveBackground(server.rdb_filename,rsiptr);
                break;
            }
        }
        // ...
  /* Start a scheduled BGSAVE if the corresponding flag is set. This is
     * useful when we are forced to postpone a BGSAVE because an AOF
     * rewrite is in progress.
     *
     * Note: this code must be after the replicationCron() call above so
     * make sure when refactoring this file to keep this order. This is useful
     * because we want to give priority to RDB savings for replication. */
    if (!hasActiveChildProcess() &&
        server.rdb_bgsave_scheduled &&
        (server.unixtime-server.lastbgsave_try > CONFIG_BGSAVE_RETRY_DELAY ||
         server.lastbgsave_status == C_OK))
    {
        rdbSaveInfo rsi, *rsiptr;
        rsiptr = rdbPopulateSaveInfo(&rsi);
        if (rdbSaveBackground(server.rdb_filename,rsiptr) == C_OK)
            server.rdb_bgsave_scheduled = 0;
    }
    // ...
```

#### RDB的优势

* RDB非常适合备份，RDB文件是Redis数据的非常紧凑的单文件时间点表示。可以轻松还原数据集的不同版本。
* RDB对于灾难回复非常有用，它是一个紧凑的文件，可以传输到远程数据中心。
* RDB最大限度提高
* 与AOF相比，RDB允许大型数据集更快地重启。

#### RDB的缺点

* 如果需要最大程度地减少数据丢失的可能性，RDB不适用。RDB通常在几分钟内达到指定修改才会进行备份，如果在这期间Redis没有正确关闭下停止工作，会丢失数分钟的数据。
* RDB通常需要fork()子进程进行持久化操作，如果数据集很大，fork()可能很耗时，如果数据集很大，CPU性能不好，可能会导致Redis停止为客户机服务几毫秒甚至一秒钟。

RDB相关配置：

* `dbfilename`: 持久化数据存储在本地的文件，默认值dump.rdb。
* `dir`: 持久化数据存储在本地的路径。
* `save`: save时间，默认配置：`save 900 1`表示更改了1个key时间隔900s进行持久化存储；`save 300 10`表示更改了10个key时间隔300s进行存储；`save 60 10000`表示更改10000个key时间隔60s进行存储。满足上面条件`bgsave`命令就会被执行。

**检查保存条件是否满足：**

Redis 的服务器周期性操作函数 serverCron 默认每隔 100 毫秒就会执行一次，该函数用于对正在运行的服务器进行维护，它的其中一项工作就是检查 save 选项所设置的保存条件是否已经满足，如果满足的话，就执行`bgsave`命令。

### AOF持久化

记录写命令添加到AOF文件（Append Only File）的末尾。

AOF 可以做到全程持久化，只需要在配置中开启 appendonly yes。这样 Redis 每执行一个修改数据的命令，都会把它添加到 AOF 文件中，当 Redis 重启时，将会读取 AOF 文件进行重放，恢复到 Redis 关闭前的最后时刻。

重启redis服务，前提是配置文件必须设置了appendonly yes，然后会从appendfile的文件加载文件。反之是从RDB中加载数据的。

#### AOF重写

因为AOF持久化是通过保存被执行的写命令来记录数据状态的，所以随着服务器运行时间的流逝，AOF文件中的内存回越来越多，文件体积越来越来，这时候就需要重写AOF。

AOF重写代码位置aof.c/rewriteAppendOnlyFileBackground。

AOF重写的原理是从数据获取键当前的值，然后用一条命令去记录键值对，代替之前记录这个键值对的多条命令。

**注意：**在AOF重写中，为了避免在执行命令时造成客户端输入缓冲区溢出，重写程序在处理列表、哈希表、集合、有序集合这四种可能出现带有多个元素时，会先检查键所包含的元素数是否超过server.h/AOF_REWRITE_ITEMS_PER_CMD常量的值（默认值64），如果超过这个值就使用多个命令来记录键的值，而不是使用单一命令，每个命令保存的值最大不超过server.h/AOF_REWRITE_ITEMS_PER_CMD。

#### AOF后台重写

AOF重写会进行大量的写入操作，直接调用重写函数会阻塞服务器，所以AOF重写程序放到子进程里执行，这样的目的：

* 子进程进行AOF重写期间，服务器进程（父进程）可以继续处理命令请求；
* 子进程带有服务器进程的数据副本，使用子进程而不是线程，可以避免使用锁的情况下，保证数据的安全性。

在AOF重写期间，服务器执行写命令后需要将写命令追加到AOF缓存区和AOF重写缓冲区。

* AOF缓冲区的内容会定期被写入和同步到AOF文件，对现有AOF文件的处理工作会如常进行。
* 从创建子进程开始，服务器执行的所有写命令都会被记录到AOF重写缓冲区里面。当子进程完成AOF重写工作之后，它会向父进程发送一个信号，父进程在接收到该信号之后，会调用一个信号处理函数执行以下工作：
  * 将AOF重写缓冲区中的所有内容写入到新AOF文件中，这是新AOF文件所保存的数据库状态将和服务器当前的数据库状态一致。
  * 对新的AOF文件进行改名，原子地（atomic）覆盖现有的AOF文件，完成新旧两个AOF文件的替换。

Redis服务器会根据相关配置是否满足自动执行AOF重写操作。

执行的大致过程为：

在函数serverCron内周期检查：
1.如果没有正在执行的子进程（包括正在执行的bgsave、aof重写等）并且在上次AOF重写中有因为其他子进程执行（包括正在执行的bgsave、aof重写等）而导致AOF重写被抑制，则执行AOF重写。

2.如果没有正在执行的子进程（包括正在执行的bgsave、aof重写等），检查是否需要进行AOF重写操作：如果重写百分比配置存在并且当前AOF文件大于运行重写的最小文件大小，并且当前AOF文件的大小比上次文件的大小增加的百分比大于等于重写百分比。

部分代码：

```c
int serverCron(struct aeEventLoop *eventLoop, long long id, void *clientData) {

    // ...
    /* Start a scheduled AOF rewrite if this was requested by the user while
     * a BGSAVE was in progress. */
    if (!hasActiveChildProcess() &&
        server.aof_rewrite_scheduled)
    {
        rewriteAppendOnlyFileBackground();
    }

     /* Check if a background saving or AOF rewrite in progress terminated. */
    if (hasActiveChildProcess() || ldbPendingChildren())
    {
        checkChildrenDone();
    } else {
        // ...
        /* Trigger an AOF rewrite if needed. */
        if (server.aof_state == AOF_ON &&
            !hasActiveChildProcess() &&
            server.aof_rewrite_perc &&
            server.aof_current_size > server.aof_rewrite_min_size)
        {
            long long base = server.aof_rewrite_base_size ?
                server.aof_rewrite_base_size : 1;
            long long growth = (server.aof_current_size*100/base) - 100;
            if (growth >= server.aof_rewrite_perc) {
                serverLog(LL_NOTICE,"Starting automatic rewriting of AOF on %lld%% growth",growth);
                rewriteAppendOnlyFileBackground();
            }
        }
    }
    // ...
}
```

#### AOF的优势

* 使Redis持久化更好，可以设置不同的fsync策略，AOF的默认策略是每秒钟 Fsync 一次，在这种配置下，就算发生故障停机，也最多丢失一秒钟的数据。
* AOF日志是仅追加的日志，因此，如果断电，则不会出现寻道或损坏问题。即使由于某种原因（磁盘已满或其他原因）以半写命令结束日志，redis-check-aof工具也可以轻松修复它。
* 当AOF文件太大时，Redis可以在后台自动重写AOF。
* AOF以易于理解和解析的格式包含所有操作的日志。

#### AOF的缺点

* 对于同一数据集，AOF文件通常大于等效的RDB文件。
* 根据具体的fsync策略，AOF可能比RDB慢。

#### AOF相关配置

* `appendonly`: 控制AOF是否开启，默认yes。
* `appendfilename`: AOF保存的文件名，默认appendonly.aof。
* `appendfsync`: AOF的fsync策略，默认值everysec。always：一旦插入命令，立即同步到磁盘，保证了完全的持久化，但是速度慢，且浪费Redis的性能，不推荐；everysec：AOF每秒进行同步；no：不自动同步，根据系统进行同步，性能最好，但是持久化没有保证。通常，Linux使用此配置每30秒刷新一次数据，但这取决于内核的精确调整。
* `auto-aof-rewrite-percentage`: 当目前的AOF文件大小超过上一次重写文件大小的百分之几时进行重写，如果没有重启过，则以启动时的AOF文件大小为依据，默认值100。
* `auto-aof-rewrite-min-size`: 允许重写的最小AOF文件大小，默认值64MB

## Redis哨兵模式、集群模式

## 参考

* [Redis 教程](https://www.runoob.com/redis/redis-keys.html)
* [搞懂这些Redis知识点](https://baijiahao.baidu.com/s?id=1660009541007805174&wfr=spider&for=pc)
* [redis进阶: redisObject对象详解](http://www.fidding.me/article/108)
* [redis底层原理](https://blog.csdn.net/wcf373722432/article/details/78678504)
* [github Redis](https://github.com/CyC2018/CS-Notes/blob/master/notes/Redis.md)
* [github 缓存](https://github.com/CyC2018/CS-Notes/blob/master/notes/%E7%BC%93%E5%AD%98.md)
* [缓存穿透、缓存击穿和缓存雪崩实践](https://www.jianshu.com/p/d00348a9eb3b)
