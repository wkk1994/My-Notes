# Spring数据绑定

## Spring数据绑定使用场景

* Spring BeanDefinition 到 Bean 实例创建：Spring的Bean在创建过程中属性的赋值过程涉及到数据绑定以及类型转换的过程。
* Spring 数据绑定（DataBinder）：核心类`org.springframework.validation.DataBinder`。
* Spring Web 参数绑定（WebDataBinder）：这部分包括Spring WebMVC和Spring Web Flux。

## Spring数据绑定组件

Spring数据绑定中的组件一般分为标准组件和Web组件两种。

* 标准组件
  * org.springframework.validation.DataBinder：比较常用的，用来将属性值绑定到对应的属性上。
* Web组件：作用是将request中的请求参数、请求数据绑定到对应的属性或Bean实例中。
  * org.springframework.web.bind.WebDataBinder
  * org.springframework.web.bind.ServletRequestDataBinder
  * org.springframework.web.bind.support.WebRequestDataBinder
  * org.springframework.web.bind.support.WebExchangeDataBinder（since 5.0）

DataBinder的核心属性有：

* target: 关联目标 Bean
* objectName: 目标 Bean名称
* bindingResult: 属性绑定结果
* typeConverter: 类型转换器，使用Java Bean标准中的PropertyEditor实现类型转换。
* conversionService: 类型转换服务，Spring 3.0开始单独实现的类型转换接口，不再依赖于PropertyEditor。
* messageCodesResolver: 校验错误文案 Code 处理器
* validators: 关联的 Bean Validator 实例集合

DataBinder绑定方法：

* bind(PropertyValues)：将PropertyValues中的Key-Value内容映射到关联Bean（target）中的属性上。

  举例：比如PropertyValues中有“name=hello”的键值对，同时Bean中有name属性，当bind方法执行时，User对象中的name属性值被绑定为“hello”。

## Spring数据绑定元数据

DataBinder 元数据 - PropertyValues：

* 数据来源：主要来源是XMl资源配置的BeanDefinition。
* 数据结构：由一个或多个PropertyValue组成。
* 成员结构：PropertyValue包含属性名称，属性值（包括原始值、类型转换后的值）。
* 常见实现：MutablePropertyValues，它是一个支持写操作的PropertyValue。
* Web扩展实现：ServletConfigPropertyValues、ServletRequestParameterPropertyValues。
* 相关生命周期：InstantiationAwareBeanPostProcessor#postProcessProperties，允许在属性bind之前，对PropertyValues进行修改。

## Spring数据绑定控制参数

DataBinder绑定特殊场景分析：

* 当PropertyValues中包含属性名x，目标Bean中不存在该属性x时，进行绑定时会发生什么？
* 当PropertyValues中包含属性名x，目标Bean中也包含该属性x时，在绑定时，如何避免这个属性的绑定？
* 当PropertyValues中包含嵌套属性，如x.y，目标Bean中存在属性x（嵌套属性y），在绑定时，会发生什么？

DataBinder绑定控制参数：

* ignoreUnknownFields：是否忽略未知字段，默认值：true；
* ignoreInvalidFields：是否忽略非法字段，默认值：false；
* autoGrowNestedPaths：是否自动增加嵌套路径，默认值：true；
* allowedFields：绑定字段白名单；
* disallowedFields：绑定字段黑名单；
* requiredFields：必须绑定字段。

DataBinder参数控制示例：[DataBindingDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/data-binding/src/main/java/com/wkk/learn/spring/ioc/data/binding/DataBindingDemo.java)

## Spring底层Java Beans替换实现

* JavaBeans 核心实现 - java.beans.BeanInfo
  * 属性（Property）
    * java.beans.PropertyEditor
  * 方法（Method）
  * 事件（Event）
  * 表达式（Expression）
* Spring 替代实现 - org.springframework.beans.BeanWrapper
  * 属性（Property）
    * java.beans.PropertyEditor
  * 嵌套属性路径（nested path）

Spring数据绑定中，舍弃了Java Beans中一些复杂的描述，比如方法描述，在属性描述中和Java Beans都是使用PropertyEditor来实现。Spring中的事件是自成一套体系，虽然也是基于事件标准的API，但是事件的监听和发送自定义了一套实现。Spring中的表达式相比于Java Beans的表达式支持了条件表达式，即支持条件运算。

> Java Beans中不要求getter、setter方法成对实现。

## BeanWrapper的使用场景

* 作为Spring底层JavaBeans基础设施的中心化接口：它在获取BeanInfo等信息也是通过JavaBeans的API来获取的。相当于一个装饰器模式，封装了JavaBeans的部分功能。
* 通常不会直接使用，间接使用于BeanFactory和DataBinder：比如BeanFactory中创建Bean实例过程中的属性绑定。
* 提供标准 JavaBeans 分析和操作，能够单独或批量存储 Java Bean 的属性（properties）
* 支持嵌套属性路径（nested path）
* 实现类 org.springframework.beans.BeanWrapperImpl：目前只有这一个内置的实现类，自己实现BeanWrapper的意义不大，BeanWrapper的拓展点并不多。

## 标准Java Beans介绍

标准Java Beans的API介绍：

* java.beans.Introspector：JavaBeans内省 API。
* java.beans.BeanInfo：JavaBeans元信息 API，描述Bean的信息，从上一步获取。
* java.beans.BeanDescriptor：JavaBeans信息描述符。
* java.beans.PropertyDescriptor：JavaBeans属性描述符，包含属性的read和write方法。
* java.beans.MethodDescriptor：JavaBeans方法描述符，主要描述public方法。
* java.beans.EventSetDescriptor：JavaBeans事件集合描述符。

## DataBinder数据校验

DataBinder 与 BeanWrapper

* bind 方法生成 BeanPropertyBindingResult
* BeanPropertyBindingResult 关联 BeanWrapper

DataBinder在执行bind操作时，会通过BeanPropertyBindingResult实例获取BeanWrapper实例，通过BeanWrapper实例执行属性绑定过程（方法：AbstractPropertyAccessor#setPropertyValues(PropertyValues, boolean)）。

## 面试题

* Spring 数据绑定 API 是什么？

  org.springframework.validation.DataBinder，获取绑定结果的API是BindingResult。

* BeanWrapper 与 JavaBeans 之间关系是？

  JavaBeans是一个java核心的API或一个规范，BeanWrapper是基于JavaBeans做了一个封装，简化了JavaBeans，对一些不重要的信息进行忽略，比如事件。BeanWrapper一般不会直接使用，都是间接使用。BeanWrapper内置只有一个实现BeanWrapperImpl。

* DataBinder 是怎么完成属性类型转换的？

  下章解答。
