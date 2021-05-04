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
