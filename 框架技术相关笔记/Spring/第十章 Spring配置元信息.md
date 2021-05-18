# Spring配置元信息(Configuration Metadata)

## Spring配置元信息

Spring配置元信息大致可以分为五类，分别是：

* Spring Bean配置元信息，代表的类是BeanDefinition；
* Spring 属性元信息，代表的类是PropertyValues
* Spring容器配置元信息，这类的元信息是没有API可以直接操作的，它是作用于IoC容器，可以将所有的配置元信息都看作是Spring容器的配置元信息。
* Spring外部化配置元信息，代表类PropertySource，这一类元信息是用来控制Spring的配置的来源，以及读取顺序的。
* Spring Profile元信息，Spring3.1新添加的，代表类@Profile，用来在不同环境激活不同的配置。

## Spring Bean配置元信息

Spring Bean的配置元信息的接口是BeanDefinition，Spring中的实现方式有三种，分别是GenericBeanDefinition、RootBeanDefinition、AnnotatedBeanDefinition。

* GenericBeanDefinition：通用型的BeanDefinition，是默认的BeanDefinition实现方式，除了标准实现外，还提供了parentName属性，可以配置Parent BeanDefinition。

* RootBeanDeinition：没有parent的BeanDefinition或者合并后的BeanDefinition。它的setParentName方法不能调用，会抛出异常。

* AnnotatedBeanDefinition：注解标注的BeanDefinition，

  有三个实现分别是AnnotatedGenericBeanDefinition：基于注解的方式读取BeanDefinition，ConfigurationClassBeanDefinition：基于configclass的方式读取BeanDefinition，ScannedGenericBeanDefinition基于包扫描的方式读取BeanDefinition。

## Spring Bean属性元信息

和Spring Bean属性元信息相关的有：

* Bean属性元信息：PropertyValues，它实现了Iterable，内部有多个PropertyValue作为元素成员组合而成，并且有一个MutablePropertyValues实现，可以通过这个实现对PropertyValue进行修改。

* Bean属性上下文存储：AttributeAccessor，BeanDefinition都实现了AttributeAccessor接口，通过`AttributeAccessor#setAttribute`方法可以将属性存储到BeanDefinition中，而且不会对Bean的实例化初始化有影响，可以在需要的时候从BeanDefinition中进行获取。

* Bean元信息元素：BeanMetadataElement，只有一个方法`BeanMetadataElement#getSource`，BeanDefinition中都实现了改方法，可以设置属性的来源，在合适的时机进行获取使用。

示例代码：[BeanConfigurationMetadataDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/configuration-metadata/src/main/java/com/wkk/learn/spring/ioc/configuration/metadata/BeanConfigurationMetadataDemo.java)

## Spring容器配置元信息

Spring中的xml文件的配置的xmlns和xsd之间的关系，以及xml文件的解析类`BeanDefinitionParserDelegate`，解析类中对默认值的处理方式。

> 什么是`outter beans`？在Spring的xml文件中，通过import导入的xml配置，当前xml配置就是导入的xml文件的`outter beans`。
> schema和dtd的区别

## 基于XML资源装载SpringBean配置元信息

在XMl资源中，与SpringBean配置元信息相关的xml元素有：

|xml元素|使用场景|
|--|--|
|`<beans:beans/>`|单个xml资源下多个spring bean配，beans只能作为xml配置的最后一项，即beans元素后面不应该再有其他非beans元素|
|`<bean:bean/>`|单个SpringBean定义（BeanDefinition）配置|
|`<bean:alias/>`|为SpringBean定义（BeanDefinition）映射别名|
|`<beans:import/>`|加载外部SpringXml资源配置|

基于xml资源的解析类是`XmlBeanDefinitionReader`，核心实现方法`DefaultBeanDefinitionDocumentReader#doRegisterBeanDefinitions`，最终还是依赖`BeanDefinitionParserDelegate`对不同的xml元素进行解析，支持的元素有`import`、`alias`、`bean`、`beans`，并且根据不同的xml元素进行不同的处理，比如在处理`bean`xml元素时，会根据dom解析成BeanDefinitionHolder，然后在通过`registerBeanDefinition`方法注册BeanDefinition。

