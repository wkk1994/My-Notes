# Spring Bean生命周期

## Spring Bean元信息配置阶段

BeanDefinition的配置方式：

* 面向资源
  * XML配置方式：直接通过xml定义bean信息`<bean > </bean>`
  * properties资源的配置方式：和xml的配置类似，也需要手动加载配置文件，只不过配置文件的格式不同。
* 面向注解：使用@Bean、@Component、@Configuration等方式声明的bean。
* 面向API：[ApiregistryBeanDefinitionDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/spring-bean/src/main/java/com/wkk/learn/spring/ioc/bean/definition/ApiregistryBeanDefinitionDemo.java)

## Spring Bean元信息的解析阶段

* 面向资源 BeanDefinition 解析
  * BeanDefinitionReader
  * XML 解析器 - BeanDefinitionParser
* 面向注解 BeanDefinition 解析
  * AnnotatedBeanDefinitionReader：该类是注解BeanDefinition解析的类，是单独实现的并没有实现`BeanDefinitionReader`，因为注解BeanDefinition的解析不需要从文件中获取，所以单独实现，AnnotatedBeanDefinitionReader的默认的Bean名称生成器是`AnnotationBeanNameGenerator`，可以通过`AnnotatedBeanDefinitionReader#setBeanNameGenerator`方法设置自定义的Bean名称生成器。

## Spring Bean元信息注册

Spring BeanDefinition的注册接口为`BeanDefinitionRegistry`，`DefaultListableBeanFactory`实现了该接口。注册方法为`DefaultListableBeanFactory#registerBeanDefinition`。

注册过程：

* BeanDefinition信息的校验；
* beanDefinitionMap是否已经存在了指定beanName的BeanDefinition信息；
* 已经存在的话检查是否可以覆盖`isAllowBeanDefinitionOverriding`，可以覆盖的话就直接put到beanDefinitionMap中；不能覆盖抛出BeanDefinitionOverrideException异常；
* 不存在时，检查BeanFactory是否在创建Bean实例过程中，如果是则需要加锁（beanDefinitionMap）进行put操作，不是直接对beanDefinitionMap进行put操作。在这个put操作后还会对beanDefinitionNames进行add操作，将beanName放到list中；
* 如果beanName的元信息已经存在，并且已经实例化完成，则重新设置BeanDefinition的信息。

> beanDefinitionMap使用ConcurrentHashMap实现，是线程安全的，但是如果BeanFactory是在初始化bean实例过程中对beanDefinitionMap进行put操作，可能导致BeanFactory在其他线程中正在遍历beanDefinitionMap的操作失败，所以在对beanDefinitionMap进行put操作时候会加锁。
> beanDefinitionNames的作用：因为beanDefinitionMap是无序的，所以通过一个FIFO的list保存beanName声明的顺序。

## Spring BeanDefinition 合并阶段

Spring BeanDefinition的合并主要是进行父子BeanDefinition的合并，在合并过程中存在层次性查找的方式（从父BeanFactory中获取BeanDefinition），以及通过缓存加速查找过程（使用线程安全的ConcurrentHashMap存放已经merged查找过的BeanDefinition，mergedBeanDefinitions），最终返回的BeanDefinition都是`RootBeanDefinition`。

主要逻辑代码：`AbstractBeanFactory#getMergedBeanDefinition(java.lang.String)`

实现过程：

