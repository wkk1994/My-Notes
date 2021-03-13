# 第九章 Spring Boot

## Spring Boot的特性

* 方便地创建可独立运行的Spring应用程序
* 直接内嵌Tomcat、Jetty或Undertow
* 简化了项目的构建配置
* 为Spring及第三方库提供自动配置
* 提交生产级特性
* 无需生成代码或XML配置

## Spring Boot的四大特性

* 自动配置-Auto Configuration
* 起步依赖-Starter Dependency
* 命令行界面-Spring Boot CLI
* Actuator

## Spring Boot自动配置

*代码位置：springboot/autoconfig-demo*

### 自动配置

* 基于添加的JAR依赖自动对Spring Boot应用程序进行配置
* spring-boot-autoconfiguration，所有的自动配置类基本都在这个jar包内

### 开启自动配置

* @EnbaleAutoConfiguration注解开启自动配置
  exclude = Class<?>[]属性可以排除指定自动配置类

* @SpringBootApplication包含了@EnbaleAutoConfiguration注解

### 自动配置的实现原理

* @EnableAutoConfiguration
  * 引入 AutoConfigurationImportSelector
  * META-INF/spring.factories
    * org.springframework.boot.autoconfigure.EnableAutoConfiguration

```java
/**
 * 获取自动配置类
 *
 */
protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
    List<String> configurations = SpringFactoriesLoader.loadFactoryNames(getSpringFactoriesLoaderFactoryClass(), getBeanClassLoader());
    Assert.notEmpty(configurations, "No auto configuration classes found in META-INF/spring.factories. If you " + "are using a custom packaging, make sure that file is correct.");
    return configurations;
}
```

* 条件注解
  spring通过一下的条件注解实现自动配置
  * @Conditional
  * @ConditionalOnClass
  * @ConditionalOnBean
  * @ConditionalOnMissingBean
  * @ConditionalOnProperty

### 了解自动配置的情况

* 观察自动配置的判断结果
  * --debug 启动时添加这个参数可以在日志中看到自动配置的情况，比如当前自动配置的匹配的，自动配置没有匹配上的

## 实现自动配置

* 条件注解
  * @Conditional
* 类条件
  * @ConditionalOnClass 当存在指定class时
  * @ConditionalOnMissingClass 当不存在指定class时
* 属性条件
  * @ConditionalOnProperty 当存在指定属性时
* Bean条件
  * @ConditionalOnBean  当存在bean时
  * @ConditionalOnMissingBean 当不存在bean时
  * @ConditionalOnSingleCandidate 当存在指定的bean，并且指定的bean实例是单个，或者有一个primary候选项时
* 资源条件
  * @ConditionalOnResource 但存在指定资源时
* Web应用条件
  * @ConditionalOnWebApplication 当是web应用时
  * @ConditionalOnNotWebApplication 当不是web应用时
* 其他条件
  * @ConditionalOnExpression SpEL表达式返回true时
  * @ConditionalOnJava 根据运行的jvm版本条件
  * @ConditionalOnJndi 根据jndi的匹配作为条件
* 自动配置的执行顺序
  * @AutoConfigureBefore
  * @AutoConfigureAfter
  * @AutoConfigureOrder

实现自动配置

* 编写 Java Config
  * @Configuration
* 添加条件
  * @Conditional
* 定位⾃动配置
  * META-INF/spring.factories

示例：

```java
@Configuration
@ConditionalOnClass(AutoconfigRunner.class)
public class AutoconfigConditional{

    @Bean
    @ConditionalOnMissingBean(AutoconfigRunner.class)
    @ConditionalOnProperty(name = "autoconfig.enabled", havingValue = "true", matchIfMissing = true)
    public AutoconfigRunner autoconfigRunner() {
        return new AutoconfigRunner("autoconfig");
    }
}
```

/resources/META-INF/spring.factories

```text
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.wkk.learn.springboot.autoconfig.conditional.AutoconfigConditional
```

## 如何在低版本 Spring 中快速实现类似⾃动配置的功能

*代码位置：springboot/autoconfig-demo*

* 低版本的spring存在的问题
  * 3.X的Spring没有条件注解
  * 无法自动定位需要加载的自动配置
* 解决的方式
  * 条件判断通过BeanFactoryPostProcessor进行判断
  * 配置加载通过component-scan注解扫描或者通过XML文件import

* 常用的条件判断
* 判断类是否存在
  * ClassUtils.isPresent()
* 判断 Bean 是否已定义
  * ListableBeanFactory.containsBeanDefinition()
  * ListableBeanFactory.getBeanNamesForType()
* 注册 Bean 定义
  * BeanDefinitionRegistry.registerBeanDefinition()
  * GenericBeanDefinition
  * BeanFactory.registerSingleton()

## Spring Boot 的配置加载机制

### 外化配置加载顺序

* 开启 DevTools 时，~/.spring-boot-devtools.properties
* 测试类上的 @TestPropertySource 注解
* @SpringBootTest#properties 属性
* 命令⾏参数（ --server.port=9000 ）
* SPRING_APPLICATION_JSON 中的属性
* ServletConfig 初始化参数
* ServletContext 初始化参数
* java:comp/env 中的 JNDI 属性
* System.getProperties()
* 操作系统环境变量
* random.* 涉及到的 RandomValuePropertySource
* jar 包外部的 application-{profile}.properties 或 .yml
* jar 包内部的 application-{profile}.properties 或 .yml
* jar 包外部的 application.properties 或 .yml
* jar 包内部的 application.properties 或 .yml
* @Configuration 类上的 @PropertySource
* SpringApplication.setDefaultProperties() 设置的默认属性

### application.properties

默认位置：

* ./config
* ./
* CLASSPATH 中的 /config
* CLASSPATH 中的 /

可以通过参数修改配置文件名称和地址

* spring.config.name 配置文件名称
* spring.config.location 配置文件地址
* spring.config.additional-location 在spring.config.location地址之前先搜索该地址

### PropertySource 抽象

表示一个键值对，代表属性源。Spring内部是通过它来承载来自不同地方都的属性源的。

添加 PropertySource 的方式：

* <context:property-placeholder>
* PropertySourcesPlaceholderConfigurer
  * PropertyPlaceholderConfigurer
* @PropertySource
* @PropertySources

### SpringBoot中的@ConfigurationProperties

SpringBoot中可以通过@ConfigurationProperties注解快速添加一个属性源。

特性：可以将属性绑定到结构化的对象上；支持Relaxed Binding；支持安全的类型转换；

### 定制 PropertySource

*代码位置：springboot/property-source-demo*

如何定制一个PropertySource：

* 实现PropertySource<T>；
* 从Environment取得PropertySources；
* 将自己的PropertySource添加到合适的位置；
* 切入点位置：
  * EnvironmentPostProcessor
  * BeanFactoryPostProcessor

示例：

```java
public class CustomEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private PropertiesPropertySourceLoader loader = new PropertiesPropertySourceLoader();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources propertySources = environment.getPropertySources();
        ClassPathResource classPathResource = new ClassPathResource("custom.properties");
        try {
            PropertySource ps = loader.load("YetAnotherPropertiesFile", classPathResource)
                    .get(0);
            propertySources.addFirst(ps);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
// @Value("${property.custom}")
// private String custom;
// spring.factories: org.springframework.boot.env.EnvironmentPostProcessor=com.wkk.learn.springboot.property.sources.CustomEnvironmentPostProcessor
```
