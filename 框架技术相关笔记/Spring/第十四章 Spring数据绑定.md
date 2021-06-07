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