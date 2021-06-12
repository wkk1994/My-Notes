# Spring类型转换

## Spring类型转换实现

Spring的类型转换在版本发展过程中有两种实现方式：

* 基于JavaBeans接口的类型转换实现：主要基于java.beans.PropertyEditor接口扩展
* Spring 3.0+通用类型转换实现。

## 类型转换实现的使用场景

|场景|基于 JavaBeans 接口的类型转换实现|Spring 3.0+ 通用类型转换实现|
|--|--|--|
|数据绑定|YES|YES|
|BeanWrapper|YES|YES|
|Bean属性类型转换|YES|YES|
|外部化配置类型转换|NO|YES|

DataBinder和BeanWrapper都是关联了ConversionService，将ConversionService作为内部属性依赖。

> ConversionService是Spring通用类型转换的实现。

## 基于JavaBeans接口的类型转换

基于JavaBeans接口的类型转换的核心职责是将String类型的内容转换为目标类型对象，它只支持String类型。

扩展原理：

* Spring框架将文本内容传递到PropertyEditor实现的setAsText(String)方法；
* PropertyEditor#setAsText(String)方法实现将String类型转换为目标类型的对象；
* 将目标类型对象传递入PropertyEditor#setValue(Object)方法；
* PropertyEditor#setValue(Object)方法实现需要临时存储传入对象；
* Spring框架将通过PropertyEditor#getValue()获取类型转换后的对象。

自定义PropertyEditor示例：
[PropertyEditorDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/conversion/src/main/java/com/wkk/learn/spring/ioc/conversion/PropertyEditorDemo.java)
[StringToPropertiesPropertyEditor.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/conversion/src/main/java/com/wkk/learn/spring/ioc/conversion/StringToPropertiesPropertyEditor.java)

## Spring内建PropertyEditor扩展

Spring内建的PropertyEditor扩展都在org.springframework.beans.propertyeditors包下，常见的有：

|转换场景| 实现类|
|--|--|
|String -> Byte数组| org.springframework.beans.propertyeditors.ByteArrayPropertyEditor|
|String -> Char| org.springframework.beans.propertyeditors.CharacterEditor|
|String -> Char数组| org.springframework.beans.propertyeditors.CharArrayPropertyEditor|
|String -> Charset| org.springframework.beans.propertyeditors.CharsetEditor|
|String -> Class| org.springframework.beans.propertyeditors.ClassEditor|
|String -> Currency| org.springframework.beans.propertyeditors.CurrencyEditor|

## 自定义PropertyEditor扩展

* 扩展模式
  * 扩展java.beans.PropertyEditorSupport类
* 注册PropertyEditorSupport
  * 1.实现org.springframework.beans.PropertyEditorRegistrar
  * 2.重写registerCustomEditors(org.springframework.beans.PropertyEditorRegistry) 方法
  * 在registerCustomEditors方法中通过PropertyEditorRegistry将自定义的PropertyEditor注册为Spring Bean。
* PropertyEditorRegistry注册PropertyEditor的方式：
  * 通用类型实现registerCustomEditor(Class<?>, PropertyEditor)：这种方式是直接将该类的转换都交由PropertyEditor实现。
  * Java Bean属性类型实现：registerCustomEditor(Class<?>, String, PropertyEditor)：这种方式是指定类型的某一个属性的类型转换由PropertyEditor实现。

注册自定义的PropertyEditor代码示例：
[CustomizedPropertyEditorRegistrar.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/conversion/src/main/java/com/wkk/learn/spring/ioc/conversion/CustomizedPropertyEditorRegistrar.java)
[SpringCustomizedPropertyEditorDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/conversion/src/main/java/com/wkk/learn/spring/ioc/conversion/SpringCustomizedPropertyEditorDemo.java)

## Spring PropertyEditor的设计缺陷

* 违法单一原则
  java.beans.PropertyEditor接口职责太多，除了类型转换，还包括 Java Beans 事件和 Java GUI 交互。
  java.beans.PropertyEditorSupport#paintValue是对GUI交互的接口定义，java.beans.PropertyEditorSupport在执行setValue(Object)方法时，会调用firePropertyChange方法进行事件通知。

* java.beans.PropertyEditor实现类型局限

  来源类型只能为java.lang.String类型，不过Spring的属性来源基本上都是String类型的。

* java.beans.PropertyEditor实现缺少类型安全

  setValue(Object)、getValue()方法返回的都是Object类型，除了实现类命名可以表达语义，实现类无法感知目标转换类型。

