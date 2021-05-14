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