# spring-clond笔记
> [服务注册与发现](#eureka)  使用 eureka--心跳机制

> 客户端负载均衡 Ribbon

> 声明式 Http Client Feign

> 微服务容错 

* 雪崩效应 一个依赖的服务发生问题导致系统不可用
* [熔断器](#Hystrix)  

> 微服务优点
+ 易于开发和维护
+ 启动较快
+ 局部修改容易部署
+ 技术栈不受限制
+ 按需伸缩
+ DevOps
> 挑战
+ 运维成本要求高 要运维多个应用
+ 分布式的复杂性 
+ 接口调整成本高 一个接口调整对应依赖就要调整
+ 重复劳动 不同开发语言不能通用util

<h2 id="eureka"> 服务注册与发现</h2>
1. 服务发现

+ 客户端发现  eureka zookeeper
+ 服务端发现  consul+nginx

    eurekat通过心跳检查、健康检查、 客户端缓存等机制,确保系统高可用性,灵活性和伸缩性
+ 客户端负载均衡 ribbon

<h2 id="Hystrix">断路器Hystrix</h2>

    在微服务架构中通常有多层服务调用,一个服务出错会产生级联错误，依赖它的服务都不能运行，断路器可以检测当服务出现错误超过阈值时断开服务由fallbackMethod 执行。断路器状态 关闭 打开 半开
> Hystrix Dashboard
  
    Hystrix仪表板以有效的方式显示每个断路器的运行状况。
> Turbine

    是将所有相关/hystrix.stream端点聚合到Hystrix仪表板中使用的/turbine.stream的应用程序。

> 路由器和过滤器 Zuul

> ### spring cloud config 
    
        分布式配置中心组件,它支持配置服务放在配置服务的内存中(本地)，也支持放在远程的git仓库中。在spring cloud config 组件中，分两个角色，一是config server，二是config client
        统一管理的好处：不同环境不同配置，运行期间动态调整配置，自动刷新

        http请求地址和资源文件映射如下:
            /{application}/{profile}[/{label}]
            /{application}-{profile}.yml
            /{label}/{application}-{profile}.yml
            /{application}-{profile}.properties
            /{label}/{application}-{profile}.properties
> spring config bus

        将分布式系统的节点与轻量级消息代理链接。这可以用于广播状态更改（例如配置更改）或其他管理指令

> 分布式系统CAP定理
    
     C-数据一致性；A-服务可用性；P-服务对网络分区故障的容错性，这三个特性在任何分布式系统中不能同时满足，最多同时满足两个