> bean命名前缀一般会被省略。
> beans元素的作用：可以将一组bean元素放到beans元素中，这组bean元素都可以使用beans元素定义的属性，比如default-lazy-init、default-init-method。
> 主动import其他配置时，被import是无法感知的，所以不太友好，一般建议平级的形式去引用其他xml配置，比如在load时，一次load多个xml配置。
> BeanDefinitionHolder有beanDefinition、beanName、aliases三个属性，是dom解析后生成的对象。因为BeanDefinition没有beanName属性，所以使用这个类一次解析获得所有信息。

## 基于Properties资源装载SpringBean配置元信息

使用properties文件定义SpringBean配置元信息时，Spring支持的内置属性有：

|Properties属性名|使用场景|
|--|--|
|(class)|Bean类全称限定名|
|(abstract)|是否为抽象的BeanDefinition|
|(parent)|指定parent BeanDefinintion名称|
|(lazy-init)|是否为延迟初始化|
|(ref)|引用其他Bean的名称|
|(scope)|设置Bean的scope属性|
|${n}|n表示第n+1个构造参数，比如$1|

基于Properties资源的解析类是`PropertiesBeanDefinitionReader`，解析方式和`XmlBeanDefinitionReader`方式差不多，但是相比于通过xml配置Bean元信息，properties资源的定义方式有很多局限性，不支持的配置很多，比如不支持定义Bean的init-method、是否是primary、不支持自定义bean的name（会根据key的前缀截取为bean的名称）。

## 基于Java注解装载SpringBean配置元信息

### Spring模式注解

在Spring中可以通过注解的方式进行Bean元信息的配置，Spring为了支持DDD（领域驱动设计）的概念，将注解根据不同的作用分成多个模式注解，如下表：

|Spring注解|场景说明|起始版本|
|--|--|--|
|@Repository|数据仓库模式注解|2.0|
|@Component|通用组件模式注解|2.5|
|@Service|服务模式注解|2.5|
|@Controller|Web控制器模式注解|2.5|
|@Configuration|配置类模式注解|3.0|

@Repository、@Service、@Controller、@Configuration都是属于@Component的派生注解，在Java中不存在注解继承的概念，所以其他注解通过元标注的形式，将@Component的能力进行继承。

### Spring Bean 定义注解

|Spring注解|场景说明|起始版本|
|--|--|--|
|@Bean|替换 XML 元素`<bean>`|3.0|
|@DependsOn|替代 XML 属性`<bean depends-on="..."/>`|3.0|
|@Lazy|替代 XML 属性`<bean lazy-init="true|falses" />`|3.0|
|@Primary|替换 XML 元素`<bean primary="true|false" />`|3.0|
|@Role|替换 XML 元素`<bean role="..." />`|3.1|
|@Lookup|替代 XML 属性`<bean lookup-method="...">`|4.1|

### Spring Bean依赖注入注解

|Spring注解|场景说明|起始版本|
|--|--|--|
|@Autowired|Bean 依赖注入，支持多种依赖查找方式|2.5|
|@Qualifier|细粒度的 @Autowired 依赖查找|2.5|

|Java 注解|场景说明|起始版本|
|--|--|--|
|@Resource|类似于 @Autowired|2.5|
|@Inject|类似于 @Autowired|2.5|

### Spring Bean条件装配注解

|Spring注解|场景说明|起始版本|
|--|--|--|
|@Profile|配置化条件装配|3.1|
|@Conditional|编程条件装配|4.0|

### Spring Bean生命周期回调注解

|Spring注解|场景说明|起始版本|
|--|--|--|
|@PostConstruct|替换 XML 元素`<bean init-method="..." />`或InitializingBean|2.5|
|@PreDestroy|替换 XML 元素`<bean destroy-method="..." />`或DisposableBean|2.5|

## Spring Bean 配置元信息底层实现

