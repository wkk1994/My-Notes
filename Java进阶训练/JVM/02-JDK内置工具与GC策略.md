# JDK内置工具与GC策略

## JDK内置的命令行工具

|工具|简介|
|--|--|
|javap|反编译class文件的工具，`javap -verbose -c className`|
|jdeps|探测class或jar包需要的依赖，`jdeps -v HelloByteCode.class/jdeps -s HelloByteCode.class`|
|jar|打包工具，可以将文件和目录打包成.jar文件|
|keytool|安全证书和密钥的管理工具;（支持生成、导入、导出等操作）|
|jarsigner|JAR 文件签名和验证工具|
|jps|查看正在运行的java进程|
|jinfo|查看java进程的详细信息，比如VM参数，class_path|
|jstat|查看JVM内部GC信息|
|jmap|查看heap或类占用空间统计|
|jstack|查看线程信息|
|jcmd|执行jvm相关的分析命令（整合命令）|
|jrunscript/jjs|执行js命令|

### jps

查看当前正在运行的java进程。**jps貌似只会显示当前用户的java进程。**

常用指令：

* `jps`：查看正在运行的进程
* `jps -q`：只输出pid
* `jps -mlv`：-m参数输出传递给main 方法的参数；-l参数输出main class的完整包名；-v输出传递给JVM的参数。

### jinfo

查看java进程的配置信息包括VM配置、系统配置等信息。

常用命令：

* `jinfo pid`：查询全部信息
* `jinfo -flags pid`：查看jvm的参数
* `jinfo -sysprops pid`：查看java系统参数

### jstat

用于监控基于HotSport的JVM，对它的堆使用情况进行实时的命令行进行统计

常用命令：

* `jstat -class pid`：查看类加载情况
* `jstat -gc pid`：JVM中堆的使用情况和垃圾回收情况
* `jstat -gccapacity pid`：新生代、老年代和元数据的内存使用情况
* `jstat -gccause pid`：用于查看垃圾收集的统计情况，包括最近发生垃圾的原因
* `jstat -gcnew pid`：新生代垃圾收集的情况
* `jstat -gcnewcapacity pid`：新生代的存储容量情况
* `jstat -gcold pid`：老生代及持久代发生GC的情况
* `jstat -gcoldcapacity pid`：老生代的存储容量情况