* 检查当前BeanFactory的beanDefinitionMap中是否有对应的BeanDefinition的定义，没有就从父BeanFactory中获取合并后的BeanDefinitio并返回。
* 从本地BeanFactory中获取合并的BeanDefinition；
  * 先从缓存`mergedBeanDefinitions`中获取，有并且没有过期就返回；
  * 调用`AbstractBeanFactory#getMergedBeanDefinition(java.lang.String, org.springframework.beans.factory.config.BeanDefinition, org.springframework.beans.factory.config.BeanDefinition)`方法开始获取合并的BeanDefinition；
  * 这里会通过互斥锁mergedBeanDefinitions的方式获取合并的BeanDefinitin；
  * 首先从缓存mergedBeanDefinitions获取，如果存在并且没有过期返回合并的BeanDefinitin；
  * 否则，检查BeanDefinition的parentName是否为空，为空的话就将当前的BeanDefinition转换成RootBeanDefinition，并作为合并的BeanDefinitin返回。
  * BeanDefinition的parentName不为空，获取合并后的parentName的BeanDefinition，这里相当于递归调用，将parentName的BeanDefinition包装成RootBeanDefinition，并使用当前的BeanDefinition覆写RootBeanDefinition，作为合并的BeanDefinitin返回。
  * 将得到的BeanDefinition，缓存到mergedBeanDefinitions中。

> 在获取parent的BeanDefinition时，会检查当前的parentName是否和正在获取BeanDefinition的beanName一致，如果一致就会从ParentBeanFactory中去查询MergedBeanDefinition，因为在一个BeanFactory中不允许beanName重复的BeanDefinition，所以会进行一个层次性的查找。

## Spring Bean Class加载阶段

Spring Bean Class加载阶段发生在`AbstractBeanFactory#doGetBean`方法调用时，最终实现Class加载的核心方法为`AbstractBeanFactory#resolveBeanClass`。Spring Bean Class加载的最终处理还是由Java ClassLoader进行类加载，只不过在加载之前进行了Java安全的校验，其中还有Spring自己实现的tempClassLoader。

说明：

* AbstractBeanDefinition中的beanClass字段使用的是Object类型，可以存放当前的className或者Class对象，当类被加载后beanClass就会指向加载后的Class对象，这样通过判断beanClass是否是Class类型的就知道类是否被加载过了。
* tempClassLoader是ConfigurableBeanFactory的一个临时的ClassLoader。
* Spring Bean Class在加载的时候默认是不进行初始化的，`Class.forName(name, false, clToUse)`。

## Spring Bean实例化前阶段

在Bean的实例化前阶段，会调用`InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation`方法，如果该方法返回的对象不为null，就不会执行bean接下来的实例化，用返回的对象作为Bean实例化的对象。

[InstantiationAwareBeanPostProcessorDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/spring-bean/src/main/java/com/wkk/learn/spring/ioc/bean/cyclelife/InstantiationAwareBeanPostProcessorDemo.java)

## Spring Bean实例化阶段

Spring Bean实例化方式通常有两种：

* 传统实例化方式：通过实例化策略进行实现，`InstantiationStrategy`。
* 构造器依赖注入实例化方式。

传统的实例化注入方式是通过默认无参数的构造器注入，实现方法`SimpleInstantiationStrategy#instantiate(org.springframework.beans.factory.support.RootBeanDefinition, java.lang.String, org.springframework.beans.factory.BeanFactory)`，最终还是通过Java反射执行Constructor对象的newInstance方法。

构造器依赖注入实现方式，是通过获取对应的构造器方法进行实例化对象。也是通过Java反射执行Constructor对象的newInstance方法。

## Spring Bean 实例化后阶段

通过方法`InstantiationAwareBeanPostProcessor#postProcessAfterInstantiation`可以控制Bean实例化后属性赋值操作。

方法默认返回true，如果方法返回false，表示属性赋值会被跳过。

实现细节：`AbstractAutowireCapableBeanFactory#populateBean`

示例代码：[InstantiationAwareBeanPostProcessorDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/spring-bean/src/main/java/com/wkk/learn/spring/ioc/bean/cyclelife/InstantiationAwareBeanPostProcessorDemo.java)

## Spring Bean属性赋值前阶段

PropertyValues保存了Bean属性值元信息，Spring提供了扩展机制，可以在属性赋值之前对PropertyValues进行操作：

* Spring 1.2 - 5.0：InstantiationAwareBeanPostProcessor#postProcessPropertyValues

  方法如果返回null，表示不对属性值进行设置，直接返回。

* Spring 5.1：InstantiationAwareBeanPostProcessor#postProcessProperties

  方法如果返回null，表示不对属性值进行修改，按照原先的PropertyValue进行属性填充。