## Spring3通用类型转换接口

Spring3的类型转换接口有：

* 类型转换接口 - org.springframework.core.convert.converter.Converter\<S,T>
  * 泛型参数 S：来源类型，参数 T：目标类型
  * 核心方法：T convert(S)
  Converter属于类型安全的，但是在设计上有些局限性，因为Java使用了泛型擦除，所以在实际转换中不能获取到来源类型S的类型。

* 通用类型转换接口 - org.springframework.core.convert.converter.GenericConverter
  * 核心方法：convert(Object,TypeDescriptor,TypeDescriptor)
  * 配对类型：org.springframework.core.convert.converter.GenericConverter.ConvertiblePair，使用类似键值对的形式保存来源类型TypeDescriptor和目标类型TypeDescriptor。
  * 类型描述：org.springframework.core.convert.TypeDescriptor
  GenericConverter通过TypeDescriptor解决了Converter的局限性，TypeDescriptor记录了类型描述。GenericConverter通过ConvertiblePair，支持多类型的相互转换，不再只支持单个类型之间转换。

> 为什么是Spring3引入的通用类型转换，通用类型转换依赖于泛型，Spring3依赖于Java5，Java5才有的泛型支持。

## Spring内建的类型转换器

|转换场景| 实现类所在包名（package）|
|--|--|
|日期/时间相关| org.springframework.format.datetime|
|Java 8日期/时间相关| org.springframework.format.datetime.standard|
|通用实现| org.springframework.core.convert.support|

## Converter接口的局限性

* 局限一：缺少对SourceType和TargetType的前置判断

  对于这个局限性，Spring提供了ConditionalConverter接口，实现ConditionalConverter#matches可以对前置类型进行判断。所以可以在实现Converter的时候同时实现ConditionalConverter。

* 局限二：仅能支持单一的SourceType和TargetType

  对于这个局限性，Spring提供了GenericConverter通过ConvertiblePair可以支持多个SourceType和TargetType的转换。

> 为什么局限二的说明是：Converter不能很好的支持集合类型？我的理解局限二应该是同一个Converter不能支持多个SourceType和TargetType的转换，而GenericConverter通过ConvertiblePair可以支持多个SourceType和TargetType的转换。

## GenericConverter接口

org.springframework.core.convert.converter.GenericConverter

* 使用场景：主要用于“复合”类型转换场景，比如Collection、Map、数组等。
* 转换范围：Set\<ConvertiblePair> getConvertibleTypes()方法可以获取当前GenericConverter的转换范围。
* 配对类型：org.springframework.core.convert.converter.GenericConverter.ConvertiblePair。
* 转换方法：convert(Object,TypeDescriptor,TypeDescriptor)。
* 类型描述：org.springframework.core.convert.TypeDescriptor。

GenericConverter通常会整合单一类型转换器Converter，举例：CollectionToArrayConverter用来将Collection转换为Array，在convert方法中，通过迭代的方式将集合类型中的成员元素，逐一调用ConversionService#convert方法进行类型转换，这个方法最终也会用到Converter#convert。

## 优化GenericConverter接口

GenericConverter局限性：

* 缺少SourceType和TargetType前置判断；
* 单一类型转换复杂：例如CollectionToArrayConverter中需要通过迭代的方式将单一类型转换成目标类型。

GenericConverter优化接口 - ConditionalGenericConverter：

ConditionalGenericConverter接口通过继承GenericConverter和ConditionalConverter，扩展GenericConverter的局限性。

* 复合类型转换：org.springframework.core.convert.converter.GenericConverter
* 类型条件判断：使用org.springframework.core.convert.converter.ConditionalConverter进行类型的前置判断。

## 扩展Spring类型转换器

扩展Spring类型转换器的步骤：

1.实现类型转换器接口，以下3个接口实现一个就可以了：

* org.springframework.core.convert.converter.Converter
* org.springframework.core.convert.converter.ConverterFactory
* org.springframework.core.convert.converter.GenericConverter

2.注册类型转换器实现，以下2种方式都可以实现：

* 通过 ConversionServiceFactoryBean Spring Bean
* 通过 org.springframework.core.convert.ConversionService API

扩展Spring类型转换示例：
[PropertiesToStringConverter.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/conversion/src/main/java/com/wkk/learn/spring/ioc/conversion/PropertiesToStringConverter.java)
[property-editors-context.xml](https://github.com/wkk1994/spring-ioc-learn/blob/master/conversion/src/main/META-INF/property-editors-context.xml)

