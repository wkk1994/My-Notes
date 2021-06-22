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

## 基于接口的Spring事件监听器

Spring的事件监听器也遵循规则，实现了Java的标准事件监听器java.util.EventListener。

* 核心API：org.springframework.context.ApplicationListener
* 处理方法：onApplicationEvent(ApplicationEvent)
* 事件类型：org.springframework.context.ApplicationEvent

可以看出ApplicationListener只有一个事件监听的方法`onApplicationEvent`，所以说ApplicationListener不同于Java的一些事件监听器，它只关注某一个事件，或者说它一次只处理一个事件。可以说ApplicationListener是单一的设计原则。

基于接口的Spring事件监听示例：[ApplicationListenerDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/event/src/main/java/com/wkk/learn/spring/ioc/event/ApplicationListenerDemo.java)

## 基于注解的Spring事件监听器

Spring支持使用注解的方式注册事件监听器，API：org.springframework.context.event.EventListener，它的特性有：

* 支持多ApplicationEvent类型，没有接口的约束。
* 支持在方法上注释。
* 支持异步执行，只需要在方法上添加@Async，并且激活异步执行（@EnableAsync）
* 支持泛型类型事件。
* 配合@Order注解，可以实现顺序控制。

基于注解实现Spring事件监听器：[AnnotationApplicationListenerDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/event/src/main/java/com/wkk/learn/spring/ioc/event/AnnotationApplicationListenerDemo.java)

## 注册Spring ApplicationListener

* 方法一：ApplicationListener作为Spring Bean注册

* 方法二：使用ConfigurableApplicationContext API注册

  ```java
  // 注册事件监听
  applicationContext.addApplicationListener(event -> {
      System.out.println("接收到Spring事件：" + event);
  });
  ```

## Spring事件发布器

通过Spring发布事件的方式：

* 方法一：通过ApplicationEventPublisher发布Spring事件
  * 获取ApplicationEventPublisher的方式只能通过依赖注入，使用ApplicationEventPublisherAware回调注入。

* 方法二：通过ApplicationEventMulticaster发布Spring事件
  * 获取ApplicationEventMulticaster的方式可以通过依赖注入和依赖查找。

ApplicationEventPublisher发布Spring事件示例：[ApplicationEventPublisherDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/event/src/main/java/com/wkk/learn/spring/ioc/event/ApplicationEventPublisherDemo.java)

## Spring层次性上下文事件传播

当Spring应用出现多层次Spring应用上下文（ApplicationContext）时，如SpringMVC、SpringBoot或SpringCloud场景下，由子ApplicationContext发起Spring事件可能会传递到其Parent ApplicationContext（直到Root）的过程。

如何避免事件的传播：

* 定位Spring事件源（ApplicationContext）进行过滤处理。
* 在事件监听器中添加逻辑判断，如果当前事件已经处理过，就不处理了。

Spring的事件在上下文中传播的代码可以参考`org.springframework.context.support.AbstractApplicationContext#publishEvent(java.lang.Object, org.springframework.core.ResolvableType)`中的：

```text
if (this.parent != null) {
    if (this.parent instanceof AbstractApplicationContext) {
        ((AbstractApplicationContext) this.parent).publishEvent(event, eventType);
    }
    else {
        this.parent.publishEvent(event);
    }
}
```

层次性Spring事件传播示例：[HierarchicalSpringEventPropagateDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/event/src/main/java/com/wkk/learn/spring/ioc/event/HierarchicalSpringEventPropagateDemo.java)

## Spring内建事件

ApplicationContextEvent派生事件：

* ContextRefreshedEvent：Spring应用上下文就绪事件
* ContextStartedEvent：Spring上下文启动事件
* ContextStopedEvent：Spring上下文停止事件
* ContextClosedEvent：Spring上下文关闭事件

在调用Spring上下文refresh、start、stop、close方法时会触发对应的事件，ApplicationContext接口继承了ApplicationEventPublisher，所以Spring上下文的实现类都实现了事件发生的接口publishEvent，具有事件发送能力。

> start 和 stop 方法是一种辅助的特性，通常使用不多。

