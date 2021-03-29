# Spring IoC依赖查找

## 依赖查找的前世今生

Java中对依赖查找的实现有：

* 单一类型依赖查找
  * JNDI-javax.naming.Context
  * JavaBeans-java.beans.beancontext.BeanContext
* 集合类型依赖查找
  * java.beans.beancontext.BeanContext
    java.beans.beancontext.BeanContextServices实现了集合类型查找
* 层次性依赖查找
  * java.beans.beancontext.BeanContext
    JavaBeans规范规定了BeanContext之间可以有父子关系。
  
**Spring IoC的依赖查找参考了JavaBeans的实现方式，不过Spring在实现的过程中更加优雅。**

## 单一类型依赖查找

* 根据 Bean 名称查找
  * getBean(String)
  * Spring 2.5 覆盖默认参数：getBean(String,Object...)，不建议使用
* 根据 Bean 类型查找
  * Bean 实时查找
    * Spring 3.0 getBean(Class)
    * Spring 4.1 覆盖默认参数：getBean(Class,Object...)
  * Spring 5.1 Bean 延迟查找
    * getBeanProvider(Class)
    * getBeanProvider(ResolvableType)
* 根据 Bean 名称 + 类型查找：getBean(String,Class)

示例代码：[ObjectProviderDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/dependency-lookup/src/main/java/com/wkk/learn/spring/ioc/dependency/lookup/ObjectProviderDemo.java)

## 集合类型依赖查找

集合类型的依赖查找主要使用`ListableBeanFactory`接口进行查找，主要方式：

* 根据Bean类型进行查找
  * 获取同类型的Bean的名称列表
    * getBeanNamesForType(Class)
    * Spring 4.2 getBeanNamesForType(ResolvableType)
  * 获取同类型 Bean 实例列表
    * getBeansOfType(Class)以及重载方法：会提前初始化Bean，不建议使用这个方法，会导致类的实例的不确定因素，可能初始化不完整。
* 根据注解类型进行查找
  * Spring 3.0 获取标注类型 Bean 名称列表
    * getBeanNamesForAnnotation(Class<? extends Annotation>)
  * Spring 3.0 获取标注类型 Bean 实例列表
    * getBeansWithAnnotation(Class<? extends Annotation>)
  * Spring 3.0 获取指定名称 + 标注类型 Bean 实例
    * findAnnotationOnBean(String,Class<? extends Annotation>)

**推荐使用Bean的名称来判断Bean是否存在，这种方式可以避免提早初始化Bean，产生一些不确定的因素。**

## 层次性依赖查找

层次性依赖查找和ClassLoader的双亲委派模式比较相似，HierarchicalBeanFactory是它的基础接口。

可以通过HierarchicalBeanFactory#getParentBeanFactory()获取父BeanFactory。

层次性查找的实现：

* 根据Bean名称查找
  * 基于containsLocalBean方法实现，containsLocalBean方法只会查找当前BeanFactory，不会查找父类的。
* 根据Bean类型查找实例列表
  * 单一类型：BeanFactoryUtils#beanOfType
  * 集合类型：BeanFactoryUtils#beansOfTypeIncludingAncestors
* 根据 Java 注解查找名称列表
  * BeanFactoryUtils#beanNamesForTypeIncludingAncestors

以上的方法都是在当前本地进行查找，不会从父BeanFactory中查找。

示例代码：[HierarchicalDependencyLookupDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/dependency-lookup/src/main/java/com/wkk/learn/spring/ioc/dependency/lookup/HierarchicalDependencyLookupDemo.java)

## 延迟依赖查找

Bean实现延迟依赖查找的接口：

* org.springframework.beans.factory.ObjectFactory
* org.springframework.beans.factory.ObjectProvider
  * Spring 5 对 Java 8 特性扩展
    * 函数式接口
    * getIfAvailable(Supplier)：如果对象不存在时调用Supplier。
    * ifAvailable(Consumer)：如果对象存在时调用Consumer。
  * Stream 扩展 - stream()

ObjectProvider是ObjectFactory的子类，在ObjectFactory的基础上进行了扩展，支持当bean不存在时可以执行默认创建的方式，并支持查找bean实例的集合。

示例代码：[ObjectProviderDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/dependency-lookup/src/main/java/com/wkk/learn/spring/ioc/dependency/lookup/ObjectProviderDemo.java)

延迟查找的意义：ObjectProvider提供的延迟查找的ifAvailable方法，可以让程序在对象实例化完成时，进行预定义对象的一些行为或事件，从而实现对象的个性化配置。getIfAvailable方法，提供了Bean实例不存在的个性化配置。

