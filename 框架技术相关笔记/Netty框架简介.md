# Netty框架简介

## Netty框架简介

### Netty是什么

Netty是基于异步和事件驱动的网络应用程序框架，通过它可以快速开发、高性能、高可扩展的服务器应用和客户端应用。

Netty的特性是：高吞吐、低延迟、低开销、零拷贝、可扩容。基于jdk5，兼容大部分通用协议，并且支持自定义协议。

## IO模式

### 三种IO模式

BIO（阻塞同步IO）、NIO（非阻塞同步IO）、AIO（非阻塞异步IO）。

### Netty对三种IO模式的支持

* BIO: 在Netty中对应OIO，现在已经不推荐使用。~~OioServerSocketChannel，OioSocketChannel~~。

* NIO：netty在不同的平台有对应不同的实现，以及一个通用的实现。
  * 通用实现：NioEventLoopGroup、NioEventLoop、NioServerSocketChannnel、NioSocketChannel。
  * Linux平台实现：EpollEvnetLoopGroup、EpollEventLoop、EpollServerSocketChannnel、EpollSocketChannel。
  * masOS/BSD平台实现：KQueueEvnetLoopGroup、KQueueEventLoop、KQueueServerSocketChannnel、KQueueSocketChannel。

* AIO: 在新版本中已经删除了实现，AioEvnetLoopGroup、AioEventLoop、AioServerSocketChannnel、AioSocketChannel。

**为什么不推荐BIO？**

在连接数高的情况下，阻塞IO耗资源，效率低。

**为什么删除了AIO的支持？**

* Windows平台对AIO的实现比较成熟，但是Windows很少用来做服务器。
* Linux常用来做服务器，但是AIO实现不够成熟。
* Linux下AIO的性能提升相对于NIO不明显。

**Netty为什么不使用通用的NIO实现，而是自己实现一个？**

* JDK默认实现是水平触发， Netty默认实现是边缘触发，但是可以切换到水平触发。
* Netty实现的垃圾回收更少，性能更好。

> **水平触发**：主要文件描述符关联的内核缓冲区非空，有数据可读，就会一直发送可读信号进行通知，当文件描述符关联的内核写缓冲区不满，有空间可以写入，就一直发出可写信号进行通知。
> **边缘触发**：当文件描述符关联的读内核缓冲区由空转化为非空的时候，则发出可读信号进行通知，当文件描述符关联的内核写缓冲区由满转化为不满的时候，则发出可写信号进行通知。
> **两者的区别：** 水平触发是只要读缓冲区有数据，就会一直触发可读信号，而边缘触发仅仅在空变为非空的时候通知一次。
> select,poll就属于水平触发，epoll支持水平触发和边缘触发。

### Reactor的三种模式

NIO模式对应的开发模式Reactor模式。

Reactor模式称为响应器模式，核心流程：注册事件，扫描事件是否发生，事件发生后作出相应处理。

* 单线程模式

  netty中使用Reactor单线程模式：

  ```java
  EventLoopGroup bossGroup = new NioEventLoopGroup(1);
  ServerBootstrap serverBootstrap = new ServerBootstrap();
  serverBootstrap.group(bossGroup);
  ```

* 多线程模式

  netty中使用Reactor多线程模式：

  ```java
  EventLoopGroup bossGroup = new NioEventLoopGroup();
  ServerBootstrap serverBootstrap = new ServerBootstrap();
  serverBootstrap.group(bossGroup);
  ```

* 主从多线程模式

  netty中使用Reactor主从多线程模式：

  ```java
  EventLoopGroup bossGroup = new NioEventLoopGroup();
  EventLoopGroup workerGroup = new NioEventLoopGroup();
  ServerBootstrap serverBootstrap = new ServerBootstrap();
  serverBootstrap.group(bossGroup, workerGroup);
  ```

## 粘包和半包（拆包）

### 什么是粘包半包

粘包：发送方发送的多个消息，在接收方接收时是一个消息，比如：发送方发送 ABC  DEF，接收方接收的时候是ABCDEF。
半包：一个消息被拆，通过多个传输包发送。

