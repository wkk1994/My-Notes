# Spring IoC概述

## Spring IoC依赖查找

**代码位置：ioc-container-overview**

* 根据Bean名称查找
  * 实时查找
  * 延迟查找
* 根据Bean类型查找
  * 单个Bean对象
  * 集合Bean对象
* 根据Bean名称+类型查找
* 根据Java注解查找
  * 单个Bean对象
  * 集合Bean对象

## Spring IoC依赖注入

**代码位置：ioc-container-overview**

* 根据Bean名称注入
* 根据Bean类型注入
  * 单个Bean对象
  * 集合Bean对象
* 注入容器内建Bean对象
* 注入非Bean对象
* 注入类型
  * 实时注入
  * 延迟注入

## Spring IoC依赖来源

* 自定义的Bean
* 容器内建Bean对象
* 容器内建依赖

## Spring IoC配置元信息

* Bean定义配置
  * 基于XML文件
  * 基于Properties文件
  * 基于Java注解
  * 基于Java API
* IoC容器配置
  * 基于XML配置
  * 基于Java注解
  * 基于Java API
* 外部化属性配置
  * 基于Java注解 例如：@Value

**BeanFactory和ApplicationContext谁才是Spring IoC容器？**
BeanFactory是一个底层的IoC容器，ApplicationContext内部组合了BeanFactory，ApplicationContext实现了BeanFactory的全部能力，相当于BeanFactory的超集，并且提供了更多的特性，比如：消息资源处理，事件的发布等。
