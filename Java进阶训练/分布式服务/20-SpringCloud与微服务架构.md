# 分布式服务-SpringCloud与微服务架构

## 1.微服务架构发展历程*

### 信息化的发展**

（1999-2008）电子信息化 —-> （2008-2014）网络移动化 --> （2014-至今）数字智能化

电子信息化：将原先纸质的数据全部通过it实现信息存储。
网络移动化：随着手机的发展，将业务发展到手机上，通过手机就可以完成各种业务操作。
数字智能化：以往只能通过历史的经验进行新业务的拓展，现在可以通过积累的信息，根据数据驱动业务变化。

随着业务的越来越复杂、数据越来越多，系统对性能、稳定性、一致性、可用性、扩展性、可维护性，要求越来越高。

**这些需求和当前的架构能力的限制之间的矛盾在推动着软件架构的演进。**

### 软件架构的发展

单体架构 --> 垂直架构 --> SOA架构 --> 微服务架构

单体架构：最简单的架构风格。所有代码都在一个项目中，开发人员可以随时修改任意一段代码或者新增一些代码。
垂直架构：分层是一个典型的对复杂系统进行结构化思考和抽象聚合的通用性解决办法。MVC是一个常见的3层结构架构模式。
SOA架构：面向服务架构，是一种建设企业IT生态系统的架构指导思想。SOA的关注点是服务。服务最基本的业务功能单元，由平台中立性的接口契约来定义。
微服务架构：微服务架构风格，以实现一组微小服务的方式来开发一个独立的应用系统的方法。其中每个小微服务都运行在自己的进程中，一般采用HTTP资源API这样轻量的机制相互通信。

**SOA和微服务的区别**

微服务是SOA的延续，SOA更适合大型企业的业务应用，对服务的划分更粗粒度。而微服务的目的是有效的拆分应用，对服务的划分更加细粒度。

**微服务的优点：**

* 异构性：每个服务都可以使用不同的语言、存储来实现。
* 弹性：一个组件不可用时，不会影响整个系统。
* 扩展性：单体服务不容易扩展，微小的服务容易扩展，可以按需进行扩展。
* 易于部署：相比单体服务，微小服务的部署更加容易。
* 与组织架构对齐：组织架构决定系统架构。

**微服务的代价：**

* 分布式系统更加复杂。
* 开发、测试更加复杂。
* 部署、监控等运维复杂性。

### 响应式微服务

响应式微服务来源于响应式编程。响应式编程是一个专注于**数据流和变化传递的异步编程**范式。

响应式系统具备的特性：即时响应性（Responsive）、回弹性（Resilient）、弹性（Elastic）以及消息驱动（Message Driven）。

即时响应性：只要有可能，系统就会及时做出响应。
回弹性：系统在出现失败时依然保持即时响应性，这就需要单个系统在出现部分失败或故障时，不会危机整个系统。
弹性：系统在不断变化的工作负载之下依然保持即时响应性，要求系统可以应对突然大量的请求。
消息驱动：反应式系统依赖异步的消息传递，从而确保了松耦合、隔离、位置透明的组件之间有着明确边界。