Spring BeanDefinition的解析方式主要有三种，分别是XML资源解析，实现类是`XmlBeanDefinitionReader`；Properties资源解析，实现类是`PropertiesBeanDefinitionReader`；Java注解解析，实现类是`AnnotatedBeanDefinitionReader`。其中`XmlBeanDefinitionReader`和`PropertiesBeanDefinitionReader`都是基于资源的解析方式，都实现了`AbstractBeanDefinitionReader`，`AnnotatedBeanDefinitionReader`的解析方式和资源无关，所以并没有实现`AbstractBeanDefinitionReader`。

### Spring XML资源BeanDefinition解析与注册

Spring XML资源BeanDefinition解析与注册的核心类是`XmlBeanDefinitionReader`，底层依赖`BeanDefinitionDocumentReader`。

`BeanDefinitionDocumentReader`的XML解析，使用的是Java DOM Level 3 API；BeanDefinition的解析使用的是`BeanDefinitionParserDelegate`类实现；BeanDefinition的注册使用`BeanDefinitionRegistry`。

`BeanDefinitionDocumentReader`提供了`preProcessXml`和`postProcessXml`方法，用来在xml资源解析完和解析前进行自定义处理，目前这两个方法为空方法。可以通过继承`BeanDefinitionDocumentReader`覆盖这两个方法实现用户的自定义处理，但是需要将自定义的`BeanDefinitionDocumentReader`通过方法`XmlBeanDefinitionReader#setDocumentReaderClass`set到`XmlBeanDefinitionReader`中。

`BeanDefinitionDocumentReader`中对xml资源的解析细节可以参考方法`DefaultBeanDefinitionDocumentReader#doRegisterBeanDefinitions`

### Spring Properties资源BeanDefinition解析与注册

Spring Properties资源BeanDefinition解析与注册的核心API是`PropertiesBeanDefinitionReader`，它的资源主要有字节流(Resource)和字符流(EncodedResource)，字节流默认编码ISO-8859-1，字符流可以指定编码方式。底层存储使用的是java.util.Properties，java.util.Properties继承了Hashtable，是线程安全的。BeanDefinition的解析是依靠内部的API实现，主要是根据properties的key进行不同的属性设置，其中包括一些内置的属性。BeanDefinition的注册也是使用`BeanDefinitionRegistry`。

`PropertiesBeanDefinitionReader`中对Properties资源的解析细节可以参考方法`PropertiesBeanDefinitionReader#registerBeanDefinitions(java.util.Map<?,?>, java.lang.String, java.lang.String)`。

### Spring Java注解BeanDefinition解析与注册

核心API：`AnnotatedBeanDefinitionReader`：`AnnotatedBeanDefinitionReader`并没有继承`AbstractBeanDefinitionReader`或者实现`BeanDefinitionReader`，它的资源来源是类对象(java.lang.Class)。

底层实现相关：

* 条件评估 - ConditionEvaluator
* Bean范围解析 - ScopeMetadataResolver
* Bean名称生成器 - AnnotationBeanNameGenerator
* BeanDefinition解析 - 内部 API 实现
* BeanDefinition处理 - AnnotationConfigUtils.processCommonDefinitionAnnotations
* BeanDefinition注册 - BeanDefinitionRegistry

粗略的处理流程：

* 1.根据beanClass的生成`AnnotatedGenericBeanDefinition`，这里通过`StandardAnnotationMetadata`反射的形式进行获取。
  `StandardAnnotationMetadata`属于AnnotationMetadata的实现，使用Java反射的形式获取Class的自省信息，关于`AnnotationMetadata`的实现还有`SimpleAnnotationMetadata`，它通过ASM获取Class信息。

* 2.验证@Conditional条件注解是否满足，不满足return。
* 3.获取@Scope注解的信息，并组装成`ScopeMetadata`.
* 4.如果没有指定name，根据名称生成器生成一个name。
* 5.调用`AnnotationConfigUtils#processCommonDefinitionAnnotations`方法，获取bean上标注的@Primary、@Lazy、@DependsOn、@Role、@Description注解，AnnotatedBeanDefinition进行赋值。
* 6.构建BeanDefinitionHolder，并registerBeanDefinition。

