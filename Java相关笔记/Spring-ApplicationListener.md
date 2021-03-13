# Spring ApplicationListener

## 什么是ApplicationListener

spring的监听器，用来监听事件（ApplicationEvent）的传递，属于设计模式中的观察者模式。

## ApplicationEvent

spring监听事件的父类，所有自定义监听事件都需要实现这个类。

### 实现类

* SpringApplicationEvent spring应用启动相关事件
  * ApplicationEnvironmentPreparedEvent： 当springApplication启动并且环境第一次可用检查和修改的时候发布事件
  * ApplicationPreparedEvent： 当SpringApplication启动后并且上下文充分准备但没刷新的时候发布事件，bean定义将会加载并且环境在这步已经准备好被使用
  * ApplicationStartedEvent： 一旦上下文刷新，在任何应用或命令行调用前都会被发布事件
  * ApplicationReadyEvent： 表示SpringApplication已经准备好为外面提供服务
  * ApplicationFailedEvent：当SpringApplication启动失败时发布事件
  * ApplicationStartingEvent： 当springApplication启动时发布事件

执行顺序
ApplicationStartingEvent --> ApplicationEnvironmentPreparedEvent --> ApplicationPreparedEvent --> ApplicationStartedEvent --> ApplicationReadyEvent

### 示例

* 自定义事件类

    ```java
    public class CustomEvent extends ApplicationEvent{
        public CustomEvent(Object source) {
            super(source);
        }
    }
    ```

* 监听自定义事件类
  
  * 监听事件类：
  
  ```java
    public class CustomEventListener implements  ApplicationListener<CustomEvent> {

        @Override
        public void onApplicationEvent(CustomEvent customEvent) {
            System.out.println("custom event");
        }
    }
  ```

  * 添加监听
    * 方式一：

    ```java
    // 在启动类添加监听
    SpringApplication application = new SpringApplication(BootApplication.class);
    application.addListeners(new CustomEventListener());
    ```

    * 方式二：

    resources/META-INF/spring.factories在文件中添加监听配置
  
    ```text
    org.springframework.context.ApplicationListener=\
    stcsm.framework.core.listeners.CustomEventListener
    ```

* 发送自定义事件信号

```java
    @Autowired
    private ApplicationContext applicationContext;

    public void test() {
        applicationContext.publishEvent(new CustomEvent("sss"));
    }
```

## spring.factories

springboot的扩展机制。通过在文件中定义接口的实现扩展功能。

### 实现机制

spring-core包里定义了SpringFactoriesLoader类，这个类实现了检索META-INF/spring.factories（resources/META-INF/spring.factories）文件，并获取指定接口的配置的功能。在这个类中定义了两个对外的方法：

* loadFactories。根据接口类获取其实现类的实例，这个方法返回的是对象列表。
* loadFactoryNames。根据接口获取其接口类的名称，这个方法返回的是类名的列表。

示例：

```text
# PropertySource Loaders
org.springframework.boot.env.PropertySourceLoader=\
org.springframework.boot.env.PropertiesPropertySourceLoader,\
org.springframework.boot.env.YamlPropertySourceLoader
# 应用启动监听器
org.springframework.boot.SpringApplicationRunListener=\
org.springframework.boot.context.event.EventPublishingRunListener
# Application Context Initializers
org.springframework.context.ApplicationContextInitializer=\
org.springframework.boot.context.ConfigurationWarningsApplicationContextInitializer,\
org.springframework.boot.context.ContextIdApplicationContextInitializer
# 应用监听器配置
org.springframework.context.ApplicationListener=\
org.springframework.boot.builder.ParentContextCloserApplicationListener
# 应用自动配置类配置
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration,\
com.xxx.xxx.xxx.xxx.config.DataPermissionConfig
```

**AutoConfigureAfter**

AutoConfigureAfter，AutoConfigureBefore，AutoConfigureOrder注解的使用时，需要添加spring.factories文件，在文件中添加：

```text
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration,\
com.xxx.xxx.xxx.xxx.config.DataPermissionConfig
```

spring只会对spring.factory文件下的配置类进行排序

## 参考

* [Spring中的ApplicationListener的使用详解案例](https://blog.csdn.net/u010963948/article/details/83507185)
* [springboot源码----ApplicationListener](https://www.jianshu.com/p/4163a8b1bdd1)
