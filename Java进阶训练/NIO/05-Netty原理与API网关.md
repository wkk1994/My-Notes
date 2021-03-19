# Netty原理与API网关

## 高性能

### 什么是高性能

* 高并发用户（Concurrent Users）：从业务外部衡量，属于业务指标。
* 高吞吐量：QPS（每秒查询），TPS（每秒事物操作），系统内部，属于技术指标。
* 低延迟： 也属于技术指标。

```text
~ % wrk -c 40 -d 30s --latency http://127.0.0.1:8808/test
Running 30s test @ http://127.0.0.1:8808/test
  2 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   686.85us    6.80ms 177.27ms   99.46%
    Req/Sec    78.15k     8.27k   85.22k    94.67%
  Latency Distribution
     50%  237.00us
     75%  255.00us
     90%  281.00us
     99%  786.00us
  4666008 requests in 30.10s, 485.03MB read
Requests/sec: 154995.03
Transfer/sec:     16.11MB
```

说明：

`-c 40`是并发数；`Requests/sec: 154995.03`如果是查询就是QPS；如果是事物操作就是TPS。`Latency Distribution`表示的是响应时间。

**延迟(Latency)和响应(Response Time)时间的关系：**

用户从发起请求到接收到响应的时间称为响应时间。系统接收到请求开始处理到处理完成发送响应的时间称为延迟。响应时间是相对于用户或者调用者，受网络的影响。延迟时间是相对于系统内部的，延迟时间对于每个请求来说是差不多的。

### 高性能的副作用

* 系统复杂度：为了实现高性能要引入更多的东西。

* 建设和维护成本提高。

* 故障或BUG导致的破坏性更大：性能越高故障破坏性越大。

**应对策略：**

稳定性建设（混沌工程）：

* 1.容量：清楚应用的最大负载，吞吐量、并发量等。

> 每天有86400秒，taobao每天有3000万TPS，美团滴滴大概1000万。支付宝在双十一最大的并发，前几分大概50万左右TPS每秒。三大云服务供应商：AWS，微软，Aliyun。

* 2.爆炸半径：bug造成的影响。应对方式：应用拆分，微服务。

* 3.工程方法积累与改进：天灾人祸，超出认知的bug。

> 混沌工程(Chaos Engineering)：Netflix提出的，通过在整个系统中随机位置引发故障，帮助分布式系统提升容错性和可恢复性的混沌工程工具。ChaosBlade：阿里巴巴的混沌测试工具。

## Netty概览

网络应用开发框架：异步、事件驱动、基于NIO。适用于：服务端、客户端、TCP/UDP。

基于NIO：高并发量；事件驱动：API解耦；异步：高性能。

### 事件处理机制

