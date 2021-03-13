# 第十章 运行中的Spring Boot

## SpringBoot中的各类Actuator Endpoint

https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/reference/html/production-ready-features.html

* Spring Boot-Actuator简介

  SpringBoot-Actuator项目可以监控并管理Spring Boot应⽤，比如健康检查、审计、统计和Http追踪。这些特性可以通过JMX或者HTTP endpoints来获得。Actuator同时还可以与外部应用监控系统整合，比如 Prometheus, Graphite, DataDog, Influx, Wavefront, New Relic等。这些系统提供了非常好的仪表盘、图标、分析和告警等功能，使得你可以通过统一的接口轻松的监控和管理你的应用。

* 访问方式：HTTP或者JMX

* 依赖项：spring-boot-starter-actuator

### 常用的Endpoint

|id|说明|默认开启|默认HTTP|默认JMX|
|--|---|-------|-------|-------|
| beans | 显示容器中的Bean列表 | Y | N | Y |
| caches | 显示应用中的缓存（spring-boot-cache中的缓存）| Y | N | Y |
| conditions | 显示配置条件的技术情况 | Y | N | Y |
| configprops | 显示@ConfigurationProperties的信息 | Y | N | Y |
| env | 显示ConfigurableEnvironment中的属性 | Y | N | Y |
| health | 显示健康检查信息 | Y | Y | Y |
| httptrace | 显示HTTP Trace信息(默认追踪100个请求) | Y | N | Y |
| info | 显示设置好的应用信息 | Y | Y | Y |
| loggers| 显示并更新⽇志配置 | Y | N | Y |
| metrics| 显示应⽤的度量信息 | Y | N | Y |
| mappings| 显示所有的 @RequestMapping 信息 | Y | N | Y |
| scheduledtasks| 显示应⽤的调度任务信息 | Y | N | Y |
| shutdown| 优雅地关闭应⽤程序 | N | N | Y |
| threaddump| 执⾏ Thread Dump | Y | N | Y |
| heapdump| 返回 Heap Dump ⽂件，格式为 HPROF | Y | N | N/A |
| prometheus| 返回可供 Prometheus 抓取的信息 | Y | N | N/A |

## 如何访问Actuator Endpoint

* HTTP访问
  * /actuator/<id>
* 端口与路径
  * management.server.address=
  * managerment.server.port=
  * managerment.endpoints.web.base-path/actuator
  * management.endpoints.web.path-mapping.<id>=路径
* 开启 Endpoint
  * management.endpoint.<id>.enabled=true #开启知道endpoint
  * management.endpoints.enabled-by-default=false #是否默认开启endpoint
* 暴露 Endpoint
  * management.endpoints.jmx.exposure.exclude= #排除指定endpoint
  * management.endpoints.jmx.exposure.include=* #暴露指定endpoint
  * management.endpoints.web.exposure.exclude= #排除指定endpoint
  * management.endpoints.web.exposure.include=info, health #暴露指定endpoint

## Health Indicator

### Spring Boot ⾃带的 Health Indicator

检查应用程序的运行状态。

* 状态：
  * DOWN - 503
  * OUT_OF_SERVICE - 503
  * UP - 200
  * UNKNOWN - 200

* 实现机制：通过 HealthIndicatorRegistry 收集信息；HealthIndicator 实现具体检查逻辑
* 配置项
  * management.health.defaults.enabled=true|false
  * management.health.<id>.enabled=true
  * management.endpoint.health.show-details=never|whenauthorized|always
* Spring Boot ⾃带的 Health Indicator

CassandraHealthIndicator ElasticsearchHealthIndicator MongoHealthIndicator SolrHealthIndicator
CouchbaseHealthIndicator InfluxDbHealthIndicator Neo4jHealthIndicator
DiskSpaceHealthIndicator JmsHealthIndicator RabbitHealthIndicator
DataSourceHealthIndicator MailHealthIndicator RedisHealthIndicator

### ⾃定义 Health Indicator

* 实现 HealthIndicator 接⼝
* 根据⾃定义检查逻辑返回对应 Health 状态
  * Health 中包含状态和详细描述信息

示例：

```java
/**
 * @Description 自定义健康检查
 * @Date 2020/5/7 21:22
 */
@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Autowired
    private CoffeeService coffeeService;

    /**
     * Return an indication of health.
     *
     * @return the health
     */
    @Override
    public Health health() {
        List<Coffee> allCoffee = coffeeService.findAllCoffee();
        int count = allCoffee.size();
        if(count > 0) {
            return Health.up().withDetail("count", count)
                    .withDetail("message", "We have enough coffee.").build();
        }else {
            return Health.down().withDetail("count", count)
                    .withDetail("message", "We are out of coffee.").build();
        }
    }
}
```

## 通过 Micrometer 获取运⾏数据

### Micrometer 简介

Micrometer为Java平台上的性能数据收集提供了一个通用的API，应用程序只需要使用Micrometer的通用API来收集性能指标即可。Micrometer会负责与不同监控系统的适配工作。这就使得切换监控系统变得很容易。Micrometer 还支持推送数据到多个不同的监控系统。类似于SLF4。

### 特性

* 多维度度量
  * 支持Tag
* 预置大量探针
  * 缓存、类加载器、GC、CPU利用率、线程池...
* 于Spring深度整合
* 支持多种监控系统

### Micrometer in Spring Boot 2.x

