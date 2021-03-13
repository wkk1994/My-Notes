
[TOC]  

>  <h3 id="10">目录</h3>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[1.引用](#1)  

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[2.代码块](#2)  

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[3.标题](#3)  

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[4.字体](#4)  

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[5.链接](#5)   

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[6.列表](#6)  

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[7.图片](#7) 

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[8.行内 HTML 元素](#8) 

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[9.符号转义](#9) 

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[10.html标签](#10) 

## MarkDown基本语法

### <h3 id="1">1.引用</h3>

> 引用1

> 引用1  
引用1  
引用1  

嵌套引用

> 最外层引用
>> 多一个 > 嵌套一层引用 
>>> 可以嵌套很多层

>     同样的，在前面加四个空格形成代码块
>  
> ```
> 或者使用 ``` 形成代码块
> ```


### <h3 id="2">2.代码块</h3>

```javascript
$(document).ready(function () {
    alert('hello world');
});
```

```java
    public static void main(String[] args){
         System.out,println("123");
    }
```

```select * from dual; ```

### <h3 id="3">3.标题</h3>

标题1
======

标题2
-----

## 大标题 ##
### 小标题 ###

### <h3 id="4">4.字体</h3>

*斜体文本*    _斜体文本_  
**粗体文本**    __粗体文本__  
***粗斜体文本***    ___粗斜体文本___  
*我做的馅饼
是全天下*  
<font size="10" color="#006600" face="黑体">size为8</font><br /> 

### <h3 id="5">5.链接</h3>

文字链接 [链接名称](http://链接网址)  
网址链接 <http://链接网址>

这个链接用 java 作为网址变量 [Baidu][1].  
这个链接用 yahoo 作为网址变量 [Yahoo!][yahoo].

  [1]: http://www.baidu.com/
  [yahoo]: http://www.yahoo.com/

### <h3 id="6">6.列表</h3>

- 列表文本前使用 [减号+空格]
+ 列表文本前使用 [加号+空格]
* 列表文本前使用 [星号+空格]

1. 列表前使用 [数字+空格]
2. 我们会自动帮你添加数字
7. 不用担心数字不对，显示的时候我们会自动把这行的 7 纠正为 3

### <h3 id="7">7.图片</h3>

![图片名称](images/mybatis-generator.png)

如果另起一行，只需在当前行结尾加 2 个空格  
如果是要起一个新段落，只需要空出一行即可。

前面的段落

---

后面的段落

### <h3 id="8">8.行内 HTML 元素</h3>

使用 <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>Del</kbd> 重启电脑

<b> Markdown 在此处同样适用，如 *加粗* </b>

### <h3 id="9">9.符号转义</h3>

\_不想这里的文本变斜体\_  
\*\*不想这里的文本被加粗\*\*

![GitHub Mark](http://github.global.ssl.fastly.net/images/modules/logos_page/GitHub-Mark.png "GitHub Mark")

### <h3 id="10">10.html标签</h3>

<details>
<summary>Copyright 2011.</summary>
<p>All pages and graphics on this web site are the property of W3School.</p>
</details>

### <h3 id="11">11.特殊符号</h3>

2^8^=256

a~10~=1


## 工具
可以使用vscdeo编写Markdown文档

使用插件[Markdown Preview Enhanced](https://shd101wyy.github.io/markdown-preview-enhanced/#/zh-cn/)

Markdown Preview Enhanced可以实现更好的实时预览，文档转html，转pdf，转PNG, 以及 JPEG功能




