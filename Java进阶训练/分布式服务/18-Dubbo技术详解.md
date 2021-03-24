# 分布式服务-Dubbo技术详解

## 1.Dubbo框架介绍*

### Dubbo的发展历史

开源期（2011-2013，横空出世）：Dubbo是阿里巴巴B2B开发的，2011年开源。
沉寂期（2013-2017，潜龙在渊）：2013年到2017年，dubbo维护程度很低。
复兴期（2017-2019，朝花夕拾）：2017年8月份重启维护，2018年2月加入Apache孵化器，2019年5月顺利毕业。

Dubbo产生于阿里巴巴的B2B实际业务需求，随着B2B退市，淘系的HSF的使用，导致Dubbo停滞。

> 在Apache孵化器中的项目，一般一年半就会毕业，如果一年半不能毕业基本上就很难毕业了。
> 当当、京东等公司的服务化都是基于Dubbo实现的，四大行都有使用。当当的DubboX，京东的jsf。
> Dubbo和Spring Cloud的关系类似于IE浏览器与Chrome浏览器的故事。

### Dubbo的主要功能

Apache Dubbo是一个高性能、轻量级的开源的Java服务框架。

六大核心能力：

* 面向接口代理的高性能RPC调用
  提供高性能的基于代理的远程调用能力，服务以接口为粒度，为开发者屏蔽远程调用底层实现细节。
* 智能负载均衡
  内置多种负载均衡策略，智能感知下游节点的健康状况，显著减少调用延迟，提高系统吞吐量。
* 服务自动注册与发现
  支持多种注册中心，服务上线下线实时感知。
* 高度可扩展能力
  遵循微内核+插件的设计原则，所有核心能力，如Protocol、Transport、Serialization被设计为扩展点，平等对待内置实现和第三方实现。
* 运行期流量调度
  内置条件、脚本等路由策略，通过配置不同的路由规则，轻松实现灰度发布，同机房优先等功能。
* 可视化的服务治理与运维
  提供丰富的服务治理、运维工具：随时查询服务元数据、服务健康状态及调用统计，实时下发路由策略、调整配置参数。**可视化操作功能不是很强**

**Dubbo和ESB的区别**

ESB全称是企业服务总线，将企业中所有的服务管理起来，包括服务注册、查找ESB、数据传输等，服务之间的调用也需要通过ESB来进行。比较流行的商业ESB有IBM的WMB，oracle的OSB，开源的ESB有jbossESB、openESB。

SOA是面向服务架构，常见的落地方式主要有分布式服务化和集中式管理两种。Dubbo和ESB都是SOA架构的实现，不同的是Dubbo是分布式服务化技术，服务之间的调用通过服务注册发现组件获取后，点对点进行调用，这样调用的效率高；而ESB中，服务之间的调用需要通过ESB来进行。

**Dubbo之所以叫做分布式服务化，ESB之所以叫做集中式的SOA**

**基础功能**

Dubbo的基础功能就是RPC调用，支持多协议（序列化、传输方式、RPC），服务注册发现，配置、元数据管理。

