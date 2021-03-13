### SimpleDateFormat线程安全问题

[参考](https://www.cnblogs.com/zemliu/p/3290585.html)

[代码地址](https://gitee.com/maanshan/demos/tree/SimpleDateFormat线程安全问题)
#### 原因
```
    SimpleDateFormat类内部有一个Calendar对象引用，它用来储存和这个sdf相关的日期信息,
  例如sdf.parse(dateStr), sdf.format(date) 诸如此类的方法参数传入的日期相关String,
  Date等等, 都是交友Calendar引用来储存的.
  执行parse或者format方法时Calendar实例会先clear之前的数据再set当前要解析的值，然后通过
  Calendar实例的getTime()方法，多线程时会导致clear未获取的值，因此出现问题
```

#### 解决方法

* 方法内部创建SimpleDateFormat
    
```
     将共享的SimpleDateFormat对象放进方法内声明，每个线程单独一个SimpleDateFormat对象；
   会重复创建对象，浪费资源，GC产生一定压力
```
* 将SimpleDateFormat进行同步使用

```text
      每次使用SimpleDateFormat对象时加锁，使用完毕释放锁；影响性能，当多线程并发量大时会对性能
    产生一定影响；
```     
* 使用ThreadLocal变量
```text
  用空间换时间，这样每个线程就会独立享有一个本地的SimpleDateFormat变量；
```
* 使用DateTimeFormatter代替SimpleDateFormat
 ```text
      jdk1.8中新增了LocalDate与LocalDateTime等类解决日期处理方法，DateTimeFormatter解决日期
    格式化问题
    
``` 
* 使用第三方jar
```text
    如：apache.commons.lang包下的FastDateFormat
    FastDateFormat aDateFormat=FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
    String cc=aDateFormat.format(new Date(dLong));
```
 ### ThreadLocal学习
 [参考](https://www.cnblogs.com/dolphin0520/p/3920407.html)
 
 #### 理解ThreadLocal
 ```text
    线程本地变量,ThreadLocal为变量在每个线程中都创建了一个副本，那么每个线程可以访问自己内部的副本变量。
```   

#### API
```text
    public T get() { }  //获取ThreadLocal在当前线程中保存的变量副本
    public void set(T value) { }    //设置当前线程中变量的副本
    public void remove() { }    //移除当前线程中变量的副本
    protected T initialValue() { }  //初始化当前线程要使用的变量值
```