实现细节：`AbstractAutowireCapableBeanFactory#populateBean`

示例代码：[MyInstantiationAwareBeanPostProcessor#postProcessProperties](https://github.com/wkk1994/spring-ioc-learn/blob/master/spring-bean/src/main/java/com/wkk/learn/spring/ioc/bean/cyclelife/InstantiationAwareBeanPostProcessorDemo.java)

## Spring Bean Aware 接口回调阶段

Spring提供了多个BeanAware接口在适当的时机进行回调：

* BeanNameAware
* BeanClassLoaderAware
* BeanFactoryAware
* EnvironmentAware
* EmbeddedValueResolverAware
* ResourceLoaderAware
* ApplicationEventPublisherAware
* MessageSourceAware
* ApplicationContextAware

BeanNameAware、BeanClassLoaderAware、BeanFactoryAware的回调属于BeanFactory的回调，所以在BeanFactory时会回调，但是其他的Aware属于ApplicationContext的回调机制，必须要在ApplicationContext中进行回调。

BeanNameAware、BeanClassLoaderAware、BeanFactoryAware的回调顺序为BeanNameAware -> BeanClassLoaderAware -> BeanFactoryAware，参考方法`AbstractAutowireCapableBeanFactory#invokeAwareMethods`

```java
if (bean instanceof Aware) {
  if (bean instanceof BeanNameAware) {
    ((BeanNameAware) bean).setBeanName(beanName);
  }
  if (bean instanceof BeanClassLoaderAware) {
    ClassLoader bcl = getBeanClassLoader();
    if (bcl != null) {
      ((BeanClassLoaderAware) bean).setBeanClassLoader(bcl);
    }
  }
  if (bean instanceof BeanFactoryAware) {
    ((BeanFactoryAware) bean).setBeanFactory(AbstractAutowireCapableBeanFactory.this);
  }
}
```

剩下的顺序参考：`ApplicationContextAwareProcessor#invokeAwareInterfaces`

示例代码：[BeanAwareDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/spring-bean/src/main/java/com/wkk/learn/spring/ioc/bean/cyclelife/BeanAwareDemo.java)

## Spring Bean初始化前阶段

Spring Bean初始化前阶段，属性设置完成阶段会回调方法`BeanPostProcessor#postProcessBeforeInitialization`。如果方法返回的bean对象不为空，就用返回的bean对象替换原来的bean实例对象，通过这个方法可以对bean进行代理增强，返回代理的bean实例对象。

实现细节：`AbstractAutowireCapableBeanFactory#applyBeanPostProcessorsBeforeInitialization`

```java
  @Override
  public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
      throws BeansException {

    Object result = existingBean;
    for (BeanPostProcessor processor : getBeanPostProcessors()) {
      Object current = processor.postProcessBeforeInitialization(result, beanName);
      if (current == null) {
        return result;
      }
      result = current;
    }
    return result;
  }
```

示例代码：[MyInstantiationAwareBeanPostProcessor#postProcessBeforeInitialization](https://github.com/wkk1994/spring-ioc-learn/blob/master/spring-bean/src/main/java/com/wkk/learn/spring/ioc/bean/cyclelife/BeanAMyInstantiationAwareBeanPostProcessorwareDemo.java)

> 通过XmlBeanDefinitionReader loadBeanDefinitions是将xml中bean对应的beanDefinition注册到beanFactory中,底层通过BeanDefinitionReaderUtils#registerBeanDefinition()方法实现,这个在BeanDefinition注册阶段讲过,这个时候只是注册beanDefinition没有像ApplicationContext.refresh()中的registerBeanPostProcessors()将bean post Processor添加到beanFactory的beanPostProcessors list集合中操作,所以xml读取的时候需要手动的addBeanPostProcessor;如果通过ClassPathXmlApplicationContext创建ApplicationContext的方式xml中定义MyInstantiationAwareBeanPostProcessor是可以的,因为ClassPathXmlApplicationContext创建时会执行refresh()操作会从beanFactory中找到MyInstantiationAwareBeanPostProcessor bean后添加到beanPostProcessors的list集合中

## Spring Bean初始化阶段

Spring Bean初始化阶段的回调方式有三种：

* @PostConstruct标注的方法

  @PostConstruct注解需要注解驱动的支持，即`CommonAnnotationBeanPostProcessor`

* 实现InitializingBean接口的afterPropertiesSet() 方法

* 自定义初始化方法

@PostConstruct不仅需要注解驱动，它的实现方式是在postProcessBeforeInitialization里调用的，通过获取被注解定义的method，然后执行method的invoke方法实现调用。核心实现API：`InitDestroyAnnotationBeanPostProcessor#postProcessBeforeInitialization`。

实现InitializingBean接口和自定义初始化方式的实现，都是在同一个方法中进行调用的，也是通过反射的形式，核心实现API：`AbstractAutowireCapableBeanFactory#invokeInitMethods`。

## Spring Bean 初始化后阶段

Spring Bean 初始化后阶段的回调方法是`BeanPostProcessor#postProcessAfterInitialization`。

实现API：`AbstractAutowireCapableBeanFactory#applyBeanPostProcessorsAfterInitialization`。

## Spring Bean初始化总结

org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#initializeBean(java.lang.String, java.lang.Object, org.springframework.beans.factory.support.RootBeanDefinition)

## Spring Bean初始化完成阶段

Spring4.1版本提供了`SmartInitializingSingleton#afterSingletonsInstantiated`的方法回调，可以在Bean初始化完成时进行回调处理，这个回调方法通过方法`DefaultListableBeanFactory#preInstantiateSingletons`进行触发，通常在BeanFactory中不会被触发，需要手动调用preInstantiateSingletons方法，在ApplicationContext中会主动调用beanFactory的preInstantiateSingletons方法进行触发。

核心实现是：`DefaultListableBeanFactory#preInstantiateSingletons`，实现细节会根据注册的beanDefinitionNames列表，对Bean进行逐一创建（getBean方法），然后判断Bean是否实现了`SmartInitializingSingleton#afterSingletonsInstantiated`，进行调用。

> `DefaultListableBeanFactory#preInstantiateSingletons`方法提前对注册的BeanDefinition进行了实例化初始化，使得在`SmartInitializingSingleton#afterSingletonsInstantiated`方法中可以获取到最终初始化完整的Bean实例，比较推荐在这里进行Bean初始化的扩展，可以避免后续再有方法对Bean实例进行修改。

## Spring Bean销毁前阶段

Spring提供回调方法`DestructionAwareBeanPostProcessor#postProcessBeforeDestruction`在Bean实例被销毁前进行调用。可以调用`AbstractBeanFactory#destroyBean(java.lang.String, java.lang.Object)`主动销毁Bean实例，销毁Bean只是调用Bean的销毁方法，Bean并不会被GC回收，也不会从IOC容器中除移。

核心实现：`DisposableBeanAdapter#destroy`。

示例代码：[MyDestructionAwareBeanPostProcessor](https://github.com/wkk1994/spring-ioc-learn/blob/master/spring-bean/src/main/java/com/wkk/learn/spring/ioc/bean/cyclelife/MyDestructionAwareBeanPostProcessor.java)

## Spring Bean销毁阶段

和Spring Bean初始化阶段一样，Spring也提供了销毁阶段的三个回调方式：

* @PreDestroy标注方法

  @PreDestroy注解需要注解驱动的支持，即`CommonAnnotationBeanPostProcessor`

* 实现DisposableBean接口的destroy()方法

* 自定义销毁方法

示例代码：[UserHolder](https://github.com/wkk1994/spring-ioc-learn/blob/master/spring-bean/src/main/java/com/wkk/learn/spring/ioc/bean/cyclelife/UserHolder.java)

> `ConfigurableBeanFactory#destroySingletons`方法会清除 Spring BeanFactory 缓存的 Bean 对象

## Spring Bean垃圾收集

Spring Bean垃圾回收的过程：

* 关闭Spring容器（上下文）；
* 执行GC；
* Spring Bean覆盖finlize()方法被回调。

> 在SpringBoot或SpringCloud中上下文是可以被替换的，在替换时需要注意Bean是否被真正引用到，或者被垃圾回收，防止内存泄漏。

## 面试题

* BeanPostProcessor 的使用场景有哪些？

  BeanPostProcessor提供了Spring Bean的初始化前和初始化后生命周期回调，分别对应postProcessBeforeInitialization 以及postProcessAfterInitialization 方法，允许对关心的 Bean 进行扩展，甚至是替换。其中，ApplicationContext 相关的 Aware 回调也是基于BeanPostProcessor 实现，即 ApplicationContextAwareProcessor。

* BeanFactoryPostProcessor 与BeanPostProcessor 的区别

  BeanFactoryPostProcessor 是 Spring BeanFactory（实际为ConfigurableListableBeanFactory） 的后置处理器，用于扩展BeanFactory，或通过 BeanFactory 进行依赖查找和依赖注入。BeanFactoryPostProcessor 必须有 Spring ApplicationContext 执行，BeanFactory 无法与其直接交互。
  而 BeanPostProcessor 则直接与BeanFactory 关联，属于 N 对 1 的关系。

* BeanFactory 是怎样处理 Bean 生命周期？

  BeanFactory的默认实现为DefaultListableBeanFactory，其中Bean生命周期与方法映射关系为：

  * BeanDefinition注册阶段：调用`DefaultListableBeanFactory#registerBeanDefinition`
  * BeanDefinition合并阶段：调用`AbstractBeanFactory#getMergedBeanDefinitio`
  * Bean实例化前回调：调用`AbstractAutowireCapableBeanFactory#resolveBeforeInstantiation`，该方法最终会逐一调用`BeanPostProcessor#postProcessBeforeInitialization`，如果方法返回了bean实例对象，直接作为Bean实例返回。
  * Bean实例化阶段：调用`AbstractAutowireCapableBeanFactory#createBeanInstance`方法。
  * Bean实例化后阶段：调用`AbstractAutowireCapableBeanFactory#populateBean`方法，先根据`InstantiationAwareBeanPostProcessor#postProcessAfterInstantiation`返回值，决定是否执行属性赋值操作，如果返回false表示跳过属性赋值，默认返回true。
  * Bean属性赋值前阶段：调用`AbstractAutowireCapableBeanFactory#populateBean`方法，逐一调用`InstantiationAwareBeanPostProcessor#postProcessProperties`，该方法如果返回不为null，使用返回值作为新的PropertyValues对实例进行属性赋值。
  * Bean属性赋值阶段：调用`AbstractAutowireCapableBeanFactory#populateBean`方法。
  * Bean Aware接口回调阶段：调用`AbstractAutowireCapableBeanFactory#initializeBean`，根据bean实现的Aware接口进行回调。
  * Bean初始化前阶段：调用`AbstractAutowireCapableBeanFactory#initializeBean`，最终会调用`BeanPostProcessor#postProcessBeforeInitialization`，ApplicationContextAware接口的回调也是在这个时候触发。
  * Bean初始化阶段：调用`AbstractAutowireCapableBeanFactory#initializeBean`，最终`AbstractAutowireCapableBeanFactory#invokeInitMethods`，依次执行Bean定义的初始化方法。
  * Bean初始化后阶段：调用`AbstractAutowireCapableBeanFactory#initializeBean`，最终调用`BeanPostProcessor#postProcessAfterInitialization`方法。
  * Bean初始化完成阶段：调用`DefaultListableBeanFactory#preInstantiateSingletons`，最终调用`SmartInitializingSingleton#afterSingletonsInstantiated`方法。
  * Bean销毁前阶段：调用`AbstractBeanFactory#destroyBean`，最终调用`DestructionAwareBeanPostProcessor#postProcessBeforeDestruction`。
  * Bean销毁阶段：调用`AbstractBeanFactory#destroyBean`，最终调用Bean定义的销毁方法。
