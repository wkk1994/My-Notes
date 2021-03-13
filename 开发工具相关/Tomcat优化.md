[TOC]

# Tomcat和JVM配置优化

## Tomcat权限开启

tomcat-users.xml下添加用户：

```xml
<role rolename="manager"/>
<role rolename="manager-gui"/>
<role rolename="admin"/>
<role rolename="admin-gui"/>
<user username="tomcat" password="tomcat" roles="admin-gui,admin,manager-gui,manager"/>
```

通过tomcat管理页面可以查看tomcat运行模式，占用内存，线程池等信息，

## Tomcat调优

Tomcat调优分为系统优化，Tomcat 本身的优化，Java 虚拟机（JVM）调优。

### Tomcat本身优化

#### 连接器（运行模式）

Tomcat Connector的三种不同的运行模式性能相差很大，这三种模式的不同之处如下：

* BIO：
一个线程处理一个请求。缺点：并发量高时，线程数较多，浪费资源。Tomcat7或以下，在Linux系统中默认使用这种方式。

* NIO：
利用Java的异步IO处理，可以通过少量的线程处理大量的请求。Tomcat8在Linux系统中默认使用这种方式。
Connector节点：protocol="org.apache.coyote.http11.Http11NioProtocol"

* APR：
即Apache Portable Runtime，从操作系统层面解决io阻塞问题。Tomcat7或Tomcat8在Win7或以上的系统中启动默认使用这种方式。Linux如果安装了apr和native，Tomcat直接启动就支持apr。

org.apache.coyote.http11.Http11Protocol- 阻塞Java连接器
org.apache.coyote.http11.Http11NioProtocol- 非阻塞Java连接器
org.apache.coyote.http11.Http11AprProtocol- APR /本机连接器。

#### 线程池设置

server.xml:

```xml
<Executor name="tomcatThreadPool"
        namePrefix="catalina-exec-"
        maxThreads="10"
        minSpareThreads="5"
        prestartminSpareThreads="true"/>
```

参数说明：

* namePrefix：线程前缀
* maxThreads：最大线程数
* minSpareThreads：最小空闲线程数
* prestartminSpareThreads： 是否初始化空闲线程

```xml
<Connector port="8075" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443"
               executor="tomcatThreadPool"
               enableLookups="false"
               maxPostSize="10485760"
               URIEncoding="utf-8"
               acceptCount="10"
               acceptorThreadCount="2"
               compression="on"
               compressionMinSize="50"
               noCompressionUserAgents=""
               compressableMimeType="text/html,text/xml,text/javascript,application/x-javascript,application/javascript,text/css,text/plain"
    />
```

* protocol：默认值是 HTTP/1.1使用自动切换机制来选择连接器，本地存在apr库使用apr连接
* connectionTimeout：网络连接超时，单位：毫秒，设置为 0 表示永不超时
* redirectPort：https请求转发端口
* executor：对Executor 元素中的名称的引用。如果设置将忽略所有其他线程属性。
* enableLookups：是否反查域名，以返回远程主机的主机名，为了提高处理能力，应设置为 false。
* maxPostSize：最大请求大小
* URIEncoding：指定 Tomcat 容器的 URL 编码格式
* acceptCount：指定当所有可以使用的处理请求的线程数都被使用时，可传入连接请求的最大队列长度，超过这个数的请求将不予处理，默认为100个。
* acceptorThreadCount：最大请求处理数
* compression：是否对响应的数据进行 GZIP 压缩，off：表示禁止压缩；on：表示允许压缩（文本将被压缩）、force：表示所有情况下都进行压缩，默认值为off，压缩数据后可以有效的减少页面的大小，一般可以减小1/3左右，节省带宽。
* compressionMinSize：表示压缩响应的最小值，只有当响应报文大小大于这个值的时候才会对报文进行压缩，如果开启了压缩功能，默认值就是2048。
* noCompressionUserAgents="gozilla, traviata"： 对于以下的浏览器，不启用压缩。
* compressableMimeType：压缩类型，指定对哪些类型的文件进行数据压缩。
  
更多属性设置[参考](https://tomcat.apache.org/tomcat-7.0-doc/config/http.html)

### 安全性配置

关闭war自动部署unpackWARs="false" autoDeploy="false"。防止被植入木马等恶意程序。
在server.xml中,Host修改如下：

```xml
<Host name="localhost"  appBase="webapps"
    unpackWARs="false" autoDeploy="false">
```

### JVM调优

[参考]( https://blog.csdn.net/axbyc1234/article/details/52806845)

修改starup.bat脚本，在头部添加JVM内存设置。
如：set JAVA_OPTS=-Xmn1125M -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction=0 -XX:+CMSClassUnloadingEnabled -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70

web服务器示例：

```text
-server//服务器模式
-Xmx2g //JVM最大允许分配的堆内存，按需分配
-Xms2g //JVM初始分配的堆内存，一般和Xmx配置成一样以避免每次gc后JVM重新分配内存。
-Xmn256m //年轻代内存大小，整个JVM内存=年轻代 + 年老代 + 持久代
-XX:PermSize=128m //持久代内存大小
-Xss256k //设置每个线程的堆栈大小
-XX:+DisableExplicitGC //忽略手动调用GC, System.gc()的调用就会变成一个空调用，完全不触发GC
-XX:+UseConcMarkSweepGC //并发标记清除（CMS）收集器
-XX:+CMSParallelRemarkEnabled //降低标记停顿
-XX:+UseCMSCompactAtFullCollection //在FULL GC的时候对年老代的压缩
-XX:LargePageSizeInBytes=128m //内存页的大小
-XX:+UseFastAccessorMethods //原始类型的快速优化
-XX:+UseCMSInitiatingOccupancyOnly //使用手动定义初始化定义开始CMS收集
-XX:CMSInitiatingOccupancyFraction=70 //使用cms作为垃圾回收使用70％后开始CMS收集
```

## 参考

* [Tomcat和JVM配置优化手册](https://blog.csdn.net/axbyc1234/article/details/52806845)
* [JDK8 JVM参数与实际环境中的优化配置实践](https://blog.csdn.net/boonya/article/details/69230214)