## Spring 4.2 Payload事件

Spring Payload事件API：org.springframework.context.PayloadApplicationEvent，它的使用场景是：简化Spring事件的发送，关注源事件本身，在调用publishEvent(Object)方法发布事件时，它会将Object作为source，构建出一个PayloadApplicationEvent实例，作为事件来传递。

发送方法：ApplicationEventPublisher#publishEvent(java.lang.Object)

PayloadApplicationEvent为什么不是一个良好的扩展？

* PayloadApplicationEvent本身是用来作为Spring框架内部使用，但是内部使用的很少，而且是public，允许继承。
* 在继承PayloadApplicationEvent的时候不能简单继承，要指定实现类的具体化否则报错`MyPayloadApplicationEvent<String> extends PayloadApplicationEvent<String>`。
* 在发送事件时，如果是普通的事件使用publishEvent(java.lang.Object)方法就好，它会转换成PayloadApplicationEvent，没必要在创建一个PayloadApplicationEvent实例进行发送。
* 在事件监听时，只能监听PayloadApplicationEvent，不能具体到监听泛型具体化的消息内容，比如PayloadApplicationEvent\<String>，这是一个局限性。

扩展PayloadApplicationEvent实现示例：[PayloadApplicationEventDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/event/src/main/java/com/wkk/learn/spring/ioc/event/PayloadApplicationEventDemo.java)

## 自定义Spring事件

* 1.扩展org.springframework.context.ApplicationEvent；
* 2.实现org.springframework.context.ApplicationListener；
* 3.注册实现的ApplicationListener；
* 4.发布扩展的ApplicationEvent事件。

自定义Spring事件示例：

[MySpringEvent.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/event/src/main/java/com/wkk/learn/spring/ioc/event/MySpringEvent.java)
[MySpringEventListener.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/event/src/main/java/com/wkk/learn/spring/ioc/event/MySpringEventListener.java)
[MySpringEventDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/event/src/main/java/com/wkk/learn/spring/ioc/event/MySpringEventDemo.java)

## 依赖注入ApplicationEventPlublisher

通过依赖注入ApplicationEventPlublisher的方式通常有两种方式：

* 通过ApplicationEventPlublisherAware回调接口
* 通过@Autowired ApplicationEventPlublisher

除之之外，因为ApplicationContext实现了ApplicationEventPlublisher接口，也可以通过注入ApplicationContext，获取ApplicationContext的实例实现事件的发布。

注入ApplicationEventPublisher示例：[InjectingApplicationEventPublisherDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/event/src/main/java/com/wkk/learn/spring/ioc/event/InjectingApplicationEventPublisherDemo.java)

## 依赖查找ApplicationEventMulticaster

ApplicationEventMulticaster支持的依赖查找的条件：

* 通过Bean名称查找：applicationEventMulticaster
* 通过Bean类型查找：org.springframework.context.event.ApplicationEventMulticaster

ApplicationEventMulticaster相比于ApplicationEventPlublisher是订阅模式，关注于一对多的关系，支持观察者的维护操作，比如注册观察者、删除观察者。

ApplicationEventMulticaster的初始化过程：

在调用AbstractApplicationContext#refresh方法时，会调用
AbstractApplicationContext#initApplicationEventMulticaster进行ApplicationEventMulticaster的初始化。

> 在Bean的destroy方法触发时依赖ApplicationEventMulticaster实现的。
> ApplicationEventMulticaster并不是不支持依赖注入，它是通过`SingletonBeanRegistry#registerSingleton`方法进行注入的，所以没有BeanDefinition信息，当依赖注入ApplicationEventMulticaster时，但是ApplicationEventMulticaster还没有实例化，就会提示错误。所以在initApplicationEventMulticaster方法之后可以依赖注入ApplicationEventMulticaster。

## ApplicationEventPublisher底层实现

ApplicationEventPublisher的底层实现依赖ApplicationEventMulticaster。

ApplicationEventMulticaster的抽象类AbstractApplicationEventMulticaster，对应的实现类只有一个SimpleApplicationEventMulticaster。

