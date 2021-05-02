# Spring Bean的作用域

## Spring Bean作用域

目前Spring Bean包含5个作用域：

* singleton: 默认的Spring Bean的作用域，一个BeanFactory中只有一个实例，对应的是单例模式。
* prototype: 原型作用域，每次依赖查找和依赖注入都会生成Bean对象，对应的是原型模式。
* request: 将Spring Bean存储在ServletRequest上下文，每次存储到ServletRequest中都是一个新的Bean实例。
* session: 将Spring Bean存储在HttpSession中，每次存储到HttpSession中都是一个新的Bean实例。
* application: 将Spring Bean存储在ServletContext中，每次存储在ServletContext中都是一个新的Bean实例。

最常用的Bean作用域就是singleton和prototype，现在开发模式都是前后端分离的形式，后面的和Servlet相关的作用域基本不使用了。

## Singleton Bean作用域

Singleton作用域是在一个BeanFactory中唯一的，但是对于有层次性关系的BeanFactory中，可能存在多个。

实际上BeanDefinition#isSingleton()和BeanDefinition#isPrototype()的方法不是互斥的，允许同时返回true，同时返回true只会先判断isSingleton()，后判断isPrototype()。一个Bean是singleton还是prototype在业务定义上是互斥的。

> org.springframework.beans.factory.support.AbstractBeanFactory#doGetBean

## Prototype Bean作用域

配置方式：

* xml：

  ```xml
  <bean id="xxx" class="xxxx" scope="prototype"></bean>
  ```

* 注解：

  ```java
  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  private User prototypeUser() {
      User user = new User();
      user.setId(String.valueOf(System.nanoTime()));
      return user;
  }
  ```

代码示例：[BeanScopeDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/spring-ioc-learn/bean-scope/src/main/java/com/wkk/learn/spring/bean/scope/BeanScopeDemo.java)

Singleton Bean无论是依赖注入还是依赖查找都是同一个对象；Prototype Bean无论是依赖注入还是依赖查找都是新生成的对象。

如果依赖注入集合类型的对象，Singleton Bean 和 Prototype Bean 均会存在一个，但是Prototype Bean 有别于其他地方的依赖注入 Prototype Bean。

无论是 Singleton 还是 Prototype Bean 均会执行初始化方法回调，不过仅 Singleton Bean 会执行销毁方法回调。

注意事项:
Spring 容器没有办法管理 prototype Bean 的完整生命周期，也没有办法记录实例的存在。销毁回调方法将不会执行，可以利用 BeanPostProcessor 进行清扫工作。所以使用原型Bean的时候要注意销毁工作，防止OOM。

实际上调用BeanPostProcessor进行清扫工作是不能进行的。如果需要进行bean的销毁，可以通过在当前注入bean实例的对象中调用bean实例的销毁方法。

```java
@Override
public void destroy() throws Exception {
    System.out.println("当前 BeanScopeDemo Bean 正在销毁中...");

    this.prototypeUser1.destroy();
    this.prototypeUser2.destroy();
    // 获取 BeanDefinition
    for (Map.Entry<String, User> entry : this.users.entrySet()) {
        String beanName = entry.getKey();
        BeanDefinition beanDefinition = beanFactory.getBeanDefinitio(beanName);
        if (beanDefinition.isPrototype()) { // 如果当前 Bean 是 prototypescope
            User user = entry.getValue();
            user.destroy();
        }
    }
    System.out.println("当前 BeanScopeDemo Bean 销毁完成");
}
```

> @Autowired 依赖注入的方式是先根据类型查找,在判断是否有Primary bean，再判断resolvableDependencies和bean的名称与字段对应的descriptor.getDependencyName()是否相等,源码:DefaultListableBeanFactory#determineAutowireCandidate() 所以当
@Autowired
private User singletonUser;
@Autowired
private User prototypeUser;时并不会报NoUniqueBeanDefinitionException的错误

## Request Bean作用域

配置方式：

* xml:`<bean class= "..." scope = "request" />`；
* Java 注解 : `@RequestScope 或 @Scope(WebApplicationContext.SCOPE_REQUEST)`。

> `@Scope(WebApplicationContext.SCOPE_REQUEST)`，SpringMVC不支持

代码示例：[IndexController.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/spring-ioc-learn/bean-scope/src/main/java/com/wkk/learn/spring/bean/scope/web/controller/IndexController.java)

实现API：`org.springframework.web.context.request.RequestScope`

## Session Bean作用域

配置方式：

* xml:`<bean class= "..." scope = "session" />`；
* Java 注解 : `@RequestScope 或 @Scope(WebApplicationContext.SCOPE_REQUEST)`。

实现API：`org.springframework.web.context.request.SessionScope`

## Application Bean作用域

配置方式：

* xml:`<bean class= "..." scope = "application" />`；
* Java 注解 : `@ApplicationScope 或 @Scope(WebApplicationContext.SCOPE_APPLICATION)`。

实现API：`org.springframework.web.context.support.ServletContextScope`

## 自定义Bean作用域

自定义Bean作用域步骤：

* 1.实现Scope接口`org.springframework.beans.factory.config.Scope`
* 2.注册自定义的Scope
  * 通过API注册

     ```java
      applicationContext.addBeanFactoryPostProcessor(beanFactory -> {
            beanFactory.registerScope(ThreadLocalScope.THREAD_LOCAL_SCOPE, new ThreadLocalScope());
      });
     ```

  * 通过xml注册

    ```xml
    <bean class="org.springframework.beans.factory.config.CustomScopeConfigurer">
        <property name="scopes">
            <map>
                <entry key="..."></entry>
            </map>
        </property>
    </bean>
    ```

实现线程作用域Bean的代码示例：[ThreadLocalScopeDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/spring-ioc-learn/bean-scope/src/main/java/com/wkk/learn/spring/bean/scope/ThreadLocalScopeDemo.java)

Spring内置了一个Bean的线程作用域的实现：`org.springframework.context.support.SimpleThreadScope`。

> NamedThreadLocal和ThreadLocal的区别：NamedThreadLocal只是多了一个name作为区分名。

## Spring Cloud中Scope的应用

Spring Cloud中通过自定义RefreshScope，实现Bean的动态刷新。

## 面试题

* Spring 內建的 Bean 作用域有几种？

  从设计模式来说有singelton、prototype；
  从web来说有request、session、application。

* singleton Bean是否在一个应用中唯一？

  否，singleton bean 仅在当前 Spring IoC 容器（BeanFactory）中是单例对象。

* “application”Bean 是否被其他方案替代

  可以的，实际上，“application” Bean 与“singleton” Bean 没有本质区别。
