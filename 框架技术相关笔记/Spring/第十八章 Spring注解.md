# Spring注解

## Spring注解驱动编程发展历程

* 注解驱动启蒙时代：Spring 1.X

  Spring 1.x版本处于03年之间，开始还是兼容的JDK1.2，没有支持注解的特性。
  代表注解：@Transactional：Spring1.2版本引入。

* 注解驱动过渡时代：Spring 2.X

  Spring 2.X版本虽然没有强制要求使用JDK1.5，但是已经开始兼容JDK1.5了。
  注解举例：@Repository、@Component、@Service

* 注解驱动黄金时代：Spring 3.X

  开始引入大量的注解，注解举例：@Bean、@Lazy、@Primary、@Configuration

* 注解驱动完善时代：Spring 4.X

  基本完成了注解驱动的编程模型。
  注解举例：@Conditional

* 注解驱动当下时代：Spring 5.X

  没有引入新注解，更多的是性能优化。
  注解举例：@Indexed：对注解扫描进行优化，可以在编译时就确定有哪些Bean。

## Spring核心注解场景分类

Spring模式注解：

* @Repository： 数据仓储模式注解
* @Component：通用组件模式注解，基本上所有的模式注解都是来自于它的派生。
* @Service：服务模式注解
* @Controller：Web控制模式注解
* @Configuration：配置类模式注解

装配注解：

* @ImportResource：替换XML中的\<import>
* Import：导入Configuration类
* ComponentScan：扫描指定包下的Spring模式注解类

依赖注入注解：

* @Autowired：Bean的依赖注入，支持多种依赖查找方式
* @Qualifier：细粒度的@Autowired依赖查找

## Spring注解编程模型

* 元注解
* Spring模式注解
* Spring组合注解
* Spring注解属性别名和覆盖

