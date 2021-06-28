# Spring Environment抽象

## 理解Spring Environment抽象

Spring3.1开始引入的Environment抽象，它主要有两个作用：统一的Spring配置属性管理和条件化SpringBean装配管理。

* 统一的Spring配置属性管理

  Environment相关的API统一了Spring配置属性的管理，包括占位符处理和类型转换，不仅完整地替换了PropertyPlaceholderConfigurer，而且还支持更丰富的配置属性源（PropertySource）。

* 条件化SpringBean装配管理

  通过Environment Profiles信息，帮助Spring容器提供条件化地装配Bean。

## Spring Environment接口使用场景

* 用于属性占位符处理：PropertyResolver#resolvePlaceholders方法可以将${xxx}中的xxx转换为对应的属性值。

* 用于转换Spring配置属性类型：PropertyResolver#getProperty的多个重载方法可以根据key获取属性值，并转换为指定的Class。

* 用户存储Spring属性配置源（PropertySource）：方法getActiveProfiles和getDefaultProfiles可以获取Profiles。

* 用于Profiles状态的维护：Profiles状态的维护由Environment的实现AbstractEnvironment实现，AbstractEnvironment的方法setActiveProfiles和setDefaultProfiles可以对Profiles进行维护。

## Environment占位符处理

在Spring3.1之前占位符的处理：

* 组件：org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
* 接口：org.springframework.util.StringValueResolver

在Spring3.1之后占位符处理：

* 组件：org.springframework.context.support.PropertySourcesPlaceholderConfigurer
* 接口：org.springframework.util.StringValueResolver

PropertyPlaceholderConfigurer和PropertySourcesPlaceholderConfigurer都继承了PlaceholderConfigurerSupport抽象类。

PropertyPlaceholderConfigurer占位符处理示例：
[PropertyPlaceholderConfigurerDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/environment/src/main/java/com/wkk/learn/spring/ioc/environment/PropertyPlaceholderConfigurerDemo.java)

## 理解条件配置Spring Profiles

核心接口：org.springframework.core.env.ConfigurableEnvironment

* 修改：addActiveProfile、setActiveProfiles、setDefaultProfiles
* 获取：getActiveProfiles、getDefaultProfiles
* 匹配：acceptsProfiles、acceptsProfiles

注解：@Profile

> 外部化配置：不在程序内部产生的配置都是外部化配置，这部分的外部化配置可能是来自运行时传入的参数、外部化文件。

## Spring4重构@Profile实现

Spring4以后对@Porfile的实现为：通过在@Profile注解上进行元标注@Conditional，指定它的Condition实现为ProfileCondition，在通过ProfileCondition实现对@Profile条件的判断。

## 依赖注入Environment

直接依赖注入：

* 通过EnvironmentAware接口回调注入Environment实例
* 通过@Autowired注入Environment

ApplicationContext中持有Environment的示例，因此可以通过注入ApplicationContext间接依赖注入：

* 通过ApplicationContextAware接口回调
* 通过@Autowired注入ApplicationContext

依赖注入Environment示例：
[InjectionEnvironmentDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/environment/src/main/java/com/wkk/learn/spring/ioc/environment/InjectionEnvironmentDemo.java)

## 依赖查找Environment

* 直接依赖查找：通过org.springframework.context.ConfigurableApplicationContext#ENVIRONMENT_BEAN_NAME作为Bean Name进行查找。
* 间接依赖查找：通过ApplicationContext示例的getEnvironment方法间接获取Environment实例。

依赖查找Environment示例：
[LookupEnvironmentDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/environment/src/main/java/com/wkk/learn/spring/ioc/environment/LookupEnvironmentDemo.java)

## 依赖注入@Value

通过注入@Value的实现类：AutowiredAnnotationBeanPostProcessor#postProcessProperties。

**@Value的实现细节：**

TODO

@Value注解示例：[ValueAnnotationDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/environment/src/main/java/com/wkk/learn/spring/ioc/environment/ValueAnnotationDemo.java)

## String类型转换在Environment中的运用

Environment的底层实现：

参考AbstractEnvironment。

Environment的能力依赖于org.springframework.core.env.PropertySourcesPropertyResolver，相当于装饰器模式，将PropertySourcesPropertyResolver作为内部属性依赖，对外的能力委托给PropertySourcesPropertyResolver处理。

参考源码PropertySourcesPropertyResolver#getProperty(jString, Class, boolean)
MutablePropertySources是PropertySource的迭代器，保存了所有的PropertySource，PropertySourcesPropertyResolver通过MutablePropertySources获取PropertySource的迭代器，然后迭代获取参数值。

PropertySourcesPropertyResolver的类型转换方法为convertValueIfNecessary，它也是使用org.springframework.core.convert.ConversionService实现类型转换的，如果ConversionService的实现为空，则使用DefaultConversionService。

## Spring类型转换在@Value中的运用

@Value底层实现：

核心API：`org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor`
- `org.springframework.beans.factory.support.DefaultListableBeanFactory#doResolveDependency`

底层服务：`org.springframework.beans.TypeConverter`

* 默认实现：org.springframework.beans.TypeConverterDelegate
  * java.beans.PropertyEditor
  * org.springframework.core.convert.ConversionService

@Value的类型转换最终也是依赖PropertyEditor或ConversionService来实现的，如果PropertyEditor不为空就使用PropertyEditor。

## Spring配置属性源PropertySource

API：

* 单配置属性源：org.springframework.core.env.PropertySource

  PropertySource包含name和source属性，name不允许重复，source代表属性的来源，可以是Map或者文件等，根据实现类不同source也不同。

* 多配置属性源：org.springframework.core.env.PropertySources

  它实现了迭代器模式，可以认为它会保存多个PropertySource，并且保存的PropertySource是有顺序的，在进行属性获取的时候，按照顺序对PropertySource进行属性获取，获取到之后就不会继续遍历了。

注解：

* 单配置属性源：org.springframework.context.annotation.PropertySource

  注解用来关联资源，资源可以是文件或者其他任何语义。

* 多配置属性源：org.springframework.context.annotation.PropertySources

关联：

* 存储对象：org.springframework.core.env.MutablePropertySources
* 关联方法：org.springframework.core.env.ConfigurableEnvironment#getPropertySources