[反应式宣言](https://www.reactivemanifesto.org)

### 服务网格与云原生

**什么是云原生？**

*云原生技术有利于各组织在公有云、私有云和混合云等新型动态环境中，构建和运行可弹性扩展的应用。-- CNCF Cloud Native Definition v1.0*

关于云原生的定义不同厂商有不同的定义，云原生不能称为一种架构，它是一种设计哲学，运行在云原生基础设施上的应用称作云原生应用，只有符合云原生设计哲学的应用架构才叫做云原生应用架构。云原生设计哲学改变了应用程序和基础设施之间的关系，让业务变的更加纯粹，只需要关注于业务开发，与业务无关的能力解耦到基础设施中，提高了交付速度。

**云原生技术**

云原生技术从纵向看，可以分为应用架构、生命周期管理、流量管理和基础设施及依赖四个维度，应用架构层的常用方式有微服务和12 Factor Apps，生命周期管理的常用的是容器技术，流量管理常用的Service Mesh（服务网格）技术，基础设施及依赖常用的有BaaS、GitOps/IaC。

**云原生应用**

Pivotal公司定义云原生应用需要满足的四要素：

![云原生应用四要素](https://note.youdao.com/yws/api/personal/file/WEB09c8c6b1946f6865104baf45cbf3c296?method=download&shareKey=5ec2e99c423e6f4f34a216b11e7e6d97)

* DevOps：开发与运维一同致力于交付高品质的软件服务于客户。
* 持续交付：软件的构建、测试和发布，要更快、更频繁、更稳定。
* 微服务：以一组小型服务的形式来部署应用。
* 容器：提供比传统虚拟机更高的效率。

**服务网格**

Service Mesh的本质是管理流量，服务在向外提供服务和服务之间进行网络通信时，都涉及流量的接收和发送，在这个过程中如何管理服务发现、流量路由规则等都使用Service Mesh 技术。一般Service Mesh的特点有应用程序间通讯的中间层、轻量级网络代理、应用程序无感知、解耦应用程序的重试/超时、监控、追踪和服务发现。

![Service Mesh架构](https://note.youdao.com/yws/api/personal/file/WEB69f3104b59f85de8a57c0e4887aef9e3?method=download&shareKey=f77e4bbd249099ea947f36ff19a38b5d)

Service Mesh的总体架构上分为数据层或数据平面（data plane）和控制层或控制平面（control plane）。

数据层是由多个相互连接的sidecar（边车）组成，sidecar（边车）是一个代理的网络，一般这些代理被注入到每个服务部署中，服务不直接通过网络调用服务，而是通过调用本地的sidecar（边车）实现服务请求，sidecar封装了服务间通信的复杂性。

控制层是就是控制和管理数据层的sidecar代理，完成配置的分发、服务发现和授权鉴权等功能。

Service Mesh的实现有Istio、Linkderd。

> 后端服务：redis、mysql等都是资源。
> 通过微服务、容器化、持续交付、Devops等技术，组成了所谓的“元原生”体系。
> [Service Mesh参考](https://www.servicemesher.com/istio-handbook/concepts/overview.html)

### 数据库网格

数据库网格和服务网格类似，服务在访问数据库时，不直接访问数据库，而是通过数据库网格的sidecar访问数据库，sidecar封装了数据库的分库分表等逻辑。

![数据库网格.png](https://note.youdao.com/yws/api/personal/file/WEBe965f43341ad7bb20c3386877d5fa1b4?method=download&shareKey=16ac78539f2958da5e453e4610601fd8)

### 单元化架构

![单元化架构.png](https://note.youdao.com/yws/api/personal/file/WEBf7557a7562b3601b2c3dba750ab4d5eb?method=download&shareKey=fc3bd0a6514beadd7c480b5501fc449b)

以单元为组织架构，以单元为调度单元。
每个单元都是一个缩小版的整站，拥有全部功能；但是它不是全量的，只接受一部分流量操作。

好处：能够单元化的系统，很容易在多机房中部署，因为可以轻易地把几个单元部署在一个机房，而把另外几个部署在其他机房。通过在业务入口处设置一个流量调配器，可以调整业务流量在单元之间的比例。

> 银行热衷于单元化架构的原因：单元化架构过程中，将所有的基础设施软件放到PASS中，可以趁机做基础软件的国产化；单元化部署，对硬件的要求已经不高了，通过大机下移实现硬件国产化。（去IOE）
> 银行有采购国产化硬件的指标。

## 2.微服务架构应用场景*

### 什么时候用微服务

微服务应用在复杂度低的情况下，生产力反而比单体架构低。在复杂度高的情况下，情况恰恰相反。随着复杂度升高，单体架构的生产力快速下降，而微服务相对平稳。

> 微服务不适合在一开始创建应用的时候就使用。

### 怎么应用微服务-I6I

![应用微服务架构-I6I](https://note.youdao.com/yws/api/personal/file/WEB981793867fc244bf144dd2aec9350e65?method=download&shareKey=c2d1ad03e676eaff5eb519cc27a0244a)

调研：前期对系统整体架构进行分析调研。
分析：对每个系统进行具体分析清除。
规划：根据前面的分析结果，对现状进行规划，先拆分哪个系统、如果拆分。
组织：组织相关人员参与到微服务改造中，对改造方案进行研讨。
拆分：根据规划，对系统进行拆分。
部署：拆分完成后进行测试部署。
治理：对拆分的系统进行监控，收集信息，是否符合拆分后的预期效果。
改进：对上次的拆分进行改进，继续下一个循环。

通过多次的循环，系统渐渐演进成符合当前团队开发和维护的状态。

## 3.微服务架构最佳实践*

六大最佳实践

### 遗留系统改造

大部分进行微服务系统改造都是遗留系统。

* 功能剥离、数据解耦

  不拆分数据意义不大。

* 自然演进、逐步拆分

  在需要拆分的时候才进行拆分。

* 小步快跑、快速迭代

  将系统一小部分一小部分进行拆分，避免一次性拆分带来的工作量。

* 灰度发布、谨慎试错
* 提质量线、还技术债

### 恰当粒度拆分

拆分原则：

* 高内聚低耦合
* 不同阶段拆分要点不同

### 扩展立方体

根据系统特征选择合适的扩展方式。

1.水平复制：复制系统
2.功能解耦：拆分业务
3.数据分区：切分数据

比较推荐的实践：增加特性开关和容错设计。通过在线上让一部分流量先走新的应用，如果出现错误，及时关闭新特性开关，避免出现大批量的问题。测试一段时间通过后，再将所有流量走新应用。

### 自动化管理

系统进行微服务化改造后，服务增多，运维测试更加复杂。可以通过自动化测试、自动化部署、自动化运维来提升测试、部署、运维效率。

### 分布式事务

通过幂等、去重、补偿等方式实现分布式事务，一般情况下谨慎使用分布式事务。

### 完善监控体系

* 技术指标监控
* 业务指标监控
* 容量规划
* 报警预警
* 运维流程：标准的上线流程一般称为SOP。
* 故障处理：故障报告，COE。

## 4.Spring Cloud技术体系*

### Spring Cloud简介

Spring Cloud为常见的分布式系统的模式，提供了一致性的编程模型（比如提抽象了服务注册与发现的接口DiscoveryClient，这样不论服务注册与发现使用Eureka或Zookeeper都可以使用一致的接口），可以方便开发人员快速构建分布式系统。

### Spring Cloud的主要功能和组件

* 服务注册和发现

  Eureka（Netflix不再开源）、Zookeeper、Consul、Nacos。

* 服务熔断和降级

  服务熔断的目的是在服务或网络不好时，自动进行服务降级，对服务进行保护。常见的熔断器有Hystrix、Resilience4j、Alibaba Sentinel。
  Hystrix（Netflix不再开源）是Netflix开源的，但是已经停止维护了。
  Resilience4j是一个轻量级和易于使用的熔断库，借鉴了Hystrix的实现，但是是专门为了Java8和函数式编程而设计的。

* 配置服务

  常用的配置中心有可以通过Spring Cloud Config从远程获取配置（支持git、svn），Zookeeper、Consul、Nacos、Aapollo。

* 服务调用

  Feign（Netflix不再开源）可以作为HTTP Client访问REST服务接口，可以和ribbon、hytrix等组件联合使用。
  OpenFeign是Spring Cloud在Feign的基础上支持了SpringMVC的注解。

* 负载均衡

  Ribbon提供了客户端的负载均衡，有多种负载均衡策略，多协议支持(HTTP, TCP, UDP)。

* 服务安全

  Spring Cloud Security

* 服务网关

  常见的网关：Zuul（Netflix不再开源）、Zuul2、Spring Cloud Gateway；Zuul属于BIO，Zuul2基于Netty实现的NIO，Spring Cloud Gateway基于Webflux实现的NIO。

* 分布式消息

  Spring Cloud Stream：用于构建消息驱动的微服务应用程序的轻量级框架。它的特性有声明式编程模型、支持多个概念抽象（发布订阅、消费组、分区）、支持多种消息中间件（RabbitMQ、Kafka、RocketMQ）。

* 分布式跟踪

  Spring Cloud Sleuth为分布式跟踪提供了SpringBoot的自动配置，默认将跟踪信息输出到日志中，一般包括[appname，traceId，spanId，exportable]，也可以和Zipkin联合使用，将跟踪信息上传到Zipkin中。

* 各种云平台的支持

服务安全：Spring Cloud Ser

## 5.微服务相关框架与工具

### 分布式跟踪和应用监控

APM：应用性能监控，常用的框架：

* Apache Skywalking
* Pinpoint
* Zipkin
* Jaeger

![APM工具对比.png](https://note.youdao.com/yws/api/personal/file/WEBd9120d08af3daf599df576db1f62c65b?method=download&shareKey=59af718b9d415235d8809ce38a289312)

> 大多数分布式追踪系统的思想模型都来自[Google's Dapper论文](http://bigbully.github.io/Dapper-translation/)，OpenTracing也使用相似的术语。

监控常用的框架：

* ELK：将系统日志输出到ELK中进行分析展示。
* promethus+Grafana：日志输出到promethus，再通过Grafana进行展示。promethus中已经包含了一个时序数据库。
* MQ+时序数据库(InfluxDB/openTSDB等)。

对于应用的可观测性一般有三个维度：Logging、Tracing、Metrics。
对应的规范有OpenTracing、OpenSensus、OpenTelemetry。
OpenTracing和OpenSensus一直是竞争关系，OpenTracing只是定义了Tracing的规范，而OpenSensus定义了Tracing和Metrics的规范，OpenTelemetry是两者一起制定的新规范。

### 权限控制

权限控制最核心的就是3A，除此之外还有资源管理、安全加密等。

* Authc: Authentication，认证。
* Authz: Authorization，授权。
* Audit：审计。

常用框架SpringSecurity, Apache Shiro，推荐使用Shiro。

### 数据处理

1、读写分离与高可用HA：
2、分库分表Sharding：
3、分布式事务DTX：
4、数据迁移Migration：
5、数据集群扩容Scaling：
6、数据操作审计Audit：

## Tips

* istio是service mesh的实施标准，k8s是容器编排的实施标准。
* 中台 --> 基础软件国产化；大机下移 --> 硬件国产化；这可能是未来银行的趋势，逐渐国产化。
* mainframe
* Oracle一体机，一台2、3千万，超高的性能、TPS。
* SOP：标准操作流程。
* COE：根因分析，来源于丰田。
* 制度管理人，流程管理事，流程规范化效率低但是不容易出问题，按照流程来出了问题不需要承担责任。
* 人的因素处理人，组织的因素组织买单。
* TSF：腾讯对外输出的一套微服务技术框架。
* 基于QPS或TPS的流量限制可能出现不公平的情况，比如一个查询接口和一个订单处理接口，订单处理接口需要1s，查询接口处理很快，如果基于访问次数进行限流，可能出现请求成功的大部分都是查询接口。
* SSO：单点登录系统。
* rsocket：阿里推出的。
* REST与其他协议之争（websocket/actor/rsocket/mq...）
* 波测
* QUIC：谷歌制定的一种基于UDP的低时延的互联网传输层协议。

## 参考

* [云原生时代，应用架构将如何演进？](https://developer.aliyun.com/article/776786?utm_content=g_1000197929)
* [云原生的设计哲学](https://www.bookstack.cn/read/kubernetes-handbook-201910/cloud-native-cloud-native-philosophy.md)
* [Service Mesh参考](https://www.servicemesher.com/istio-handbook/concepts/overview.html)