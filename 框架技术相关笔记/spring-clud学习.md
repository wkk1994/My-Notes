# spring-clud学习
## 一、微服务简介
* **什么是微服务**  
将一个单一应用程序开发成一组小型服务的方式，每个服务独立运行，各服务间通过轻量级通信机制互相通信。

* **微服务的好处**  
  * 易于开发和维护
  * 单个服务启动较快
  * 技术栈不受限
  * 按需伸缩
* **面临的挑战**  
  * 运维要求高
  * 分布式固有的复杂性
  * 接口调整成本高
  * 容易重复劳动
* **微服务设计原则**
  * 单一责任原则
  * 服务治理原则
  * 轻量级通信机制
  * 微服务粒度

**<font color="blue" size="2px">拓展</font>：** 康威定律：组织形式等同于系统设计

## 二、Spring Cloud简介  
Spring Cloud是一个基于SpringBoot实现的微服务架构开发的工具。它为微服务架构中
涉及的 配置管理、服务治理、 断路器、 智能路由、微代理、 控制总线、 全局锁、 决策竞选、
分布式会话和集群状态管理等操作提供了一种简单的开发方式。  
SpringCloud包含多个子项目，如下：
* **Spring Cloud Config**: 配置管理工具， 支持使用Git存储配置内容， 可以使用它实现
应用配置的外部化存储，并支持客户端配置信息刷新、 加密／解密配置内容等。
* **Spring Cloud Netflix**: 核心组件，对多个Netflix OSS开源套件进行整合。
  * Eureka：服务治理组件，包含服务注册中心，服务注册与发现机制的实现。
  * Hystrix: 容错管理组件，实现断路器模式，帮助服务依赖中出现的延迟和为故障提供的强大的容错能力。
  * Ribbon： 客户端负载均衡的服务调用组件。
  * Feign： 基于Ribbon和Hystrix的声明式服务调用组件。
  * Zuul： 网关组件，提供智能路由、访问过滤等功能。
  * Archaius: 外部化配置组件。