`AnnotatedBeanDefinitionReader`对BeanDefinition的解析与注册细节可以参考代码`AnnotatedBeanDefinitionReader#doRegisterBean`。

## 基于XML资源装载Spring IoC容器配置元信息

Spring IoC 容器相关 XML 配置

|命名空间|所属模块|Schema 资源 URL|
|--|--|--|
|beans|spring-beans|https://www.springframework.org/schema/beans/spring-beans.xsd|
|context|spring-context|https://www.springframework.org/schema/context/spring-context.xsd|
|aop|spring-aop|https://www.springframework.org/schema/aop/spring-aop.xsd|
|tx|spring-tx|https://www.springframework.org/schema/tx/spring-tx.xsd|
|util|spring-beans|https://www.springframework.org/schema/util/spring-util.xsd|
|tool|spring-beans|https://www.springframework.org/schema/tool/spring-tool.xsd|

与命名空间相关的有两个重要的文件`META-INF/spring.schemas`和`META-INF/spring.handlers`。

`META-INF/spring.schemas`文件定义了命名空间中schmea资源的所在的地址，一般不会在url地址后加上版本号了，比如`https://www.springframework.org/schema/beans/spring-beans-4.3.xsd`，直接使用`https://www.springframework.org/schema/beans/spring-beans.xsd`有更好的兼容性，能兼容多个版本的，它可以始终获取到最新的版本。

`META-INF/spring.handlers`文件定义了对应命名空间解析的类，比如`http\://www.springframework.org/schema/util=org.springframework.beans.factory.xml.UtilNamespaceHandler`

## 基于Java注解装载Spring IoC容器配置元信息

Spring IoC容器装配注解：

|Spring注解|场景说明|起始版本|
|--|--|--|
|@ImportResource|替换XML元素`<import>`|3.0|
|@Import|导入Configuration Class|3.0|
|@ComponentScan|扫描指定package下标注Spring模式注解的类|3.1|

Spring IoC配属属性注解：

|Spring 注解|场景说明| 起始版本|
|--|--|--|
|@PropertySource|配置属性抽象 PropertySource 注解|3.1|
|@PropertySources|@PropertySource集合注解|4.0|

@PropertySource和@PropertySources的关系，java8开始支持重复注解，所以可以在一个类上添加多个@PropertySource，以替换@PropertySources注解。

示例代码：[AnnotatedSpringIoCContainerMetadataConfigurationDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/configuration-metadata/src/main/java/com/wkk/learn/spring/ioc/configuration/metadata/AnnotatedSpringIoCContainerMetadataConfigurationDemo.java)

## 基于Extensible XML authoring扩展Spring XML元素

Spring提供了xml扩展机制，让用户可以实现自定义的xml格式，并且根据配置的解析类进行解析。要想实现一个xml扩展，需要的步骤有：

* 编写XML Schema文件：定义XML结构。
* 自定义NamespaceHandler实现：用来和命名空间进行绑定。
* 自定义BeanDefinitionParser实现：用来实现XML元素与BeanDefinition的解析。
* 注册XML扩展：实现命名空间与XML Schema映射。

