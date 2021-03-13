# redisObject对象介绍

## redis数据结构

### SDS（简单动态字符串）

Redis没有直接使用C语言传统的字符串表示，而是自己实现了一个动态字符串，并将SDS用作Redis的默认字符串表示。

SDS结构：

```c
struct sdshdr {
    // buf 中已占用空间的长度
    int len;

    // buf 中剩余可用空间的长度
    int free;

    // 字节数组，用于保存字符串
    char buf[];
};
```

SDS示例：

![SDS.png](https://wx2.sbimg.cn/2020/06/07/SDS.png)

说明：

* free属性的值为0，表示这个SDS没有分配任何未使用空间。
* len属性的值为5，表示这个SDS保存了一个五字节长的字符串。
* buf属性是一个char类型的数组，数组的前五个字节分别保存了Redis五个字符，而最后一个字节则保存了空字符'\0'   。
  SDS遵循C字符串以空字符结尾的惯例，保存的空字符的1字节空间不计入len属性。遵循C字符串的好处是可以直接重用C字符串库里面的函数。

#### SDS与C字符串的区别

1. **常数复杂度获取字符串长度**

  因为C字符串并不记录自身的长度信息，所以获取C字符串长度需要遍历整个字符串，时间复杂度为O(N)。SDS在len属性中记录了SDS本身的长度，所以获取一个SDS长度的复杂度为O(1)。

2. **杜绝缓冲区溢出**

  C字符串不记录自身长度带来的另一个问题是容易造成缓冲区溢出，例如当进行字符串拼接的时候，如果没有分配足够的内存就会产生缓冲区溢出。SDS的空间分配策略完全杜绝发生缓冲区溢出的可能性：当SDS API需要对SDS进行修改时，API会先检查SDS的空间是否满足所修改的要求，如果不慢足，API会自动将SDS的空间扩展至执行修改所需的大小。

3. **减少修改字符串时带来的内存重分配次数**

  Redis 作为高性能的内存数据库，需要较高的相应速度。字符串也很大概率的频繁修改。如果使用C字符串，每次增长或者缩短一个C字符串，程序都总要对保存这个C字符串进行一次内存重分配操作。每次修改字符串的长度都需要执行一次内存重分配的话，那么光是执行内存重分配的时间就会占去修改字符串所用的时间的一大部分，如果这种修改频繁发生的话，可能还对性能造成影响。
  为了避免C字符串的这种缺陷，SDS通过未使用空间free这个参数，将字符串的长度和底层buf的长度之间的关系解除了，在SDS中，buf数组的长度不一定就是字符串数量加一，数组里面可以包含未使用的字节，这些字节的数量就由SDS的free属性记录。
  通过未使用空间，SDS实现了空间预分配和惰性空间释放两种优化策略。

* 空间预分配
  空间预分配用于优化SDS的字符串增长操作：当SDS的API对一个SDS进行修改，并且需要对SDS进行空间扩展时，程序不仅会为SDS分配修改所必须要的空间，还会为SDS分配额外的未使用空间。
  
  * 如果对SDS修改后，len的长度小于1MB，那么程序分配和len属性相同大小的未使用空间free。那么数组的大小就为：2 * len + 1byte
  * 如果对SDS修改后，len的长度大于等于1MB，那么程序会分配1MB的未使用空间。那么数组的大小就为：1MB + len + 1byte

* 惰性空间释放
  惰性空间释放用于优化SDS的字符串缩短操作：当SDS的API需要缩短SDS保存的字符串时，程序不会立即使用内存重分配来回收缩短后多出来的字节，而是使用free属性将这些字节数量记录起来，并等待将来使用。SDS也提供了相应的API，可以在需要的时候真正释放SDS的未使用空间。

4. **二进制安全**

C 语言中的字符串是以 ”\0“ 作为字符串的结束标记。而 SDS 是使用 len 的长度来标记字符串的结束。所以SDS 可以存储字符串之外的任意二进制流。因为有可能有的二进制流在流中就包含了”\0“造成字符串提前结束。也就是说 SDS 不依赖 “\0” 作为结束的依据。

5. 兼容部分C字符串函数

SDS 按照惯例使用 ”\0“ 作为结尾的管理。部分普通C 语言的字符串 API 也可以使用。

### 链表

C语言中并没有链表这个数据结构所以 Redis 自己实现了一个。Redis的链表是双端链表，被广泛用于实现Redis的各种功能，比如列表键，发布与订阅，慢查询，监视器等。

每个链表节点使用一个adlist.h/listNode结构来表示：

```c
typedef struct listNode {

    // 前置节点
    struct listNode *prev;

    // 后置节点
    struct listNode *next;

    // 节点的值
    void *value;
}
```

多个listNode可以通过prev和next指针组成双端链表：

![listNode.png](https://wx2.sbimg.cn/2020/06/07/listNode.png)

每个链表使用一个adlist.h/list结构来表示：

```c
typedef struct list {

    // 表头节点
    listNode *head;

    // 表尾节点
    listNode *tail;

    // 节点值复制函数
    void *(*dup)(void *ptr);

    // 节点值释放函数
    void (*free)(void *ptr);

    // 节点值对比函数
    int (*match)(void *ptr, void *key);

    // 链表所包含的节点数量
    unsigned long len;

} list;
```

list结构为链表提供来表头指针head、表尾指针tail，以及链表长度计数器len；而dup、free、match成员则是用于实现多态链表所需的类型特定函数：

* dup函数用于复制链表节点所保存的值；
* free函数用于释放链表节点所保存的值；
* match函数用于比较链表节点所保存的值，和另一个输入的值是否相等。

list和listNode组成的链表结构：

![listlistNode.png](https://wx1.sbimg.cn/2020/06/07/listlistNode.png)

**Redis的链表特性：**

* 双端：链表节点带有prev和next的指针，获取某个节点的上一节点或下一节点的复杂度都是O(1)；
* 无环：表头节点的prev指针和表尾节点的next指针都指向NULL，对链表的访问以NULL为终点。
* 带表头指针和表尾指针：通过list结构的head和tail指针，获取表头节点和表尾节点的时间复杂度都是O(1)；
* 带链表长度计数器：程序使用list结构的len属性来对list持有的链表节点进行计数，获取链表中节点数量的复杂度为O(1)；
* 多态：链表节点使用void*指针来保存节点的值，并且可以通过list结构的dup、free、match三个属性为节点值设置类型特定函数，所以链表可以用于保存各种不同类型的值。

### quicklist

[quicklist参考](http://zhangtielei.com/posts/blog-redis-quicklist.html)

Redis 3.2之前的列表对象底层编码使用ziplist和linkedlist实现，3.2版本之后，重新引入了一个quicklist的数据结构，列表的底层都由quicklist实现。

quicklist也是一个双向链表，并且是一个ziplist的双向链表。quicklist的每个节点都是一个ziplist。

**quicklist为什么这样设计？**

* 双向链表便于在两端进行push和pop操作，但是它的内存开销比较大。首先它要在每个节点上要保存数据之外还需要额外保存两个指针；其次双向链表的各个节点是单独的内存块，地址不连续，节点多了容易产生内存碎片。
* ziplist由于是一整块连续内存，所以存储的效率很高。但是，它不利于修改操作，每次数据变动都会引发一次内存的realloc。特别是的当ziplist长度很长的时候，一次realloc可能会导致大量的数据拷贝，进一步降低性能。

因此，quicklist结合了双向链表和ziplist的优点，但是产生了一个新问题：到底一个quicklist节点包含多长的ziplist合适呢？比如，同样是存储12个数据项，既可以是一个quicklist包含3个节点，而每个节点的ziplist又包含4个数据项，也可以是一个quicklist包含6个节点，而每个节点的ziplist又包含2个数据项。

这又是一个需要找平衡点的难题。只从存储效率上分析一下：

* 每个quicklist节点上的ziplist越短，则内存碎片越多。内存碎片多了，有可能在内存中产生很多无法被利用的小碎片，从而降低存储效率。这种情况的极端是每个quicklist节点上的ziplist只包含一个数据项，这就蜕化成一个普通的双向链表了。

* 每个quicklist节点上的ziplist越长，则为ziplist分配大块连续内存空间的难度就越大。有可能出现内存里有很多小块的空闲空间（它们加起来很多），但却找不到一块足够大的空闲空间分配给ziplist的情况。这同样会降低存储效率。这种情况的极端是整个quicklist只有一个节点，所有的数据项都分配在这仅有的一个节点的ziplist里面。这其实蜕化成一个ziplist了。

可见，一个quicklist节点上的ziplist要保持一个合理的长度。那到底多长合理呢？这可能取决于具体应用场景。实际上，Redis提供了一个配置参数list-max-ziplist-size，就是为了让使用者可以来根据自己的情况进行调整。

`list-max-ziplist-size`: 默认值-2，当取正值的时候，表示按照数据项个数来限定每个quicklist节点上的ziplist长度。比如，当这个参数配置成5的时候，表示每个quicklist节点的ziplist最多包含5个数据项。当取负值的时候，表示按照占用字节数来限定每个quicklist节点上的ziplist长度。这时，它只能取-1到-5这五个值，每个值含义如下：-5: 每个quicklist节点上的ziplist大小不能超过64 Kb；-4: 每个quicklist节点上的ziplist大小不能超过32 Kb；-3: 每个quicklist节点上的ziplist大小不能超过16 Kb；-2: 每个quicklist节点上的ziplist大小不能超过8 Kb；-1: 每个quicklist节点上的ziplist大小不能超过4 Kb。

另外，list的设计目标是能够用来存储很长的数据列表的。当列表很长的时候，最容易被访问的很可能是两端的数据，中间的数据被访问的频率比较低（访问起来性能也很低）。如果应用场景符合这个特点，那么list还提供了一个选项，能够把中间的数据节点进行压缩，从而进一步节省内存空间。Redis对于quicklist内部节点的压缩算法，采用的LZF——一种无损压缩算法。Redis的配置参数list-compress-depth就是用来完成这个设置的。

`list-compress-depth`: 默认值0，表示一个quicklist两端不被压缩的节点个数。注：这里的节点个数是指quicklist双向链表的节点个数，而不是指ziplist里面的数据项个数。实际上，一个quicklist节点上的ziplist，如果被压缩，就是整体被压缩的。0: 是个特殊值，表示都不压缩。这是Redis的默认值；1: 表示quicklist两端各有1个节点不压缩，中间的节点压缩...以此类推

#### quicklistNode

```c
typedef struct quicklistNode {
    // 指向链表前一个节点的指针。
    struct quicklistNode *prev;
    // 指向链表后一个节点的指针。
    struct quicklistNode *next;
    // 数据指针。如果当前节点的数据没有压缩，那么它指向一个ziplist结构；否则，它指向一个quicklistLZF结构。
    unsigned char *zl;
    // 表示zl指向的ziplist的总大小（包括zlbytes, zltail, zllen, zlend和各个数据项）。
    // 需要注意的是：如果ziplist被压缩了，那么这个sz的值仍然是压缩前的ziplist大小。
    unsigned int sz;
    // 表示ziplist里面包含的数据项个数。
    unsigned int count : 16;
    // 编码方式，表示ziplist是否压缩了（以及用了哪个压缩算法）。
    // 目前只有两种取值：2表示被压缩了（而且用的是LZF压缩算法），1表示没有压缩
    unsigned int encoding : 2;
    // 是一个预留字段。本来设计是用来表明一个quicklist节点下面是直接存数据，还是使用ziplist存数据，或者用其它的结构来存数据（用作一个数据容器，所以叫container）。
    // 但是，在目前的实现中，这个值是一个固定的值2，表示使用ziplist作为数据容器。
    unsigned int container : 2;
    // 解压标记 当我们使用类似lindex这样的命令查看了某一项本来压缩的数据时，需要把数据暂时解压，这时就设置recompress=1做一个标记，等有机会再把数据重新压缩。
    unsigned int recompress : 1;
    // 这个值只对Redis的自动化测试程序有用。不用管它。
    unsigned int attempted_compress : 1;
    // 扩展字段，暂时没用
    unsigned int extra : 10;
} quicklistNode;
```

```c
typedef struct quicklistLZF {
    // LZF压缩后占用的字节数
    unsigned int sz;
    // 柔性数组，指向数据部分
    char compressed[];
} quicklistLZF;
```

#### quicklist

```c
typedef struct quicklist {
    // 指向头节点（左侧第一个节点）的指针。
    quicklistNode *head;
    // 指向尾节点（右侧第一个节点）的指针。
    quicklistNode *tail;
    // 所有ziplist数据项的个数总和。
    unsigned long count;
    // quicklist节点的个数。
    unsigned int len;
    // ziplist大小设置，存放list-max-ziplist-size参数的值。
    int fill : 16;
    // 节点压缩深度设置，存放list-compress-depth参数的值。
    unsigned int compress : 16;
} quicklist;
```

![quicklist](http://zhangtielei.com/assets/photos_redis/redis_quicklist_structure.png)

#### 插入操作

* 当插入位置所在的ziplist大小没有超过限制时，直接插入到ziplist中就好了；
* 当插入位置所在的ziplist大小超过了限制，但插入的位置位于ziplist两端，并且相邻的quicklist链表节点的ziplist大小没有超过限制，那么就转而插入到相邻的那个quicklist链表节点的ziplist中；
* 当插入位置所在的ziplist大小超过了限制，但插入的位置位于ziplist两端，并且相邻的quicklist链表节点的ziplist大小也超过限制，这时需要新创建一个quicklist链表节点插入。
* 对于插入位置所在的ziplist大小超过了限制的其它情况（主要对应于在ziplist中间插入数据的情况），则需要把当前ziplist分裂为两个节点，然后再其中一个节点上插入数据。

### 字典

字典，又称为符号表（symbol table）、映射（map），是一种用于保存键值对的抽象数据结构。由于C语言中没有内置这种数据结构，所以Redis构建了自己的字典实现。

字典在Redis中的应用广泛，Redis的数据库就是使用字典作为底层实现的，字典也是哈希键的底层实现之一，当一个哈希键的键值对比较多，又或者键值对中的元素都是比较长的字符串时，Redis就会使用字典作为哈希键的底层实现。

Redis的字典使用哈希表作为底层实现，一个哈希表里面可以有多个哈希表节点，而每个哈希表节点就保存了字典中的一个键值对。

#### 哈希表节点

哈希表节点使用dictEntry结构表示，每个dictEntry结构都保存着一个键值对：

```c
typedef struct dictEntry {

    // 键
    void *key;

    // 值
    union {
        void *val;
        uint64_t u64;
        int64_t s64;
    } v;

    // 指向下个哈希表节点，形成链表
    struct dictEntry *next;

} dictEntry;
```

key保存着键值对中的键，v保存着键值对的值，v的值可以是一个指针，或者是一个uint64_t整数，又或者是一个int64_t整数。next属性指向另一个哈希表节点的指针，这个指针可以将多个哈希值相同的键值对连接在一起，以此解决键冲突问题。

#### 哈希表

哈希表使用dict.h/dictht结构定义：

```c
typedef struct dictht {

    // 哈希表数组
    dictEntry **table;

    // 哈希表大小
    unsigned long size;

    // 哈希表大小掩码，用于计算索引值
    // 总是等于 size - 1
    unsigned long sizemask;

    // 该哈希表已有节点的数量
    unsigned long used;

} dictht;
```

table属性是一个数组，数组中的每个元素都是一个指向dict.h/dictEntry结构的指针。size属性记录了哈希表的大小，即table数组的大小。used属性记录了哈希表目前的已有节点的数量。sizemask属性的值总是等于size - 1，这个属性和哈希值一起决定一个键应该被放到table数组的哪个索引上面。

哈希表示例：

![dictht.png](https://wx1.sbimg.cn/2020/06/08/dictht.png)

#### 字典

Redis中的字典由dict.h/dict结构表示：

```c
typedef struct dict {

    // 类型特定函数
    dictType *type;

    // 私有数据
    void *privdata;

    // 哈希表
    dictht ht[2];

    // rehash 索引
    // 当 rehash 不在进行时，值为 -1
    int rehashidx; /* rehashing not in progress if rehashidx == -1 */

    // 目前正在运行的安全迭代器的数量
    int iterators; /* number of iterators currently running */

} dict;
```

* type属性和privdata属性是针对不同类型的键值对，为创建多态字典而设置的：
  * type属性是一个指向dicyType结构的指针，每个dictType结构保存了一组用于操作特定类型键值对的函数，Redis会为用途不同的字典设置不同的类型特定函数。
  * privdate属性保存了需要传给那些类型特定函数的可选参数。

dictType结构：

```c
ypedef struct dictType {

    // 计算哈希值的函数
    unsigned int (*hashFunction)(const void *key);

    // 复制键的函数
    void *(*keyDup)(void *privdata, const void *key);

    // 复制值的函数
    void *(*valDup)(void *privdata, const void *obj);

    // 对比键的函数
    int (*keyCompare)(void *privdata, const void *key1, const void *key2);

    // 销毁键的函数
    void (*keyDestructor)(void *privdata, void *key);

    // 销毁值的函数
    void (*valDestructor)(void *privdata, void *obj);

} dictType;
```

* ht属性是一个包含两项的数组，数组中的每一项都是一个dictht哈希表，一般情况下，字典只使用ht[0]哈希表，ht[1]哈希表只会在对ht[0]哈希表进行rehash时使用。

* rehashidx属性记录了rehash目前的进度，如果当前没有进行rehash时，值为-1。

没有进行rehash的字典示例：

![84a820bb84e264866dece75c0c0a5aea.png](https://wx1.sbimg.cn/2020/06/08/84a820bb84e264866dece75c0c0a5aea.png)

#### 哈希算法

当要将一个新的键值对添加到字典里面时，程序需要先根据键值对的键计算出哈希值和索引值，然后再根据索引值，将包含新键值对的哈希表节点放到哈希表数组的指定索引上。

Redis计算哈希值和索引值方法如下：

```text
# 使用字典设置的哈希函数，计算key的哈希值
hash = dict -> type -> hashFunction(key);
# 使用哈希表的sizemask属性和哈希值，计算出索引值
# 根据情况不同，ht[x]可以是ht[0]或者ht[1]
index = hash & dict -> ht[x].sizemask
```

Redis使用MurmurHash2算法来计算键的哈希值，目前最新版为MurmurHash3。

#### 解决键冲突

Redis的哈希表使用链地址法来解决键冲突，每个哈希表节点都有一个next指针，多个哈希表节点可以用next指针构成一个单向表，被分配到同一个索引上的多个节点可以用这个单向链表连接起来，这样觉解决了键冲突问题。

由于dictEntry节点组成的链表没有指向链表表尾的指针，所以为了速度考虑，程序总是将新节点添加到链表的表头位置（复杂度为O(1)），排在其他已有节点的前面。

#### rehash

随着操作的进行，哈希表保存的键值对会逐渐减少或增加，为了让哈希表的负载因子（load factor）维持在一个合理的范围之内，当哈希表保存的键值对数量太多或者太少时，程序需要对哈希表的大小进行相应的扩展或收缩。

扩展或收缩哈希表工作通过rehash（重新排列）操作完成，rehash操作步骤如下：

* 1.为字典的ht[1]哈希表分配空间，这个哈希表的空间大小取决于要执行的操作，以及ht[0]当前包含的键值对数量（ht[0].used属性值）。
  * 如果执行的是扩展操作，那么ht[1]的大小为第一个大于等于ht[0].used* 2的$2^n$，如used为7，那么新的ht[1]大小为16。
  * 如果执行的收缩操作，那么ht[1]的大小等于第一个大于等于ht[0].used的$2^n$。
* 2.将保存在ht[0]上的键值对rehash到ht[1]上：rehash指的是重新计算键的哈希值和索引值，将键值对放到ht[1]哈希表的指定位置上。
* 3.将ht[0]上的键值对都rehash到ht[1]之后，释放ht[0]，将ht[1]设置为ht[0]，并在ht[1]新创建一个空白的哈希表，为下一次rehash做准备。

#### 渐进式rehash

如果哈希表的键值对很多，达到千万级，rehash时会需要大量的时间和计算量，可能会导致服务器在一段时间内停止服务。为了避免这个问题redis的rehash是分多次、渐进式地将ht[0]里面的键值对慢慢地rehash到ht[1]。

渐进式rehash的步骤：

* 为ht[1]分配空间，让字典同时持有ht[0]和ht[1]两个哈希表。
* 在字典中维持一个索引计数器变量rehashidx，并将它的值设置为0，表示rehash开始。
* 在rehash期间，对字典的添加、删除、查找或者更新操作时，程序除了执行指定的操作外，还会顺带将ht[0]哈希表在rehashidx索引上的所有键值对rehash到ht[1]，当rahash工作完成后，程序将rehashidx属性的值增一。
* 随着字典操作的不断执行，最终ht[0]上的所有键值对都会被rehash至ht[1]，这时程序将rehashidx属性的值设置为-1，表示rehash操作完成。

渐进式rehash的好处是将rehash键值对所需的计算工作均摊到对字典每个添加、删除、查找和更新操作上，避免了集中式rehash而带来的庞大计算量。

**渐进式rehash执行期间的哈希表操作：**

因为在渐进式rehash的过程中，字典会同时使用ht[0],ht[1]两个哈希表，所以在渐进式rehash执行期间，字典的删除、查找、更新等操作会在两个哈希表上进行。如果进行了 delete 和 update 等操作，会在两个哈希表上进行。如果是 find 的话优先在ht[0] 上进行，如果没有找到，再去 ht[1] 中查找。如果是 insert 的话那就只会在 ht[1]中插入数据。这样就会保证了 ht[1] 的数据只增不减，ht[0]的数据只减不增。

#### 哈希表单的扩展与收缩

当以下条件中的任意一个被满足时，程序会自动开始对哈希表执行扩展操作：

* 服务器目前没有执行`bgsave`命令或`bgrewriteaof`命令，并且哈希表的负载因子大于等于1。
* 服务器目前正在执行`bgsave`命令或`bgrewriteaof`命令，并且哈希表的负载因子大于对等于5。

当负载因子小于0.1时，程序自动开始对哈希表执行收缩操作。

负载因子计算公示：load_factor = ht[0].used / ht[0].size

### 跳跃表

跳跃表（skiplist）是一种有序数据结构，它通过在每个节点中维持多个指向其他节点的指针，从而达到快速访问节点的目的。

跳跃表支持平均$O(logN)$、最坏O(N)复杂度的节点查找，还可以通过顺序性操作来批量处理节点。在大部分情况下，跳跃表的效率可以和平衡树相媲美，而且跳跃表的实现比平衡树更简单。

Redis只在两个地方使用了跳跃表，一个是实现有序集合键，另一个是集群节点中用作内部数据结构。

Redis跳跃表由redis.h/zskiplistNode和redis.h/zskiplist两个结构定义，其中zskiplistNode结构用于表示跳跃表节点，而zskiplist结构则用于保存跳跃表节点的相关信息，比如节点数量，表头节点表尾节点指针等。

#### 跳跃表节点

跳跃表的节点实现由redis.h/zskiplistNode结构定义：

```c
typedef struct zskiplistNode {
  // 层
  struct zskiplistLevel {
    // 前进指针
    struct zskipListNode *forward;
    // 跨度
    unsigned int span;
  } level[];
  
  // 后退指针
  struct zskiplistNode *backward;

  // 分值
  double score;

  // 成员对象
  robj *obj;
} zskiplistNode;
```

* 层

跳跃表节点的level数组可以包含多个元素，每个元素都包含一个指向其他节点的指针，程序可以通过这些层来加快访问其他节点的速度，一般来说，层越多，访问其他节点的速度就越快。

每次创建一个新跳跃表节点时，程序都是根据幂次定律（power law，越大的数出现的概率越小）随机生成一个介于1和32之间的值作为level数组的大小，这个大小就是层的高度。

* 前进指针

每个层都有一个指向表尾方向的前进指针（level[i].forward属性），用于从表头向表尾方向访问节点。

* 跨度

层的跨度（level[i].span属性）用于记录两个节点之间的距离：
  
  * 两个节点之间的跨度越大，它们相距的就越远。
  * 指向null的所有前进指针的跨度为0，因为它们没有指向任何节点。

跨度的作用是用来计算排位（rank）的，在查找某个节点的过程中，将沿途访问过的所有层的跨度累计起来，得到的结果就是目标节点在跳跃表中的排位。

* 后退指针

跳跃表的后退指针（backward属性）用于从表尾向表头方向访问节点：和一次可以跳过多个节点的前进指针不同，因为每个节点只有一个后退指针，所以每次只能后退至前一个节点。

* 分值

节点的分值（score属性）是一个double类型的浮点数，跳跃表中的所有节点都是按照分值从小到大来排序的。

* 成员对象

节点的成员对象（obj属性）是一个指针，它指向一个字符串对象，而字符串对象则保存着一个SDS值。在同一个跳跃表中，各个节点保存的成员对象必须是唯一的，但是多个节点保存的分值却可以是相同的：分值相同的节点将按照成员对象在字典序中的大小来进行排序，成员对象较小的节点将排在前面。

#### 跳跃表

redis的跳跃表使用zskiplist结构表示：

```c
typedef struct zskiplist {
  
  // 表头节点和表尾节点
  structz skiplistNode *header, *tail;

  // 表中节点的数量
  unsigned long length;

  // 表中层数最大的节点的层数
  int level;
}
```

header和tail指针分别指向跳跃表和表头和表尾节点，通过这两个指针，程序定位表头节点和表尾节点的复杂度是O(1)。通过length属性来记录节点的数量，程序可以在复杂度O(1)内返回跳跃表的长度。level属性则用于在O(1)复杂度内获取跳跃表中层高最大的那个节点的层数量，表头节点的层高不计算在内。

跳跃表示例：

![zskiplist.png](https://wx2.sbimg.cn/2020/06/13/zskiplist.png)

### 整数集合

整数集合（intset）是集合键的底层实现之一，当一个集合只包含整数元素，并且这个集合的元素数量不多时，Redis就会使用整数集合作为该集合键的底层实现。

#### 整数集合的实现

整数集合可以保存类型为int16_t、int32_t或者int64_t的整数值，并且保证集合中不会出现重复的元素。

Redis使用intset.h/intset结构表示整数集合：

```c
typedef struct intset {

  // 编码方式
  uint32_t encoding;

  // 集合包含的元素数量
  uint32_t length;

  // 保存元素的数组
  int8_t contents[];

} intset;
```

* contents数组是整数集合的底层实现：整数集合的每个元素都是contents数组的一个数组项（item），各个项在数组中按照值的大小从小到大排列，并且不会出现重复。
* encoding表示的了当前contents的数据类型：
  * INTSET_ENC_INT16，contents就是一个int16_t类型的数组，数组里的每个项都是一个int16_t类型的整数值（-32768～32767）。
  * INTSET_ENC_INT32，contents就是一个int32_t类型的数组，数组里的每个项都是一个int32_t类型的整数值（-2147483648～2147483647）。
  * INTSET_ENC_INT16，contents就是一个int64_t类型的数组，数组里的每个项都是一个int64_t类型的整数值（-9223372036854775808～9223372036854775807）。

int64示例：

![intset_int64.png](https://wx1.sbimg.cn/2020/06/13/intset_int64.png)

contents数组的大小为sizeof(int64_t) * 4 = 64 * 4 = 256位。虽然contents数组保存的四个数值中只有第一个是真正需要用int64_t类型来保存的，而其他的1、3、5三个值可以用int16_t类型来保存，不过根据整数集合的升级规则，当一个底层为int16_t数组的整数集合添加一个int64_t类型的整数值时，整数集合已有的所有元素都会被转换成int64_t类型，所以contents数组保存的四个整数值都是int64_t类型的。

#### 升级

当添加一个新元素到整数集合里，并且新元素的类型比整数集合现有的元素类型都要长时，整数集合需要先进行升级（upgrade），然后才能将新元素添加到整数集合里。

升级整数集合并添加新元素的步骤：

* 1.根据新元素的类型，扩展整数集合底层数组的空间大小，并为新元素分配空间。
* 2.将底层数组现有的所有元素都转换成与新元素相同的类型，并将类型转换后的元素放置到正确位置，并且放置元素的过程中，需要继续维持底层数组的有序性质不变。
* 3.将新元素添加都底层数组里。

举例：
 将int32_t类型的元素添加到int16_t的整数集合中。

初始集合状态：

![upgrade_example1.png](https://wx1.sbimg.cn/2020/06/13/upgrade_example1.png)

集合数组元素位置示意图：

![upgrade_example2.png](https://wx2.sbimg.cn/2020/06/13/upgrade_example2.png)

现在要将类型为int32_t的整数值65535添加到整数集合里，因为65535的类型int32_t比整数集合当前所有元素的类型都长，所在在将65535添加到整数集合之前，需要将整数集合进行升级。升级首先要做的是根据新类型的长度，以及整数集合的元素数量（包括要添加的新元素在内），对集合底层数组进行空间重分配。

当前整数集合包括要添加的新元素有四个元素，元素类型需要扩展为int32_t，所以在重分配之后，底层数组的大小为：32 * 4 = 128位，如下图所示。虽然程序对底层数组进行了空间重分配，但数组原有的三个元素1、2、3仍然是int16_t类型，这些元素还保存在数组的前48位里面，所以程序接下来要做的就是将这是三个元素转换成int32_t类型，并将转换后的元素放置到正确的位上。

![upgrade_example3.png](https://wx1.sbimg.cn/2020/06/13/upgrade_example3.png)

首先，因为元素3在1、2、3、6553四个元素中排名第三，所以它将被移到contents数组的索引2上，也就是数组64位至95位的空间内，如下图：

![upgrade_example4.png](https://wx2.sbimg.cn/2020/06/13/upgrade_example4.png)

依次是元素2、元素1。

[![upgrade_example5.png](https://wx1.sbimg.cn/2020/06/13/upgrade_example5.png)](https://sbimg.cn/image/0QXYI)
[![upgrade_example6.png](https://wx1.sbimg.cn/2020/06/13/upgrade_example6.png)](https://sbimg.cn/image/0QMdR)

最后是新元素65535：

[![upgrade-example7.png](https://wx1.sbimg.cn/2020/06/13/upgrade-example7.png)](https://sbimg.cn/image/0Q5Xa)

最后，将整数集合encoding属性值改成INTSET_ENC_INT32，并将length属性的值从3改为4，修改完成后的集合如下图：

![upgrade_example8.png](https://wx2.sbimg.cn/2020/06/13/upgrade_example8.png)

因为每次向整数集合添加新元素都可能会引起升级，而每次都需要对底层数组中已有的元素进行类型转换，所以向整数集合添加新元素的时间复杂度为O(N)。

**升级之后新元素的数组位置：**

因为引发升级的新元素的长度总是比整数集合的所有元素的长度都长，所以这个新元素的值要么大于所有现有元素，要么就小于所有现有元素。小于现有的所有元素，新元素就会被放置在数组头部；大于现有所有元素，新元素就会被放置在数组尾部。

#### 升级的好处

整数集合的升级策略有两个好处，一个是提升整数集合的灵活性，另一个是尽可能地节约内存。

* 提升灵活性

因为C语言是静态类型语言，为了避免类型错误，通常不会将两种类型不同的值放到同一个数据结构里。但是，因为整数集合可以通过升级底层数组来适应新元素，所以可以随意地将int16_t、int32_t、int64_t类型的整数添加到集合中，不必担心出现类型错误，这样的做法很灵活。

* 节约内存

想让一个数组可以同时保存int16_t、int32_t、int64_t三种类型的整数，最简单的做法是直接使用int64_t类型的数组最为整数集合的底层实现，不过这样一来保存int16_t、int32_t也使用int64_t类型的空间去保存，出现内存浪费的情况。整数集合的升级操作可以避免这种情况。

#### 降级

整数集合不支持降级，一旦对数组进行了升级，编码会一直保持升级后的状态。

### 压缩列表

压缩列表（ziplist）是列表键和哈希键的底层实现之一。当一个列表键只包含少量列表项，并且每项要么是小整数值，要么是长度比较短的字符串，那么redis就会使用压缩列表来做列表键的底层实现。当一个哈希键只包含少量键值对，并且每个键值对的键和值要么是小整数值，要么是长度比较短的字符串，那么redis就会使用压缩列表来做哈希键的底层实现。

#### 压缩列表的构成

压缩列表是Redis为了节约内存开发的，是由一系列特殊编码的连续内存块组成的顺序型（sequential）数据结构。一个压缩列表可以包含任意多个节点（entry），每个节点可以保存一个字节数组或者一个整数值。

压缩列表示例：

![ziplist2e910032705a2afa.png](https://wx2.sbimg.cn/2020/06/13/ziplist2e910032705a2afa.png)

|属性|类型|长度|用途|
|--|--|--|--|
|zlbytes|uint32_t|4字节|记录整个压缩列表占用的内存字节数：在对压缩列表进行内存重分配，或者计算zlend的位置时使用|
|zltail|uint32_t|4字节|记录压缩列表表尾节点距离压缩列表起始地址有多少字节：通过这个偏移量，可以直接确定表尾节点的地址，不需要遍历整个压缩列表|
|zllen|uint16_t|2字节|记录了压缩列表包含的节点数量：当这个属性的值小于UINT16_MAX(65535)时，这个属性的值就是压缩列表包含的节点数量；当这个属性值等于UINT16_MAX时，节点的数量需要遍历整个压缩列表才能计算出 |
|entry|列表节点|不定|压缩列表包含的各个节点，节点的长度由节点保存的内容决定|
|zlend|uint8_1|1字节|特殊值0xff（十进制255），用于标记压缩列表的末端|

#### 压缩列表节点

每个压缩列表节点可以保存一个字节数组或者一个整数值，其中字节数组可以是以下的一种：

* 长度小于等于63($2^6 - 1$)字节的字节数组；
* 长度小于等于16383($2^14 - 1$)字节的字节数组；
* 长度小于等于4294967295($2^32 - 1$)字节的字节数组；

而整数值则可以是以下六中长度的其中一种：

* 4位长，介于0至12之间的无符号整数；
* 1字节长的有符号整数；
* 3字节长的有符号整数；
* int16_t类型整数；
* int32_t类型整数；
* int64_t类型整数；

每个压缩列表节点都由previous_entry_length、encoding、content三部分组成，如下图：

![ziplist-node.png](https://wx2.sbimg.cn/2020/06/13/ziplist-node.png)

* previous_entry_length属性

  以字节为单位，记录了压缩列表中前一个节点的长度，previous_entry_length属性的长度可以是1字节或者5字节：
  
  * 如果前一个节点的长度小于254字节，那么previous_entry_length属性的长度为1字节：前一个节点的长度就保存在这一个字节里面。
  * 如果前一个节点的长度大于等于254字节，那么previous_entry_length属性的长度为5字节：其中属性的第一字节会被设置为0xFE（十进制254），而之后的四个字节则用于保存前一节点的长度。

  下图所示：previous_entry_length属性值为0xFE00002766，表示的前一个节点的长度为00002766（十进制10086）
  ![ziplist-node-1.png](https://wx2.sbimg.cn/2020/06/13/ziplist-node-1.png)

  因为节点的previous_entry_node属性记录了前一个节点的长度，所以程序可以通过指针运算，根据当前节点的起始地址来计算出上一节点起始地址。压缩列表的从表尾向表头遍历操作就是使用这一原理实现的。

* encoding属性

  节点encoding属性记录了节点的content属性所保存的数据的类型以及长度：
  * 1字节、2字节或者5字节长，值的最高位为00、01或者10的字节数组编码：这种编码表示节点的content属性保存字节数组，数组的长度由编码除去最高两位之后的其它位记录。
  * 1字节长，值的最高位以11开头的是整数编码：这种编码表示节点的content属性保存着整数值，整数值的类型和长度由编码除去最高位之后的其它位记录。

  编码的可选值和对应关系：
  |编码|编码长度|content属性保存的值|
  |--|--|--|
  |00xxxxxx|1字节|长度小于等于63字节的字节数组|
  |01xxxxxx xxxxxxxx|2字节|长度小于等于 16383字节的字节数组|
  |10______ xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxx|5字节|长度小于等于4294967295的字节数组|
  |11000000| 1字节|int16_t类型的整数|
  |11010000| 1字节|int32_t类型的整数|
  |11100000| 1字节|int64_t类型的整数|
  |11110000| 1字节|24位有符号整数|
  |11111110| 1字节|8位有符号整数|
  |1111xxxx| 1字节|使用这一编码的节点没有相应的content属性，因为编码本身的xxxx四个位已经保存了一个介于0和12之间的值，所以它无须content属性|

* content属性

  节点的content属性负责保存节点的值，节点值可以是一个节点数组或者整数，值的类型和长度由节点的encoding属性决定。
  下图展示了一个保存字节数组的节点示例：
  
  [![ziplist-nodefa3326697a39f9ec.png](https://wx2.sbimg.cn/2020/06/13/ziplist-nodefa3326697a39f9ec.png)](https://sbimg.cn/image/0AUsV)
  
  * 编码的最高位00表示节点保存的是一个字节数组；
  * 编码的后六位001011记录了字节数组的长度11；
  * content属性保存着节点的值"hello world"。

  下图展示了一个保存整数值的节点示例：

  [![ziplist-nodeef9ef2646927e2f4.png](https://wx2.sbimg.cn/2020/06/13/ziplist-nodeef9ef2646927e2f4.png)](https://sbimg.cn/image/0AiU7)

  * 编码11000000表示节点保存的是一个int16_t类型的整数值；
  * content属性保存着节点的值10086

#### 连锁更新

每个节点的previous_entry-length属性都记录了前一个节点的长度，占用的字节会根据前一个节点的大小而决定。

如果在一个压缩列表中，有多个连续的、长度介于250字节到253字节之间的节点e1到eN，如果在e1前面添加一个新元素newe，并且新元素newe占用的字节数大于等于254。这个时候e1的previous_entry_length属性仅长1字节，没办法保存新节点的长度，所以程序将对压缩列表执行空间重分配操作，并将e1节点的previous_entry_length属性长度扩展为5字节长。但是，当e1的previous_entry_length属性扩展后，e1的长度就大于等于254字节了，又需要扩展e2的previous_entry_length属性。依此递推，程序需要不断地对压缩列表执行空间重分配操作，知道eN为止。

redis将这种特殊情况下产生的连续多次空间扩展操作称之为“连续更新”（cascade update）。除了添加新节点可能引发连锁更新之外，删除节点也可能会引发连锁更新。

因为连锁更新在最坏的情况下需要多列表执行N次空间重分配操作，而每次空间重分配最坏复杂度为O(N)，所以连锁更新的最坏复杂度为O($N^2$)。

**需要注意的是，尽管连锁更新复杂度较高，但它真正造成性能问题的几率很低：**

* 首先，压缩列表里要恰好有多个连续的，长度介于250字节至253字节之间的节点，连锁更新才有可能被引发，在实际中，这种情况并不多见。
* 其次，即使出现连锁更新，但只要被更新的节点数量不多，就不会对性能造成任何影响。

## redisObject简介

Redis底层使用redisObject对象来表示数据库中的键和值，每次在redis数据库中新创建一个键值对时，redis都会创建两个对象，一个对象用作键值对的键（键对象），另一个对象用作键值对的值（值对象）。

Redis的对象系统实现了基于引用计数技术的内存回收机制，当程序不在使用某个对象时，这个对象所占用的内存会自动释放。另外通过引用计数实现了对象共享机制，这一机制可以在适当条件下，通过让多个数据库键共享同一个对象来节约内存。Redis的对象带有访问时间记录信息，该信息可以记录数据库键的空转时长，在服务器启了maxmemory功能情况下，空转时间较长的键可能被优先删除。

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

### 编码和底层实现

对象的ptr指针指向对象的底层实现数据结构，而这些数据结构由对象的`encoding`属性决定。

`encoding`属性记录了对象的底层实现数据结构，属性值可以是下表中的一个

|编码常量|编码对应的底层数据结构| object encoding 命令输出|
|--|--|--|
|REDIS_ENCODING_INT|long类型的整数|"int"|
|REDIS_ENCODING_EMBSTR|embstr编码的简单动态字符串|"embstr"|
|REDIS_ENCODING_RAW|简单动态字符串|"raw"|
|REDIS_ENCODING_HT|字典|"hashtable"|
|REDIS_ENCODING_LINKEDLIST|双端链表|"linkedlist"|
|REDIS_ENCODING_ZIPLIST|压缩列表|"ziplist"|
|REDIS_ENCODING_INTSET|整数集合|"intset"|
|REDIS_ENCODING_SKIPLIST|跳跃表和字典|"skiplist"|

通过`encoding`属性指定对象使用的编码，而不是为特定类型的对象关联一种固定的编码，极大的提升了Redis的灵活性和效率，因为Redis可以根据不同的使用场景来为一个对象设置不同的编码，从而优化对象在某一场景下的效率。

### type（类型）

redisObject的type属性记录了对象的类型，属性值可以是下表中的一个

| 类型常量 | 对象的名称 | type命令输出|
|--|--|--|
|REDIS_STRING| 字符串对象 |"string"|
|REDIS_LIST|列表对象|"list|
|REDIS_HASH|哈希对象|"hash|
|REDIS_SET|集合对象|"set"|
|REDIS_ZSET|有序集合对象|"zset"|

`type`命令可以获取指定key的值的类型，而不是键的类型。

### 字符串对象

字符串对象的编码可以是int、raw或者embstr。

* 如果字符串对象保存的是整数值，并且这个整数值可以使用long类型表示，那么字符串对象会将整数值保存在字符串对象结构的ptr属性里面（将void*转换成long），并将字符串对象的编码设置为int。

* 如果字符串对象保存的是一个字符串值，并且这个字符串的字节数小于44，那么将使用embstr编码的方式来保存。

* 如果字符串对象保存的字符串值的字节数超过44，那么将使用SDS保存值，并且编码方式为raw。
* long double类型也是作为字符串保存。

![raw.png](https://wx2.sbimg.cn/2020/06/21/raw.png)

#### embstr编码

embstr编码是专门用于保存短字符串的一种优化编码方式，这种编码方式和raw一样，都使用redisObject结构和sdshdr结构来表示字符串对象，但raw编码会调用两次内存分配函数来分别创建redisObject和sdshdr，而embstr编码则通过调用一次内存分配函数来分配一块连续的空间，空间中依次包含redisObject和shshdr两个结构。如下图：

![embstr.png](https://wx1.sbimg.cn/2020/06/21/embstr.png)

embstr编码的字符串在对象执行命令时，产生的效果和raw编码一样，但是使用embstr编码的字符串对象来保存短字符串值有以下好处：

* embstr编码将创建字符串对象所需的内存分配次数从raw编码的两次降低为一次。
* 释放embstr编码的字符串对象只需要调用一次内存释放函数，而释放raw编码的需要调用两次。
* 因为embstr编码的字符串对象的所有数据都保存在一块连续的内存里面，所以这种编码的字符串对象比起raw编码的字符串对象能够更好的利用缓存带来的优势。

#### embstr与raw的区别

CPU和内存在之间存在一个缓存结构，用来协调CPU的高效读取和内存访问慢的矛盾。当CPU要访问内存之前会先在缓存中查找（L1 Cache，L2 Cache，L3 Cache），如果没有就去内存查找，之后放到缓存中。这个缓存的单位一般是64字节，一次性缓存连续的64个字节，这个最小的单位称为`缓存行`。

embstr编码是将redisObject对象和SDS对象紧紧挨到一起，然后将整体放到一个缓存行中去，这样，在查询的时候就可以直接从缓存中获取到相关的数据，提高了查询的效率。

![embstr编码和raw编码的区别](https://img-blog.csdn.net/2018090821140821?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTA4NTM3MDE=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

#### 为什么是44个字节

缓存行一般的长度为64字节，如果想要把对象存到缓存行中，首先整体的长度不得超过64字节，每个请求头redisObject占用16字节，那么SDS对象只能占用48字节。在SDS对象中包含属性：len、free、buf，在3.2之前 len 和 free 的数据类型都是 unsigned int，那么buf可用的字节为40，buf中还存在一个'\0'，所以3.2之前embstr结构可以保存的字节数是39；在3.2之后，sdshdr 变成了 sdshdr8， sdshdr16 和 sdshdr32还有 sdshdr64。优化的地方就在于如果 buf 小，使用更小位数的数据类型来描述 len 和 free 减少他们占用的内存，同时增加了一个char flags。emstr使用了最小的 sdshdr8。 这个时候 sds header 就变成了(len(1b) + free(1b) + flags(1b)) 3个字节，比之前的实现少了5个字节。所以新版本的 emstr 的最大字节变成了 44。

![embstr编码](https://img-blog.csdn.net/20180908211542600?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTA4NTM3MDE=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

#### 编码转换

int编码的字符串对象和embstr编码的字符串对象在条件满足的情况下，会被转换成raw编码的字符串对象。

对于int编码的字符串对象来说，如果向对象执行了一些命令，使得这个对象保存的不再是一个整数值，而是一个字符串，那么字符串对象的编码将从int转为raw。如执行`append`命令。

因为Redis没有提供embstr编码的字符串对象的任何修改程序，所以embstr编码的字符串对象实际上是只读的。当对embstr编码的字符串对象执行任何修改命令时，程序会先将对象的编码从embstr转换成raw，然后在执行修改命令。因此，embstr编码的对象在执行修改命令之后，总会变成一个raw编码的字符串对象。（`incrbyfloat`命令不会进行转换，embstr可以执行）

### 列表对象

列表对象在3.2版本之前，列表底层的编码是ziplist或者linkedlist，但是在3.2版本之后重新引入了一个quicklist的数据结构，列表的底层都由quicklist实现。

ziplist编码的列表对象：

![ziplist.png](https://wx1.sbimg.cn/2020/06/24/ziplist.png)

linkedlist编码的列表对象：

![linkedlist.png](https://wx1.sbimg.cn/2020/06/24/linkedlist.png)

#### 编码转换

在3.2版本之前：

当列表对象可以同时满足以下两个条件时，列表对象使用ziplist编码：

* 列表对象保存的所有字符串元素长度都小于64字节；
* 列表对象保存的元素数量小于512个；不能满足这两个条件的列表对象需要使用linkedlist编码。

```text
上面的条件限制可以修改，配置项：`list-max-ziplist-entries`和`list-max-ziplist-value`
```

在3.2版本之后，都是由quicklist实现。

### 哈希对象

哈希对象的编码可以是ziplist或者hashtable。

ziplist编码的哈希对象使用压缩列表作为底层实现，每当有新的键值对要加入到哈希对象时，程序会先将保存了键的压缩列表节点推入到压缩列表表尾，然后再将保存了值的压缩列表节点推入到压缩列表的表尾，因此：

* 保存了同一键值对的两个节点总是紧挨在一起，保存键的节点在前，保存值的节点在后；
* 先添加到哈希对象中的键值对会被放到压缩列表的表头方向，而后来添加到哈希对象中的键值对会被放到压缩列表表尾方向。

hashtable编码的哈希对象使用字典作为底层实现，哈希对象中的每个键值对都使用一个字典键值对来保存：

* 字典的每个键都是一个字符串对象，对象中保存了键值对的键；
* 字典的每个值都是一个字符串对象，对象中保存了键值对的值。

#### 编码转换

当哈希对象可以同时满足以下两个条件时，哈希对象使用ziplist编码，不能满足以下两个条件的哈希对象需要使用hashtable编码。

* 哈希对象保存的所有键值对的键和值的字符串长度都要小于64字符；
* 哈希对象保存的键值对数量小于512个。

这两个上限的值由参数`hash-max-ziplist-value`和`hash-max-ziplist-entries`控制。

```text
hash-max-ziplist-entries 512
hash-max-ziplist-value 64
```

### 集合对象

集合对象的编码可以是insert或者hashtable。

inset编码的集合对象使用整数集合作为底层实现，集合对象包含的所有元素都被存在整数集合里面。hashtable编码的集合对象使用字典作为底层实现，字典的每个键都是一个字符串对象，每个字符串对象包含了一个集合元素，而字典的值则全部被设置为NULL。

inset编码的集合对象：

![2hKOR.png](https://wx2.sbimg.cn/2020/06/28/2hKOR.png)

hashtable编码的集合对象：

![2hHza.png](https://wx2.sbimg.cn/2020/06/28/2hHza.png)

#### 编码的转换

当集合对象同时满足以下两个条件时，对象使用inset编码，否则使用hashtable编码：

* 集合对象保存的所有元素都是整数值；
* 集合对象保存的元素数量不超过512个。

参数`set-max-intset-entries`控制上面条件的元素数量。

```text
set-max-intset-entries 512
```

### 有序集合对象

有序集合对象的编码可以是ziplist或者skiplist。

ziplist编码的有序集合对象使用压缩列表作为底层实现，每个集合元素使用两个紧挨在一起的压缩列表节点来保存，第一个节点保存的元素的成员（member），而第二个元素则保存元素的分值（score）。压缩列表内的集合元素按照从小到大进行排序，分值较小放置在靠近表头的位置。

skiplist编码的有序集合对象使用zset结构作为底层实现，一个zset结构同时包含一个跳跃表和一个字典。

```c
typedef struct zset {
  zskiplist *zsl;
  dict *dict;
} zset;
```

zset结构中的zsl跳跃表按分值从小到大保存了所有集合元素，每个跳跃表节点都保存了一个集合元素：跳跃表节点的object属性保存了元素的成员，score属性则保存了元素的分值。通过这个跳跃表可以实现对有序集合进行范围操作，比如zrank、zrange等命令就是基于跳跃表API来实现的。

zset结构中的dict字典为有序集合创建了一个从成员到分值的映射，字典中的每个键值对都保存了一个集合元素：字典的键保存元素的成员，字典的值保存元素的分值。通过这个字典，可以用O(1)时间复杂度查找给定成员的分值，zscore命令就是根据这一特性实现的，很多其他有序集合命令都在实现的内部用到这一特性。

有序集合的每个元素都是一个字符串对象，而每个元素的分值都是一个double类型的浮点数。zset使用字典和跳跃表来保存有序集合元素，但是这两种数据结构都会通过指针来共享相同元素的成员和分值，所以同时使用跳跃表和字典来保存有序集合元素不会产生任何重复成员或分值，也不会因此而浪费额外的内存。

#### 编码转换

当有序集合对象可以同时满足以下两个条件时，对象使用ziplist编码，否则使用skiplist编码：

* 有序集合保存的元素数量小于128个；
* 有序集合保存的所有元素成员的长度都小于64字节。

这两个上限的值由参数`zset-max-ziplist-entries`和`zset-max-ziplist-valu`控制。

```text
zset-max-ziplist-entries 128
zset-max-ziplist-value 64
```

### 内存回收

因为C语言不具备自动内存回收功能，所以redis在自己的对象系统中构建了一个引用计数（reference counting）实现的内存回收机制，通过这一机制，程序可以通过跟踪对象的引用计数信息，在适当的时候自动释放对象进行内存回收。

每个对象的引用计数信息由redisObject结构的`refcount`属性记录：

```c
typedef struct redisObject {
  // ...
  // 引用计数
  int refcount;
  // ...
} robj;
```

对象的引用计数信息会随着对象的使用状态不断变化：

* 在创建一个新对象时，引用计数的值会被初始化为1；
* 当对象被一个新程序使用时，它的引用计数的会增加1；
* 当一个对象不被使用时，它的引用计数会减1；
* 当对象的引用计数值变为0时，对象所占用的内存会被释放。

### 对象共享

除了用于实现引用计数内存回收机制之外， 对象的引用计数属性还带有对象共享的作用。目前来说， Redis 会在初始化服务器时， 创建一万个字符串对象， 这些对象包含了从 0 到 9999 的所有整数值， 当服务器需要用到值为 0到 9999 的字符串对象时， 服务器就会使用这些共享对象， 而不是新创建对象。

命令`obejct refcount keyname`可以显示出给定键的值的引用次数。

**`obejct refcount`并不准确或者已经弃用，没有查到相关资料**

**为什么 Redis 不共享包含字符串的对象？**

当服务器考虑将一个共享对象设置为键的值对象时， 程序需要先检查给定的共享对象和键想创建的目标对象是否完全相同， 只有在共享对象和目标对象完全相同的情况下， 程序才会将共享对象用作键的值对象， 而一个共享对象保存的值越复杂， 验证共享对象和目标对象是否相同所需的复杂度就会越高， 消耗的 CPU 时间也会越多：

* 如果共享对象是保存整数值的字符串对象， 那么验证操作的复杂度为 O(1) ；
* 如果共享对象是保存字符串值的字符串对象， 那么验证操作的复杂度为 O(N) ；
* 如果共享对象是包含了多个值（或者对象的）对象， 比如列表对象或者哈希对象， 那么验证操作的复杂度将会是 O(N^2) 。

因此， 尽管共享更复杂的对象可以节约更多的内存， 但受到 CPU 时间的限制， Redis 只对包含整数值的字符串对象进行共享。

### 对象的空转时长

redisObject结构的`lru`属性记录了该对象最后一次被命令程序访问的时间:

```c
typedef struct redisObject {
    // ...
    unsigned lru:22;
    // ...
} robj;
```

命令`obejct idletime keyname`可以显示出给定键的空转时长，这个空转时长就是通过将当前时间减去键的值对象的`lru`时间计算得出的。

*注意：命令`obejct idletime keyname`实现是特殊的， 这个命令在访问键的值对象时， 不会修改值对象的 lru 属性。*

属性`lru`的另外一个用处是，如果服务器打开了`maxmemory`选项， 并且服务器用于回收内存的算法为`volatile-lru`或者`allkeys-lru`， 那么当服务器占用的内存数超过了`maxmemory`选项所设置的上限值时， 空转时长较高的那部分键会优先被服务器释放， 从而回收内存。

## 参考

* [quicklist](http://zhangtielei.com/posts/blog-redis-quicklist.html)
* [Redis 为什么用跳表而不用平衡树](https://juejin.im/post/6844903446475177998#heading-2)