* **Spring Cloud Bus**:事件、消息总线，用于传播集群中的状态变化或事件， 以触发后续的处理， 比如用来动态刷新配置等。
* **Spring Cloud Security**: 安全工具包， 提供在 Zuul 代理中对OAuth2 客户端请求的中继器。
* **Spring Cloud ZooKeeper**: 基于 ZooKeeper 的服务发现与配置管理组件。
* **...**
### 版本说明
[参考](https://blog.csdn.net/chen497147884/article/details/79896141)

## 三、Spring Boot简介
* **自定义参数**  
通过@Value("${book.name})获取yml中自定义的参数值
* **命令行参数**  
java -jar xxx.jar --server.port= 8888, 直接以命令行的方式来设置server.port属性， 并将启动应用的端口设为8888  
java -jar xxx.jar --spring.profiles.active=test，多环境配置，指定当前的环境
* **多环境配置**  
yml中通过以下指定一个test环境配置
    ```text
    
    ---
    spring:
      profiles: test
    server:
      port: 8082
    ```
* **属性加载顺序**  
1. 在命令行中传入的参数。
2. SPRING_APPLICATION_JSON中的属性。 SPRING_APPLICATION_JSON是以JSON格式配置在系统环境变量中的内容。
3. java:comp/env中的JNDI 属性。
4. Java的系统属性， 可以通过System.getProperties()获得的内容。
5. 操作系统的环境变量。
6. 通过random.*配置的随机属性。
7. 位于当前应用 jar 包之外，针对不同{profile}环境的配置文件内容，例如application-{profile}.properties或是YAML定义的配置文件。
8. 位于当前应用 jar 包之内 ，针对不同{profile}环境的配置文件内容，例如application-{profile}.properties或是YAML定义的配置文件。
9. 位于当前应用jar包之外的application.properties和YAML配置内容。
10. 位于当前应用jar包之内的application.properties和YAML配置内容。
11. 在@Configuration注解修改的类中，通过@PropertySource注解定义的属性。
12. 应用默认属性，使用SpringApplication.setDefaultProperties 定义的内容。

## 四、spring-boot-starter-actuator介绍
提供了应用程序的监控端点，通过url直接访问。
* **原生端点**  
根据端点的作用可以将原生端点分为三类：
  * **应用配置类**：获取应用程序中加载的应用配置、环境变量、自动化配置报告等与Spring Boot应用密切相关的配置类信息。
  * **度量指标类**：获取应用程序运行过程中用于监控的度量指标， 比如内存信息、线程池信息、HTTP请求统计等。
  * **操作控制类**：提供了对应用的关闭等操作类功能。

  * **应用配置类**：
     * /autoconfig  获取应用的自动化配置报告，其中包括所有自动化配置的候选项。
        * positiveMatches中返回的是条件匹配成功的自动化配置。
        * negativeMatches中返回的是条件匹配不成功的自动化配置。
    * /beans 获取应用上下文创建的所有Bean。
    * /configprops 获取应用中配置的属性信息报告。
    * /env  该端点与/configprops不同它用来获取应用所有可用的环境属性报告。 包括环境变量、NM属性、应用的配置属性、命令行中的参数。
    * /mappings: 该端点用来返回所有Spring MVC的控制器映射关系报告。
    * /info: 该端点用来返回一些应用自定义的信息。 默认清况下， 该瑞点只会返回一个空的JSON内容。我们可以在application.properties配置文件中通过info前缀来设置一些属性。
* **度量指标类**
    * **/metrics（重要）** 该端点用来返回当前应用的各类重要度量指标，比如内存信息、线程信息、垃圾回收信息等。可以通过/metrics/{name}接口来更细粒度地获取度量信息。
    * /health 获取应用的各类 健康指标信息。
    ```java
    /**
     * @Description 自定义健康指标
     * @Author wkk
     * @Date 2019-03-03 21:22
     **/
    @Component
    public class RocketMQHealthIndicator implements HealthIndicator {
        @Override
        public Health health() {
            int errorCode = check();
            if (errorCode != 0) {
                return Health.down().withDetail("Error Code",   errorCode).build();

            }
            return Health.up().build();
        }

        private int check() {
            return 0;
        }
    }
    //返回的json中会包含 "rocketMQ":{"status":"UP"}
    ```
    * /dump: 该端点用来暴露程序运行中的线程信息。
    * /trace: 该端点用来返回基本的 HTTP 跟踪信息。 默认情况下， 跟踪信息的存储采用org.springfrarnework.boot.actuate.trace.InMernoryTraceRepository实现的内存方式，始终保留最近的100条请求记录。

* **操作控制类**  
在原生端点中， 只提供了一个用来关闭应用的端点： /shutdown。默认关闭的，可通过endpoints.shutdown.enabled=true开启。只需要访问该应用的/shutdown端点就能实现关闭该应用的远程操作。危险操作需要加入安全验证。
## 五、服务注册与发现  
在微服务架构中，服务发现组件是一个非常关键的组件。  
> 服务注册表：是服务发现组件的核心，它用来记录各个微服务的信息，例如微服务的名称，ip，端口等。服务注册表提供查询API和管理API，查询API用户查询可用的微服务实例，管理API用于服务注册和注销。  

Spring Cloud提供了多种服务发现组件的支持，例如Eureka，Consul和Zookeeper等。
* **Eureka简介**  
是Netflix开源的服务发现组件，本身是一个基于REST的服务。它包含Server和Client两部分。Spring Cloud将它集成在子项目Spring Cloud Netflix中，从而实现微服务的注册与发现。  
注意点：
    * 微服务启动后，会周期性（默认30s）地向Eureka Server发送心跳以续约自己的租期。
    * 如果 Eureka Server在一定时间内没有收到某个微服务的心跳，Eureka Server会注销该实例（默认90s），Eureka Server有保护机制当失效实例过多时，不会直接剔除实例。
    * 默认情况下，Eureka Server也是Eureka Clinet。多个Eureka Server实例，互相之间通过复制的方式，来实现服务注册表数据的同步（**以此达到Eureka Server的高可用** ）。可通过
  ```eureka.client.register-with-eureka: false```
```eureka.clientfetch-registry: false```关闭客户端模式，只作为Server。

    Eureka通过心跳检查，客户端缓存等机制，提高了系统的灵活性，可伸缩性和可用性。
* **编写Eureka Server**
    * 引入依赖
    ```java
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-eureka-server</artifactId>
    </dependency>
    ```
    * 在启动类上加入@EnableEurekaServer注解，声明这是一个Eureka Server
    * 部分配置项说明：  
        * eureka.client.register-with-eureka: 表明是否将自己注册到Eureka Server，默认为true。  
        * eureka.clinet.fetch-registry: 表面是否从Eureka Server获取注册信息，默认为true。
        * eureka.clinet.service-url.defaultZone: 设置与eureka交互的地址，查询服务和注册服务都需要依赖这个地址。多个地址`,`分隔。
* **将微服务注册到Eureka Server上**
    * 引入依赖
    ```java
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-eureka</artifactId>
    </dependency>
    ```
    * 在启动类添加注解@EnableDiscoveryClient，声明这是一个Eureka Client，或者使用@EnableEurekaClient（只适用于Eureka作为服务治理组件）。
    * 部分配置说明：
        * spring.application.name：用于指定注册到Eureka Server上的应用名称。
        * eureka.instance.prefer-ip-address=true表明江自己的IP注册到Eureka Server。不配置或者为false，表示注册微服务所在操作系统的hostname到Eureka Server。

* **Eureka Server的高可用**  
Eureka Server可以通过运行多个实例并相互注册的方式实现高可用部署，Eureka Server实例会`彼此增量`地同步信息，从而确保所有的节点数据一致。

