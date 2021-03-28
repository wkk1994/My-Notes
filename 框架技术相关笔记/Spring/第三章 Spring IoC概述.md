# Spring IoC概述

* 代码位置：ioc-container-overview

## Spring IoC依赖查找

* 根据Bean名称查找
  * 实时查找
  * 延迟查找
* 根据Bean类型查找
  * 单个Bean对象
  * 集合Bean对象
* 根据Bean名称+类型查找
* 根据Java注解查找
  * 单个Bean对象
  * 集合Bean对象

代码示例：

[DependencyLookupDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/ioc-container-overview/src/main/java/com/wkk/learn/spring/ioc/overview/dependency/lookup/DependencyLookupDemo.java)

> ObjectFactory和FactoryBean的关系：

## Spring IoC依赖注入

**代码位置：ioc-container-overview**

* 根据Bean名称注入
* 根据Bean类型注入
  * 单个Bean对象
  * 集合Bean对象
* 注入容器内建Bean对象
   内建Bean对象是Spring提供的内置的Bean。
* 注入非Bean对象
* 注入类型
  * 实时注入
  * 延迟注入

代码示例：

[DependencyInjectionDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/ioc-container-overview/src/main/java/com/wkk/learn/spring/ioc/overview/dependency/injection/DependencyInjectionDemo.java)

## Spring IoC依赖来源

* 自定义的Bean
  xml配置、注解配置等配置的Bean。
* 容器内建Bean对象
  如Environment，Spring自己初始化的Bean对象，通过getBean可以获取到对象。
* 容器内建依赖
  如BeanFactory，一般通过getBean方法不能获取到对象。

## Spring IoC配置元信息

配置元信息的来源：

* Bean定义配置
  * 基于XML文件
  * 基于Properties文件
  * 基于Java注解
  * 基于Java API
* IoC容器配置
  * 基于XML配置
  * 基于Java注解
  * 基于Java API
* 外部化属性配置
  * 基于Java注解 例如：@Value

## ApplicationContext和BeanFactory的关系

BeanFactory是一个底层的IoC容器，提供了基本的IoC容器的功能；ApplicationContext继承了BeanFactory这个接口，但是它又在内部组合了BeanFactory，ApplicationContext实现了BeanFactory的全部能力，相当于BeanFactory的超集，并且提供了更多的特性，比如：AOP更好的组合，消息资源处理，事件的发布等。

**ApplicationContext 除了 IoC 容器角色，还有提供：**

* 更好的AOP编程方式；
* 配置元信息；
* 资源管理；
* 事件；
* 国际化；
* 注解：比如@Component注解；
* Environment抽象。

## Spring IoC容器的生命周期

* 启动
  基于org.springframework.context.support.AbstractApplicationContext#refresh分析。
  * 加互斥锁进行启动；
  * 启动前准备；
    * 记录启动时间；重置状态；
    * 初始化属性资源；
    * 环境验证；
    * 添加监听事件；
  * 获取BeanFactory；
  * 准备BeanFactory；
    * 包括注册BeanFactory、ApplicationContext、Environment等；
    * 这一步在添加内建Bean和依赖Bean；
  * 扩展点，自定义BeanFactory的设置；
  * 扩展点，注册bean的处理器；
  * 初始化信息资源（国际化相关）；
  * 初始化应用时间；
  * 注册监听器；
  * ...

* 运行

* 停止

## 面试题精选

* 什么是Spring IoC容器？

  Spring IoC是控制反转的实现，通过使用依赖注入和依赖查找的方式实现依赖的反转，并维护Bean信息。

* BeanFactory和FactoryBean？

  BeanFactory是IoC的底层实现；而FactoryBean是创建Bean的一种方式，可以解决复杂的构造场景和初始化场景。**创建的Bean会不会还经过Bean的生命周期？？**

* Spring IoC容器启动时做了哪些准备？

  IoC 配置元信息读取和解析、IoC 容器生命周期（初始化、准备阶段、扩展点等）、Spring事件发布、国际化等。
