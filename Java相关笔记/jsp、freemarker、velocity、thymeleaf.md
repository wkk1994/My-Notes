### jsp、freemarker、velocity、thymeleaf[参考](https://blog.csdn.net/ztchun/article/details/76407612)只能作为参考，有歧义
#### 1. java表现层技术

- jsp
- freemarker
- velocity
- thymeleaf

#### jsp

全称java server page,java服务器页面，动态页面开发技术
- 优点：
    
    1.功能强大，可以写java代码（也是缺点，太多java代码，破坏了mvc结构）  
    2.支持jsp标签  
    3.支持表达式语言（EL）  
    4.官方推荐，用户群大  
    5.性能良好。jsp编译成class文件执行，有很好的性能表现 
- 缺点

   1.JSP页面执行时, 首先被转换为 .java文件(Servlet), 然后将.java文件编译为字节码文件. 这样,出错信息实际上指向的是转换后的那个.java文件(Servlet), 而不是JSP本身. (调试有难度)
- 说明：

   jsp只会在第一次请求时进行编译成class
   
#### freemarker[参考](http://www.importnew.com/16944.html)

   FreeMarker是一个用Java语言编写的模板引擎，它基于模板来生成文本输出。FreeMarker与Web容器无关，即在Web运行时，它并不知道Servlet或HTTP。它不仅可以用作表现层的实现技术，而且还可以用于生成XML，JSP或Java 等。
- 优点：  

   1、不能编写Java代码，可以实现严格的mvc分离    
   2、性能非常不错    
   3、对jsp标签支持良好    
   4、内置大量常用功能，使用非常方便    
   5、宏定义（类似jsp标签）非常方便    
   6、使用表达式语言    
  
- 缺点：  
  
  1、不是官方标准  
  2、用户群体和第三方标签库没有jsp多  
  3、显示普通页面性能没有jsp高

#### velocity
  
   基于Java的模板引擎，性能比jsp高，mvc分离

#### Thymeleaf
  
   Thymeleaf是个XML/XHTML/HTML5模板引擎，可以用于Web与非Web应用。
- 优点
   
   静态html嵌入标签属性，浏览器可以直接打开模板文件，便于前后端联调。springboot官方推荐方案
- 缺点

    模板必须符合xml规范