## 安全依赖查找

依赖查找的安全性是指当查找的Bean实例不存在时，会不会抛出异常（NoSuchBeanDefinitionException）。

|依赖查找类型| 代表实现| 是否安全|
|--|--|--|
|单一类型查找| BeanFactory#getBean| 否|
||ObjectFactory#getObject| 否|
||ObjectProvider#getIfAvailable| 是|
|集合类型查找| ListableBeanFactory#getBeansOfType| 是|
||ObjectProvider#stream| 是|

推荐使用ObjectProvider进行依赖查找，这种方式既可以表示的单一类型又可以表示集合类型。

注意：层次性依赖查找的安全性取决于其扩展的单一或集合类型的 BeanFactory 接口。

## 内建可查找的依赖

### AbstractApplicationContext 内建可查找的依赖

AbstractApplicationContext是AnnotationConfigApplicationContext、ClassPathXmlApplicationContext等ApplicationContext的父类。

|Bean 名称| Bean 实例| 使用场景|
|--|--|--|
|environment| Environment 对象| 外部化配置以及 Profiles|
|systemProperties| java.util.Properties 对象 |Java 系统属性|
|systemEnvironment| java.util.Map 对象| 操作系统环境变量|
|messageSource| MessageSource 对象 |国际化文案|
|lifecycleProcessor| LifecycleProcessor 对象| Lifecycle Bean 处理器|
|applicationEventMulticaster| ApplicationEventMulticaster 对象|Spring 事件广播器|

### 注解驱动 Spring 应用上下文内建可查找的依赖（部分）

|Bean 名称| Bean 实例| 使用场景|
|--|--|--|
|org.springframework.context.annotation.internalConfigurationAnnotationProcessor|ConfigurationClassPostProcessor 对象|处理 Spring 配置类|
|org.springframework.context.annotation.internalAutowiredAnnotationProcessor|AutowiredAnnotationBeanPostProcessor 对象|处理 @Autowired 以及 @Value 注解|
|org.springframework.context.annotation.internalCommonAnnotationProcessor|CommonAnnotationBeanPostProcessor 对象|（条件激活）处理 JSR-250 注解，如 @PostConstruct 等|
|org.springframework.context.event.internalEventListenerProcessor|EventListenerMethodProcessor 对象|处理标注 @EventListener 的Spring 事件监听方法|
|org.springframework.context.event.internalEventListenerFactory|DefaultEventListenerFactory 对 象|@EventListener 事件监听方法适配为 ApplicationListener|
|org.springframework.context.annotation.internalPersistenceAnnotationProcessor|PersistenceAnnotationBeanPostProcessor 对象|（条件激活）处理 JPA 注解场景|

类AnnotationConfigUtils中定义了注解驱动的Spring上下文中的内建Bean。

## 依赖查找中的经典异常

|异常类型|触发条件（举例）|场景举例|
|--|--|--|
|NoSuchBeanDefinitionException| 当查找 Bean 不存在于 IoC 容器时 |BeanFactory#getBean ObjectFactory#getObject|
|NoUniqueBeanDefinitionException|类型依赖查找时，IoC 容器存在多个 Bean 实例|BeanFactory#getBean(Class)|
|BeanInstantiationException| 当 Bean 所对应的类型非具体类时| BeanFactory#getBean|
|BeanCreationException| 当 Bean 初始化过程中| Bean 初始化方法执行异常时|
|BeanDefinitionStoreException| 当 BeanDefinition 配置元信息非法时|XML 配置资源无法打开时|

以上异常都是BeansException的子类，并且BeansException是RuntimeException的子类。

示例代码：[BeanInstantiationExceptionDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/dependency-lookup/src/main/java/com/wkk/learn/spring/ioc/dependency/lookup/BeanInstantiationExceptionDemo.java)

## 面试题

* ObjectFactory 与 BeanFactory 的区别？

  ObjectFactory 与 BeanFactory 均提供依赖查找的能力。不过 ObjectFactory 仅关注一个或一种类型的 Bean 依赖查找，并且自身不具备依赖查找的能力，能力则由 BeanFactory 输出。BeanFactory 则提供了单一类型、集合类型以及层次性等多种依赖查找方式。

* BeanFactory.getBean 操作是否线程安全？

  BeanFactory.getBean 方法的执行是线程安全的，操作过程中会增加互斥锁。

* Spring 依赖查找与注入在来源上的区别?