![事件处理机制](https://note.youdao.com/yws/api/personal/file/WEB0d459e4573ef19bdf65fa65df12607b0?method=download&shareKey=16bcb824e18265cc608717938f41dd40)

事件处理机制的流程：

客户端发起请求事件（Event）--> 事件进入到事件队列（Event Queue）排队 --> 当轮到这个事件的时候，事件分发器（Event Mediator）将事件分发到事件通道（Event Channel）--> 事件通道（Event Channel）在调用事件处理器（Event Processor）处理。

### 从事件处理机制到Reactor模型

![Reactor模型](https://note.youdao.com/yws/api/personal/file/WEBcb2230c0600ab6385be1b39ca1a595a6?method=download&shareKey=ba41741adf572f4bcc7766de9967ad55)

Reactor模式是一种典型的事件驱动的编程模型，首先是由事件驱动的，有一个或者多个并发输入源，有一个ServiceHandler和多个EventHandlers。
ServiceHandler负责将客户端请求hand住，然后将请求多路复用的分发给相应的Event Handler。ServiceHandler只负责一件事更高效。

### 从Reactor模型到Netty NIO

Reator模型的三种模式：单线程模式、多线程模式、主从多线程模式。

**单线程模式**

所有的I/O操作都是在同一个NIO线程上面完成。NIO线程的职责如下：作为NIO服务端，接收客户端的TCP连接；作为NIO客户端，像服务端发起TCP连接；读取通信对端的请求或应答消息；向通信对端发送消息请求或应答消息。

适合小容量的应用场景，对于高并发的应用场景不适合，原因：一个NIO线程性能上无法支撑成千上百的链路；当NIO线程负载过重后，处理会变慢，会导致客户端连接超时（超时之后往往会重发）；可靠性差，单线程出现问题会导致系统不可用。

**多线程模式**

多线程模式与单线程模式相比较，就是有一组NIO线程来处理I/O操作。

特点：

* 有专门一个NIO线程（Acceptor线程）用户监听服务端，接收客户端的TCP连接请求。
* 网络I/O操作，读写等都由一个NIO的线程池负责，线程池的实现可以包含一个任务队列和N个可用线程，由这些NIO线程负责消息的读取、编解码和发送。
* 一个NIO线程可以同时处理N条链路，但是一个链路只对应一个NIO线程，防止发生并发操作问题。

大部分场景下，多线程模式都可以满足性能需求。对于个别场景，比如服务端需要对客户端握手进行安全认证，但是认证本身比较消耗性能，那么单个Acceptor线程就可能存在性能不足的问题。

**主从多线程模式**

与多线程相比，主从多线程将Reactor线程拆分了mainReactor和subReactor两个部分，mainReactor线程池负责接收连接并分发给Acceptor连接建立，建立连接后再分发给subReactor线程池。其中mainReactor一般只有一个，subReactor的数量，可以根据 CPU 的核数来灵活设置。

![NettyNIO模型](https://note.youdao.com/yws/api/personal/file/WEB895059f545dd86ddcf167cf5b4bdcb17?method=download&shareKey=2200c77c7e926a0f824ecf29ccb25224)

Netty三种模式都支持，上面是最复杂的一种。

Boss EventLoopGroup：负责接收客户端请求，再交给Worker EventloopGroup处理，数量较少。
Worker EventLoopGroup：负责处理客户端请求。

### Netty运行原理

![Netty运行流程](https://note.youdao.com/yws/api/personal/file/WEBe7f61684aaf01a92b4afaae33046a9bf?method=download&shareKey=a971016bd0f8d0b385adce6f834e60d5)

TODO

### 关键对象

![Netty的关键对象.png](https://note.youdao.com/yws/api/personal/file/WEBde6f20621419b0f03d299f70b2ac2d5c?method=download&shareKey=f78b38c62f22f1d4c135e4c7aa2f9694)

* Bootstrap：Netty的启动器，有两个ServerBootstrap和Bootstrap，一个用于服务端一个用于客户端。
* EventLoopGroup：一组EventLoop，如果将EventLoop理解成线程，这个就是线程池。
* EventLoop：可以理解成线程
* SocketChannel：网络相关
* ChannelInitializer：初始化，绑定处理链。
* ChannelPipeline：处理器链。
* ChannelHandler：处理器。

### ChannelPipeline

处理器链，负责ChannelHandler的管理和调度。

特征：

* 支持运行时动态添加或删除Handler，使用示例：在业务高峰期动态添加系统的拥塞保护Headler，高峰期过后再动态删除。
* ChannelPipeline是线程安全的，但是ChannelHandler不是线程安全的。

### EventLoop

### Event & Handler

**Event**

Netty中的事件分为inbound事件和outbound事件。

inbound事件一般由I/O线程触发，比如链路建立、链路关闭、读事件、异常通知事件等。
outbound事件一般由用户主动发起的网络I/O操作，比如打开链接、关闭连接、消息发送等事件。

**Handler**

Handler是事件处理的接口，一般有：

* ChannelHandler
* ChannelOutboundHandler
* ChannelInboundHandler
* ChannelInboundHandlerAdapter 适配器（空实现，需要继承使用）
* ChannelOutboundHandlerAdapter 适配器（空实现，需要继承使用）

## Netty网络程序优化

### 粘包与拆包

TCP本身没有粘包和拆包的问题。

**Netty的ByteToMessageDecoder提供了一些实现用来解决粘包和拆包问题：**

* FixedLengthFrameDecoder: 定长协议解码器，可以指定固定长度字节数算一个完整的报文。
* LineBasedFrameDecoder: 行分隔符解码器，遇到\n或者\r\n，则认为是一个完整的报文。
* DelimiterBasedFrameDecoder：分隔符解码器，分隔符可以自己指定。
* LengthFieldBasedFrameDecoder：长度编码解码器，将报文划分为报文头/报文体。
* JsonObjectDecoder：json格式解码器，当检测到匹配数量的"{" 、”}”或”[””]”时，则认为是一个完整的json对象或者json数组。

> nginx也有类似的操作，不知道文件的大小的时候，会在响应的头部添加chuck...

### Nagle与TCP_NODELAY

**Nagle算法**

Nagle算法：当发送数据的缓冲区满了或者超过200ms（默认）了，就将数据发送出去，有点组提交的感觉。Nagle算法的优化条件是缓冲区满，达到超时。Nagle算法的目的是优化单个小的包的发送。

怎么避免这个优化：

* 可以将操作系统的这个优化关闭，当并发高的时候这个优化收益很大。
* 修改最大的传输单元（MTU: Maxitum Transmission Unit）和最大分段大小（MSS: Maxitum Segment Size）。最大传输单元一般1500byte；最大分段大小tcp下一般1460byte。

> 为什么是1460byte？因为一个tcp数据包，还好包含tcp头等信息差不多40byte。
> 在代码中调用send()方法发生数据，这个操作只是调用操作系统底层的发生接口，最终发不发还要由操作系统决定。接收数据也是一样。

### 连接优化

![TCP三次握手四次挥手](https://note.youdao.com/yws/api/personal/file/WEB5922f9c2f656b1527a56478747fbca01?method=download&shareKey=31857591c16b8eb0b28f2cfd094787e5)

TCP三次握手，四次挥手。

在TCP断开连接的时候，四次挥手中的最后一次客户端需要等待2MSL才能将端口释放掉，Linux上默认1MSL=2分钟，Window上默认1MSL=1分钟。所以一次压力测试后可能在客户端看到很多在WAIT_TIMEOUT的端口。所以如果有大量的高并发请求，会有很多端口的状态在等待断开状态。

解决方式：

* 降低断开连接的等待周期，MSL的值。
* 开启端口复用功能，可以复用状态是TIME_WAIT的端口。

### Netty优化

* 1.不要阻塞EvnetLoop，因为EventLoop是单线程的。不要加同步锁，会导致单线程的轮询过程，相互之间被阻塞。

* 2.系统参数优化

  调整文件描述符的大小`ulimit -a`；
  调整TCP挥手等待周期的时间，Linux修改/proc/sys/net/ipv4/tcp_fin_timeout，Windows在注册表中修改参数TcpTimedWaitDelay。

* 3.缓冲区优化

  在Netty代码中调整下面参数：
  SO_RCVBUF（接收缓冲区）/SO_SNDBUF（发送缓冲区）/SO_BACKLOG（保持连接状态）/REUSEXXX（端口的重用）

  > SO_BACKLOG（保持连接数量）：用来控制在连接状态中的TCP连接的数量。linux和mac上默认值128，window上默认值200。
  > SO_REUSEADDR: 地址重用；SO_REUSEPORT: 端口重用。

* 4.心跳周期优化

  心跳机制与断线重连：用来检查客户端是否断开连接以及重连。心跳机制的频率要控制在适当范围内。

* 5.内存与ByteBuffer优化

  减少内核态和用户态的copy，使用DirectBuffer与HeapBuffer直接操作内存。

  > DirectBuffer: 直接内存，不需要内核态和用户态的copy；HeapBuffer: 使用堆内存，还需要内核态和用户态的copy。
  > Netty自己建立了类似GC的内存管理机制，每次使用后清空，可以复用。

* 6.其他优化

  * IoRatio: IO操作和非IO操作占用CPU时间的比例，默认是50:50。
  * Watermark：缓冲区水位，写满时的处理。
  * TrafficShaping：基于水位操作，对流量进行限流。

## 典型应用：API网关

### 网关的结构和功能

**网关的主要功能：**

* 请求接入：作为API接口服务请求的接入点。
* 业务聚合：作为后端业务跟服务的聚合点。
* 中介策略：实现安全、验证、路由、过滤、流控等策略。
* 统一管理：对所有API服务和策略进行统一管理。

### 网关的分类

流量网关：关注稳定与安全，提供日志统计、防止SQL注入、防止Web攻击、黑白名单等。例如：OpenResty，Kong。
业务网关：提供更好的服务，有服务级别流控、服务熔断与降级、路由与负载均衡、灰度策略、权限验证与用户等级策略、业务规则与参数校验、多级缓存策略。例如：Spring Cloud Gateway，Zuul2。

* Zuul

Netflix开源的API网关系统，主要设计目标是动态路由、监控、弹性、安全。

* Zuul2

基于Netty内核重构的版本。

* Spring Cloud Gateway

底层基于WebFlux，WebFlux基于Netty Reactor。功能更强大。

### 动手实现API网关

**架构设计：**

* 设计：技术复杂度与业务复杂度
  分析问题的本质，心态问题？管理问题？技术问题？业务问题？  
* 抽象：概念理清，命名正确
  模块之间的命名，正确的命名形成自己内部的DSL。
* 组合：组件之间的相互关系

## Tips

* 早期使用`select 1`进行数据库的探活，JDBC4以后驱动底层有java.sql.Connection#isValid方法可以检测连接是否有效。
* 一般外部的流量进来才需要走网关，内部的流量请求没必要走网关。
