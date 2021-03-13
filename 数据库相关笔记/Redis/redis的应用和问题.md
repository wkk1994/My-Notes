# redis的应用和问题

## 基于Redis实现分布式锁

相关文章:

* [Redis的分布式锁](https://redis.io/topics/distlock)
* [基于Redis的分布式锁到底安全吗（下）？](https://mp.weixin.qq.com/s?__biz=MzA4NTg1MjM0Mg==&mid=2657261521&idx=1&sn=7bbb80c8fe4f9dff7cd6a8883cc8fc0a&chksm=84479e08b330171e89732ec1460258a85afe73299c263fcc7df3c77cbeac0573ad7211902649#rd)
* [基于Redis的分布式锁到底安全吗（上）？](https://mp.weixin.qq.com/s?__biz=MzA4NTg1MjM0Mg==&mid=2657261514&idx=1&sn=47b1a63f065347943341910dddbb785d&chksm=84479e13b3301705ea29c86f457ad74010eba8a8a5c12a7f54bcf264a4a8c9d6adecbe32ad0b&scene=21#wechat_redirect)

## Redis大key优化方案

### 大key带来的问题

* 读写大key会导致超时严重，甚至阻塞服务。
* 集群模式在slot分片均匀情况下，会出现数据和查询倾斜情况，部分有大key的Redis节点占用内存多，QPS高。
* 大key相关的删除或者自动过期时，会出现qps突降或者突升的情况，极端情况下，会造成主从复制异常，Redis服务阻塞无法响应请求。

### 如何确定大key

* redis-rdb-tools工具。redis实例上执行bgsave，然后对dump出来的rdb文件进行分析，找到其中的大KEY。

* redis-cli --bigkeys命令。可以找到某个实例5种数据类型(String、hash、list、set、zset)的最大key。

* 自定义的扫描脚本，以Python脚本居多，方法与redis-cli --bigkeys类似。

* debug object key命令。可以查看某个key序列化后的长度，每次只能查找单个key的信息。官方不推荐。

* memory usage命令。给出一个key和它值在RAM中占用的字节数。对于嵌套数据类型，可以使用选项SAMPLES，其中COUNT表示抽样的元素个数，默认值为5。当需要抽样所有元素时，使用SAMPLES 0。

#### redis-cli --bigkeys命令

`redis-cli --bigkeys`是redis-cli自带的一个命令。

使用示例：

```text
redis-cli -h 127.0.0.1 -p 6379 -a admin123 --bigkeys
```

输出结果：

```text
Warning: Using a password with '-a' or '-u' option on the command line interface may not be safe.

# Scanning the entire keyspace to find biggest keys as well as
# average sizes per key type.  You can use -i 0.1 to sleep 0.1 sec
# per 100 SCAN commands (not usually needed).

[00.00%] Biggest string found so far 'bigKey3' with 5248652 bytes
[00.00%] Biggest string found so far 'bigKey1' with 5255108 bytes
[00.00%] Biggest list   found so far 'bigKeyList3' with 1 items
[00.00%] Biggest set    found so far 'coffee' with 1 members
[00.00%] Biggest list   found so far 'bigKeyList1' with 2 items

-------- summary -------

Sampled 8 keys in the keyspace!
Total key length in bytes is 64 (avg len 8.00)

Biggest string found 'bigKey1' has 5255108 bytes
Biggest   list found 'bigKeyList1' has 2 items
Biggest    set found 'coffee' has 1 members

4 strings with 15752260 bytes (50.00% of keys, avg size 3938065.00)
3 lists with 4 items (37.50% of keys, avg size 1.33)
1 sets with 1 members (12.50% of keys, avg size 1.00)
0 hashs with 0 fields (00.00% of keys, avg size 0.00)
0 zsets with 0 members (00.00% of keys, avg size 0.00)
0 streams with 0 entries (00.00% of keys, avg size 0.00)
```

扫描结果只显示，当前扫描发现的最大key；summary中显示每种数据结构中最大的Key以及统计信息。

**说明：**

* `redis-cli --bigkeys`使用scan方式对key进行统计，不会对redis造成阻塞。
* 扫描结果中只有string类型是以字节长度为衡量标准，List、set、zset等都是以元素个数作为衡量标准，元素个数多不能说明占用内存就一定多。
* 扫描结果的信息较少，内容不够精确。

#### redis-rdb-tools工具

rdb工具使用离线分析RDB文件的方式获取大key，可以看到输出的信息包括数据类型，key、内存大小、编码类型等。Rdb工具优点在于获取的key信息详细、可选参数多、支持定制化需求，结果信息可选择json或csv格式，后续处理方便，其缺点是需要离线操作，获取结果时间较长。

使用命令示例：

```text
rdb -c memory /home/app/redis/data/dump.rdb --bytes 6291512 -f memory.csv
```

输出示例：

```text
database,type,key,size_in_bytes,encoding,num_elements,len_largest_element,expiry
0,string,bigKey3,6291512,string,5248652,5248652,
0,string,bigKey1,6291512,string,5255108,5255108,
0,list,bigKeyList1,10510501,quicklist,2,5255108,
0,string,bigKey2,6291512,string,5248496,5248496,
```

命令参数列表：

* 必要参数
  * dump_file rdb文件的地址。
* 可选参数
  * -c CMD, --command CMD 命令，可选值：json：输出为json格式数据；memory：输出为csv格式数据；justkeys：只输出key；justkeyvals：只输出value；
  * -f FILE, --file FILE 指定输出的文件名称。
  * -n DBS, --db DBS 数据库号，可以是多个，如果未指定，则将包括所有数据库。
  * -k KEYS, --key KEYS 指定检查的键，支持正则表达式。
  * -t TYPES, --type TYPES 指定要检查的数据类型，可选值有 string,hash, set, sortedset, list。
  * -b BYTES, --bytes BYTES 指定大于改数值的内存才输出。
  * -l LARGEST, --largest LARGEST 将内存输出限制为前N个键（按大小）
  * -x, --no-expire
  * -a N, --amend-expire N

[rdb工具地址](https://github.com/sripathikrishnan/redis-rdb-tools)

[使用参考](https://my.oschina.net/momomo/blog/3067124)

#### debug object key

redis的命令，可以查看某个key序列化后的长度。

命令示例：

```text
> debug object bigKey1
Value at:0x7fd2a98c45c0 refcount:1 encoding:raw serializedlength:1075180 lru:4165173 lru_seconds_idle:245960
```

输出说明：

* Value at：key的内存地址
* refcount：引用次数
* encoding：编码类型
* serializedlength：序列化长度
* lru_seconds_idle：空闲时间

[其他Object命令说明](https://blog.csdn.net/qmhball/article/details/85342007)

#### memory usage命令

`memory usage key [samples count]`命令是redis 4.0开始提供的命令。

memory usage是通过调用objectComputeSize来计算key的大小。可选参数`samples`指定嵌套类型的抽样样本数量，默认大小为5。objectComputeSize大致的逻辑是，对于嵌套类型根据`samples`的大小查询指定数量的样本，通过样本总大小除以样本数量获得的平均值再乘以数据的总数量得到的值就是该key的总大小。

使用示例：

```text
> memory usage bigKey1
5255169
```

#### 自定义的扫描脚本

可以通过Python脚本在集群低峰时扫描Redis，用较小的代价去获取所有key的内存大小。

### 如何删除大key

在对大key删除或者自动过期时，会出现qps突降或者突升的情况。

大key的体积与删除耗时可参考下表：

|类型|数量|删除用时|
|--|--|--|
| Hash | 100万 | 1000ms |
| List | 100万 | 1000ms |
| Set | 100万 | 1000ms |
| Sorted Set | 100万 | 1000ms |

#### lazyfree机制

Redis 4.0开始支持lazyfree机制。lazyfree的原理是在删除的时候，先删除key，将val的释放放到bio(Background I/O)单独的子线程处理中，减少删除大key对Redis主线程的阻塞，有效地避免因删除大key带来的性能问题。

lazyfree机制的源码分析：

```c
/* delGenericCommand函数根据lazy参数来决定是同步删除还是异步删除。 */
void delGenericCommand(client *c, int lazy) {
    int numdel = 0, j;

    for (j = 1; j < c->argc; j++) {
        expireIfNeeded(c->db,c->argv[j]);
        int deleted  = lazy ? dbAsyncDelete(c->db,c->argv[j]) :
                              dbSyncDelete(c->db,c->argv[j]);
        if (deleted) {
            signalModifiedKey(c,c->db,c->argv[j]);
            notifyKeyspaceEvent(NOTIFY_GENERIC,
                "del",c->argv[j],c->db->id);
            server.dirty++;
            numdel++;
        }
    }
    addReplyLongLong(c,numdel);
}

/* 当数据库里有删除键、值或者相关的过期条目时，如果有足够的内存来释放key，
 * 则可以将其放入延迟释放列表来替代同步释放。惰性删除的列表将在另一个bio.c线程中回收 */
#define LAZYFREE_THRESHOLD 64
int dbAsyncDelete(redisDb *db, robj *key) {
    /* 如果是过期键，需要先删除过期字典中的key引用 */
    if (dictSize(db->expires) > 0) dictDelete(db->expires,key->ptr);

    /* If the value is composed of a few allocations, to free in a lazy way
     * is actually just slower... So under a certain limit we just free
     * the object synchronously. */
    dictEntry *de = dictUnlink(db->dict,key->ptr);
    if (de) {
        robj *val = dictGetVal(de);
        /*获取key的value的大小，不同的类型大小的计算方式不同*/
        size_t free_effort = lazyfreeGetFreeEffort(val);

        /* 对删除key进行判断，满足阈值条件时进行后台删除  
         * 如果free_effort大于64，并且value的引用数等于1 */
        if (free_effort > LAZYFREE_THRESHOLD && val->refcount == 1) {
            atomicIncr(lazyfree_objects,1);
            /* 将value对象放入BIO_LAZY_FREE后台线程任务队列 */
            bioCreateBackgroundJob(BIO_LAZY_FREE,val,NULL,NULL);
            /* 将key的value设置为null，方便下面释放key对象 */
            dictSetVal(db->dict,de,NULL);
        }
    }

    /* 删除数据库字典条目，释放资源 */
    if (de) {
        dictFreeUnlinkedEntry(db->dict,de);
        if (server.cluster_enabled) slotToKeyDel(key->ptr);
        return 1;
    } else {
        return 0;
    }
}

```

lazyfree相关参数：

* lazyfree-lazy-eviction 是否开启基于lazyfree的驱逐功能（由于 maxmemory 和 maxmemory policy 配置导致的内存回收动作），可选值：yes，表示开启。no，默认值，表示不开启。
* lazyfree-lazy-expire 是否开启基于lazyfree的过期key删除功能，可选值：yes，表示开启。no，默认值，表示不开启。
* lazyfree-lazy-server-del 内部删除选项是否基于lazyfree异步删除数据，比如rename命令将oldkey修改为一个已存在的newkey时，会先将newkey删除掉。如果newkey是一个大key,可能会引起阻塞删除。可选值：yes，表示开启。no，默认值，表示不开启。建议开启。
* replica-lazy-flush 是否开启复制副本时清空数据开启lazyfree功能，在复制过程中，当一个 replica 节点执行一个全量同步时，replica 需要删除整个数据库的内容以加载传输过来的 RDB 文件，可选值：yes，表示开启。no，默认值，表示不开启。
* lazyfree-lazy-user-del 执行DEL命令时是否基于lazyfree异步删除数据，可选值：yes，表示开启。no，默认值，表示不开启。`unlink key`命令会将key进行lazyfree异步释放，`del key`命令会根据该值决定是否lazyfree异步释放。

#### 参考

[lazyfree 和memory usage源码分析](https://www.cnblogs.com/chou1214/p/11514468.html)

### 如何优化大key

**string类型的数据存储的value值很大**

可以尝试将对象分拆成多个key-value， 使用multiGet获取值，这样分拆的意义在于分拆单次操作的压力，将操作压力平摊到多个redis实例中，降低对单个redis的IO影响；

如果该值每次都只需要存储部分数据，可以将对象拆分成多个key-value，然后存储在一个hash中，每个field代表一个具体的属性，使用hget,hmget来获取部分的value，使用hset，hmset来更新部分属性。

**hash， set，zset，list 中存储过多的元素**

这些类型的数据存储的元素过多时，可以将这些元素分拆。

以hash为例，原先的正常存取流程是 hget(hashKey, field) ; hset(hashKey, field, value)
现在，固定一个桶的数量，比如 10000， 每次存取的时候，先在本地计算field的hash值，模除 10000， 确定了该field落在哪个key上。

```c
newHashKey  =  hashKey + (*hash*(field) % 10000）;
hset (newHashKey, field, value) ;  
hget(newHashKey, field)
```

set, zset, list 也可以类似上述做法。但有些不适合的场景，比如，要保证 lpop 的数据的确是最早push到list中去的，这个就需要一些附加的属性，或者是在 key的拼接上做一些工作（比如list按照时间来分拆）。

#### 参考

* [如何解决Redis大key问题](https://www.jianshu.com/p/50c0894c0a19)
