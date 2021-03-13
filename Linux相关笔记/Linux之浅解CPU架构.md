# 浅解CPU架构

一个CPU处理器中一般有多个运行核心，称为物理核，每个物理核可以运行应用程序。每个物理核都拥有私有的一级缓存（L1 cache），包括一级指令缓存和一级数据缓存，以及私有的二级缓存（L2 cache）。另外，在主流的CPU处理器中，一个物理核中通常会运行两个超线程，叫做逻辑核。通常的四核八线程就是指CPU中有四个物理核，每个物理核可以模拟出2个逻辑核。同一个物理核的逻辑核会共享使用 L1、L2 缓存。CPU处理器中还有一个三级缓存（L3 cache）是物理核共享的。

![CPU架构示例图](https://static001.geekbang.org/resource/image/d9/09/d9689a38cbe67c3008d8ba99663c2f09.jpg)

## L1 cache、L2 cache和L3 cache

因为L1 cache和L2 cache是物理核私有的，所以，当数据或指令保存在 L1、L2 缓存时，物理核访问它们的延迟不超过 10 纳秒，速度非常快。但是，L1 cache和L2 cache的大小受限于处理器的制作技术，一般只有KB级别，存不了太多数据。如果L1、L2缓存中没有所需的数据，应用程序就需要访问内存来获取数据，而访问内存一般在百纳秒级别，是访问L1、L2缓存延迟的10倍，不可避免地会对性能造成影响。为了减小这个影响，CPU又提供了L3 cache。

L3 缓存能够使用的存储资源比较多，所以一般比较大，能达到几 MB 到几十 MB，这就能让应用程序缓存更多的数据。当 L1、L2 缓存中没有数据缓存时，可以访问 L3，尽可能避免访问内存。

## UMA架构

在历史上CPU、内存数量比较小的年代里的总线方案是FSB，FSB的全称是Front Side Bus（前端总线）。CPU通过FSB总线连接到北桥芯片，然后再连接到内存。内存控制器是集成在北桥里的，CPU和内存之间的通信全部都要通过这一条FSB总线来进行。

![FSB总线](https://pic3.zhimg.com/80/v2-c2176155b7a09009d8ac0a43bd471a5a_1440w.jpg)

这样的架构称为**UMA架构**（Uniform Memory Access)，直译为“统一内存访问”。这样的架构对软件层面来说非常容易，总线模型保证所有的内存访问是一致的，即每个处理器核心共享相同的内存地址空间。

## NUMA架构

当CPU的主频提升到3GHz每秒以后，单个CPU已经到了物理极限了。所以，改变了性能改进的方法，改成向多核、多CPU的方法发展。在这种情况下，如果仍然使用FSB总线的方式，会导致所有的CPU核内存通信通过都经过总线，这样总线就成为了瓶颈，无法充分发挥多核的优势与性能。

所以CPU制造商们把内存控制器从北桥搬到了CPU内部，这样CPU便可以直接和自己的内存进行通信了。那么，如果CPU想要访问不和自己直连的内存条怎么办呢？所以就诞生了新的总线类型，它就叫QPI总线。

![QPI总线](https://pic3.zhimg.com/80/v2-3f1230e90320d06aa47d6b3cf4f3f8ce_1440w.jpg)

**Linux下的NUMA架构**

NUMA 全称 Non-Uniform Memory Access，译为“非一致性内存访问”。在这种架构下，不同的内存器件和CPU核心从属不同的Node，每个Node都有自己的集成内存控制器（IMC）。在 Node 内部，架构类似SMP，使用 IMC Bus 进行不同核心间的通信；不同的 Node 间通过QPI（Quick Path Interconnect）进行通信。

NUMA架构是为了解决多CPU系统下共享BUS带来的性能问题。

![NUMA架构](https://upload-images.jianshu.io/upload_images/17885431-b2381fd6ca9d1cbb.png)

**需要注意的是，QPI的延迟要高于IMC Bus，也就是说在NUMA架构下，CPU访问自己同一个node里的内存要比其它内存要快。**

### NUMA的内存分配策略

在Linux上可以使用`numactl`命令对进程进行NUMA访问策略的手工配置。

* –interleave=nodes，允许进程在多个node之间交替访问，当–interleave=all，表示全部node。
* –membind=nodes，将内存固定在某个node上，CPU则选择对应的core。
* –cpunodebind=nodes，与membind相反，将CPU固定在某几个core上，内存则限制在core对应的NUMA node之上。
* –physcpubind=cpus，与–cpunodebind类似，不同的是这里cpus表示物理核？？（表示有疑问🤔️）。
* –localalloc，使用当前 node，默认。
* –preferred=node，优先实用指定的 node，实在不行用其他的 node 也可以。

`numactl`的使用，在程序的启动命令前添加`numactl --interleave=all`。

## 参考

* [NUMA架构下的内存访问延迟区别！](https://zhuanlan.zhihu.com/p/90624389)
* [深挖NUMA](https://zhuanlan.zhihu.com/p/33621500)
* [NUMA架构下MySQL的应用](https://zhuanlan.zhihu.com/p/62795773)
* [Interleave机制为什么对MySQL更友好](https://blog.csdn.net/liguangxianbin/article/details/80797400)
