# Spring IoC依赖注入

## 依赖注入的模式和类型

### 依赖注入的模式

* 手动模式：通过配置或者编程的方式，提前设置注入规则。
  常见的方式：
  * XML资源配置元信息；
  * Java注解配置元信息；
  * API配置元信息。开发中不常用。

* 自动模式：实现方提供依赖自动关联的方式，按照内建的注入规则。
  * Autowiring（自动绑定）：这是Spring开发者提出但是不推荐使用的方式。

### 依赖注入的类型

|依赖注入类型| 配置元数据举例|
|--|--|--|
|Setter方法| `<proeprty name="user" ref="userBean"/>`|
|构造器| `<constructor|-arg name="user" ref="userBean" />`|
|字段| @Autowired User user;|
|方法| @Autowired public void user(User user) { ... }|
|接口回调| class MyBean implements BeanFactoryAware { ... }|

## 自动绑定（Autowiring）

Spring IoC容器可以自动维护Bean之前的依赖关系，包括Bean和Bean之间的关系和Bean和资源之前的关系。

Spring官方认为自动绑定有两个优点：

* 1.自动绑定可以减少手动绑定时需要指定属性和构造参数的需要。
* 2.自动绑定的属性可以跟随依赖的对象的更新而更新配置。（这一个看来不自动绑定的优势，而是Java引用传递的优势，Java中只有值传递，引用对象传递的是引用地址，这样对象属性更新时，保存这个引用指向的属性都会更新。）

### 自动绑定（Autowiring）模式

Spring IoC容器提供的自动绑定的模式可以通过注解类`org.springframework.beans.factory.annotation.Autowire`中查看，大致提供了四种自动绑定的模式：

* no：默认值，未激活 Autowiring，需要手动指定依赖注入对象。
* byName：根据被注入属性的名称作为 Bean 名称进行依赖查找，并将对象设置到该属性。
* byType：根据被注入属性的类型作为依赖类型进行查找，并将对象设置到该属性。
* constructor：特殊 byType 类型，用于构造器参数。

byName的缺陷，如果属性名称或Bean实例名称修改，对应的byName就会注入失败。
byType的缺陷，如果通过byType找到多个Bean实例，就需要根据@Primary注解，选择主要的Bean实例注入。

### 自动绑定的限制和不足

自动绑定这种依赖注入的方式不是官方所推荐的，在日常开发也很少使用，主要有以下限制和不足：

* 不支持原生类型的自动绑定，比如int、String、Class等。
* 自动绑定不能在运行前就检测需要自动绑定的Bean是否存在，只有在运行时才能发现，因此不能够在IDE等工具或文档上进行提示。
* 对于同一个类型有多个Bean实例时，无法确定绑定哪个Bean；

这里说的Autowiring使用不多指的是AbstractBeanDefinition的autowireMode这个属性默认是no，意思是一个spring bean里面的属性spring不自动注入，必须自己加注解（@Autowired）等方式注入。如果不为no，即使不使用@Autowired注解也能将属性注入。因为autowireMode为no，所以要使用@Autowired注解手动注入，这两个根本不是一个东西。

## Setter 方法注入

Setter方法注入的方式分为手动注入和自动注入：

