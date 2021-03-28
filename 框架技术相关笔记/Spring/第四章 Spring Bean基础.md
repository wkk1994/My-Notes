# Spring Bean基础

## 定义Spring Bean

什么是BeanDefinition

BeanDefinition是Spring中用来描述Bean的配置元信息的接口，其中包括：

* Bean的类名；
* Bean行为配置元素，比如作用域、自动绑定的模式（@Autowired注解）、生命周期的回调等等；
* 其他Bean引用，又可以称为合作者或者依赖；
* 配置设置，比如一些属性信息。

## BeanDefinition 元信息

|属性|说明|
|--|--|
|Class| Bean 全类名，必须是具体类，不能用抽象类或接口|
|Name| Bean 的名称或者 ID|
|Scope| Bean 的作用域（如：singleton、prototype 等）|
|Constructor arguments| Bean 构造器参数（用于依赖注入）|
|Properties| Bean 属性设置（用于依赖注入）|
|Autowiring mode| Bean 自动绑定模式（如：通过名称 byName）|
|Lazy initialization mode| Bean 延迟初始化模式（延迟和非延迟）|
|Initialization method| Bean 初始化回调方法名称|
|Destruction| method Bean 销毁回调方法名称|

## BeanDefinition的构建方式

* 通过BeanDefinitionBuilder构建；

  ```java
  // 1.通过BeanDefinitionBuilder构建
  BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(User.class);
  beanDefinitionBuilder.addPropertyValue("id", "1")
                .addPropertyValue("name", "hello bean definition1");
  AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
  // beanDefinition并非最终状态，还可以进行修改
  ```

* 通过AbstractBeanDefinition以及派生类构建；

  ```java
  // 2.通过AbstractBeanDefinition的派生类构建
  GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
  genericBeanDefinition.setBeanClass(User.class);
  MutablePropertyValues mutablePropertyValues = new MutablePropertyValues();
  mutablePropertyValues.add("id", "1").add("name", "hello bean definition2");
  genericBeanDefinition.setPropertyValues(mutablePropertyValues);
  ```

## Spring Bean的命名

### Spring Bean的名称

每个Spring Bean都有一个或多个标识符，这些标识符在Bean所在的**容器中**必须是唯一的。通常，一个Bean仅有一个表示符，也可以使用别名（Alias）的方式来扩充，这样让Bean在不同的场景下有不同语义的别名。容器会可以通过Bean的标识符进行依赖注入或依赖查找。

在基于XML的配置元信息中，可以用id或者name属性来规定Bean的标识符。通常Bean 的 标识符由字母组成，允许出现特殊字符。如果要想引入 Bean 的别名的话，可在name 属性使用半角逗号（“,”）或分号（“;”) 来间隔。

Bean 的 id 或 name 属性并非必须制定，如果留空的话，容器会为 Bean 自动生成一个唯一的名称。Bean 的命名尽管没有限制，不过官方建议采用驼峰的方式，更符合 Java 的命名约定。

### Bean名称生成器（BeanNameGenerator）

Spring Bean的名称可以自己手动指定，当没有指定时，Spirng会使用默认的Bean名称生成器生成名称。主要的Bean名称生成器有`DefaultBeanNameGenerator`和`AnnotationBeanNameGenerator`。

* DefaultBeanNameGenerator：默认通用BeanNameGenerator实现，Spring 2.0.3引入。

  生成规则：如果当前BeanName（类的全限定名）还没有对应的Bean，Bean名称就为BeanName，否则就是BeanName+#+当前Bean实例的数量。
* AnnotationBeanNameGenerator：基于注解扫描的 BeanNameGenerator 实现，Spring 2.5引入。

  生成规则：如果注解的value值存在就使用value作为BeanName，否则就当前类名的驼峰值。

### Bean的别名

Bean别名的好处/价值：

* 复用现有的 BeanDefinition；
* 更具有场景化的命名方法，比如：`<alias name="myApp-dataSource" alias="subsystemA-dataSource"/><alias name="myApp-dataSource" alias="subsystemB-dataSource"/>`。

## 注册Spring Bean

如何将BeanDefinition注册到Spring容器中。

* XML 配置元信息：`<bean name=”...” ... />`；
* Java 注解配置元信息：
  * @Bean
  * @Component
  * Import
  * ...

    代码示例：[AnnotationBeanDefinitionDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/spring-bean/src/main/java/com/wkk/learn/spring/ioc/bean/definition/AnnotationBeanDefinitionDemo.java)

* Java API 配置元信息
  * 命名方式：BeanDefinitionRegistry#registerBeanDefinition(String,BeanDefinition);
  * 非命名方式：BeanDefinitionReaderUtils#registerWithGeneratedName(AbstractBeanDefinition,Be
