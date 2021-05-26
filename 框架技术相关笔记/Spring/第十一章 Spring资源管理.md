# Spring资源管理

## Spring资源管理的引入动机

Java已经提供标准的资源管理，Spring为什么还重复造轮子？

* Java标准的资源管理虽然强大，但是扩展复杂，资源的存储方式并不统一，Spring的作者根据自己的理解以及需要需要重新实现资源管理。
* Spring要自立门户：Spring的目标是做成一个生态，所以有些实现不能特别依赖于第三方。
* Spring的“抄”、“超”和“潮”：Spring的资源管理借鉴了Java的资源管理，但是实现上超越了Java的资源管理，引领技术潮流。

## Java标准资源管理

**Java标准的资源定位：**

|资源|说明|
|--|--|
|面向资源| 文件系统、artifact（jar、war、ear 文件）以及远程资源（HTTP、FTP 等）|
|API整合| java.lang.ClassLoader#getResource、java.io.File 或 java.net.URL|
|资源定位| java.net.URL 或 java.net.URI|
|面向流式存储| java.net.URLConnection: 根据上面的URL或URI获取特定资源的连接。|
|协议扩展| java.net.URLStreamHandler 或 java.net.URLStreamHandlerFactory|

> URLStreamHandlerFactory用来根据不同的协议创建URLStreamHandler，URLStreamHandler根据URL来创建URLConnection。

**基于java.net.URLStreamHandlerFactory扩展协议**

![URLStreamHandlerFactory扩展协议](./images/URLStreamHandlerFactory扩展协议.png)

从上图可以知道，URLStreamHandlerFactory用来根据不同的protocol创建URLStreamHandler，URLStreamHandler根据URL来创建URLConnection。而URL拥有URLStreamHandler和URLStreamHandlerFactory变量，而且URLStreamHandlerFactory是静态的，setURLStreamHandlerFactory方法也是静态的，并且setURLStreamHandlerFactory方法有检查，如果factory不为空，会抛出错误，也就是说在一个JVM中一般URL.class只会存在一个，对应的factory也只能有一个被使用，其他的factry都不会使用。

JDK1.8内置的协议实现：

|协议| 实现类|
|--|--|
|file| sun.net.www.protocol.file.Handler|
|ftp| sun.net.www.protocol.ftp.Handler|
|http| sun.net.www.protocol.http.Handler|
|https| sun.net.www.protocol.https.Handler|
|jar| sun.net.www.protocol.jar.Handler|
|mailto| sun.net.www.protocol.mailto.Handler|
|netdoc| sun.net.www.protocol.netdoc.Handler|

可以看出的规律是jdk提供的默认条件的Handler的包名需要满足：sun.net.www.protocol.${protocol}.Handler。

如果要实现对URLStreamHandler的扩展协议需要满足：

通过 Java Properties的`java.protocol.handler.pkgs`指定实现类包名，实现类名必须为“Handler”。如果存在多包名指定，通过分隔符 “|”。需要注意的是一般都在Java启动命令中通过—D传递这个参数，如果启动后或启动中在代码中设置，可能参数`java.protocol.handler.pkgs`以及读取完成了，不会起作用。

扩展协议的具体原理参考`java.net.URL#getURLStreamHandler`。

## Java标准资源管理扩展

Java标准资源管理的扩展方式有三种，实现方式分别为：

* 方式一：实现URLStreamHandler并放置在sun.net.www.protocol.${协议名}.Handler包下，Java在获取Resource会自动读取该包下的协议扩展。

  自定义Java协议扩展示例代码：[XHandlerTest.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/resource/src/main/java/sun/net/www/protocol/x/XHandlerTest.java)

* 方式二：
  * 实现 URLStreamHandler；
  * 添加 -Djava.protocol.handler.pkgs 启动参数，指向 URLStreamHandler 实现类的包下。
  
  自定义Java协议扩展示例代码：[Handler.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/resource/src/main/java/com/wkk/learn/spring/resource/springx/Handler.java)
  启动添加参数：`-Djava.protocol.handler.pkgs=com.wkk.learn.spring.resource`

* 方式三：实现URLStreamHandlerFactory并传递到 URL 之中。

  自定义Java协议扩展示例代码：[MySpringXURLStreamHandlerFactory.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/resource/src/main/java/com/wkk/learn/spring/resource/springx/MySpringXURLStreamHandlerFactory.java)

## Spring 资源接口

Spring内建的资源接口：

|类型| 接口|
|--|--|
|输入流| org.springframework.core.io.InputStreamSource|
|只读资源| org.springframework.core.io.Resource|
|可写资源| org.springframework.core.io.WritableResource|
|编码资源| org.springframework.core.io.support.EncodedResource|
|上下文资源| org.springframework.core.io.ContextResource: 应用场景比较少，一般给Servlet使用|

* InputStreamSource：只有一个getInputStream方法，也就是该接口的实现类允许获取资源的输入流。
* Resource：只读资源，继承了InputStreamSource接口，也具有获取输入流功能。同时具有getURL、getURI、getFile等功能，对资源只提供读取功能的接口。
* WritableResource：可写资源，继承Resource，有isWritable来判断资源是否可写，同时可以getOutputStream获取输出流。
* EncodedResource：编码资源，继承InputStreamSource，针对需要指定资源编码如UTF-8这种的资源。属性有Resource对象，主要通过getInputStreamReader来实现编码。
* ContextResource：上下文资源，一般给Servlet引擎使用。

## Spring内建Resource实现

