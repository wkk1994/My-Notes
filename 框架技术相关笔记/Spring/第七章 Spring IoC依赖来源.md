# Spring IoC依赖来源

## 依赖查找的来源

依赖查找的来源主要有：

* 用户自定义的BeanDefinition
  比如`<bean id="user" class="org.geekbang...User">`、`@Bean public User user(){...}`、`BeanDefinitionBuilder`
* 单例对象：通过API实现，API不是只能实现单例对象，通常是通过API来创建单例对象。可以通过方法`SingletonBeanRegistry#registerSingleton`进行注册到容器中。
* Spring内建的Beandefinition
  |Bean 名称| Bean 实例| 使用场景|
  |--|--|--|
  |org.springframework.context.annotation.internalConfigurationAnnotationProcessor|ConfigurationClassPostProcessor 对象|处理 Spring 配置类|
  |org.springframework.context.annotation.internalAutowiredAnnotationProcessor|AutowiredAnnotationBeanPostProcessor 对象|处理 @Autowired 以及@Value 注解|
  |org.springframework.context.annotation.internalCommonAnnotationProcessor|CommonAnnotationBeanPostProcessor 对象|（条件激活）处理 JSR-250 注解，如 @PostConstruct 等|
  |org.springframework.context.event.internalEventListenerProcessor|EventListenerMethodProcessor 对 象|处理标注 @EventListener 的Spring 事件监听方法|

  BeanDefinition的注入可以参考代码`AnnotationConfigUtils#registerAnnotationConfigProcessors`
* Spring 內建单例对象
  |Bean 名称| Bean 实例| 使用场景|
  |--|--|--|
  |environment| Environment 对象| 外部化配置以及 Profiles|
  |systemProperties| java.util.Properties 对象| Java 系统属性|
  |systemEnvironment| java.util.Map 对象| 操作系统环境变量|
  |messageSource| MessageSource 对象| 国际化文案|
  |lifecycleProcessor| LifecycleProcessor 对象| Lifecycle Bean 处理器|
  |applicationEventMulticaster| ApplicationEventMulticaster 对 象|Spring 事件广播器|

  内建的单例对象注入可以参考代码`AbstractApplicationContext#prepareBeanFactory`

Spring提供的内建Bean不一定只是这些，如果激活了AOP或者Transactiona，还会注入更多的Bean。

## 依赖注入的来源

依赖注入的来源有：

* 用户自定义的BeanDefinition
  比如`<bean id="user" class="org.geekbang...User">`、`@Bean public User user(){...}`、`BeanDefinitionBuilder`
* 单例对象：通过API实现
* 非 Spring 容器管理对象：和之前的Spring内建可注入的依赖是一回事。
  这部分是依赖注入特有的，相关代码参考：`AbstractApplicationContext#prepareBeanFactory`663~666行。通过getBean获取会抛出NoSuchBeanDefinitionException。

代码示例：[DependencyInjectSourceDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/dependency-source/src/main/java/com/wkk/learn/spring/ioc/dependency/source/DependencyInjectSourceDemo.java)

**依赖查找的来源，依赖注入都支持, 而且依赖注入比依赖查找多非spring容器管理对象。**

## Spring依赖来源分析

|来源| Spring Bean 对象| 生命周期管理| 配置元信息| 使用场景|
|--|--|--|--|--|
|Spring BeanDefinition|是| 是| 有| 依赖查找、依赖注入|
|单例对象| 是| 否| 无| 依赖查找、依赖注入|
|Resolvable Dependency|否| 否| 无| 依赖注入|

## Spring BeanDefinition 作为依赖来源

* 元数据：BeanDefinition
* 注册：BeanDefinitionRegistry#registerBeanDefinition
* 类型：延迟和非延迟
* 顺序：Bean 生命周期顺序按照注册顺序

DefaultListableBeanFactory对BeanDefinitionRegistry的实现中，使用ConcurrentHashMap保存注册的BeanDefinition信息，又冗余了一个类型ArrayList的beanDefinitionNames字段，保存注册时BeanDefinition的顺序，这样可以保证在初始化Bean的时候根据注册顺序进行初始化。

## 单例对象作为依赖来源

Spring IoC中单例对象可以通过API注入，作为依赖来源之一。这个单例对象不一定非要是JavaBeans，普通的Java对象都可以。通过调用`SingletonBeanRegistry#registerSingleton`方法进行注入。但是单例对象注入有不存在生命周期的管理和无法实现延迟初始化Bean的限制，但是它都可以作为依赖查找和依赖注入的来源。

`DefaultListableBeanFactory#registerSingleton`实现细节：

* 调用父类的registerSingleton方法：使用类型为ConcurrentHashMap的singletonObjects来存放单例对象，其中singletonObjects、singletonFactories、earlySingletonObjects是互斥的关系，相同beanName只能存在一个Map中。
* 调用updateManualSingletonNames更新当前类中类型为LinkedHashSet的manualSingletonNames，manualSingletonNames用来保存注入的单例对象的顺序。

`AbstractBeanFactory#doGetBean`实现的细节：会优先从singletonObjects中获取Bean实例。

> singletonObjects属性用来存放所有已经完成初始化和实例化的对象，earlySingletonObjects存放实例化完成但初始化未完全的对象，singletonFactories存放对象的初始化方法；通过这三个的组合可以解决循环依赖问题。

## 非Spring容器管理对象作为依赖来源

非Spring容器管理的对象也可以称为Spring内建可注入Bean，通过`ConfigurableListableBeanFactory#registerResolvableDependency`方法进行添加，使用限制有：没有生命周期管理、无法实现延迟初始化Bean、无法通过依赖查找、只能使用类型注入，不能通过名称注入。

代码示例：[ResolvableDependencySourceDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/dependency-source/src/main/java/com/wkk/learn/spring/ioc/dependency/source/ResolvableDependencySourceDemo.java)

代码示例：[ResolvableDependencySourceDemo2.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/dependency-source/src/main/java/com/wkk/learn/spring/ioc/dependency/source/ResolvableDependencySourceDemo2.java)

## 外部化配置作为依赖来源

外部化配置是Spring Boot引入的概念，属性值不通过硬编码的方式进行编写，而是通过外部属性文件进行配置。它属于非常规的Spring对象的依赖来源。使用限制也是没有生命周期管理、无法实现延迟初始化Bean、无法通过依赖查找、只能使用名称注入。

@Value注入过程分析：@Value注解也是使用AutowiredAnnotationBeanPostProcessor进行依赖注入的，和@Autowired处理过程差不多，区别在于DefaultListableBeanFactory#doResolveDependency的1125行，会走@Value的逻辑进行依赖注入。

代码示例：[ExternalConfigurationDependencySourceDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/dependency-source/src/main/java/com/wkk/learn/spring/ioc/dependency/source/ExternalConfigurationDependencySourceDemo.java)

## 面试题

* 注入和查找的依赖来源是否相同？

  否，依赖查找的来源仅限于 Spring BeanDefinition 以及单例对象，而依赖注入的来源还包括 Resolvable Dependency 以及@Value 所标注的外部化配置。Spring更注重于依赖注入，因为依赖查找是主动的，需要二次的操作。

* 单例对象能在 IoC 容器启动后注册吗？

  可以的，单例对象的注册与 BeanDefinition 不同，BeanDefinition会被ConfigurableListableBeanFactory#freezeConfiguration() 方法影响，从而冻结注册（实际上只是设置configurationFrozen为true，并不会真正冻结注册），单例对象则没有这个限制。

* Spring 依赖注入的来源有哪些？

  Spring BeanDefinition、单例对象、Resolvable Dependency、@Value 外部化配置。