[信息指令参考](https://www.cnblogs.com/duanxz/archive/2012/11/03/2752166.html)

### jmap

查看JVM的堆内存信息和所有对象情况。

常用命令：

* `jmap -heap pid`：查看堆内存使用情况
* `jmap -histo[:live] pid`：打印每个class的实例数目,内存占用,类全名信息，加上live参数只统计存活的对象。
* `jmap -dump:[live,]format=b,file=<filename>  pid`：使用hprof二进制形式,输出jvm的heap内容到文件，指定live选项,那么只输出活的对象到文件。示例：`jmap -dump:format=b,file=/Users/xxx/Desktop/15718.hprof 15718`
* `jmap -finalizerinfo pid`：打印正等候回收的对象的信息。

**`jmap -dump`命令对运行中的系统影响较大，甚至卡顿谨慎使用。**

线上系统，内存特别大，jmap执行期间会对进程产生很大影响，甚至卡顿（电商不适合）；
1：设定了参数`-XX:+HeapDumpOnOutOfMemoryError`，OOM的时候会自动产生堆转储文件；
2：很多服务器备份（高可用），停掉这台服务器对其他服务器不影响；
3：在线定位(一般小点儿公司用不到)。

[信息指令参考](https://www.cnblogs.com/duanxz/p/4890475.html)

### jstack

jstack用于生成java虚拟机当前时刻的线程快照。主要作用是可以定位线程出现长时间停顿的原因，如线程死锁、死循环、请求外部资源导致的长时间等待等。线程出现停顿的时候通过jstack来查看各个线程的调用堆栈，就可以知道没有响应的线程到底在后台做什么事情，或者等待什么资源。

常用命令：

* `jstack -l pid`：长列表模式. 将线程相关的 locks 信息一起输出，比如持有的锁，等待的锁。
* `jstack -F pid`：强制执行thread dump，当使用上面的命令没有响应时可以使用这个命令强制执行，可能需要系统权限。
* `jstack -m pid`：混合模式(mixed mode),将 Java 帧和 native帧一起输出, 此选项可能需要系统权限。

说明：

* 不同的Java虚拟机的线程dump的创建方式和文件格式是不一样的，不同的JVM版本，dump信息也有差别。
* 在实际使用中，往往一次dump的信息，还不足以确认问题。建议产生三次dump信息，如果每次dump都指向同一个问题，才能确定问题的典型性。

[信息指令参考](https://www.cnblogs.com/duanxz/p/5487576.html)

### jcmd

jdk7之后新出的工具，包含了前面几个工具的功能。可以用来导出堆，查看Java进程，导出线程信息，执行GC等。

常用命令：

* `jcmd`：查看正在运行的java进程，等同于命令`jsp -lm`，一样有用户隔离问题。
* `jcmd pid VM.flags`：查看指定进程的JVM标志，等同于`jinfo -flags pid`。
* `jcmd pid VM.command_line`：查看指定进程的JVM参数。
* `jcmd pid VM.system_properties`：查看指定进程的系统参数，等同于`jinfo -sysprops pid`。
* `jcmd pid Thread.print`：输出进程的线程信息，等同于`jstack -l pid`。
* `jcmd pid GC.class_histogram`：查看进程每个class实例的数目，等同于`jmap -histo:live pid`。
* `jcmd pid GC.heap_info`：查看进程堆信息，等同于`jmap -heap pid`。

## JDK内置图形化工具

### jconsole

### jvisualvm

### VisualGC

### jmc

## 垃圾收集器与内存分配策略

垃圾收集需要考虑的问题：哪些内存需要回收？什么时候回收？如何回收？

### 如何判定一个对象已经没有使用

#### 引用计数法

引用计数算法的思路是，在一个对象中存在一个引用计数器，如果对象被引用就加一，当不被引用时就减一，当引用计数器的值为零时，就表明这个对象已经没有在使用，可以被回收了。

**引用计数算法的问题：**

引用计数法需要额外的空间存储计数器而且还要保证计数器加减的原子性，也无法解决对象直接相互引用的问题。

#### 可达性分析法

目前主流的JVM都是使用可达性分析算法来判定对象是否存活。它的思路是，通过一系列被称为"GC Roots"的根对象作为起始对象，从这些对象开始，根据引用关系向下查找，查找过程形成的链称为“引用链”(Reference Chain)，如果对象到GC Roots之间没有引用链（即GC Roots到这个对象不可达），则该对象已经没用了，可以被回收。

**哪些对象可以作为GC Roots**

* 当前虚拟机栈中引用的变量，如各个线程被调用的方法堆栈中使用到的参数、局部变量、临时变量等。
* 方法区中类静态变量引用的对象。
* 方法区中常量引用的对象，如字符串常量池里的引用。
* 在本地方法栈中JNI（即通常所说的Native方法）引用的对象。
* Java虚拟机内部的引用，如基本数据类型对应的Class对象，一些常驻的异常对象（比如 NullPointExcepiton、OutOfMemoryError）等，还有系统类加载器。
* 所有被同步锁（synchronized关键字）持有的对象。
* 反映Java虚拟机内部情况的JMXBean、JVMTI中注册的回调、本地代码缓存等。

除了这些固定的GC Roots外，根据JVM使用垃圾收集器以及当前收集的区域的不同，还有其他对象临时性加入，共同构成完整的GC Roots集合。

>Tips
>引用类型：强引用(Strongly Reference)、软引用(Soft Reference)、弱引用(Weak Reference)、虚引用(Phantom Reference)。
>**finalize()方法：** 在对象被判定为不可达后，如果对象没有覆盖finalize()方法或者finalize()方法已经被执行过了，那么虚拟机就不会执行对象的finalize()方法，否则会将对象放到F-Queue的队列中，以低优先级的线程去执行F-Queue的队列中的方法。

### 方法区的回收

方法区（HotSport中的元空间或永久代）的垃圾回收一般效果低，主要回收两部分内容：废弃的常量和不再使用的类型。

判定常量是否可以回收和上面对象的方式类似，但是判定一个类型不再使用了需要满足下面三个条件：

* 该类所有的实例都已经被回收，也就是Java堆中不存在该类及其任何派生子类的实例。
* 加载该类的类加载器已经被回收，这个条件除非是经过精心设计的可替换类加载器的场景，如 OSGi、JSP的重加载等，否则通常是很难达成的。
* 该类对应的java.lang.Class对象没有在任何地方被引用，无法在任何地方通过反射访问该类的方法。

HotSpot虚拟机提供了`-Xnoclassgc`参数控制是否进行类型的回收，加上该参数表明不会回收类型。

### 垃圾收集算法

#### 标记-清除算法(mark-sweep)

标记清除算法分为两个阶段：首先需要标记所有需要回收的对象，在标记完成后，统一回收掉所有被标记的对象，也可以反过来，标记存活对象，清除未被标记的对象。

**标记清除算法的不足：**

* 执行效率不高，如果内存中有大部分对象需要清除，效率会变低，即执行效率随着清除对象的数量的增长而降低。
* 内存空间碎片化，标记、清除之后会产生大量不连续的内存碎片。

#### 标记-复制算法(mark-copy)

将内存按照容量分为大小相等的两部分，每次只使用其中的一个，当这块内存用完了，就将还存活的对象，复制到另一块内存中，把之前的内存中的对象清除。

**标记-复制算法不足：**

* 标记复制算法虽然实现简单，运行高效，也不会产生内存碎片，但是需要将可用内存缩小为原来的一半，浪费了部分内存空间。
* 效率随着存活对象的增长而降低，如果内存中存在大部分存活的对象，复制对象的时长会增加，效率降低。

**Appel式回收**

因为在新生代中有大部分对象都熬不过第一轮回收，所以使用半区回收的方式太浪费内存了。Appel式回收的做法是将新生代分为一块较大的Eden空间和两块较小的Survivor空间，每次分配内存只使用Eden和其中一块Survivor。发生垃圾回收时，将Eden和一块Survivor中仍然存活的对象复制到另一块Survivor空间上，然后直接清除掉Eden和使用过的Survivor空间。HotSpot虚拟机默认Eden和Survivor的大小比例是8∶1。

#### 标记-清除-压缩算法(Mark-Sweep-Compact)

标记-清除-压缩算法，其中的标记过程仍然与“标记-清除”算法一样，但后续步骤不是直接对可回收对象进行清理，而是让所有存活的对象都向内存空间一端移动，然后直接清理掉边界以外的内存。

标记清除整理算法，需要移动对象，尤其是在老年代这种每次回收后都有大量存活对象，移动存活对象并更新所有引用这些对象的地方将是一个极为负重的操作，必须要STW。

如果不移动对象就会产生内存碎片，只能通过更为复杂的内存分配器和内存访问器来解决。比如通过“分区空闲分配链表”来解决内存分配问题。**内存访问是用户程序最频繁的操作，如果在这个环节上添加额外的负担，势必会直接影响应用程序的吞吐量。**

另外还有一种解决方案的做法是，让虚拟机平时多数时间都使用标记-清除算法，暂时容忍内存碎片的存在，直到内存空间的碎片化程度已经大到影响对象分配时，再采用标记-整理算法收集一次，以获得规整的内存空间。CMS算法就是采用这种方式的。

> HotSpot虚拟机里面关注吞吐量的Parallel Scavenge收集器是基于标记-整理算法的，而关注延迟的CMS收集器则是基于标记-清除算法的。
> 最新的ZGC和Shenandoah收集器使用读屏障（Read Barrier）技术实现了整理过程与用户线程的并发执行。

### JVM内存分代模型

#### 分代假设

目前主流的JVM垃圾收集器，都是遵循了“分代收集”理论进行设计的。分代收集是基于两个分代假设之上：绝大多数对象都是朝生夕死的；熬过越多次垃圾收集过程的对象越难以消亡。

#### 跨代引用假设

假如每次只在新生代或者老年代进行垃圾收集，新生代可能引用老年代的对象，也能被老年代引用，因此进行垃圾收集时GC Roots的确定，又需要额外扫描新生代或者老年代，这样就会花费更多时间。为了解决这个问题，提出了第三条分代假设：

**跨代引用假设**：跨代引用相对于同代应用来说仅占极少数。

基于这个假设，就不需要在为了少量的跨代假设而扫描整个新生代或者老年代，只需要在建立一个全局的数据结构记录跨代引用，这个数据结构称为“记忆集”，Remembered Set。

### HotSpot的算法细节实现

#### 根节点枚举

在OopMap的协助下，HotSpot可以快速准确地完成GC Roots枚举。

#### 安全点（safe point）

[详细见《深入理解JAVA虚拟机》3.4.2](深入理解JAVA虚拟机)

**为什么存在安全点（Safe Point）？**

在执行GC操作时，有些步骤需要所有的工作线程必须停顿，这个停顿就是通过在代码中添加安全点实现的。

**安全点的位置**

安全点太多或太少都不好，安全点太多，GC过于频繁，增大运行时负荷；安全点太少，GC等待时间太长。一般安全点的位置在循环的末尾、方法返回前、调用方法之后、抛异常的位置。

**由安全点导致长时间停顿 见深入理解JAVA虚拟机 5.2.8**

#### 安全区域（Safe Region）

安全点是对正在执行的线程设定的，但是当有些线程处于Sleep或者Blocked状态时，这时候线程就无法响应虚拟机的中断请求，不能走到安全点。这个时候引入了安全区域的概念。

安全区域是指能够确保某一段代码片段中，引用关系不会发生变化，因此在这个区域中任意地方开始垃圾收集都是安全的。

线程在进入 Safe Region 的时候先标记自己已进入了 Safe Region，等到被唤醒时准备离开 Safe Region 时，先检查能否离开，如果 GC 完成了，那么线程可以离开，否则它必须等待直到收到安全离开的信号为止。

#### 记忆集与卡表

为解决对象跨代引用所带来的问题，垃圾收集器在新生代中建立了名为记忆集（Remembered Set）的数据结构，可以避免把整个老年代加进GC Roots扫描范围。

**记录集的记录精度：**

* 字长精度：每个精度精确到一个机器字长，该字节包含跨代指针。比较浪费内存。
* 对象精度：每个记录精确到一个对象，该对象里有字段含有跨代指针。
* 卡精度：每个记录精确到一块内存区域，该区域内有对象含有跨代指针。

卡表是记忆集中卡精度的具体实现方式：每个记录精确到一块内存区域，该区域内有对象含有跨代指针。

在Hotspot中默认的卡表实现使用一个字节数组CARD_TABLE实现，字节数组CARD_TABLE的每一个元素都对应内存空间中的一块特定大小的内存块，这个内存块称为“卡页”（Card Page）。一般来说，卡页都是以2的N次幂的字节数，Hotspot中卡页是2的9次幂，即512字节大小。

如果卡表标识内存区域的起始地址是0x0000的话，数组CARD_TABLE的第0、1、2号元素，分别对应了 地址范围为0x0000～0x01FF、0x0200～0x03FF、0x0400～0x05FF的卡页内存块。

一个卡页的内存中通常包含不止一个对象，只要卡页内有一个或多个对象的字段存在跨代引用，那就把对应的卡表数组的元素的值标识为1，表示这个元素变脏（Dirty），没有则表示为0。这样在垃圾回收时，只需要筛选出卡表中变脏的元素，就能轻易得出哪些卡页内存块中包含跨代指针，把它们加入GC Roots中一并扫描。

**G1和CMS都是使用卡表来处理跨代引用的，不过在Card Table的实现上不同。具体见下**

#### 写屏障

解决卡表元素如何维护的问题，例如它们何时变脏、谁来把它们变脏等。

> 除了写屏障的开销外，卡表在高并发场景下还面临着“伪共享”（False Sharing）问题。伪共享是处 理并发底层细节时一种经常需要考虑的问题，现代中央处理器的缓存系统中是以缓存行（Cache Line） 为单位存储的，当多线程修改互相独立的变量时，如果这些变量恰好共享同一个缓存行，就会彼此影 响（写回、无效化或者同步）而导致性能降低，这就是伪共享问题。

#### 并发的可达性分析

#### 三色标记算法

在CMS和G1中都有并发标记阶段，并发标记的难点在于标记对象的过程中，对象的引用关系又正在发生改变。Hotspot使用三色标记算法来解决这个问题。

三色标记算法将扫描的对象分为三种颜色，分别表示对象不同的状态：

* 黑色：根对象，或者对象和对象的子对象（field）都被扫描过。
* 灰色：对象本身被扫描过，但是还没有扫描对象中的子对象（filed）。
* 白色：不可达对象。

**漏标的情况：**

在三色标记算法会有一种情况发生漏标：在标记阶段，一个灰色对象A引用了白色对象B，如果又有一个黑色对象C引用指向这个白色对象B，灰色对象A对白色对象B的引用取消，这个时候黑色对象C已经是扫描过的对象了，不会再去扫描它的子对象引用，在这种情况下就会产生白色对象B被漏标的情况，会导致白色对象B被回收。

**漏标的解决方式：**

* 1.增量更新（Incremental Update）算法：关注引用的增加，只要在写屏障中发现一个白色对象引用被赋值到一个黑色对象的字段里，就将这个黑色对象变为灰色，下次重新扫描属性。CMS使用该方式。
* 2.原始快照（Snapshot At The Beginning, SATB）算法：关注引用的减少，当灰色对象删除执行白色对象的引用关系时，会将这个要删除的引用记录下来，在并发扫描结束之后，再将这些记录过得引用关系重新扫描一次。G1、Shenandoah则是用原始快照来实现。

### 常见的垃圾收集器
`
#### Serial收集器

Serial收集器有老年代和年轻代版本。年轻代版本：Serial收集器，老年代版本：Serial Old收集器。

对年轻代使用mark-copy（标记-复制）算法，对老年代使用mark-sweep-compact（标记-清除-整理）算法。只会使用单线程去执行垃圾收集工作，而且在进行垃圾收集时必须STW。

Serial收集器是HotSpot虚拟机在客户端模式下的默认新生代收集器，简单高效，是所有收集器里额外内存消耗最小的。

支持的参数：

* `-XX:+UseSerialGC`: 开启Serial收集器。
* `-XX:SurvivorRatio=8`: Eden和Survivor的比例，默认为8。
* `-XX:PretenureSizeThreshold=<字节大小>`: 表示在Eden中分配的对象超过这个大小，直接在老年代中分配，默认值为0表示不会在老年代中分配。

* `-XX:HandlePromotionFailure=true`: 是否允许担保失败，在JDK 6 Update 24之后，这个参数不会再影响到虚拟机的空间分配担保策略。

#### ParNew收集器(ParNewGC)

ParNew收集器，年轻代收集器，是Serial收集器的多线程并行版本。目前只有它可以和CMS收集器配合工作。

支持的参数：

* `-XX:+UseParNewGC`: 开启ParNew收集器。
* `-XX:ParallelGCThread=4`: 限制垃圾收集的线程数。

> **ParNew收集器是CMS收集器的默认新生代收集器。可以使用-XX：+/-UseParNewGC选项来强制指定或者禁用ParNew收集器。**

**并行和并发的区别**

* 并行（Parallel）：并行描述的是多条垃圾收集器线程之间的关系，说明同一时间有多条这样的线 程在协同工作，通常默认此时用户线程是处于等待状态。

* 并发（Concurrent）：并发描述的是垃圾收集器线程与用户线程之间的关系，说明同一时间垃圾 收集器线程与用户线程都在运行。由于用户线程并未被冻结，所以程序仍然能响应服务请求，但由于 垃圾收集器线程占用了一部分系统资源，此时应用程序的处理的吞吐量将受到一定影响。

#### Parallel收集器

有年轻代和老年代版本，老年代版本Parallel Old收集器，年轻代版本Parallel Scavenge收集器。

在年轻代使用标记-复制（mark-copy）算法，在老年代使用标记-清除-整理（mark-sweepcompact）算法。Parallel收集器的关注点是吞吐量，并且提供了两个参数用于精确控制吞吐量，分别是控制最大垃圾收集停顿时间的-XX:MaxGCPauseMillis参数以及直接设置吞吐量大小的-XX:GCTimeRatio参数。

相关参数：

* `-XX:+UseParallelGC` `-XX:+UseParallelOldGC` `-XX:+UseParallelGC -XX:+UseParallelOldGC`: 开启Parallel收集器。
* `-XX:MaxGCPauseMillis=500`: 控制最大垃圾收集停顿时间，JVM只能尽最大目标来实现它，不能保证实现，500表示ms。默认不限制。
* `-XX:GCTimeRatio=99`: 控制垃圾收集时间与应用程序时间之比为1/(1+99)，表示目标垃圾收集时间占总时间的1%。默认99。
* `-XX：+/-UseAdaptiveSizePolicy`: 当这个参数被激活后，不需要人工指定新生代的大小（-Xmn）、Eden与Survivor区的比例（-XX:SurvivorRatio）、晋升老年代对象大小（-XX:PretenureSizeThreshold）等细节参数 了，虚拟机会根据当前系统的运行情况收集性能监控信息，动态调整这些参数以提供最合适的停顿时 间或者最大的吞吐量。这种调节方式称为垃圾收集的自适应的调节策略（GC Ergonomics）。默认开启。
* `-XX:PretenureSizeThreshold=10000`：指定超过这个大小的对象直接在老年代分配，默认值为0表示都先在eden分配。
* `-XX:MaxTenuringThreshold`：升代年龄，最大值15。默认值15.
* `-XX:TargetSurvivorRatio=n`：动态年龄计算时需要的比率。默认50。
* **`-XX:+ParallelGCThreads`**：并行收集器的线程数，不指定时系统默认计算。
  对于Parallel GC，G1 GC，CMS GC的默认的计算公式：
  如果CPU <= 8，ParallelGCThreads = cpus数；
  如果CPU > 8，ParallelGCThreads = 8 + (cpu - 8 ) * 5/8或ParallelGCThreads = 8 + (cpu - 8 ) * 5/16，这个由平台决定，一般是5/8。

#### CMS收集器(CMS GC)（Concurrent Mark Sweep）

CMS收集器是基于标记-清除算法实现的，作用于老年代收集器，是一种以获取最短停顿时间为目标的收集器。

CMS收集器的设计目标是避免在老年代垃圾收集时出现长时间的卡顿，主要通过两种手段来达成此
目标：

* 1.不对老年代进行整理，而是使用空闲列表（free-lists）来管理内存空间的回收。
* 2.在mark-and-sweep（标记-清除）阶段的大部分工作和应用线程一起并发执行。也就是说，在这些阶段并没有明显的应用线程暂停。但值得注意的是，它仍然和应用线程争抢CPU 时间。

CMS收集器的工作过程主要分为6个阶段：

* 初始标记(Initial Mark)：需要STW，只是标记一下GC Roots能直接关联的对象，速度很快。
* 并发标记(Concurrent Mark)：从GC Roots直接关联的对象开始遍历整个对象图的过程，耗时较长，不需要STW。
* 并发预清理(Concurrent Preclean)
* 最终标记(Final Remark)：修正在并发标记期间，因程序运行导致的标记产生变动的对象的标记记录，停顿时间比初始标记稍长。
* 并发清除(Concurrent Sweep)：清理删除掉标记阶段判断的已经死亡的对象，由于不需要移动存活对象，所以这个阶段也是可以与用户线程同时并发的。
* 并发重置(Concurrent Reset)

CMS收集器的优点就是并发收集、低停顿。但是它也有部分不足：

* 对处理器资源敏感。在并发收集阶段会占用一部分线程而导致应用程序变慢，降低吞吐量。CMS默认启动的回收线程数是（处理器核心数量 +3）/4。

* 无法处理“浮动垃圾”（Floating Garbage），有可能出现并发失败，而导致一次完全的STW的Full GC的产生。因为CMS在并发标记和并发清除阶段，用户线程还在继续运行，会产生新的对象，CMS无法在这一次垃圾收集时处理，只能留到下一次时再清理。这一部分垃圾就称为“浮动垃圾”。正是因为这一部分新对象的产生，所以CMS收集器不能像其他收集器那样，在老年代完全满了只后收集，必须预留一部分空间供并发收集时的程序运作使用。参数`-XX:CMSInitiatingOccupancyFraction`控制CMS触发GC的百分比。要是CMS运行期间预留的内存无法满足程序分配新对象的需要，就会出现一次“并发失败”（Concurrent Mode Failure），这时候虚拟机将不得不启动后备预案：冻结用户线程的执行，临时启用Serial Old收集器来重新进行老年代的垃圾收集，但这样停顿时间就很长了。所以参数`-XX:CMSInitiatingOccupancyFraction`设置得太高将会很容易导致大量的并发失败产生，性能反而降低，用户应在生产环境中根据实际应用情况来权衡设置。

* 空间碎片化。CMS使用标记-清除算法实现垃圾回收，会产生空间碎片，如果没有足够大的连续内存空间来分配对象，就需要出发Full GC。为了解决这个问题，CMS收集器提供了一个参数`-XX:+/-UseCMSCompactAtFullCollection`用来控制在CMS收集器不得不进行FullGC时开启内存合并整理过程。由于整理过程需要移动对象，这会导致STW时间增长，所以又提供了另外一个参数`-XX:CMSFullGCsBeforeCompaction`，这个参数的作用是要求CMS收集器在执行指定次数的不整理内存的Full GC后，下一次进入Full GC前先进行内存整理。

常用参数：

* `-XX:+UseConcMarkSweepGC`: 使用CMS GC。
* `-XX:ParallelCMSThreads`: 同上。
* `-XX:CMSInitiatingOccupancyFraction`: 控制CMS触发GC百分比。默认值是-1，不指定时系统自己计算获得一般是92%。
* `-XX:+UseCMSInitiatingOccupancyOnly`: 指定该参数时，就会用一直使用指定的`CMSInitiatingOccupancyFraction`值触发GC；如果不指定，JVM仅在第一次使用设定值，后续则自动调整。默认值false。
* `-XX:+/-UseCMSCompactAtFullCollection`: 用于在CMS收集器不得不进行Full GC时开启内存碎片的合并整理过程，默认是开启，JDK9废弃。
* `-XX:CMSFullGCsBeforeCompaction`: 作用是要求CMS收集器在执行过若干次（数量由参数值决定）不整理空间的Full GC之后，下一次进入Full GC前会先进行碎片整理（默认值为0，表示每次进入Full GC时都进行碎片整理），JDK9废弃。
* `-XX:+CMSScavengeBeforeRemark`: 在CMS中的重新标记阶段之前进行一次yong GC，这样年轻代中待标记的对象数量相比GC之前减少很多，剩余被看做是“GC Roots”的对象减少，remark的时间就会减少很多，一般CMS的GC耗时 80%都在remark阶段。默认值false。
* `-XX:+CMSParallelInitialMarkEnabled`: 该标志用来开启初始化标记是否进行并行化。默认值true。
* `-XX:+CMSParallelRemarkEnabled`: 该标志用来开启最终标记是否进行并行化。默认值true。
* `-XX:+GCTimeRatio`: 同上。
* `-XX:MaxGCPauseMillis=500`: 同上。
* `-XX:MaxNewSize`: 设置年轻代的大小。
  默认配置规则：

  ```text
    如果设置了NewSize，MaxNewSize等于NewSize和preferred_max_new_size中的最大值，否则等于preferred_max_new_size。
    preferred_max_new_size的来源：
    等于preferred_max_new_size_unaligned和内存的对齐值。
    preferred_max_new_size_unaligned的来源：
    等于max_heap/(NewRatio+1)和ScaleForWordSize(CMSYoungGenPerWorker * ParallelGCThreads)的最小值。
    即CMSYoungGenPerWorker * ParallelGCThreads * 13 / 10，CMSYoungGenPerWorker一般为64m。
  ```

参考：

[CMS收集器几个参数详解](https://it007.blog.csdn.net/article/details/88541589)
[关于 -XX:+CMSScavengeBeforeRemark，是否违背cms的设计初衷？](https://www.zhihu.com/question/61090975)

#### Garbage First收集器（G1）

G1收集器是垃圾收集器技术发展历史上的里程碑式的成果。它是设计的主要目的是：将STW停顿时间和分布，变成可预期且配置的。

G1收集器的独特实现：堆不再分成年轻代和老年代，而是划分多个（通常是2048个）可以存放对象的小块堆区域。每个Region根据需要可以是Eden区、Survivor区或old区。在逻辑上，所有的Eden区和Survivor区合起来就是年轻代，所有的Old区拼在一起那就是老年代。

Region中还有一类特殊的Humongous区域，专门用来存储大对象。G1认为只要大小超过了一个Region容量一半的对象即可判定为大对象。而对于那些超过了整个Region容量的超级大对象， 将会被存放在N个连续的Humongous Region之中，G1的大多数行为都把Humongous Region作为老年代的一部分来进行看待。

这样划分之后，使得G1不必每次都去收集整个堆空间，而是以增量的方式来进行处理: 每次只处理一部分内存块，称为此次GC的回收集(collection set)。每次GC暂停都会收集所有年轻代的内存块，但一般只包含部分老年代的内存块。

G1的另一项创新是，在并发阶段估算每个小堆块存活对象的总数。构建回收集的原则是：垃圾最多的小块会被优先收集。这也是G1名称的由来。

**G1 GC处理的阶段**

* 1.纯年轻代模式的转移暂停（Evacuation Pause）

  G1 GC通过一段时间的运行情况不断调整自己的回收策略和行为，以此来比较稳定地控制暂停时间。在应用程序刚启动时，G1 还没有采集到什么足够的信息，这时候就处于初始的fullyyoung 模式。

  在年轻代模式的垃圾收集过程中，G1会收集eden区和前一次GC使用的survivors区，并将存活对象复制/转移到一些新的region里，如果对象的年龄达到GC的年龄，就会提升到老年代，否则就会转移到survivors区。这个过程需要STW。

  拷贝的过程称为转移（Evacuation)，这和前面介绍的其他年轻代收集器是一样的工作原理。

  日志：
  
  ```text
  2020-11-07T17:16:57.129-0800: 0.129: [GC pause (G1 Evacuation Pause) (young), 0.0097386 secs]
   [Parallel Time: 8.3 ms, GC Workers: 10]
      [GC Worker Start (ms): Min: 128.9, Avg: 128.9, Max: 129.2, Diff: 0.3]
      [Ext Root Scanning (ms): Min: 0.0, Avg: 0.2, Max: 0.4, Diff: 0.4, Sum: 1.8]
      [Update RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
         [Processed Buffers: Min: 0, Avg: 0.0, Max: 0, Diff: 0, Sum: 0]
      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.2, Diff: 0.2, Sum: 0.2]
      [Object Copy (ms): Min: 7.2, Avg: 7.4, Max: 7.8, Diff: 0.5, Sum: 73.9]
      [Termination (ms): Min: 0.0, Avg: 0.4, Max: 0.6, Diff: 0.6, Sum: 4.4]
         [Termination Attempts: Min: 1, Avg: 1.0, Max: 1, Diff: 0, Sum: 10]
      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [GC Worker Total (ms): Min: 8.0, Avg: 8.0, Max: 8.2, Diff: 0.2, Sum: 80.5]
      [GC Worker End (ms): Min: 136.9, Avg: 137.0, Max: 137.1, Diff: 0.2]
   [Code Root Fixup: 0.0 ms]
   [Code Root Purge: 0.0 ms]
   [Clear CT: 0.1 ms]
   [Other: 1.3 ms]
      [Choose CSet: 0.0 ms]
      [Ref Proc: 1.0 ms]
      [Ref Enq: 0.0 ms]
      [Redirty Cards: 0.2 ms]
      [Humongous Register: 0.0 ms]
      [Humongous Reclaim: 0.0 ms]
      [Free CSet: 0.0 ms]
   [Eden: 25.0M(25.0M)->0.0B(21.0M) Survivors: 0.0B->4096.0K Heap: 25.0M(512.0M)->14.9M(512.0M)]
   [Times: user=0.03 sys=0.04, real=0.01 secs]
  ```

* 2.并发标记（Concurrent Marking）

  G1通过执行并发标记构建起region的实时数据信息，以便回收集能高效地进行选择。
  
  当堆内存的总体使用比例达到一定数值，就会触发并发标记。这个默认比例是45%，但也可以通过JVM参数`-XX:InitiatingHeapOccupancyPercent`来设置。和CMS一样，G1的并发标记也是由多个阶段组成，其中一些阶段是完全并发的，还有一些阶段则会暂停应用线程。

  并发标记各个主要阶段：

  * 【初始标记阶段】(Initial mark phase)：在此阶段标记GC roots，一般是附加在某次常规的年轻代GC中顺带着执行。
  * 【并发扫描GC根所在的region】(Root region scanning phase)： 根据初始标记阶段确定的GC根元素，扫描这些元素所在region，获取对老年代的引用，并标记被引用的对象。该阶段与应用线程并发执行，也就是说没有STW停顿，必须在下一次年轻代GC开始之前完成。
  * 【并发标记阶段】(Concurrent marking phase)： 遍历整个堆，查找所有可达的存活对象。 此阶段与应用线程并发执行， 也允许被年轻代GC打断。
  日志：
  * 【再次标记阶段】(Remark phase)：此阶段有一次STW暂停，以完成标记周期。 G1会清空SATB缓冲区，跟踪未访问到的存活对象，并进行引用处理。
  * 【清理阶段】(Cleanup phase)：最后这个清理阶段为即将到来的转移阶段做准备，统计region中所有存活的对象，并将region进行排序，以提升GC的效率，维护并发标记的内部状态。所有不包含存活对象的region在此阶段都被回收了。有一部分任务是并发的：例如空region的回收，还有大部分的存活率计算。此阶段也需要一个短暂的 STW 暂停。

  ```text
  2020-11-07T17:16:57.356-0800: 0.356: [GC pause (G1 Evacuation Pause) (young) (initial-mark), 0.0270500 secs]
   [Parallel Time: 25.8 ms, GC Workers: 10]
      [GC Worker Start (ms): Min: 355.7, Avg: 355.7, Max: 355.9, Diff: 0.2]
      [Ext Root Scanning (ms): Min: 0.0, Avg: 0.2, Max: 0.2, Diff: 0.2, Sum: 1.8]
      [Update RS (ms): Min: 0.0, Avg: 0.1, Max: 1.0, Diff: 1.0, Sum: 1.4]
         [Processed Buffers: Min: 0, Avg: 1.2, Max: 3, Diff: 3, Sum: 12]
      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Object Copy (ms): Min: 23.6, Avg: 24.6, Max: 25.3, Diff: 1.6, Sum: 246.4]
      [Termination (ms): Min: 0.0, Avg: 0.6, Max: 0.6, Diff: 0.6, Sum: 5.7]
         [Termination Attempts: Min: 1, Avg: 1.0, Max: 1, Diff: 0, Sum: 10]
      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [GC Worker Total (ms): Min: 25.4, Avg: 25.5, Max: 25.7, Diff: 0.3, Sum: 255.4]
      [GC Worker End (ms): Min: 381.2, Avg: 381.3, Max: 381.3, Diff: 0.1]
   [Code Root Fixup: 0.0 ms]
   [Code Root Purge: 0.0 ms]
   [Clear CT: 0.2 ms]
   [Other: 1.0 ms]
      [Choose CSet: 0.0 ms]
      [Ref Proc: 0.8 ms]
      [Ref Enq: 0.0 ms]
      [Redirty Cards: 0.1 ms]
      [Humongous Register: 0.0 ms]
      [Humongous Reclaim: 0.0 ms]
      [Free CSet: 0.0 ms]
   [Eden: 77.0M(77.0M)->0.0B(45.0M) Survivors: 14.0M->12.0M Heap: 392.5M(512.0M)->380.5M(512.0M)]
  [Times: user=0.04 sys=0.14, real=0.03 secs]

  2020-11-07T17:16:57.384-0800: 0.383: [GC concurrent-root-region-scan-start]
  2020-11-07T17:16:57.384-0800: 0.383: [GC concurrent-root-region-scan-end, 0.0001500 secs]
  2020-11-07T17:16:57.384-0800: 0.383: [GC concurrent-mark-start]
  2020-11-07T17:16:57.387-0800: 0.386: [GC concurrent-mark-end, 0.0030114 secs]
  2020-11-07T17:16:57.387-0800: 0.386: [GC remark 2020-11-07T17:16:57.387-0800: 0.386: [Finalize Marking, 0.0008544 secs] 2020-11-07T17:16:57.388-0800: 0.387: [GC ref-proc, 0.0000533 secs] 2020-11-07T17:16:57.388-0800: 0.387: [Unloading, 0.0005009 secs], 0.0018274 secs]
  [Times: user=0.01 sys=0.00, real=0.00 secs] 
  2020-11-07T17:16:57.389-0800: 0.388: [GC clearnup 402M->171M(512M), 0.0008845 secs]
  [Times: user=0.00 sys=0.00, real=0.01 secs] 
  2020-11-07T17:16:57.390-0800: 0.389: [GC concurrent-clearnup-start]
  2020-11-07T17:16:57.390-0800: 0.389: [GC concurrent-clearnup-end, 0.0001279 secs]
  ```

* 转移暂停:混合模式（Evacuation Pause (mixed)）

  并发标记完成之后，G1将执行一次混合回收（mixed collection），就是在回收时，回收集不仅包括所有的eden区和survivors区，还会选择一部分老年代的region加入回收集。

  混合模式的转移暂停不一定紧跟并发标记阶段。有很多规则和历史数据会影响混合模式的启动时机。比如，假若在老年代中可以并发地腾出很多的小堆块，就没有必要启动混合模式。

  具体添加到回收集的老年代小堆块的大小及顺序，也是基于许多规则来判定的。其中包括指定的软实时性能指标，存活性，以及在并发标记期间收集的 GC 效率等数据，外加一些可配置的 JVM 选项。混合收集的过程，很大程度上和前面的fully-young gc 是一样的。

  经过多次混合模式的垃圾收集之后，很多老年代region其实已经处理过了，然后G1又切换回纯年轻代模式，直到下一次的并发标记周期完成。

**相关参数：**

* `-XX:+UseG1GC`: 启用G1 GC。
* `-XX:G1NewSizePercent`: 初始化年轻代占整个Java Heap的大小，默认值5%。
* `-XX:G1MaxNewSizePercent`: 最大年轻代占整个 Java Heap 的大小，默认值为60%。
* `-XX:G1HeapRegionSize`: 设置每个Region的大小，单位 MB，需要为 1，2，4，8，16，32 中的某个值，默认是堆内存的1/2000。如果这个值设置比较大，那么大对象就可以进入 Region了。
* `-XX:ConcGCThreads=n`: 设置并行标记线程的数量。将n设置为大约1/4的并行垃圾收集线程（ParallelGCThreads）。
* `-XX:InitiatingHeapOccupancyPercent`:（简称 IHOP），G1内部并行回收循环启动的阈值，默认为Java Heap的45%。这个可以理解为老年代使用大于等于45%的时候，JVM会启动垃圾回收。这个值非常重要，它决定了在什么时间启动老年代的并行回收。
* `-XX:G1HeapWastePercent`: G1停止回收的最小内存大小，默认是堆大小的5%。GC会收集所有的 Region中的对象，但是如果下降到了5%，就会停下来不再收集了。就是说，不必每次回收就把所有的垃圾都处理完，可以遗留少量的下次处理，这样也降低了单次消耗的时间。
* `-XX:G1MixedGCCountTarget`: 设置并行循环之后需要有多少个混合 GC 启动，默认值是 8 个。老年代 Regions 的回收时间通常比年轻代的收集时间要长一些。所以如果混合收集器比较多，可以允许 G1 延长老年代的收集时间。
* `-XX:G1ReservePercent`: G1为了保留一些空间用于年代之间的提升，默认值是堆空间的10%。也就是老年代会预留10%的空间来给新生代的对象晋升，如果经常发生新生代晋升失败而导致Full GC，那么可以适当调高此阈值。但是调高此值同时也意味着降低了老年代的实际可用空间。
* `-XX:+GCTimeRatio`: 这个参数就是计算花在Java应用线程上和花在GC线程上的时间比率，默认是 9，跟新生代内存的分配比例一致。这个参数主要的目的是让用户可以控制花在应用上的时间，G1的计算公式是 1/（1+GCTimeRatio）。这样如果参数设置为9，则最多10%的时间会花在GC工作上面。
* `-XX:+UseStringDeduplication`: 手动开启 Java String 对象的去重工作，这个是JDK8u20 版本之后新增的参数，主要用于相同 String 避免重复申请内存，节约 Region 的使用。默认值false。
* `-XX:MaxGCPauseMills`: 预期G1每次执行 GC 操作的暂停时间，单位是毫秒，默认值是200毫秒，G1 会尽量保证控制在这个范围内。

**G1 GC的注意事项**

在某些情况下G1触发了Full GC，这时G1会退化使用Serial收集器来完成垃圾清理工作，它仅仅使用单线程来完成GC工作，GC暂停时间将达到秒级别的。

相关日志信息：

```text
// TODO
```

* 并发标记失败

  G1启动并发标记周期，但在Mixed GC之前，老年代就被填满，这时候G1会放弃标记周期。解决办法：增加堆大小或者调整周期（例如增加线程数-XX:ConcGCThreads 等）。

* 晋升失败

  没有足够的内存供存活对象或者晋升对象使用，由此触发Full GC(to-space exhausted/to-space overflow）。
  解决办法：
  * 增加–XX:G1ReservePercent选项的值（并相应增加总的堆大小）增加预留内存量。
  * 通过减少–XX:InitiatingHeapOccupancyPercent提前启动标记周期。
  * 也可以通过增加 –XX:ConcGCThreads 选项的值来增加并行标记线程的数目。

* 巨型对象分配失败

  当巨型对象找不到合适的空间进行分配时，就会启动 Full GC，来释放空间。解决办法：增加内存或者增大region的大小，参数-XX:G1HeapRegionSize。

**G1无论是为了垃圾收集产生的内存占用（Footprint）还是程序运行时的额外执行负载 （Overload）都要比CMS要高。**

**G1与CMS的区别：**

* CMS是主要作用于老年代的回收，而G1是分代回收，即回收年轻代也可以回收老年代或者混合回收。
* G1使用region方式对堆内存进行了划分，且基于标记整理算法实现，整体减少了内存碎片的产生。
* 在初始化标记阶段，搜索可达对象使用到的Card Table的实现方式不一样。
* 在小内存的应用上CMS的效率大概率优于G1，大内存应用上G1优势更大，大内存一般是6G~8G。

**G1和CMS的卡表实现不同点：**

* G1的卡表实现更加复杂，每个region都必须有一个卡表（每个Region初始化时，都会初始化一个RSet，用来记录其他region对本region对象的引用），这导致G1的记忆集可能会占整个堆容量的20%甚至更多的内存；而CMS的卡表只需要维护一份。
* G1的卡表记录的是其他region对当前region的引用；CMS的卡表记录老年代到新生代的引用。
* 在执行的负载上，CMS和G1都是使用写后屏障来更新维护卡表。但是，G1为了实现原始快照搜索算法（SATB），还需要使用写前屏障跟踪并发时的指针变化情况，所以，相比较之下G1更消耗资源。

**参考：[深入解析G1垃圾收集器与性能优化](https://renfufei.blog.csdn.net/article/details/108476781)**

#### ZGC收集器

ZGC的设计目标是在尽可能对吞吐量影响不大的前提下，实现在任意堆内存大小下都可以把垃圾收集的停顿时间限制在十毫秒以内的低延迟。ZGC的设计理念与Azul System公司的PGC和C4收集器一脉相承（C4收集器是PGC的集成灶）。

`-XX:+UnlockExperimentalVMOptions -XX:+UseZGC`

`-XX:+UnlockExperimentalVMOptions`: 解锁实验参数，允许使用实验性参数。

ZGC最主要的特点包括:

1. Region具有动态性，动态创建和销毁，以及动态的区域容量大小。
2. GC 最大停顿时间不超过 10ms。
3. 堆内存支持范围广，小至几百 MB 的堆空间，大至 4TB 的超大堆内存（JDK13 升至 16TB）。
4. 与 G1 相比，应用吞吐量下降不超过 15%。
5. 当前只支持 Linux/x64 位平台，JDK15 后支持 MacOS 和Windows 系统。

#### Shenandoah收集器 晒定道啊

RedHat公司出的，算是G1收集器的继任者。Shenandoah作为第一个不是由Oracle公司开发的Hotspot垃圾收集器，不可避免的收到了来自官方的”排挤“。目前只有OpenJdk中才包含Shenandoah收集器，OracleJDK不包含Shenandoah收集器。

Shenandoah GC宣称暂停时间与堆大小无关，无论是 200MB 还是 200 GB的堆内存，都可以保障具有很低的暂停时间（注意:并不像ZGC那样保证暂停时间在10ms以内）。

`-XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC`

与G1相比：

* 支持并发的整理算法，使用转发指针和读屏障来实现并发整理。
* 默认不使用分代收集。
* 摒弃了在G1中耗费大量内存和计算资源去维护的记忆集，改用名为“连接矩阵”（Connection Matrix）的全局数据结构来记录跨Region的引用关系，降低了处理跨代指针时的记忆集维护消耗，也降低了伪共享问题（见3.4.4节）的发生概率。

### 常用的GC组合和GC选择

#### 常用的GC组合

* Serial+Serial Old 实现单线程的低延迟垃圾回收机制，开启参数:`-XX:+UseSerialGC`。
* ParNew+CMS，实现多线程的低延迟垃圾回收机制，开启参数:`-XX:+UseConcMarkSweepGC`。
* Parallel Scavenge和Parallel Scavenge Old，实现多线程的高吞吐量垃圾回收机制，开启参数:`-XX:+UseParallelGC`/`-XX:+UseParallelOldGC`/`-XX:+UseParallelGC -XX:+UseParallelOldGC`。
* ParNew + SerialOld这个组合很少用，某些版本中已经废弃，开启参数：`-XX:+UseParNewGC`。
* G1实现可控的停顿时间，开启参数：`-XX:+UseG1GC`。

#### GC如何选择

一般性的指导原则：

* 如果系统考虑吞吐优先，CPU资源都用来最大程度处理业务，用Parallel GC；
* 如果考虑低延迟有限，每次GC时间尽量短，用CMS GC；
* 如果系统内存堆较大，同时希望整体来看平均 GC 时间可控，使用 G1 GC。

对于内存大小的考量：

* 一般 4G 以上，算是比较大，用 G1 的性价比较高。
* 一般超过 8G，比如 16G-64G 内存，非常推荐使用 G1 GC。

jdk7默认GC：Parallel Scavenge（新生代）+Parallel Old（老年代）。
jdk8默认GC：Parallel Scavenge（新生代）+Parallel Old（老年代）。
jdk9默认GC：G1。
jdk10默认GC：G1。

### 内存分配策略

#### 栈上分配

Java中默认一个对象是在堆中分配内存的，而堆中的对象不再使用时需要通过垃圾回收机制回收，这个过程相对分配在栈中的对象创建和销毁来说，更消耗时间和性能。如果通过逃逸分析发现一个对象只是在方法中使用，就会将对象在栈中分配。

**Hotspot虚拟机目前的实现导致栈上分配实现比较复杂，所以Hotspot中暂时没有实现这个优化。**

#### 标量替换

如果逃逸分析证明一个对象不会被外部访问，并且这个对象可以被拆分的话，当程序真正执行的时候可能不创建这个对象，而是直接创建它的成员变量来代替。这样讲对象拆分后，可以将对象的成员变量在栈或寄存器上分配，原本的对象就不需要分配内存空间。这种编译优化叫做标量替换。

代码：

```java

public void foo() {
     TestInfo info = new TestInfo();
     info.id = 1;
     info.count = 99;
       ...//to do something
}
```

经过逃逸分析后，可能被优化为：

```java
public void foo() {
     id = 1;
     count = 99;
     ...//to do something
}
```

#### 线程本地分配缓存区（Thread Local Allocation Buffer）

线程本地分配缓存区（Thread Local Allocation Buffer），简称TLAB，这是一个线程私有的内存分配区域。每个线程在需要分配内存时，会优先在自己的这块区域内分配。TLAB的空间非常小，默认是整个Eden区的1%。

**相关参数：**

* `-XX:UseTLAB`：启用TLAB，默认值true。
* `-XX:TLABWasteTargetPercent`：设置TLAB空间所占Eden区的百分比大小，默认值1。

**参考[浅析java中的TLAB](https://www.jianshu.com/p/8be816cbb5ed)**

#### 对象什么时候进入老年代

对象进入老年代的方式主要有两种，分别是对象年龄达到指定分代年龄阈值和大对象直接进入老年代。

* survivor区中的对象每熬过一次yong gc对象的年龄就会加1，当对象的年龄等于指定的分代年龄大小的时候，对象会进入老年代。
  * 分代年龄参数`-XX:MaxTenuringThreshold=n`，n的值在0和15之间， Parallel Scavenge收集器默认值15，CMS收集器默认6，G1收集器默认15。
* 大对象直接进入老年代。
  * 在Serial和ParNew收集器中，当对象的大小超过参数`-XX:PretenureSizeThreshold`时，对象会直接在老年代中分配内存。
  * 在G1收集器中，region中有一类特殊的Humongous区域，专门用来存放大对象。G1认为一个对象的大小超过了region容量的一半即判定为大对象。大对象会被存放到N个连续的Humongous Region之中，G1的大多数行为都把Humongous Region作为老年代 的一部分来进行看待。

#### 动态年龄

Hotspot虚拟机中并不是永远要求对象的年龄必须达到`-XX:MaxTenuringThreshold`才能晋升到老年代，它有一个动态计算晋升年龄阈值的机制。在survivor区中从年龄为1的对象占用的内存大小开始累加，当累加到都某个年龄的大小超过survivor区的一半时，取这个年龄值和MaxTenuringThreshold中更小的一个值，作为新的晋升年龄阈值。

参数`-XX:TargetSurvivorRatio=n`控制计算晋升年龄阈值时，年龄累计内存占survivor区的百分比，默认值50%。

动态计算晋升年龄阈值，防止设定的MaxTenuringThreshold过大，survivor区溢出；设定的MaxTenuringThreshold过小，对象过早提升，引起频繁Major GC。

#### 分配担保（Handle Promotion）

jdk6 uptate 24之后，在发生Minor GC之前，虚拟机必须先检查老年代最大可用的连续空间是否大于年轻代对象总大小或者历次晋升的平均大小，如果大于这次Minor GC就是安全的可以进行，否则就需要进行Full GC。这种老年代需要给年轻代进行的担保称为分配担保。

jdk6 uptate 24之前，可以通过参数`-XX:+HandlePromotionFailure`指定允许担保失败。在JDK 6 Update 24之后，`-XX:HandlePromotionFailure`参数不会再影响到虚拟机的空间分配担保策略，观察OpenJDK中的源码变化（见代码清单3-12），虽然源码中还定义了`-XX:HandlePromotionFailure`参数，但是在实际虚拟机中已经不会再使用它。JDK 6 Update 24之 后的规则变为只要老年代的连续空间大于新生代对象总大小或者历次晋升的平均大小，就会进行 Minor GC，否则将进行Full GC。

**G1 GC是否没有这个机制了？？？**

## 参考

* [G1垃圾回收器起步](https://blog.csdn.net/elinespace/article/details/78852469)
* [G1垃圾收集器入门](https://www.oracle.com/technetwork/tutorials/tutorials-1876574.html)
* [深入解析G1垃圾收集器与性能优化](https://renfufei.blog.csdn.net/article/details/108476781)