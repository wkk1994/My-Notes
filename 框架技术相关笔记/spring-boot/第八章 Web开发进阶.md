# 第八章 Web开发进阶

## 如何设计好RESTful Web Service

“REST提供了⼀组架构约束，当作为⼀个整体来应⽤时，强调组件交互的
可伸缩性、接⼝的通⽤性、组件的独⽴部署、以及⽤来减少交互延迟、增
强安全性、封装遗留系统的中间组件。” - Roy Thomas Fielding

### Richardson 成熟度模型

![Richardson 成熟度模型](https://note.youdao.com/yws/api/personal/file/WEBf40bd9dc77b72862c9edc0d998d52abe?method=download&shareKey=461a5bb8309558a7e155815b4db68182)

分为四个级别

* LEVEL 0

  该模型的出发点是使用HTTP作为远程交互的传输系统，但是不会使用Web中的任何机制。所有的请求都是在同一个资源中通过POST完成的。

* LEVEL 1 - 资源

  LEVEL 1开始引入资源这一概念，所以相比将所有的请求发送到单个服务端点(Service Endpoint)，现在会和单独的资源进行交互。不同请求调用不同的资源，但是使用也是POST请求完成。

* LEVEL 2 - HTTP动词

  在LEVEL 0和LEVEL 1中一直使用的是HTTP POST来完成所有的交互，但是有些人会使用GET作为替代。在目前的级别上并不会有多大的区别，GET和POST都是作为隧道机制(Tunneling Mechanism)让你能够通过HTTP完成交互。LEVEL 2避免了这一点，它会尽可能根据HTTP协议定义的那样来合理使用HTTP动词。

* LEVEL 3 - 超媒体控制(Hypermedia Controls)

  Hypertext As The Engine Of Application State，缩写是：HATEOAS。
  超媒体控制(Hypermedia Control)的关键在于它告诉我们下一步能够做什么，以及相应资源的URI。它会在请求响应中，多返回一个`link`元素，表示可用的资源

**LEVEL 3的意义**

让服务有可发现性，可以使用协议拥有自我描述的能力。在我看来现在使用RESTful的开发方式中没有使用。可发现性也意味着API接口的暴漏。

### 如何实现 Restful Web Service

* 识别资源
* 选择合适的资源粒度
* 设计 URI
* 选择合适的 HTTP ⽅法和返回码
* 设计资源的表述

**识别资源：**

找到领域名词、能⽤ CRUD 操作的名词、将资源组织为集合（即集合资源）、将资源合并为复合资源、计算或处理函数。

**资源的粒度：**

在服务器端，要考虑的是网络效率、表述的多少、客户端的易用性；
在客户端，要考虑的是可缓存性、修改频率、可变性。

**构建更好的 URI：**

* 使⽤域及⼦域对资源进⾏合理的分组或划分
* 在 URI 的路径部分使⽤斜杠分隔符 ( / ) 来表示资源之间的层次关系
* 在 URI 的路径部分使⽤逗号 ( , ) 和分号 ( ; ) 来表示⾮层次元素，这个很少使用
* 使⽤连字符 ( - ) 和下划线 ( _ ) 来改善⻓路径中名称的可读性
* 在 URI 的查询部分使⽤“与”符号 ( & ) 来分隔参数
* 在 URI 中避免出现⽂件扩展名 ( 例如 .php，.aspx 和 .jsp )

**选择合适的HTTP方法和返回码：**

![HTTP方法](https://note.youdao.com/yws/api/personal/file/WEB6654ca2a7934d035ea20ea1b3ea04a22?method=download&shareKey=6a7ee3fd7b0b3278317eab4053a399de)

![HTTP状态码](https://note.youdao.com/yws/api/personal/file/WEBcd033bd68f773a3965ee0bd0d6ce9bf1?method=download&shareKey=f851b2f11174915f23e03b8aac034372)

**选择合适的表述：**

* JSON
  * MappingJackson2HttpMessageConverter
  * GsonHttpMessageConverter
  * JsonbHttpMessageConverter
* XML
  * MappingJackson2XmlHttpMessageConverter
  * Jaxb2RootElementHttpMessageConverter
* HTML
  * ProtoBuf
  * ProtobufHttpMessageConverter

## HATEOAS

HATEOAS的全称是Hybermedia As The Engine Of Application State，属于LEVEL 3模型中REST统一接口的必要组成部分

**HATEOAS和WSDL**

HATEOAS表述中的超链接会提供所需的各种RESTT接口的信息，无需事先预定如何访问服务；WSDL是传统的服务契约，必须事先约定服务的地址与格式。

HATEOAS 示例：

![HATEOAS 示例](https://note.youdao.com/yws/api/personal/file/WEB0f636e9404a8fb683d219fa85e305028?method=download&shareKey=a8ddd4bf7405dadc1881bfaf3f1b07ca)

## 使⽤ Spring Data REST 实现简单的超媒体服务

空

## 分布式环境中如何解决 Session 的问题

* 代码位置 webresources/session-demo
* 参考 [Tomcat实现session共享(session 会话复制)](https://www.jb51.net/article/124641.htm)

分布式环境中解决Session问题常用的方式有：粘性会话（Sticky Session），会话复制（Session Replication）和集中会话（Centralized Session）。

### 粘性会话（Sticky Session）

粘性会话的原理是对来自同一用户的会话路由到同一个应用实例上。

* F5对会话保持的支持
  F5 BigIP支持多种的会话保持方法,其中包括:简单会话保持(源地址会话保持)､HTTP Header的会话保持,基于SSL Session ID的会话保持,I-Rules会话保持以及基于 HTTP Cookie的会话保持,此外还有基于SIP ID以及Cache设备的会话保持等,但常用的是简单会话保持,HTTP Header的会话保持以及 HTTP Cookie会话保持以及基于I-Rules的会话保持｡

* NginX对简单会话保持的支持

  ip_hash：每个请求按访问ip的hash结果分配，这样每个访客固定访问一个后端服务器，可以解决session 的问题。
  例如：
    upstream bakend {
        ip_hash;
        server 192.168.0.14:88;
        server 192.168.0.15:80;
    }
  
**存的一些问题是**：1.当其中一个应用实例异常停止时，这个服务器上的所有的请求会被转发到其他应用处理，session会丢失。2.简单会话保持存在的问题就在于当多个客户是通过代理或地址转换的方式来访问服务器时,由于都分配到同一台服务器上,会导致服务器之间的负载严重失衡｡

### 会话复制（Session Replication）

会话复制是由应用服务器提供的，一般来说应用服务器都支持这个特性，比如Tomcat、WebLogic之类的。但是在节点持续增多的情况下,会话复制带来的性能损失会快速增加.特别是当session中保存了较大的对象,而且对象变化较快时,性能下降更加显著，这种特性使得web应用的水平扩展受到了限制。

### 集中会话（Centralized Session）

集中会话是将会话信息存储到 使用redis，Mongodb，jdbc等来实现。

### 通过Spring Session实现集中会话

Spring Session是Spring提供的用于管理用户会话信息的API和实现，它简化了集群中的用户会话管理，不需要绑定特定容器的解决方案。支持的容器有：Redis、MongoDB、JDBC、Hazelcast。[官方文档](https://docs.spring.io/spring-session/docs/2.2.1.BUILD-SNAPSHOT/reference/html5/)

* Spring Session的实现原理：
  * 通过定制的HttpServletRequest 返回定制的 HttpSession
    * SessionRepositoryRequestWrapper
    * SessionRepositoryFilter
    * DelegatingFilterProxy

* 基于 Redis 的 HttpSession
  * 引入spring-session-data-redis依赖
  * 基本配置
    * 在启动类上添加@EnableRedisHttpSession注解
    * 提供 RedisConnectionFactory
    * 实现 AbstractHttpSessionApplicationInitializer
      * 配置 DelegatingFilterProxy

* Spring Boot 对 Spring Session 的⽀持
  * application.properties
    * spring.session.store-type=redis
    * spring.session.timeout=
      * server.servlet.session.timeout=
    * spring.session.redis.flush-mode=on-save
    * spring.session.redis.namespace=spring:session

## 使⽤ WebFlux 代替 Spring MVC

* 什么是 WebFlux

  ⽤于构建基于 Reactive 技术栈之上的 Web 应⽤程序；基于 Reactive Streams API ，运⾏在⾮阻塞服务器上。

* 为什么会有 WebFlux

  对于⾮阻塞 Web 应⽤的需要；对于⾮阻塞 Web 应⽤的需要。

* 关于 WebFlux 的性能

  请求的耗时并不会有很⼤的改善；仅需少量固定数量的线程和较少的内存即可实现扩展。

* WebMVC v.s. WebFlux
  * 已有 Spring MVC 应⽤，运⾏正常，就别改了
  * 依赖了⼤量阻塞式持久化 API 和⽹络 API，建议使⽤ Spring MVC
  * 已经使⽤了⾮阻塞技术栈，可以考虑使⽤ WebFlux
  * 想要使⽤ Java 8 Lambda 结合轻量级函数式框架，可以考虑 WebFlux