|资源来源| 资源协议| 实现类|
|--|--|--|
|Bean定义|无| org.springframework.beans.factory.support.BeanDefinitionResource|
|数组| 无| org.springframework.core.io.ByteArrayResource|
|类路径| classpath:/| org.springframework.core.io.ClassPathResource|
|文件系统| file:/| org.springframework.core.io.FileSystemResource|
|URL| URL 支持的协议| org.springframework.core.io.UrlResource|
|ServletContext| 无| org.springframework.web.context.support.ServletContextResource|

BeanDefinitionResource一般使用的很少，它的内部将BeanDefinition作为变量进行组合。
ByteArrayResource内部使用一个byte[]数组存储资源。
UrlResource会根据传入的URL先调用openConnection()后通过URLConnection#getInputStream()获取资源。

> org.springframework.core.io.Resource中的方法和属性和sun.misc.Resource中有很多相似的，可以看出是Spring借鉴了Java的资源实现。

## Spring Resource接口扩展

Spring对Resource接口进行了扩展，实现了WritableResource（可写资源）和EncodedResource（编码资源）。

可写资源接口：

* org.springframework.core.io.WritableResource的实现类有：

  * org.springframework.core.io.FileSystemResource
  * org.springframework.core.io.FileUrlResource（@since 5.0.2）
  * org.springframework.core.io.PathResource（@since 4.0 & @Deprecated）

编码资源接口：

* org.springframework.core.io.support.EncodedResource

带有字符编码的FileSystemResource使用示例：[EncodeFileSystemResourceDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/resource/src/main/java/com/wkk/learn/spring/resource/EncodeFileSystemResourceDemo.java)

## Spring资源加载

Spring的资源需要通过资源加载器进行加载，资源加载器接口是`org.springframework.core.io.ResourceLoader`，它的一个重要实现是`org.springframework.core.io.DefaultResourceLoader`，Spring中的资源加载器很多都是实现了该类。

`org.springframework.core.io.DefaultResourceLoader`的主要实现类有：

* org.springframework.core.io.FileSystemResourceLoader
* org.springframework.core.io.ClassRelativeResourceLoader
* org.springframework.context.support.AbstractApplicationContext

通过FileSystemResourceLoader加载FileSystemResource代码示例：[EncodeFileSystemResourceLoaderDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/resource/src/main/java/com/wkk/learn/spring/resource/EncodeFileSystemResourceLoaderDemo.java)

## Spring通配路径资源加载器

Spring的资源加载器，除了上面常规的加载器之外，还实现了通配路径的资源加载方式，通配路径资源加载器的接口是：`org.springframework.core.io.support.ResourcePatternResolver`，实现类有`org.springframework.core.io.support.PathMatchingResourcePatternResolver`。

路径匹配器接口：`org.springframework.util.PathMatcher`，内建的默认实现是`org.springframework.util.AntPathMatcher`，它是Ant风格的路径匹配模式。

Ant通配路径实现细节参考代码：`PathMatchingResourcePatternResolver#getResources`。

## Spring通配路径资源扩展

要对Spring通配路径资源进行扩展有两种方式：重写PathMatchingResourcePatternResolver类和实现PathMatcher，实现PathMatcher比较简单，具体步骤为：

* 1.实现org.springframework.util.PathMatcher类；
* 2.重制PathMatcher：通过方法PathMatchingResourcePatternResolver#setPathMatcher将默认的AntPathMatcher替换为自定义的PathMatcher实现类。

扩展Spring通配路径资源示例：[CustomizedResourcePatternResolverDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/resource/src/main/java/com/wkk/learn/spring/resource/CustomizedResourcePatternResolverDemo.java)

## 依赖注入Spring Resource

Spring Resource的注入也可以使用通过@Value注解实现依赖注入，也支持通配符的形式注入资源。

如：

```java
@Value(“classpath:/...”) 
private Resource resource;

@Value(“classpath:\/*.properties”) 
private Resource propertiesResource;
```

依赖注入Resource示例：[InjectionResourceDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/resource/src/main/java/com/wkk/learn/spring/resource/InjectionResourceDemo.java)

## 依赖注入ResourceLoader

依赖注入ResourceLoader的方式通常有三种，分别是：

* 实现ResourceLoaderAware回调；
* @Autowired注入ResourceLoader；
* 注入 ApplicationContext 作为 ResourceLoader

这三种方式注入的对象都是同一个对象，都是AbstractApplicationContext对象。

依赖注入ResourceLoader示例：[InjectionResourceLoaderDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/resource/src/main/java/com/wkk/learn/spring/resource/InjectionResourceLoaderDemo.java)

## 面试题

* Spring配置资源中有哪些常见的类型？

  Xml资源
  Properties资源
  YAML资源

* 请列举不同类型Spring配置资源？

  * XML资源
    * 普通的BeanDefinition XML配置资源：*.xml
    * Spring Schema资源：*.xsd
  * Properties资源
    * 普通 Properties 格式资源 - *.properties
    * Spring Handler 实现类映射文件 - META-INF/spring.handlers
    * Spring Schema 资源映射文件 - META-INF/spring.schemas
  * YAML资源
    * 普通YAML资源配置：\*.yaml或*.yml

* Java 标准资源管理扩展的步骤？

  * 简易实现
    * 实现 URLStreamHandler 并放置在 sun.net.www.protocol.${protocol}.Handler 包下
  * 自定义实现
    * 实现 URLStreamHandler 
    * 添加 -Djava.protocol.handler.pkgs 启动参数，指向 URLStreamHandler 实现类的包下
  * 高级实现
    * 实现 URLStreamHandlerFactory 并传递到 URL 之中