对于发送端而言粘包和半包产生的现象就是，一个发送可能占用多个传输包，多个发送也可能占用一个传输包。
对于接收端而言现象是，一个发送可能被多次接收，多个发送可能被一次接收。

粘包的主要原因：

* 发送方每次写入的数据远小于套接字缓冲区的大小，这样数据不会立即被发送出去。
* 接收方读取套接字缓冲区数据不够及时。

半包的主要原因：

* 发送方写入的数据大于套接字缓冲区的大小。
* 发送的数据大于协议的MTU（Maximum Transmission Unit，最大传输单元），必须拆包。

**TCP是流式协议，消息无边界。**

### 粘包半包的解决方式

解决问题的根本手段：**找出消息边界**

* TCP连接改成短连接，一个请求一个短连接。通过建立连接和连接释放之间的消息为传输消息。简单，但是效率底下。

* 封装成帧（Framing）：
  * 固定长度：满足固定长度的就是消息内容。方式简单，但是空间浪费，没满足固定长度的消息也要空到固定长度。
  * 分割符：分割符之间就是消息内容。实现简单不浪费空间，如果内容本身出现分隔符需要被转义。
  * 固定长度字段存个内容长度的信息：先解析固定长度的字段获取长度，然后读取响应长度的内容就是消息内容。精确定位消息，但是需要提前预知长度信息。
  * 通过JSON分割：{}内内容就是消息内容。实现简单，但是需要全部解析消息内容。

### Netty对封装成帧的支持

* FixedLengthFrameDecoder: 固定长度
* DelimiterBasedFrameDecoder: 固定分隔符
* LengthFieldBasedFrameDecoder: 固定长度字段存个内容长度的信息

**可控参数**

* FixedLengthFrameDecoder
  * final int frameLength: 固定长度

* DelimiterBasedFrameDecoder
  * final ByteBuf[] delimiters: 分隔符列表，支持多个。

* LengthFieldBasedFrameDecoder
  * lengthFieldOffset: 长度的值在消息中的偏移量。
  * lengthFieldLength: 长度值在消息中的长度。
  * lengthAdjustment: 消息正文开始的偏移量，如果长度和正文消息中间有一些固定长度的内容，真正的消息内容需要跳过这些内容。
  * initialBytesToStrip: 解码后的内容开始的偏移量。如果指定为lengthFieldOffset+lengthFieldLength+lengthAdjustment，那么编码后获取的就是消息正文。

## Netty的二次编解码

上面对于解决粘包和半包问题称为一次解码器，目的是将原始数据转换成消息数据。但是消息数据也是二进制的形式，需要通过二次编码器，转换成对应的Object。

* 一次编码器：ByteToMessageDecoder
  * io.netty.buffer.ByteBuf(原始数据) --> io.netty.buffer.ByteBuf(用户数据)

* 二次编码器：MessageToMessageDecoder\<T>
  * io.netty.buffer.ByteBuf(用户数据) --> Java Object

### 常用的二次编解码方式

* Java序列化: 不能跨语言使用，占用空间大。
* Marshaling
* XML: 可读性好，占用空间大。
* JSON
* MessagePack: 占用空间小，可读性差。
* Protobuf: 占用空间最小，可读性差。

**选择编解码的要点：** 编解码速度，编解码后的空间大小，是否追求可读性，对多语言的支持。

### Google Protobuf

* Protobuf是一个灵活的、高效的用于序列化数据的协议。
* 相比较XML和JSON格式，Protobuf更小、更快、更便捷。
* Protobuf是跨语言的，并且自带了一个编译器（protoc），只需要用它进行编译，就可以自动生成Java、python、等代码，不需要再写其他代码。但是生成的代码可读性不好，占用大。

### Netty对二次编解码的支持

Netty对编解码支持的包：io.netty.handler.codec

* Protobuf

  * ProtobufVarint32FrameDecoder: 对应的可变长度的编码器
  * ProtobufDecoder: 对应的二次编码器
  * ProtobufVarint32LengthFieldPrepender: 对应的可变长度的解码器
  * ProtobufEncoder: 二次解码器

  Netty对Protobuf的支持的一次编码器是通过可变长度的