[Spring Annotation Programming Model](https://github.com/spring-projects/spring-framework/wiki/Spring-Annotation-Programming-Model)

## Spring元注解（Mate-Annotations）

元注解是注释到注解上的注解，常见的元注解有：java.lang.annotation.Target、java.lang.annotation.Documented、java.lang.annotation.Retention。

## Spring模式注解（Stereotype Annotations）

Spring的模式注解是用于生命注释的Bean在程序中的角色，比如@Repository注释表示存储的角色。

理解@Component的”派生性”（实际上官方文档上并没有派生性的概念）：

注解不能被继承，只能通过注解注释注解的方式，间接表示注解之间的组合关系。@Component比较特殊，只要是元标注中有@Component的注解，比如常见的模式注解@Service等，它们都会”派生“（拥有）@Component的特性，它们标注的类都会被Spring上下文注册为Bean实例，并且从Spring4.0开始支持多层次”派生性“。

多层次”派生性“：

注解可能不直接被@Component标注，先标注其他注解，其他注解又被@Component标注，进而当前注解也派生了@Component的特性。

@Component”派生性“原理：

* 核心组件：org.springframework.context.annotation.ClassPathBeanDefinitionScanner
  它的父类：org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider

* 资源处理：org.springframework.core.io.support.ResourcePatternResolver
  对指定的类路径进行资源处理，获取对应路径下的指定资源。
* 将资源解析为类元信息：org.springframework.core.type.classreading.MetadataReaderFactory

  将资源解析为类的元信息。

* 类的元信息：org.springframework.core.type.ClassMetadata
  * ASM实现：org.springframework.core.type.classreading.ClassMetadataReadingVisitor
  * 反射实现：org.springframework.core.type.StandardClassMetadata

* 注解元信息：org.springframework.core.type.AnnotationMetadata
  * ASM实现：org.springframework.core.type.classreading.AnnotationMetadataReadingVisitor
  * 反射实现：org.springframework.core.type.StandardAnnotationMetadata

> ASM不需要加载class，反射需要加载class

## Spring组合注解（Composed Annotations）

Spring组合注解是使用一个或多个注解进行注释的注解，目的是将这些注解的能力和行为组合到单个自定义注解上。比如Spring的@Transactional和@Service注释进行元注释的名为@TransactionalService的注释是一个组合注释。

## Spring注解属性别名（Attribute Aliases）

属性别名是将一个注释的属性映射为另一个注释的属性。属性别名可以分为三类：

* 显示别名：如果一个注释中的两个属性通过@AliasFor声明为彼此的别名，则它们是显式别名。

  @ComponentScan中value和basePackages为显示别名：

  ```java
  @AliasFor("basePackages")
  String[] value() default {};

  @AliasFor("value")
  String[] basePackages() default {};
  ```

* 隐式别名：如果一个注释中的两个或多个属性通过@AliasFor声明为元注释中同一属性的显式重写，则它们是隐式别名。

  @MyComponentScan中values和scanBasePackages为隐式别名：

  ```java
  @AliasFor(annotation = ComponentScan.class, attribute = "basePackages")
  String[] values() default {"#"};

  // 和values为隐式别名
  @AliasFor(annotation = ComponentScan.class, attribute = "basePackages")
  String[] scanBasePackages() default {"#"};
  ```

* 传递性隐式别名：给定一个注释中的两个或多个属性通过@AliasFor声明为元注释中属性的显式重写，如果这些属性按照传递性法则有效重写元注释中的同一属性，则它们是传递性隐式别名。

  @MyComponentScan2中values和scanBasePackages为隐式别名：

  ```java
  @AliasFor(annotation = MyComponentScan.class, attribute = "value")
    String[] values() default {"#"};

  // 和values为隐式别名
  @AliasFor(annotation = MyComponentScan.class, attribute = "scanBasePackages")
  String[] scanBasePackages() default {"#"};
  ```

## Spring注解属性覆盖（Attribute Override ）

属性覆盖是指注解中的属性覆盖元标注注解中的属性。属性覆盖分为三种：

* 隐式覆盖：注解中的属性和元标注注解属性重名时，属于隐式覆盖。

  比如@MyComponentScan中的basePackages是@ComponentScan中basePackages的隐式覆盖。

  ```java
  @ComponentScan
  public @interface MyComponentScan {

    String[] basePackages() default {"#"};
  }
  ```

> 隐式覆盖有问题，不能覆盖被@AliasFor的属性，比如@ComponentScan#basePackages，但是@ComponentScan#basePackageClasses可以。

* 显示覆盖：通过@AliasFor将属性A声明为元注解中属性B的别名，A就是B的显示覆盖。

  比如@MyComponentScan中的values是@ComponentScan中basePackages的隐式覆盖。

  ```java
  @ComponentScan
  public @interface MyComponentScan {

    @AliasFor(annotation = ComponentScan.class, attribute = "basePackages")
    String[] values() default {"#"};
  }
  ```

* 传递性显示覆盖：如果注解@One中的属性A是注解@Two中属性B的显式覆盖，而B是注解@Three中属性C的显式覆盖，那么A是遵循传递性法则的C的传递性显式覆盖。

## Spring @Enable模块驱动

Spring提供了@Enable模块驱动编程模式：

驱动注解都是使用@Enable作为开头，导入注解使用@Import实现。@Import导入的具体实现有：

* 基于Configuration Class
* 基于ImportSelector接口实现
* 基于ImportBeanDefinitionRegistrar接口实现

@Enable模块驱动示例代码：
[HelloWorldConfiguration.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/annotation/src/main/java/com/wkk/learn/spring/ioc/annotation/HelloWorldConfiguration.java)
[HelloWorldImportSelector.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/annotation/src/main/java/com/wkk/learn/spring/ioc/annotation/HelloWorldImportSelector.java)
[HelloWorldImportBeanDefinitionRegistrar.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/annotation/src/main/java/com/wkk/learn/spring/ioc/annotation/HelloWorldImportBeanDefinitionRegistrar.java)
[EnableHelloWorld.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/annotation/src/main/java/com/wkk/learn/spring/ioc/annotation/EnableHelloWorld.java)
[EnableModuleDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/annotation/src/main/java/com/wkk/learn/spring/ioc/annotation/EnableModuleDemo.java)

## Spring条件注解

Spring条件注解的方式有：

* 基于配置的条件注解：@org.springframework.context.annotation.Profile
  * 关联对象：org.springframework.core.env.Environment中的Profiles
  * 实现变化：从Spring4.0开始，@Porfile基于@Conditional实现

* 基于编程条件注解：@org.springframework.context.annotation.Conditional
  * 关联对象：org.springframework.context.annotation.Condition的具体实现

Spring条件注解代码示例：
[EvenProfileCondition.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/annotation/src/main/java/com/wkk/learn/spring/ioc/annotation/EvenProfileCondition.java)
[ProfileDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/annotation/src/main/java/com/wkk/learn/spring/ioc/annotation/ProfileDemo.java)

@Conditional实现原理：

* 上下文对象：org.springframework.context.annotation.ConditionContext，存储当前Spring上下文的BeanFactory、Environment等信息。
* 条件判断：org.springframework.context.annotation.ConditionEvaluator

  条件判断实现方法为ConditionEvaluator#shouldSkip(AnnotatedTypeMetadata, ConfigurationPhase)，在该方法中先获取Bean上的Condition的实现类列表，然后遍历执行Condition，有一个条件不匹配就返回为true。

* 判断入口：org.springframework.context.annotation.ConfigurationClassPostProcessor
  * org.springframework.context.annotation.ConfigurationClassParser

## 面试题

* Spring模式注解有哪些？

  @Component、@Repository、@Service、@Controller

* @EventListener工作原理

  源码参考：org.springframework.context.event.EventListenerMethodProcessor

* @PropertySource工作原理

  下章解答。
