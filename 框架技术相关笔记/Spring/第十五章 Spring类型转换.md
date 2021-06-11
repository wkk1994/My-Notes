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

