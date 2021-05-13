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