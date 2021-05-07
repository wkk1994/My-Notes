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

## Spring Bean实例化阶段

### 实例化前阶段

在Bean的实例化前阶段，会调用`InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation`方法，如果该方法返回的对象不为null，就不会执行bean接下来的实例化，用返回的对象作为Bean实例化的对象。

[InstantiationAwareBeanPostProcessorDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/spring-bean/src/main/java/com/wkk/learn/spring/ioc/bean/cyclelife/InstantiationAwareBeanPostProcessorDemo.java)

### 实例化阶段

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