![dubboRPC调用](https://note.youdao.com/yws/api/personal/file/WEB3c2e81fe3a4163f04001da30175abefc?method=download&shareKey=38e077dd65a6fb9f296232e5a2217dee)

**扩展功能**

扩展功能：集群、高可用、管控

* 集群、负载均衡
* 治理、路由
* 控制台，管理与监控

> **灵活扩展+简单易用，是Dubbo成功的秘诀。**
> 内网之间的应用进行通信，一般使用http不如tcp效率更高。
> ESB的前身是银行的综合前置系统，银行的核心一般是不对外开放的，想要访问必须通过前置系统进行转发，慢慢就演化成了ESB，现有的大部分银行还存有部分ESB系统。

## 2.Dubbo技术原理*

### 整体架构

![Dubbo整体架构设计](https://note.youdao.com/yws/api/personal/file/WEB52d5a7b25b5de149b79377c8f27d2405?method=download&shareKey=288f750f8f6629dde140f71587c5a32f)

Dubbo的核心技术都在上面的架构设计上了。

1. config 配置层：对外配置接口，以 ServiceConfig, ReferenceConfig 为中心，可以直接初始 化配置类，也可以通过 spring 解析配置生成配置类
2. proxy 服务代理层：服务接口透明代理，生成服务的客户端 Stub 和服务器端 Skeleton, 以 ServiceProxy 为中心，扩展接口为 ProxyFactory
3. registry 注册中心层：封装服务地址的注册与发现，以服务 URL 为中心，扩展接口为 RegistryFactory, Registry, RegistryService
4. cluster 路由层：封装多个提供者的路由及负载均衡，并桥接注册中心，以 Invoker 为中心， 扩展接口为 Cluster, Directory, Router, LoadBalance
5. monitor 监控层：RPC 调用次数和调用时间监控，以 Statistics 为中心，扩展接口为 MonitorFactory, Monitor, MonitorService
6. protocol 远程调用层：封装 RPC 调用，以 Invocation, Result 为中心，扩展接口为 Protocol, Invoker, Exporter
7. exchange 信息交换层：封装请求响应模式，同步转异步，以 Request, Response 为中心， 扩展接口为 Exchanger, ExchangeChannel, ExchangeClient, ExchangeServer
8. transport 网络传输层：抽象 mina 和 netty 为统一接口，以 Message 为中心，扩展接口为 Channel, Transporter, Client, Server, Codec
9. serialize 数据序列化层：可复用的一些工具，扩展接口为 Serialization, ObjectInput, ObjectOutput, ThreadPool

* Invoker是Dubbo的核心对象，Customer端的Invoker代表对一次服务的调用。Protocol中的Invoker代表具体的RPC协议；Provider中的Invoker表示对具体实现的调用。
* Protocol：协议，也是核心之一，远程调用使用的RPC的协议的封装。
* url：也是核心之一，url是用来表示服务的接口的信息。
* Filter：通过filter做增强的处理。

### 调用链

![dubbo框架设计](https://note.youdao.com/yws/api/personal/file/WEB8ff94221fd174ec0b5e27b27151557dc?method=download&shareKey=ba62c69dae871e82cc646690eb8c61a2)

### SPI的应用

Dubbo基于ServiceLoader机制，自己进行了一些改动实现了SPI的扩展机制。
META-INF/dubbo/接口全限定名，文件内容为实现类。

> **API和SPI的区别与联系**：API是应用提供的接口，给使用者进行调用和使用的；SPI是框架接口规范，需要框架开发人员实现。

### 服务如何暴露

// TODO

### 服务如何引用

// TODO

### 集群与路由

org.apache.dubbo.rpc.cluster.router.AbstractRouter
org.apache.dubbo.rpc.cluster.loadbalance.AbstractLoadBalance

// TODO

### 泛化调用

泛化接口调用一般用于客户端没有API接口和模型类的情况下，参数和返回值中所有的POJO都使用Map表示，通常用于框架集成，比如：实现一个通用的服务测试框架，可通过 GenericService 调用所有服务实现。

[使用参考](https://dubbo.apache.org/zh/docs/v2.7/user/examples/generic-reference/)

### 隐式传参

在Context模式下，使用方法RpcContext.getContext().setAttachment("index", "1")，可以将参数可以传播到RPC调用的整个过程。

在Http协议下通过添加Header头部实现，低侵入性，其他协议的实现就必要有侵入性。

## 3.Dubbo应用场景

### 分布式服务化改造

分布式服务化改造是Dubbo的主要应用场景，也是Dubbo出现的原因。

对于传统的系统规模复杂，进行垂直拆分改造，主要需要解决的问题有数据相关改造、服务设计、不同团队的配合（分布式服务化意味着需要进行人员的拆分）、开发、测试运维的安排。

### 开放平台

平台发展的两个模式有开发模式和容器模式。

开发模式：常见的方式是提供API的方式供用户进行调用，可以通过Dubbo实现API接口的调用或rest接口。
容器模式：类似于SPI的方式，提供应用运行的容器。

### 直接作为前端使用的后端（BFF）

直接作为BFF给前端（Web或Mobile）提供服务。一般不太建议这种用法。

### 通过服务化建设中台

将公司的所有业务服务能力，包装成API，形成所谓的业务中台。前端业务服务，各个业务线，通过调用中台的业务服务，灵活组织自己的业务。从而实现服务的服用能力，以及对于业务变化的快速响应。

## 4.Dubbo最佳实践*

### 开发分包

建议将服务接口、服务模型、服务异常等均放在API包中，因为服务模型和异常也是API的一部分，这样做也符合分包的原则：重用发布等价原则(REP)，共同重用原则 (CRP)。

服务的接口尽可能的粗粒度，每个服务方法代表一个功能，而不是功能中的某一步骤，否则将面临分布式事务问题，Dubbo暂未提供分布式事务的支持。

服务接口建议以业务场景为划分单位，并对相近业务进行抽象，防止接口数量过多。

不建议使用过于抽象的通用接口，如Map query(Map)，这样的接口没有明确语义，对后期维护带来不便。

### 环境的隔离与分组

对于多环境的Dubbo服务怎么做到环境隔离？

* 部署多套：简单有效的办法，适合Dubbo服务较少的应用。
* 多注册中心机制：通过将同一组的Dubbo的消费者和生产者注册到同一个注册中心实现环境的隔离。
* group机制：Dubbo在声明接口的时候可以指定group，不同的group直接互相隔离。
* 版本机制：Dubbo在接口声明的时候可以指定version，这个是推荐的方式，对于接口的升级可以使用version的机制实现渐进的升级。

> 服务接口增加方法，或服务模型增加字段，可向后兼容，删除方法或删除字段，将不兼 容，枚举类型新增字段也不兼容，需通过变更版本号升级。

### 参数配置

一般参数以consumer端为准，如果consumer端没有设置，使用provider对应的值，如果都没有使用各自的默认值。

建议在provider配置的consumer端属性有：

* timeout：方法调用的超时时间。
* retries：重试次数，默认值是2.
* loadbalance：负载均衡的算法，默认是random。
* actives：消费者端最大的并发调用限制，当一个consumer对一个服务的调用到上限后，新的调用会阻塞直到超时，可以配置在方法或服务上。

上面这些属性一般只有provider端才知道自己的服务能力，所以在provider端配置更合理。

建议在provider端配置的provider端属性有：

* threads：服务线程池大小。
* executes：一个服务提供者并行执行请求上限，即当provider对一个服务的并发调用达到上限后，新调用会阻塞，此时consumer可能会超时。可以配置在方法或服务上。

### 容器化部署

在容器化部署时，会有注册容器ip问题，容器内提供者使用的IP如果注册到注册中心，消费者无法访问。

两个解决方法：

* 1.docker使用宿主机的网络：在docker容器启动时，指定网络`docker xxx -net xxxxx`。
* 2.docker参数指定注册的IP和端口，-e。
  * DUBBO_IP_TO_REGISTRY：注册到注册中心的IP地址
  * DUBBO_PORT_TO_REGISTRY ：注册到注册中心的端口
  * DUBBO_IP_TO_BIND：监听IP地址
  * DUBBO_PORT_TO_BIND：监听端口

### 运维与监控

Admin功能较简单，大规模使用需要定制开发，整合自己公司的运维监控系统。

### 分布式事务

Dubbo不支持分布式事务，需要自己引入柔性事务（SAGA、TCC、AT）等方式实现。

### 重试与幂等

服务调用失败默认重试2次，如果接口不是幂等的，会造成业务重复处理。
如何设计幂等接口:

* 1、去重-->(bitmap --> 16M),100w。
* 2、乐观锁机制：使用version实现乐观锁机制。

## 5.如何看Dubbo源码

调试Dubbo代码：

* provider看Protocol的export；
* consumer看ReferenceConfig；
* provider的执行逻辑看Protocol的handler。

## 参考

* [SOA架构与落地方式](https://zhuanlan.zhihu.com/p/97815644)
* [企业服务总线(ESB)和注册服务管理(dubbo)有什么区别？](https://www.zhihu.com/question/309621272/answer/577800728)

## Tips

* filter和intereptor的区别：filter一般不中断处理，interceptor可以进行中断。
* 服务：业务语义的东西，这里的服务指的是封装良好的一套业务API。这套API接口能完成一套业务的流程。