* 手动模式
  * XML 资源配置元信息

    ```xml
    <bean id="userHolder" class="xxx">
        <property name="user" ref="user"/>
    </bean>
    ```

    代码示例：[XmlDependencySetterInjectionDemo.java](https://github.com/wkk1994/spring-learn/blob/master/dependency-injection/src/main/java/com/wkk/learn/spring/ioc/dependency/injection/XmlDependencySetterInjectionDemo.java)

  * Java 注解配置元信息

    ```java
    @Bean
    public UserHolder userHolder(User user) {
        UserHolder userHolder = new UserHolder();
        userHolder.setUser(user);
        return userHolder;
    }
    ```

    代码示例：[AnnotationDependencySetterInjectionDemo.java](https://github.com/wkk1994/spring-learn/blob/master/dependency-injection/src/main/java/com/wkk/learn/spring/ioc/dependency/injection/AnnotationDependencySetterInjectionDemo.java)

  * API 配置元信息

    ```java
    // 注册BeanDefinition
    BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(UserHolder.class);
    beanDefinitionBuilder.addPropertyReference("user", "user");
    applicationContext.registerBeanDefinition("userHolder",beanDefinitionBuilder.getBeanDefinition());    
    ```

    代码示例：[ApiDependencySetterInjectionDemo.java](https://github.com/wkk1994/spring-learn/blob/master/dependency-injection/src/main/java/com/wkk/learn/spring/ioc/dependency/injection/ApiDependencySetterInjectionDemo.java)

* 自动模式
  * byName

    ```xml
    <bean id="userHolder" class="xxx" autowire="byName" />
    ```

  * byType

    ```xml
    <bean id="userHolder" class="xxx" autowire="byType" />
    ```

  代码示例：[AutoWiringByNameDependencySetterInjectionDemo.java](https://github.com/wkk1994/spring-learn/blob/master/dependency-injection/src/main/java/com/wkk/learn/spring/ioc/dependency/injection/AutoWiringByNameDependencySetterInjectionDemo.java)

## 构造器注入

* 手动模式
  * XML 资源配置元信息
  * Java 注解配置元信息
  * API 配置元信息
* 自动模式
  * constructor

代码示例：[XmlDependencyConstructorInjectionDemo.java](https://github.com/wkk1994/spring-learn/blob/master/dependency-injection/src/main/java/com/wkk/learn/spring/ioc/dependency/injection/XmlDependencyConstructorInjectionDemo.java)

## 字段注入

字段注入在Spring官方没有提及，但是目前开发中用的比较多。字段注入的常见方式是通过注解的方式注入：@Autowired、@Resource、@Inject（可选，JSR-330引入的新的API）

```java
@Autowired// 通过 @Autowired 注解实现字段注入
private
// static @Autowired会忽略静态字段
UserHolder userHolder;

@Resource
private
// static @Resource不支持静态字段
UserHolder userHolder2;
```

代码示例：[AnnotationDependencyFieldInjectionDemo.java](https://github.com/wkk1994/spring-learn/blob/master/dependency-injection/src/main/java/com/wkk/learn/spring/ioc/dependency/injection/AnnotationDependencyFieldInjectionDemo.java)

> @Autowired会忽略静态字段；@Resource不支持静态字段，会抛出错误。

## 方法注入

* Java 注解配置元信息
  * @Autowired
  * @Resource
  * @Inject（可选）
  * @Bean
  
```java
@Autowired
public void initUserHolder(UserHolder userHolder) {
    this.userHolder = userHolder;
}

@Resource
public void initUserHolder2(UserHolder userHolder2) {
    this.userHolder2 = userHolder2;
}
```

代码示例：[AnnotationDependencyMethodnjectionDemo.java](https://github.com/wkk1994/spring-learn/blob/master/dependency-injection/src/main/java/com/wkk/learn/spring/ioc/dependency/injection/AnnotationDependencyMethodnjectionDemo.java)

方法注入会根据方法的参数类型进行依赖注入，不关心方法的名称。

> 方法注入和Setter注入都是通过反射的方式执行的，不同的是Setter依赖JavaBeans。

## 接口回调注入

Spring提供了Aware系列接口，通过实现不同的Aware接口，可以在回调的时候注入内建的Bean。

|內建接口| 说明|
|--|--|
|BeanFactoryAware| 获取 IoC 容器 - BeanFactory|
|ApplicationContextAware| 获取 Spring 应用上下文 - ApplicationContext 对象|
|EnvironmentAware| 获取 Environment 对象|
|ResourceLoaderAware| 获取资源加载器 对象 - ResourceLoader|
|BeanClassLoaderAware| 获取加载当前 Bean Class 的 ClassLoader |
|BeanNameAware| 获取当前 Bean 的名称|
|MessageSourceAware| 获取 MessageSource 对象，用于 Spring 国际化|
|ApplicationEventPublisherAware| 获取 ApplicationEventPublishAware 对象，用于 Spring 事件|
|EmbeddedValueResolverAware| 获取 StringValueResolver 对象，用于占位符处理|

代码示例：[AwareInterfaceDependencyInjectionDemo.java](https://github.com/wkk1994/spring-learn/blob/master/dependency-injection/src/main/java/com/wkk/learn/spring/ioc/dependency/injection/AwareInterfaceDependencyInjectionDemo.java)

## 依赖注入类型选择

如何在使用中进行依赖注入类型的选择：

* 构造器注入：能够保证注入顺序，适合依赖注入比较少的情况，比如只需要注入3个对象，不够构造器注入无法解决循环依赖的问题。
* Setter注入：适合多依赖的情况，不能保证注入的顺序性。
* 字段注入：注入方便，但是Spring不推荐。
* 方法注入：推荐在声明类的时候使用该方式，比如@Bean注解声明一个类时。

## 基础类型注入

Spring支持的基础类型的注入有下面四类：

* 原生类型（Primitive）：boolean、byte、char、short、int、float、long、double。
* 标量类型（Scalar）：Number(Long、Integer等子类)、Character、Boolean、Enum、Locale、Charset、Currency、Properties、UUID。
* 常规类型（General）：Object、String、TimeZone、Calendar、Optional 等。
* Spring 类型：Resource、InputSource、Formatter 等这些是Spring自己提供的类型。

代码示例：[BaseTypeDependencyInjectionDemo.java](https://github.com/wkk1994/spring-learn/blob/master/dependency-injection/src/main/java/com/wkk/learn/spring/ioc/dependency/injection/BaseTypeDependencyInjectionDemo.java)

> Enum类型实际上是使用final static来定义常量，可以通过javap进行查看。

## 集合类型注入

Spring支持的集合类型的注入：

* 数组类型（Array）：原生类型、标量类型、常规类型、Spring 类型。
* 集合类型（Collection）：
  * Collection：List、Set（SortedSet、NavigableSet、EnumSet）
  * Map：Properties

代码示例：[BaseTypeDependencyInjectionDemo.java](https://github.com/wkk1994/spring-learn/blob/master/dependency-injection/src/main/java/com/wkk/learn/spring/ioc/dependency/injection/BaseTypeDependencyInjectionDemo.java)

## 限定注入

Spring的@Qualifier注解不仅可以根据Bean名称进行限定注入，还可以实现逻辑上的分组。

* 使用注解 @Qualifier 限定
  * 通过 Bean 名称限定
    添加@Qualifier，如果存在多个Bean实例，会根据Bean名称进行依赖注入。

    ```java
    @Autowired
    @Qualifier("user")
    private User user1;    
    ```

  * 通过分组限定
    在声明Bean的时候添加@Qualifier注解，可以将Bean实例进行分组，添加@Qualifier注解的Bean只能被在依赖注入时被@Qualifier注解修饰的变量注入。

    ```java
      @Autowired
      private Collection<User> allUsers; // 2 Beans = user + superUser
  
      @Autowired
      @Qualifier
      private Collection<User> qualifierUsers; // 2 Beans = user1 + user2
  
      @Bean
      @Qualifier
      public User user1() {
          User user = new User();
          user.setId("1");
          user.setName("user1");
          return user;
      }
  
      @Bean
      @Qualifier
      public User user2() {
          User user = new User();
          user.setId("2");
          user.setName("user2");
          return user;
      }
    ```

* 基于注解 @Qualifier 扩展限定
  * 自定义注解 - 如 Spring Cloud @LoadBalanced

代码示例：[QualifierDependencyInjectionDemo.java](https://github.com/wkk1994/spring-learn/blob/master/dependency-injection/src/main/java/com/wkk/learn/spring/ioc/dependency/injection/QualifierDependencyInjectionDemo.java)

> 为什么上面allUsers只会注入2个Bean实例？
> @Autowired 依赖注入，默认是先不找当前配置类内定义的，如果在其他地方找到了，例如 XML 配置文件，或者你自己注册的一个 Bean，那么当前配置类内定义的就会被忽略，如果在别的地方都找不到，才会来考虑当前配置类中定义的，这应该属于一个兜底吧，如果不这么做，就会报错了，因为 @Autowired 的 required 属性默认是 true。

## 延迟依赖注入

* 使用 API ObjectFactory 延迟注入
  * 单一类型
  * 集合类型
* 使用 API ObjectProvider 延迟注入（推荐）
  * 单一类型
  * 集合类型

ObjectProvider是ObjectFactory的子类，并在ObjectFactory上进行了扩展。

> 在Spring源码中，大量非必要的依赖使用ObjectProvider进行依赖注入，这样可以避免一些非必要bean注入提升NoSuchBeanExecption的错误。

## 依赖处理过程

分析Spring IoC依赖处理的过程，先入为主的概念：

* 入口 - DefaultListableBeanFactory#resolveDependency：依赖处理的入口。
* 依赖描述符 - DependencyDescriptor：描述依赖信息的类，主要包含字段名或方法名或参数类型和参数索引、是否必须、是否实时、依赖注入的包含类等。
* 自定绑定候选对象处理器 - AutowireCandidateResolver

分析依赖处理的过程：

* DefaultListableBeanFactory#resolveDependency依赖处理的入口，根据DependencyType（依赖的类型）不同的逻辑实现。
* 默认逻辑，区分是否是Lazy的
  * 如果是Lazy返回一个延迟加载的代理类。
  * DefaultListableBeanFactory#doResolveDependency：非延迟加载
    * DefaultListableBeanFactory#resolveMultipleBeans：检查是否是查找多个实例的集合类型，并返回多实例的集合对象。
    * DefaultListableBeanFactory#findAutowireCandidates：获取对应DependencyType类型的所有Bean实例的名称。
    * DefaultListableBeanFactory#determineAutowireCandidate：获取primary的Bean实例名称。
    * 根据primary的Bean名称获取实例并返回。
