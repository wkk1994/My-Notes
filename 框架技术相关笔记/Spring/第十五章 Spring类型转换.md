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