Spring Boot支持的指标：

* 核⼼度量项
  * JVM、CPU、⽂件句柄数、⽇志、启动时间
* 其他度量项
  * Spring MVC、Spring WebFlux
  * Tomcat、Jersey JAX-RS
  * RestTemplate、WebClient
  * 缓存、数据源、Hibernate
  * Kafka、RabbitMQ

### ⾃定义度量指标

* 通过 MeterRegistry 注册 Meter
* 提供 MeterBinder Bean 让 Spring Boot ⾃动绑定
* 通过 MeterFilter 进⾏定制

## Spring Boot Admin

Spring Boot Admin不是Spring团队提供的模块，它为actuator端点提供了良好的交互界面，并提供了额外的特性。Spring Boot Admin由两部分组成：Client和Server。

Server部分包括Admin用户界面并独立运行于被监控的应用。Client部分是包含在被监控应用中，并注册到Admin Server。

* 服务端
  * 依赖：de.codecentric:spring-boot-admin-starter-server:2.1.3
  * 启动类上添加@EnableAdminServer注解
* 客户端
  * de.codecentric:spring-boot-admin-starter-client:2.1.3
  * 配置服务端及Endpoint
    * spring.boot.admin.client.url=http://localhost:8080
    * management.endpoints.web.exposure.include=*

* 安全控制
  * 依赖：spring-boot-starter-security
  * 服务端配置
    服务端需要指定自己的用户名密码：`spring.security.user.name` `spring.security.user.password`
  * 客户端配置
    spring.boot.admin.client.username
    spring.boot.admin.client.password
    spring.boot.admin.client.instance.metadata.user.name
    spring.boot.admin.client.instance.metadata.user.password

## 定制Web容器参数

* 可选容器列表
  * spring-boot-starter-tomcat（默认）
  * spring-boot-starter-jetty
  * spring-boot-starter-undertow
  * spring-boot-starter-reactor-netty

* 容器配置
  * 端⼝
    * server.port
    * server.address
  * 压缩
    * server.compression.enabled
    * server.compression.min-response-size
    * server.compression.mime-types
  * Tomcat 特定配置
    * server.tomcat.max-connections=10000
    * server.tomcat.max-http-post-size=2MB
    * server.tomcat.max-swallow-size=2MB
    * server.tomcat.max-threads=200
    * server.tomcat.min-spare-threads=10
  * 错误处理
    * server.error.path=/error
    * server.error.include-exception=false
    * server.error.include-stacktrace=never
    * server.error.whitelabel.enabled=true
  * 其他
    * server.use-forward-headers
    * server.servlet.session.timeout

* 编程方式配置参数
  * WebServerFactoryCustomizer<T>
    * TomcatServletWebServerFactory
    * JettyServletWebServerFactory
    * UndertowServletWebServerFactory

  ```java
    @SpringBootApplication
    @EnableJpaRepositories
    @EnableCaching
    public class WaiterServiceApplication implements
        WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

        public static void main(String[] args) {
            SpringApplication.run(WaiterServiceApplication.class, args);
        }

        @Override
        public void customize(TomcatServletWebServerFactory factory) {
            Compression compression = new Compression();
            compression.setEnabled(true);
            compression.setMinResponseSize(DataSize.ofBytes(512));
            factory.setCompression(compression);
        }
    }
  ```

## SpringBoot编写命令行运行的程序

怎么开发一个命令行运行的工具：
* 控制依赖：不添加Web相关的依赖
* 配置方式：`spring.main.web-application-type=none`
* 编程方式实现：
  * SpringApplication
    * setWebApplicationType()
  * SpringApplicationBuilder
    * web()

  核心都是在调⽤ SpringApplication 的 run() ⽅法前设置 WebApplicationType

* 常⽤⼯具类

  * 不同的 Runner
    * ApplicationRunner
      * 参数是 ApplicationArguments
    * CommandLineRunner
      * 参数是 String[]
  * 返回码
    * ExitCodeGenerator

## 可执行的Jar

### 可执行jar

可执行jar指的是可以使用`java -jar`运行的jar。

**可执行的jar主要包含的内容：**

* Jar描述，META-INFO/MANIFEST.MF
* Spring Boot Loader, org/springframework/boot/loader
* 项目内容，BOOT-INF/classes
* 项目依赖，BOOT-INF/lib

**如何找到程序的⼊⼝：**

* Jar的启动类：配置在MANIFEST.MF中的`Main-Class`，例如：Main-Class: org.springframework.boot.loader.JarLauncher
* 项目的主类：被@SpringApplication注解的类会在打包时自动写入MANIFEST.MF中的`Start-Class`，例如：Start-Class: com.wkk.learn.springboot.hello.SpringBootHello

### 可直接运行的jar

可直接运行的jar指的是不需要通过`java -jar`就可以直接运行。通过`./jar名称`执行。

**如何创建可直接运行的jar**

* maven插件配置

    ```xml
    <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
            <executable>true</executable>
        </configuration>
    </plugin>
    ```

* 可以在 .conf 的同名⽂件中配置参数
  * 默认配置项：
   CONF_FOLDER: 放置 .conf 的⽬录位置，只能放环境变量中；
   JAVA_OPTS：JVM 启动时的参数， ⽐如 JVM 的内存和 GC；
   RUN_ARGS： 传给程序执⾏的参数。