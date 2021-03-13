# Spring核心特性

## Spring核心特性总览

![spring核心特性](https://raw.githubusercontent.com/geektime-geekbang/geekbang-lessons/master/Spring%20%E6%A0%B8%E5%BF%83%E7%89%B9%E6%80%A7.png)

## 特性总览

* IoC容器（IOC Container）
* Spring事件
* 资源管理（Resources）
* 国际化（i18n）
* 校验（Validation）
* 数据绑定（Data Binding）
* 类型转换（Type Conversion）
* Spring 表达式（Spring Express Language）
    和jsp表达式差不多
* 面向切面编程（AOP）
    核心内容

## 数据存储（Data Access）

* JDBC
   java api与数据库的连接
* 事务抽象（Transactions）
   来源于EJB
* DAO支持（DAO Support）
* O/R映射（O/R Mapping）
  Hibernate，JPA
* XML编列（XML Marshalling）

## Web技术（Web）

* Web Servlet技术栈
  * Spring MVC
        Spring 1到Spring4
  * WebSocket
  * SockJS
* Web Reactive技术栈
  * Spring WebFlux
     Spring5开始支持，注解和Spring MVC差不多，底层实现不同。
  * WeClient
  * WebSocket

## 技术整合（Integration）

* 远程调用（Remoting）
  * RMI JAVA标准的远程方法调用
  * Hessian社区开源的方案，Dubbo就是使用这种方式
* Java消息服务（JMS）
* Java连接架构（JCA）
* Java管理扩展（JMX）
   可以获取cpu利用率，磁盘利用率等信息
* Java邮件客户端（Email）
* 本地任务（Tasks）
* 本地调度（Scheduling）
* 缓存抽象（Caching）
* Spring测试（Testing）
  * 模拟对象（Mock Objects）
  * TestContext框架
  * Spring MVC测试
  * Web测试客户端
  
## Spring对Java版本的支持与依赖

|Spring Framework版本|Java标准版|Java企业版|
|--|--|--|
|1.x|1.3 +|J2EE 1.3 +|
|2.x|1.4.2 +|J2EE 1.3 +|
|3.x|5 +|J2EE 1.4 + 和Java EE 5|
|4.x|6 +|Java EE 6和7|
|5.x|8 +|Java EE 7|

## Spring模块化设计

* spring-aop
* spring-aspects
* spring-beans
    spring-beans和spring-content就是Spring IoC的重要核心实现
* spring-context
* spring-context-indexer
* spring-context-support
* spring-core
    核心部分，关于Java语法的特性支持，spring-beans和sping-content都依赖于spring-core
* spring-expression
    Spring的表达式语言，从Sping3开始引入，类似于JSP的EL表达式
* spring-instrument
    对java Agent的支持
* spring-jcl
    spring 5开始引入，日志框架，common-logging的替代
* spring-jdbc
* spring-jms
    Java message service，java消息服务，实现ActivitiMQ
* spring-messaging
    统一消息服务的实现，包括Kafka RockerMQ RabbitMQ
* spring-orm
    Hibernate JPA的整合
* spring-oxm
    XML的编列，XML的序列化和反序列化
* spring-test
* spring-tx
   Spring事务的实现
* spring-web
* spring-webflux
* spring-webmvc
* spring-websocket

## Spring的编程模型

* 面向对象编程
  * 契约接口： Aware，BeanPostProcessor...
  * 设计模式：观察者模式，组合模式，模板模式
  * 对象继承：Abstract*类
* 面向切面编程
  * 动态代理：jdkDynamicAopProxy
  * 字节码提升：ASM，CGLib，Aspectj...
* 面向元编程
  * 注解：模式注解（@Compoment，@Service...）
  * 配置：Environment抽象，PropertySources，BeanDefintion...
  * 泛型：GenericTypeResolver...
* 函数驱动
  * 函数接口：ApplicationEventPublisher，
* 模块驱动
  * Maven Artifacts
  * OSGi Bundles
  * Java 9 Automatic Modules
  * Spring @Enable*

## Spring的核心价值

![Spring的核心价值](https://raw.githubusercontent.com/geektime-geekbang/geekbang-lessons/master/Spring%20%E6%A0%B8%E5%BF%83%E4%BB%B7%E5%80%BC.png)

## 面试题

* 什么是Spring Framework？

* Spring Framework有哪些核心模块？

    * spring-core：Spring基础API模块，如资源管理，泛型处理
    * srping-beans：Spring Bean相关，如依赖查找，依赖注入
    * spring-aop：Spring AOP处理，如动态代理，AOP字节码提升
    * spring-context：事件驱动，注解驱动，模块驱动等
    * spring-expression：spring表达式语言模块
* Spring Framework的优势和不足？