在Spring框架中，接口ApplicationContext继承了ApplicationEventPublisher，ApplicationContext的实现为AbstractApplicationContext，AbstractApplicationContext对ApplicationEventPublisher接口的实现，参考方法`AbstractApplicationContext#publishEvent(Object, ResolvableType)`，从方法中可以看出它是通过内部依赖的属性ApplicationEventMulticaster实现发送事件的能力。这个ApplicationEventMulticaster实际上是SimpleApplicationEventMulticaster的实例。

```java
protected void publishEvent(Object event, @Nullable ResolvableType eventType) {
    Assert.notNull(event, "Event must not be null");

    // Decorate event as an ApplicationEvent if necessary
    ApplicationEvent applicationEvent;
    if (event instanceof ApplicationEvent) {
         applicationEvent = (ApplicationEvent) event;
    }
    else {
         applicationEvent = new PayloadApplicationEvent<>(this, event);
         if (eventType == null) {
              eventType = ((PayloadApplicationEvent<?>) applicationEvent).getResolvableType();
         }
    }

    // Multicast right now if possible - or lazily once the multicaster is initialized
    if (this.earlyApplicationEvents != null) {
         this.earlyApplicationEvents.add(applicationEvent);
    }
    else {
         getApplicationEventMulticaster().multicastEvent(applicationEvent, eventType);
    }

    // Publish event via parent context as well...
    if (this.parent != null) {
         if (this.parent instanceof AbstractApplicationContext) {
              ((AbstractApplicationContext) this.parent).publishEvent(event, eventType);
         }
         else {
              this.parent.publishEvent(event);
         }
    }
}
```

总结：**AbstractApplicationContext使用桥接的方式，将ApplicationEventPublisher的接口实现交由ApplicationEventMulticaster完成。**

**在`AbstractApplicationContext#publishEvent(Object, ResolvableType)`中earlyApplicationEvents属性的作用**

由于AbstractApplicationContext的事件发送依赖ApplicationEventMulticaster，而初始化ApplicationEventMulticaster在方法`AbstractApplicationContext#initApplicationEventMulticaster`，可能出现在初始化ApplicationEventMulticaster之前就进行了发送事件操作，所以将之前的发送事件保存到earlyApplicationEvents，在方法`AbstractApplicationContext#registerListeners`中对earlyApplicationEvents中的事件进行发送，并且将earlyApplicationEvents设置为null，在发送事件时，如果earlyApplicationEvents为null就不会保存事件，而是使用ApplicationEventMulticaster进行事件发送。

> AbstractApplicationContext中的earlyApplicationListeners属性的作用和earlyApplicationEvents类似，保存ApplicationEventMulticaster初始化之前注册的ApplicationEvent，并在ApplicationEventMulticaster初始化后注册到ApplicationEventMulticaster中。

## 同步和异步Spring事件广播

Spring事件监听器同步异步的方式：

**基于实现类SimpleApplicationEventMulticaster：**

SimpleApplicationEventMulticaster基于内部属性taskExecutor，如果不为空就使用taskExecutor异步执行事件。

设计缺陷：

* 基于接口契约编程，在接口ApplicationEventMulticaster没有规定同步异步执行的规则定义，异步事件执行属于SimpleApplicationEventMulticaster自己实现的。
* SimpleApplicationEventMulticaster设置Executor要使用硬编码的方式，必须从BeanFactory获取到ApplicationEventMulticaster实例，再转换为SimpleApplicationEventMulticaster，调用setTaskExecutor方法进行Executor的设置。
* 异步模式的设置是全局设置：SimpleApplicationEventMulticaster的同步异步不能基于事件或事件监听器的配置，只能是全局事件都是同步或异步。

基于接口API实现异步事件处理示例：[AsyncEventHandlerDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/event/src/main/java/com/wkk/learn/spring/ioc/event/AsyncEventHandlerDemo.java)

**基于注解实现@EventListener和@Async：**

默认条件下是同步，在事件监听方法上添加@Async实现异步。

实现限制：无法实现同步/异步动态切换。

