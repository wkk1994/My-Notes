# Spring事件

## Java事件/监听器编程模型

设计模式：观察者模式的扩展

* 被观察者（消息的发送者）：java.util.Observable
* 观察者：java.util.Observer

标准化接口：

* 事件对象：java.util.EventObject，一般发送的消息都要继承这个类，不过没有强制要求。
* 事件监听器：java.util.EventListener，所有的事件监听器都需要实现该接口，这是一个空接口，没有强制要求实现。

Java事件监听示例：[ObserverDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/event/src/main/java/com/wkk/learn/spring/ioc/event/ObserverDemo.java)

## 面向接口的事件/监听器设计模式

事件/监听器场景举例：

|Java技术规范|事件接口|监听器接口|说明|
|--|--|--|--|
|JavaBeans|java.beans.PropertyChangeEvent|java.beans.PropertyChangeEventListener||
|Java AWT|java.awt.event.MouseEvent|java.awt.event.MouseListener|监听鼠标的事件|
|Java Swing|javax.swing.event.MenuEvent|javax.swing.event.MenuListener|swing的菜单相关的事件|
|Java Preference|java.util.prefs.PreferenceChangeEvent|java.util.prefs.PreferenceChangeListener|用于接收用户编好的事件|

> 单事件和多事件：单事件是指一个监听器只有一个事件，多事件是指一个监听器可以监听多个事件。

## 面向注解的事件/监听器设计模式

![注解事件监听器设计模式](./images/注解事件监听器设计模式.png)

> 生命周期和事件没有本质的区别，都是在特定的阶段进行回调或触发。

## Spring标准事件-ApplicationEvent

ApplicationEvent是Java标准事件EventObject的扩展，它扩展添加了发生事件的时间戳。

ApplicationContextEvent是ApplicationEvent的扩展，ApplicationContextEvent和Spring应用上下文（ApplicationContext）密切相关，它将Spring应用上下文作为事件源进行保存。它的具体实现有：

* org.springframework.context.event.ContextClosedEvent
* org.springframework.context.event.ContextRefreshedEvent
* org.springframework.context.event.ContextStartedEvent
* org.springframework.context.event.ContextStoppedEvent