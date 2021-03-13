[toc]

# java容器
> [代码位置](https://gitee.com/maanshan/demos/tree/java%E5%AE%B9%E5%99%A8%E5%AD%A6%E4%B9%A0)
# 一、概述
    容器主要包括Collection和Map两种，Collection存储着对象的集合，Map存储着键值对的映射表。

## Collection
<div align="center"> <img src="https://note.youdao.com/yws/api/personal/file/6429F6656BCE429EAF9DA49952A76913?method=download&shareKey=fe0425cbffbefda73159d58efb55ae95"/> </div><br>

### 1. Set
* TreeSet： 基于红黑树实现，支持有序性操作，例如根据一个范围查找元素的操作。但是查找效率不如HashSet，HashSet查找的时间复杂度为O(1)，TreeSet则为O(logN)。**基于红黑树自动排序，不支持重复和null**。
* HashSet：基于哈希表实现，支持快速查找，但不支持有序性操作（排序），不保持插入顺序。**无序，支持null，不支持重复**。
* LinkedHashSet：具有HashSet的查找效率，且内部使用双向链表维护元素的插入顺序。**保持插入顺序，支持null，不支持重复**。

* **注意：** TreeSet、HashSet和LinkedHashSet底层都是使用Map实现存储（TreeMap、HashMap、LinkedHashMap），key为set存储的值，value为同一定值。TreeSet不支持null原因是比较2个值是否相等时会调用对象的hashCode方法。
* [时间复杂度](https://note.youdao.com/ynoteshare1/index.html?id=2b6eb6e19ca30c36129f0657c6842f47&type=note)
### 2. List
* ArrayList：基于动态数组实现，支持随机访问。**保持插入顺序，支持重复和null**
* Vector：和 ArrayList 类似，但它是线程安全的。通过在add、remove等方法上同步（添加synchronized）保证线程安全，效率低。**保持插入顺序，支持重复和null**
* LinkedList：基于双向链表实现，只能顺序访问，但是可以快速地在链表中间插入和删除元素。不仅如此，LinkedList 还可以用作栈、队列和双向队列。内部使用Node节点存储上一节点和下一节点元素实现。**保持插入顺序，支持重复和null**
### 3. Queue
* PriorityQueue： 基于堆结构实现，可以用它来实现优先队列。通过二叉小顶堆实现，可以用一棵完全二叉树表示。[参考](https://www.cnblogs.com/Elliott-Su-Faith-change-our-life/p/7472265.html)**自然排序，支持重复，不支持null**
* LinkedList：可以用它来实现双向队列。
## Map
<div align="center"> <img src="https://note.youdao.com/yws/api/personal/file/973FE17BE75D4DEA83C551F41664A3B9?method=download&shareKey=7c28955a10d2e0f02cb6b8ffa7a16a12"/> </div><br>

* TreeMap：基于红黑树实现。**key不可以为null，基于红黑树默认排序**
* HashMap：基于哈希表实现。**key可以为null，不保证插入顺序，默认排序**
* HashTable：和HashMap类似，但它是线程安全的。现在可以使用 ConcurrentHashMap 来支持线程安全，并且 ConcurrentHashMap 的效率会更高，因为 ConcurrentHashMap 引入了分段锁。**key和value都不可以为null，不保证插入顺序**
* LinkedHashMap：使用双向链表维护元素的顺序，顺序为插入顺序或者最少使用顺序（LRU）顺序。。**key可以为null**
* ConcurrentHashMap分段锁实现线程安全，相比HashTable更高效。**key和value都不可以为null**
# 二、容器中的设计模式
## 迭代器模式
Collection 继承了 Iterable 接口，其中的 iterator() 方法能够产生一个 Iterator 对象，通过这个对象就可以迭代遍历 Collection 中的元素。
## 适配器模式
java.util.Arrays#asList() 可以把数组类型转换为 List 类型。
```java
public static <T> List<T> asList(T... a)
```
asList() 的参数为泛型的变长参数，不能使用基本类型数组作为参数，只能使用相应的包装类型数组。
# 三、源码分析
**如果没有特别说明，以下源码分析基于 JDK 1.8。**
## ArrayList
### 1.概述
实现了 RandomAccess 接口，因此支持随机访问。这是理所当然的，因为 ArrayList是基于数组实现的。
数组的默认大小为 10。
### 2.扩容
添加元素时会先调用` ensureCapacityInternal(size + 1);`来保证容量足够，如果不够时，需要使用 grow() 方法进行扩容，新容量的大小为 oldCapacity + (oldCapacity >> 1)，也就是旧容量的 1.5 倍。  

扩容时会`Arrays.copyOf()` 把原数组整个复制到新数组中，这个操作代价很高，因此最好在创建 ArrayList 对象时就指定大概的容量大小，减少扩容操作的次数。
```java
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }
```
### 3.删除元素
`remove(int index);remove(Object o);`都会调用`System.arraycopy(elementData, index+1, elementData, index,numMoved);`该操作的时间复杂度为 O(N)，可以看出 ArrayList 删除元素的代价是非常高的。
```java
public E remove(int index) {
    rangeCheck(index);
    modCount++;
    E oldValue = elementData(index);
    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index, numMoved);
    elementData[--size] = null; // clear to let GC do its work
    return oldValue;
}
```
### 4.Fail-Fast（快速失效）
modCount 用来记录 ArrayList 结构发生变化的次数。结构发生变化是指添加或者删除至少一个元素的所有操作，或者是调整内部数组的大小，仅仅只是设置元素的值不算结构发生变化。

在进行序列化或者迭代等操作时，需要比较操作前后 modCount 是否改变，如果改变了就会抛出 ConcurrentModificationException。
```java
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException{
        // Write out element count, and any hidden stuff
        int expectedModCount = modCount;
        s.defaultWriteObject();

        // Write out size as capacity for behavioural compatibility with clone()
        s.writeInt(size);

        // Write out all elements in the proper order.
        for (int i=0; i<size; i++) {
            s.writeObject(elementData[i]);
        }

        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }
```
### 5.序列化
ArrayList 基于数组实现，并且具有动态扩容特性，保存元素的数组不一定都会被使用，所以序列化的时候会不序列化这部分。 保存元素的数组 elementData 使用 transient 修饰，该关键字声明数组默认不会被序列化。

ArrayList 实现了 writeObject() 和 readObject() 来控制只序列化数组中有元素填充那部分内容。

序列化时需要使用 ObjectOutputStream 的 writeObject() 将对象转换为字节流并输出。而 writeObject() 方法在传入的对象存在 writeObject() 的时候会去反射调用该对象的 writeObject() 来实现序列化。反序列化使用的是 ObjectInputStream 的 readObject() 方法，原理类似。
```java
File file = new File("d:\\ss.txt");
ObjectOutputStream objectOutputStream = 
            new ObjectOutputStream(new FileOutputStream(file));
objectOutputStream.writeObject(arrayList);
ObjectInputStream inputStream = 
            new ObjectInputStream(new FileInputStream(file));
ArrayList<String> arrayList1 = (ArrayList<String>) inputStream.readObject();
```

## Vector
与 ArrayList 类似，但是使用了 synchronized 进行同步。
### **1.与ArrayList比较**
* Vector 是同步的，因此开销就比 ArrayList 要大，访问速度更慢。最好使用 ArrayList 而不是 Vector，因为同步操作完全可以由程序员自己来控制；
* Vector 每次扩容请求其大小的 2 倍空间，而 ArrayList 是 1.5 倍。
### 2.替代方案
* 可以使用Collections.synchronizedList();得到线程安全的ArrayList。
```java
List<String> list = new ArrayList<>();
List<String> synList = Collections.synchronizedList(list);
```
* 也可以使用concurrent并发包下的CopyOnWriteArrayList类
```java
List<String> list = new CopyOnWriteArrayList<>();
```
## CopyOnWriteArrayList
### **读写分离**
写操作在一个复制的数组上进行，读操作还是在原始数组中进行，读写分离，互不影响。

写操作需要加锁，防止并发写入时导致写入数据丢失。

写操作结束之后需要把原始数组指向新的复制数组。
```java
public boolean add(E e) {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        Object[] elements = getArray();
        int len = elements.length;
        Object[] newElements = Arrays.copyOf(elements, len + 1);
        newElements[len] = e;
        setArray(newElements);
        return true;
    } finally {
        lock.unlock();
    }
}

final void setArray(Object[] a) {
    array = a;
}
```
### **适用场景**
CopyOnWriteArrayList 在写操作的同时允许读操作，大大提高了读操作的性能，因此很适合读多写少的应用场景。

但是 CopyOnWriteArrayList 有其缺陷：
* **内存占用**：在写操作时需要复制一个新的数组，使得内存占用为原来的两倍左右；
* **数据不一致**：读操作不能读取实时性的数据，因为部分写操作的数据还未同步到读数组中。
所以 CopyOnWriteArrayList 不适合内存敏感以及对实时性要求很高的场景。
## LinkedList
### 1.概述
基于双向列表实现，使用Node存储链表节点信息。每个Node节点都存储上一节点和下一节点信息。每个list都存储first和last指针
```java
transient Node<E> first;
transient Node<E> last;
// node
private static class Node<E> {
    E item;
    Node<E> next;
    Node<E> prev;
    // ....
}
```
### 2.与ArrayList比较
* ArrayList基于动态数组实现，LinkedList基于双向链表实现。
* ArrayList支持随机访问，LinkedList不支持。
* LinkedList任意位置添加删除元素快。

## HashMap
### 1.概述
HashMap内部存在一个`transient Node<K,V>[] table;`用来存储信息，Node存储着hash值，key，value以及下一个Node信息。数组`table`的每个位置被当成一个桶，一个桶存放一个链表，也就是数组+链表的方式。HashMap 使用拉链法来解决冲突，同一个链表中存放哈希值相同的Node。
**好的哈希函数会尽可能地保证 计算简单和散列地址分布均匀**
```java
static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        V value;
        Node<K,V> next;

        Node(int hash, K key, V value, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (Objects.equals(key, e.getKey()) &&
                    Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }
```
### 2.拉链法的工作原理
#### 2.1.插入元素：
hashMap会先检查key是否为null，因为null不能计算hash值，所以指定null的索引为0。
```java
HashMap<String, String> map = new HashMap<>();
map.put("K1", "V1");
map.put("K2", "V2");
map.put("K3", "V3");
```
* 新建一个 HashMap，默认大小为 16；
* 插入 <K1,V1> 键值对，先计算 K1 的 hashCode 为 115，使用除留余数法得到所在的桶下标 115%16=3。
* 插入 <K2,V2> 键值对，先计算 K2 的 hashCode 为 118，使用除留余数法得到所在的桶下标 118%16=6。
* 插入 <K3,V3> 键值对，先计算 K3 的 hashCode 为 118，使用除留余数法得到所在的桶下标 118%16=6，插在 <K2,V2> 前面。

 注意：链表的插入是以头插法方式进行的

#### 2.2.查找元素：

* 计算键值对所在的桶；
* 在链表上顺序查找，时间复杂度和链表的长度成正比。

### 3.put操作
HashMap 允许插入键为 null 的键值对。但是因为无法调用 null 的 hashCode() 方法，也就无法确定该键值对的桶下标，只能通过强制指定一个桶下标来存放。HashMap 使用第 0 个桶存放键为 null 的键值对。

### 4. 确定桶下标（复杂）

### 5.扩容-基本原理
设 HashMap 的 table 长度为 M，需要存储的键值对数量为 N，如果哈希函数满足均匀性的要求，那么每条链表的长度大约为 N/M，因此平均查找次数的复杂度为 O(N/M)。

为了让查找的成本降低，应该尽可能使得 N/M 尽可能小，因此需要保证 M 尽可能大，也就是说 table 要尽可能大。HashMap 采用动态扩容来根据当前的 N 值来调整 M 值，使得空间效率和时间效率都能得到保证。

和扩容相关的参数主要有：capacity、size、threshold 和 load_factor。

|参数|含义|
|:--:|:--|
| capacity | table 的容量大小，默认为 16。需要注意的是 capacity 必须保证为 2 的 n 次方。|
| size | 键值对数量。 |
| threshold | size 的临界值，当 size 大于等于 threshold 就必须进行扩容操作。 |
| loadFactor | 装载因子，table 能够使用的比例，threshold = capacity * loadFactor。|

**复杂：**[参考](https://github.com/CyC2018/CS-Notes/blob/master/docs/notes/Java%20%E5%AE%B9%E5%99%A8.md)

### 6.扩容-重新计算桶下标

### 7.计算数组容量

### 8.链表转红黑树
从jdk8开始,一个桶存储的链表大于8时，会将链表转换为红黑树。

### 9.与HashTable对比
* HashTable 使用 synchronized 来进行同步。
* HashMap 可以插入键为 null 的 Entry。
* HashMap 的迭代器是 fail-fast 迭代器。
* HashMap 不能保证随着时间的推移 Map 中的元素次序是不变的。

## ConcurrentHashMap
基于jdk1.7分析
### 1.存储结构
ConcurrentHashMap 和 HashMap 实现上类似，最主要的差别是 ConcurrentHashMap 采用了分段锁（Segment），每个分段锁维护着几个桶（HashEntry），多个线程可以同时访问不同分段锁上的桶，从而使其并发度更高（并发度就是 Segment 的个数）。默认的并发数为16。
### 2. size操作
每个Segment都维护了一个count变量，用来统计该Segment的键值对个数。
在执行 size 操作时，需要遍历所有 Segment 然后把 count 累计起来。

ConcurrentHashMap 在执行 size 操作时先尝试不加锁，如果连续两次不加锁操作得到的结果一致，那么可以认为这个结果是正确的。

尝试次数使用 RETRIES_BEFORE_LOCK 定义，该值为 2，retries 初始值为 -1，因此尝试次数为 3。

如果尝试的次数超过 3 次，就需要对每个 Segment 加锁。
### 3.Jdk1.8的改动
JDK 1.7 使用分段锁机制来实现并发更新操作，核心类为 Segment，它继承自重入锁 ReentrantLock，并发度与 Segment 数量相等。

JDK 1.8 使用了 CAS 操作来支持更高的并发度，在 CAS 操作失败时使用内置锁 synchronized。

并且 JDK 1.8 的实现也在链表过长时会转换为红黑树。
## LinkedHashMap
### 1.存储结构
继承HashMap，具有和HashMap一样的快速查找特性。
内部维护了一个双向链表，用来维护插入顺序或者 LRU 顺序。
```java
/**
 * The head (eldest) of the doubly linked list.
 */
transient LinkedHashMap.Entry<K,V> head;
/**
 * The tail (youngest) of the doubly linked list.
 */
transient LinkedHashMap.Entry<K,V> tail;
```
accessOrder 决定了顺序，默认为 false，此时维护的是插入顺序。
```java
final boolean accessOrder;
```
LinkedHashMap 最重要的是以下用于维护顺序的函数，它们会在 put、get 等方法中调用。
```java
void afterNodeAccess(Node<K,V> p) { }
void afterNodeInsertion(boolean evict) { }
```
### 2. afterNodeAccess方法
在put（put已经存在的key）或get方法后，如果accessOrder 为true调用，用于将访问的节点移到链表尾部。以此保证链表尾部为最近访问的节点，链表头部为最近最久未使用的节点。
```java
 void afterNodeAccess(Node<K,V> e) { // move node to last
     LinkedHashMap.Entry<K,V> last;
     if (accessOrder && (last = tail) != e) {
         LinkedHashMap.Entry<K,V> p =
             (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
         p.after = null;
         if (b == null)
             head = a;
         else
             b.after = a;
         if (a != null)
             a.before = b;
         else
             last = b;
         if (last == null)
             head = p;
         else {
             p.before = last;
             last.after = p;
         }
         tail = p;
         ++modCount;
     }
 }
```
### 3.afterNodeInsertion()
在 put,merge等操作之后执行，当 removeEldestEntry() 方法返回 true 时会移除最晚的节点，也就是链表首部节点 first。
```java
void afterNodeInsertion(boolean evict) { // possibly remove eldest
    LinkedHashMap.Entry<K,V> first;
    if (evict && (first = head) != null && removeEldestEntr(first)) {
        K key = first.key;
        removeNode(hash(key), key, null, false, true);
    }
}
```
removeEldestEntr(first)方法默认返回false，需要通过继承LinkedHashMap重写这个方法，才能实现正常删除最久数据。
### 4. LRU 缓存
使用LinkedHashMap实现的一个LRU缓存:
* 设定最大缓存空间 MAX_ENTRIES 为 3；
* 使用 LinkedHashMap 的构造函数将 accessOrder 设置为 true，开启 LRU 顺序；
* 覆盖 removeEldestEntry() 方法实现，在节点多于 MAX_ENTRIES 就会将最近最久未使用的数据移除。
```java
static class LRUCache<K,V> extends LinkedHashMap<K,V>{
    private static final int MAX_ENTRIES = 3;
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V>eldest) {
        if(this.size() > MAX_ENTRIES){
            return true;
        }
        return false;
    }
    LRUCache(){
        super(MAX_ENTRIES, 0.75f, true);
    }
}

public static void main(String[] args) {
    LRUCache<String,String> lruCache = new LRUCache<>();
    lruCache.put("a","a");
    lruCache.put("b","b");
    lruCache.put("c","c");
    lruCache.put("g","g");
    lruCache.put("d","d");
    System.out.println(lruCache.keySet());
}
```
## WeakHashMap
### 1.概述
WeakHashMap 的 Entry 继承自 WeakReference，被 WeakReference 关联的对象在下一次垃圾回收时会被回收。

WeakHashMap 主要用来实现缓存，通过使用 WeakHashMap 来引用缓存对象，由 JVM 对这部分缓存进行回收。
```java
 private static class Entry<K,V> extends WeakReference<Object> implements Map.Entry<K,V>{...}
```
```java
private static void weakHashMap(){
    Entity test3 = new Entity(1003, "test3");
    WeakHashMap<Entity,String> weakHashMap = new WeakHashMap<();
    weakHashMap.put(new Entity(1001, "test1"),"test1");
    weakHashMap.put(new Entity(1002, "test2"),"test2");
    weakHashMap.put(test3,"test3");
    System.gc();
    weakHashMap.forEach((key,value) -> 
        System.out.println("weakHashMap "+key+" : "+value));
    //输出 weakHashMap Entity{id=1003, name='test3'} : test3 
}
```
**注意：** 如果希望在系统中通过WeakHashMap自动清理数据，尽量不要在系统的其他地方强引用WeakHashMap的key,否则，这些key就不会被回收，WeakHashMap也就无法正常释放他们所占用的表项。
## ConcurrentCache
Tomcat 中的 ConcurrentCache 使用了 WeakHashMap 来实现缓存功能。

ConcurrentCache 采取的是分代缓存：

* 经常使用的对象放入 eden 中，eden 使用 ConcurrentHashMap 实现，不用担心会被回收（伊甸园）；
* 不常用的对象放入 longterm，longterm 使用 WeakHashMap 实现，这些老对象会被垃圾收集器回收。
* 当调用 get() 方法时，会先从 eden 区获取，如果没有找到的话再到 longterm 获取，当从 longterm 获取到就把对象放入 eden 中，从而保证经常被访问的节点不容易被回收。
* 当调用 put() 方法时，如果 eden 的大小超过了 size，那么就将 eden 中的所有对象都放入 longterm 中，利用虚拟机回收掉一部分不经常使用的对象。

```java
public final class ConcurrentCache<K, V> {

    private final int size;

    private final Map<K, V> eden;

    private final Map<K, V> longterm;

    public ConcurrentCache(int size) {
        this.size = size;
        this.eden = new ConcurrentHashMap<>(size);
        this.longterm = new WeakHashMap<>(size);
    }

    public V get(K k) {
        V v = this.eden.get(k);
        if (v == null) {
            v = this.longterm.get(k);
            if (v != null)
                this.eden.put(k, v);
        }
        return v;
    }

    public void put(K k, V v) {
        if (this.eden.size() >= size) {
            this.longterm.putAll(this.eden);
            this.eden.clear();
        }
        this.eden.put(k, v);
    }
}
```
