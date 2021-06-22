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