示例代码：[user.xsd](https://github.com/wkk1994/spring-ioc-learn/blob/master/configuration-metadata/src/main/resources/com/wkk/learn/spring/ioc/configuration/metadata/user.xsd) &nbsp;&nbsp; [UserNamespaceHandler.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/configuration-metadata/src/main/java/com/wkk/learn/spring/ioc/configuration/metadata/UserNamespaceHandler.java) &nbsp;&nbsp; [spring.schemas](https://github.com/wkk1994/spring-ioc-learn/blob/master/configuration-metadata/src/main/resources/META-INF/spring.schemas) &nbsp;&nbsp; [spring.handlers](https://github.com/wkk1994/spring-ioc-learn/blob/master/configuration-metadata/src/main/resources/META-INF/spring.handlers)

## Extensible XML authoring 扩展原理

触发解析xml的时机：

当调用AbstractApplicationContext#refresh方法时，间接回去调用AbstractApplicationContext#obtainFreshBeanFactory方法，接下来的调用链：

* AbstractRefreshableApplicationContext#refreshBeanFactory
  * AbstractXmlApplicationContext#loadBeanDefinitions
    * ...
      * XmlBeanDefinitionReader#doLoadBeanDefinitions
        * ...
          * DefaultBeanDefinitionDocumentReader#parseBeanDefinitions
            * BeanDefinitionParserDelegate#parseCustomElement

最终会调用到parseCustomElement方法，该方法最终会执行自定义的Handler解析xml生成BeanDefinition。

核心流程：

DefaultBeanDefinitionDocumentReader#parseBeanDefinitions：

* 判断element节点的namespaceUri是否是默认节点beans的；
* 是默认节点调用方法parseDefaultElement，走默认节点解析逻辑；
* 不是默认节点调用方法parseCustomElement，走自定义元素解析逻辑。

BeanDefinitionParserDelegate#parseCustomElement(org.w3c.dom.Element, BeanDefinition)：

* 获取element节点的namespaceUri；
* 根据namespaceUri获取对应的NamespaceHandler：如果NamespaceHandler还没有实例化，就执行实例化和初始化（init方法）操作；
* 构造 ParserContext；
* 使用上一步获取到的NamespaceHandler解析元素，获取BeanDefinition。

## 基于Properties资源装载外部化配置

装载外部化配置有两种方式：基于注解驱动和API编程。

注解驱动主要有`org.springframework.context.annotation.PropertySource`和`org.springframework.context.annotation.PropertySources`两个注解。

使用API编程的方式会使用到类`org.springframework.core.env.PropertySource`和`org.springframework.core.env.PropertySources`。API编程添加 PropertySource 操作必须在 refresh 方法之前完成，否则没有效果。默认先加载的资源会覆盖后面加载的资源，在默认条件下，会加载一些系统默认的资源。

示例代码：[PropertiesSourceDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/configuration-metadata/src/main/java/com/wkk/learn/spring/ioc/configuration/metadata/PropertiesSourceDemo.java)

## 基于Yaml资源装载外部化配置

相关API

* org.springframework.beans.factory.config.YamlProcessor

  YamlProcessor的两个派生类：

  * org.springframework.beans.factory.config.YamlMapFactoryBean
  * org.springframework.beans.factory.config.YamlPropertiesFactoryBean

基于xml的方式装载Yaml资源示例代码：[XmlBasedYamlPropertySourceDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/configuration-metadata/src/main/java/com/wkk/learn/spring/ioc/configuration/metadata/XmlBasedYamlPropertySourceDemo.java)

基于@PropertySource注解的方式装载Yaml资源示例代码：[AnnotatedBasedYamlPropertySourceDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/configuration-metadata/src/main/java/com/wkk/learn/spring/ioc/configuration/metadata/AnnotatedBasedYamlPropertySourceDemo.java)

## 面试题

* Spring 內建 XML Schema 常见有哪些？

* Spring配置元信息具体有哪些？

  * Bean配置元信息：通过媒介（比如XML、Propertie等），解析成BeanDefinition。
  * IoC容器配置元信息：通过媒介（比如XML、Properties等），控制IoC容器的行为，比如注解驱动、AOP、事务等。
  * 外部化配置：通过资源抽象（如Property、Yaml等），控制PropertySource的资源加载和顺序。
  * Spring Profile：通过外部化配置，提供条件分支流程，比如生产环境下配置、测试环境下配置。

* Extensible XML authoring 的缺点？

  * 高复杂度：开发人员需要熟悉 XML Schema，spring.handlers，spring.schemas 
以及 Spring API 。
  * 嵌套元素支持较弱：通常需要使用方法递归或者其嵌套解析的方式处理嵌套（子）元
素。
  * XML 处理性能较差：Spring XML 基于 DOM Level 3 API 实现，该 API 便于理解，然
而性能较差。
  * XML 框架移植性差：很难适配高性能和便利性的 XML 框架，如 JAXB。