基于注解实现异步事件处理示例：[AnnotationAsyncEventHandlerDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/event/src/main/java/com/wkk/learn/spring/ioc/event/AnnotationAsyncEventHandlerDemo.java)

## Spring4.1事件异常处理

Spring3.0错误处理接口：org.springframework.util.ErrorHandler

使用场景：

* Spring事件（Event）：SimpleApplicationEventMulticaster在事件处理中如果出现异常，可以选择使用ErrorHandler，进行异常处理。
* Spring本地调度（Scheduling）：
  * org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
  * org.springframework.scheduling.concurrent.ConcurrentTaskScheduler

事件异常处理示例：[AsyncEventHandlerDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/event/src/main/java/com/wkk/learn/spring/ioc/event/AsyncEventHandlerDemo.java)

## Spring事件/监听器实现原理

核心类：SimpleApplicationEventMulticaster

SimpleApplicationEventMulticaster的设计模式是观察者模式的扩展：

* 观察者：ApplicationListener的实现类，注册方式有两种：
  * API添加：可以通过方法AbstractApplicationContext#addApplicationListener进行添加。
  * 依赖查找主动注册：在方法`AbstractApplicationContext#registerListeners`中查找类型为ApplicationListener的实现类进行注册。
* 被观察者：ApplicationEvent的实现类。

执行模式：同步/异步
异常处理：ErrorHandler
泛型处理：ResolvableType

SimpleApplicationEventMulticaster的实现原理参考方法`AbstractApplicationEventMulticaster#getApplicationListeners(org.springframework.context.ApplicationEvent, org.springframework.core.ResolvableType)`：

在进行事件发布时，需要找到对应事件的监听器。AbstractApplicationEventMulticaster通过属性retrieverCache（类型为ConcurrentHashMap）缓存事件和监听器的对应关系，在有监听器加入或者删除时，都会对retrieverCache进行清除。

retrieverCache的key为ListenerCacheKey，value为ListenerRetriever。ListenerCacheKey中存有事件的类型，以及事件的来源。ListenerRetriever保存事件监听器的列表。

事件发布的过程参考方法`SimpleApplicationEventMulticaster#multicastEvent(org.springframework.context.ApplicationEvent, org.springframework.core.ResolvableType)`：

* 通过事件类型和事件的源类型，构造出ListenerCacheKey示例。
* 在retrieverCache中查询是否有对应的ListenerRetriever，有的话直接返回事件监听器列表，对事件监听器逐个进行调用。
* 如果缓存中不存在，在同步代码块中获取监听器列表，并保存到retrieverCache缓存中。监听器列表获取方式参考代码：`AbstractApplicationEventMulticaster#retrieveApplicationListeners`
  * 监听器的获取过程就是检查applicationListeners和applicationListenerBeans中的示例是否支持当前的事件类型。

> 事件监听的层次性概念：事件可以有父子类，监听事件父类的的监听器也会监听事件子类的发生。

## SpringBoot、SpringCloud事件

SpringBoot事件：

![SpringBoot事件](https://i.loli.net/2021/06/22/DI3qW56YkXce2rg.png)

SpringCloud事件：

![SpringCloud事件](https://i.loli.net/2021/06/22/kQvd8JWrMqSA27m.png)

SpringBoot和SpringCloud事件都是使用ApplicationEventPublisher进行事件传输的。使用Aware机制获取ApplicationEventPublisher。

## 面试题

* Spring事件核心接口/组件？

  * Spring事件：ApplicationEvent
  * Spring事件监听器：ApplicationListener
  * Spring事件发布器：ApplicationEventPublisher
  * Spring事件广播器：ApplicationEvnetMulticaster

* Spring同步/异步事件处理的使用场景？

  * Spring同步事件：绝大多数Spring使用场景，如ContextRefreshedEvent，适合短时间运行的任务处理。
  * Spring异步事件：主要@EventListener和@Asyc配合使用，实现异步处理，不阻塞主线程，适合长时间运行的数据计算任务等。不要轻易调整SimpleApplicationEventMuliticaster中关联的taskExecutor对象，除非使用者非常了解Spring的事件机制，否则容易出现异常行为。

* @EventListener的工作原理？

  下章解答。
