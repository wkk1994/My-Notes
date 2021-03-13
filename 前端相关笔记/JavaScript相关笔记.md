### JavaScript相关笔记

#### reload 和 replace 
* **reload：** 强迫浏览器刷新当前页面。  
location.reload([bForceGet]);  
如果该方法没有规定参数，或者参数是 false，它就会用 HTTP 头 If-Modified-Since 来检测服务器上的文档是否已改变。如果文档已改变，reload() 会再次下载该文档。如果文档未改变，则该方法将从缓存中装载文档。这与用户单击浏览器的刷新按钮的效果是完全一样的。  
如果把该方法的参数设置为 true，那么无论文档的最后修改日期是什么，它都会绕过缓存，从服务器上重新下载该文档。这与用户在单击浏览器的刷新按钮时按住 Shift 健的效果是完全一样。

* **replace：** 该方法通过指定URL替换当前缓存在历史里（客户端）的项目，因此当使用replace方法之后，你不能通过“前进”和“后退”来访问已经被替换的URL。
location.replace(URL);

* **history.go(0);**

#### URI和URL的区别
* URI = Universal Resource Identifier 统一资源标志符
* URL = Universal Resource Locator 统一资源定位符，表示资源的位置
* URN = Universal Resource Name 统一资源名称，表示资源的名称，不能通过名称定位资源

![图解](../../../youdaonote-images/index.html.html charset=utf-8) 

* ftp://ftp.is.co.za/rfc/rfc1808.txt (also a URL because of the protocol)
* http://www.ietf.org/rfc/rfc2396.txt (also a URL because of the protocol)
* ldap://[2001:db8::7]/c=GB?objectClass?one (also a URL because of the protocol)
* mailto:John.Doe@example.com (also a URL because of the protocol)
* news:comp.infosystems.www.servers.unix (also a URL because of the protocol)
* tel:+1-816-555-1212
* telnet://192.0.2.16:80/ (also a URL because of the protocol)
* urn:oasis:names:specification:docbook:dtd:xml:4.1.2

这些全都是URI, 提供了访问机制的为URL。
最后“URL”这个术语正在被弃用。