anDefinitionRegistry);
  * 配置类方式：AnnotatedBeanDefinitionReader#register(Class...)。

    代码示例：[ApiregistryBeanDefinitionDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/spring-bean/src/main/java/com/wkk/learn/spring/ioc/bean/definition/ApiregistryBeanDefinitionDemo.java)

> @Bean 和 @Service 名称相同的处理不同？使用@Bean注解声明两个名称相同的Bean，不会启动提示冲突报错，会进行覆盖；而使用@Service声明两个名称相同的Bean，会提示冲突报错。它们两个的处理逻辑不同。

## Spring Bean实例化

Spring Bean常见的实例化方式可以分为以下：

* 常规方式
  * 通过构造器实例化
    配置方式：XML、Java 注解和 Java API。
  * 通过静态工厂方法实例化
    配置方式：XML 和 Java API
  * 通过实例（Bean）工厂方法实例化
    配置方式：XML和 Java API
  * 通过FactoryBean实例化
    配置方式：XML、Java 注解和 Java API

  代码示例：[BeanInstantiationDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/spring-bean/src/main/java/com/wkk/learn/spring/ioc/bean/definition/BeanInstantiationDemo.java)

* 特殊方式
  * 通过 ServiceLoaderFactoryBean/ServiceListFactoryBean
    配置方式：XML、Java 注解和 Java API
  * 通过 AutowireCapableBeanFactory#createBean(java.lang.Class, int, boolean)
  * 通过 BeanDefinitionRegistry#registerBeanDefinition(String,BeanDefinition)

  代码示例：[SpecialBeanInstantiationDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/spring-bean/src/main/java/com/wkk/learn/spring/ioc/bean/definition/SpecialBeanInstantiationDemo.java)

## Spring Bean初始化

Spring Beande初始化方式主要以下三种：

* @PostConstruct标注的方式，在构造方法执行后执行。
* 实现InitializingBean 接口的 afterPropertiesSet() 方法，在属性设置完成后执行。
* 自定义初始化方法
  * XML配置：`<bean init-method="initMethodName" ...>`
  * Java注解：@Bean(initMethod="init")
  * Java API: AbstractBeanDefinition#setInitMethodName(String)

上面三者的执行顺序是：@PostConstruct > 实现InitializingBean接口 > 自定义初始化方法。

代码示例：[BeanInitializationDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/spring-bean/src/main/java/com/wkk/learn/spring/ioc/bean/definition/BeanInitializationDemo.java)

## Spring Bean的延迟加载

Spring Bean的延迟加载的方式主要有：

* XML 配置：`<bean lazy-init="true" ... />`；
* Java 注解：@Lazy(true)。

当Bean设置为延迟加载后，会在使用该Bean的时候才会去执行实例化和初始化，通常情况下只有Bean的方法或者字段被调用时才会触发实例化和初始化。

通过方法`BeanFactory#getBeanProvider(java.lang.Class<T>)`获取对象时，对于懒加载的对象不会执行实例化和初始化，会在调用getObject方法时才执行实例化和初始化。

## Spring Bean销毁

Spring Bean销毁触发的方法主要有以下三种：

* @PreDestroy 标注方法
* 实现 DisposableBean 接口的 destroy() 方法
* 自定义销毁方法
  * XML 配置：`<bean destroy="destroy" ... /> `
  * Java 注解：@Bean(destroy=”destroy”)
  * Java API：AbstractBeanDefinition#setDestroyMethodName(String)

销毁触发的执行顺序是：@PreDestroy > 实现 DisposableBean 接口 > 自定义销毁方法。

代码示例：[BeanDestroyDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/spring-bean/src/main/java/com/wkk/learn/spring/ioc/bean/definition/BeanDestroyDemo.java)

## 垃圾回收 Spring Bean

在ApplicationContext关闭之前，GC是不会回收Bean的，纵然显示的调用也是如此。而在ApplicationContext关闭之后，JVM会在垃圾回收周期中去回收掉Bean。

## 面试题

* 如何注册一个 Spring Bean？

  通过 BeanDefinition 和外部单体对象来注册。
  外部单体对象注册示例：[SingletonBeanRegistrationDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/spring-bean/src/main/java/com/wkk/learn/spring/ioc/bean/definition/SingletonBeanRegistrationDemo.java)
* 什么是 Spring BeanDefinition？

  保存Spring Bean的各种元信息，并且可以通过setter/getter修改和获取Bean的元信息。

* Spring 容器是怎样管理注册 Bean？

  后续论述。