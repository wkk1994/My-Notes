# 重新认识IoC

* 代码位置 java-beans-demo

## 什么是IOC

控制反转（Inversion of Control，缩写为IoC），在软件工程中是一种设计原则，相对于传统的输入输出等串型的软件架构比较复杂。程序不需要关心依赖的来源，可以减少代码之间的耦合性。最常见的方式叫做依赖注入（Dependency Injection，简称DI），还有一种方式叫“依赖查找”（Dependency Lookup）

## IoC发展简介

* 1983年，好莱坞原则：你不需要来找我们，我们需要的时候会去找你，你是指应用程序，我们是指应用程序需要的资源。
* 1988年，控制反转
* ...
* 2005年，Martin Fowler在《InversionOfControl》对IoC做出进一步说明。

## IoC主要实现策略

* 服务定位模式（service locator pattern）
    Java EE中所定义的一种模式，通过JNDI这种技术获取Java EE的组件。
* 依赖注入（dependency injection）
  * 构造器注入
  * 参数注入
  * set方法注入
  * 接口注入
* 上下文查询（contextualized lookup）
    Java Bean中有一个通用的bean context上下文，可以传输bean和管理bean的层次性，Spring的IoC设计也借鉴了Java Beans。
* 模板方法的设计模式（template method design pattern）
    Spring Bean中大量使用，例如JdbcTemplate中的Callback，不关心从哪来的。
* 策略设计模式（strategy design pattern）

**大致可以将IoC的实现策略分为依赖查找和依赖注入两个方面。Spring中都有支持。**

*《Expert One-on-One™ J2EE™ Development without EJB™》*

## IoC容器的职责

* 通用职责
  * 依赖处理
    * 依赖查找 主动注入方式
    * 依赖注入
* 生命周期管理
  * 容器的生命周期
  * 托管的资源（Java Beans或其他资源）的生命周期
* 配置
  * 容器：定时任务等的配置
  * 外部化配置：属性配置，xml配置。
  * 托管的资源（Java Beans或其他资源）

## IoC容器的实现

**主要实现**

* Java SE
  * Java Beans
  * Java ServiceLoader SPI NetBeans大量使用
  * JNDI（Java Naming and Directory Interface）
* Java EE
  * EJB（Enterprise Java Beans）
  * Servlet
    Java web的标准技术
* 开源
  * Apache Avalon
  * PicoContainer（Spring依赖查找参考了这个）
  * Google Guice
  * Spring Framework

## 传统IoC容器的实现

Java Beans作为IoC容器？？？

JavaBean是一个规范，任何一个Java类都可以是JavaBean。JavaBean规范包括所有属性为private、提供默认构造方法、提供getter和setter、实现serializable接口。符合了这些规范的Java类可以方便的获取BeanInfo，通过BeanInfo对Java类进行操作。

我的理解是JavaBeans并不能算是一个IoC容器，有了JavaBeans的自内省、Bean内容修改等等这些基础，让Spring可以方便的借鉴并利用JavaBeans实现IoC容器，数据绑定等功能。

* 特性
  * 依赖查找
  * 生命周期管理
  * 配置元信息
  * 事件
  * 自定义
  * 资源管理
  * 持久化

* 规范

  JavaBean
  BeanContext

## 轻量级IoC容器

* 特征：
  * 容器可以管理代码（启动，停止）
  * 可以快速启动（依赖资源过多也会启动慢）
  * 容器依赖的资源很少
  * 依赖的API很少
  * 容器可以管理部署
* 好处：
  * 代码和实现解耦
  * 最大化的代码复用
  * 更大程度的面向对象
  * 最大化的生态系统
  * 更好的可测试性

## 依赖查找 VS 依赖注入

优劣对比：

|类型|依赖处理|实现便利性|代码侵入性|API依赖性|可读性|
|---|-------|--------|---------|--------|-----|
|依赖查找|主动获取|相对繁琐|侵入业务逻辑|依赖容器API|良好|
|依赖注入|被动提供|相对便利|底侵入性|不依赖容器API|一般|

**Spring推荐依赖注入**

## 构造器注入 VS Setter注入

构造器注入和Setter注入一直是业界讨论的点。Spring官方文档推荐构造器注入，但是Spring的作者推荐Setter注入。

* 构造器注入的注入顺序是给定的，Setter注入的顺序不确定；
* 构造器注入注入后不可变（是缺点也是优点），Setter注入注入后可以再次多次修改（是缺点也是优点）；
* 构造器注入后续扩展不友好，Setter注入后期可以随时添加property；
* 构造器注入没有默认值，Setter注入可以有默认值，不注入也会有默认值；
* ...

推荐构造器注入：这样在bean初始化时就有一致性的状态，防止在后面使用的时候不知道什么时候修改了property。

## 面试题

* 什么是IoC容器

  简单说IoC就是反转控制，类似于好莱坞原则，降低代码的耦合性，主要有依赖查找和依赖注入实现。传统实现比如EJB，Spring。

* 依赖查找和依赖注入的区别

  依赖查找是主动获取，主动或手动的依赖查找方式，通常需要依赖于容器或标准的API；而依赖注入是被动提供，是手动或自动依赖绑定的方式，不需要依赖特定的容器和API。依赖注入比依赖查找更便利，更瞬速。

* Spring作为IoC容器有什么优势

  Spring除了是典型的IoC管理，比如提供了依赖查找和依赖注入的方式。还提供了强大的API抽象，比如AOP抽象、事务抽象等。还提供了事件机制、SPI扩展、以及强大的第三方整合。Spring还拥有很好的易测试性、更好的面向对象、设计模式的实现。
