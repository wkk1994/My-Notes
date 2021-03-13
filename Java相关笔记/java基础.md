[toc]

# java基础
[代码位置](https://gitee.com/maanshan/demos/tree/java%E5%9F%BA%E7%A1%80)
## 一、数据类型

* 基本类型
    * boolean/1bit
    * byte/8bit
    * char/16bit
    * short/16bit
    * int/32bit 范围: -2^31—— 2^31-1,即-2147483648——2147483647，第一位为符号位
    * float/32bit
    * long/64bit
    * double/64bit

通常情况下一字节等于八位二进制；数据存储是以“字节”（Byte）为单位，数据传输大多是以“位”（bit，又名“比特”）为单位，一个位就代表一个0或1（即二进制），每8个位（bit，简写为b）组成一个字节（Byte，简写为B），是最小一级的信息单位。
0
float和double的范围是由指数的位数来决定的。 float： 1bit（符号位）8bits（指数位） 23bits（尾数位） 
double： 1bit（符号位）11bits（指数位） 52bits（尾数位） 

float：2^23 = 8388608，一共七位，这意味着最多能有7位有效数字，但绝对能保证的为6位，也即float的精度为6~7位有效数字； 
double：2^52 = 4503599627370496，一共16位，同理，double的精度为15~16位。

* 包装类型
    
基本类型都对应有包装类型，基本类型与包装类型之间的赋值使用自动装箱与拆箱完成（java5开始有）  
装箱过程是通过调用包装器的valueOf方法实现的，而拆箱过程是通过调用包装器的 xxxValue方法实现的。（xxx代表对应的基本数据类型）。

## 缓存池
[参考](https://www.cnblogs.com/Pjson/p/8777940.html)

* new Integer(123)与Integer.valueOf(123)的区别在于：
    
    * new Integer(123)每次都会创建一个对象；
    * Integer.valueOf(123)会使用缓存池中的对象，多次调用会取得同一个对象的引用。valueOf()方法会先判断是否在缓存池中，如果在的话就直接返回缓存池的内容。
    
* 基本类型对应缓存池如下：

    * boolean: true false;
    * byte:  all values;
    * short: [-128,127];
    * int: [-128,127];
    * char [\u0000,\u007F];
    * long [-128,127];

## 二、String

String被声明成fianl，不能被继承。
在java8中，String内部使用char数组存储数据。Java9之后，String类改用byte数据存储字符串，同时使用code来标识使用了哪种编码。String内部存值得数组是final的，所以初始化后不能改变数组的引用，String也没有暴露可以改变数组的方法，因此可以保证String不可变。但是可以通过反射改变数组内的数值。
    
* 不可变的好处 

[参考](https://www.programcreek.com/2013/04/why-string-is-immutable-in-java/)
1. 可以缓存hash值
因为String的hash值经常被使用，例如作为HashMap的key。不可变的特性可以使得hash值也不可变，因此只需要进行一次计算。
2. String Pool的需要
如果一个String的对象已经被创建过了，就会从String Pool中获取。只有当String不可变时，才可以使用String Pool;(为什么？如果String可以变，当使用String Pool时。多个String对象指向String Pool中的同一个对象时，修改对象存值就会改变所有引用该对象地址的String对象)
3. 安全性    
String经常作为参数，String不可变性可以保证参数不可变。
4. 线程安全
String的不可变性就不存在线程不安全的问题。

### String，StringBuffer and StringBuilder
1. 可变性
* String不可变
* StringBuffer和StringBuilder可变
2. 线程安全
* String不可变，因此线程安全
* StringBuilder 不是线程安全
* StringBuffer 是线程安全的，内部使用synchronize进行同步

### String Pool
字符串常量池（String Pool）保存着所有字符串字面量（literal strings），这些字面量在编译时期就确定。运行时还可以使用String的intern()方法将字符串添加到String Pool中。
<br/><br/>
<font color='blue'>intern()方法的说明:</font>当一个字符串对象调用intern方法时，如果String Pool中已经存在一个字符串和该字符串值相等(使用equals方法确定)，就直接返回String Pool中该字符串的引用；否则就会在String Pool中添加一个字符串，并返回新字符串的引用。

```java
String s1 = new String("aaa");
String s2 = new String("aaa");
System.out.println(s1 == s2);//false

String s3 = "bbb";//会自动地将字符串放入String Pool中。
```

<font color='red'>注意：</font>在java 7之前，String Pool被放在运行时常量池中，它属于永久代。而在Java 7之后，String Pool被移到堆中。这是因为永久代空间有限，在大量使用字符串的场景下会导致OutOfMemoryError错误。

将String常量池 从 Perm 区移动到了 Java Heap区
String#intern 方法时，如果存在堆中的对象，会直接保存对象的引用，而不会重新创建对象。

[深入理解String#intern](https://tech.meituan.com/2014/03/06/in-depth-understanding-string-intern.html)
```java
public static void main(String[] args) {
    String s = new String("1");
    s.intern();
    String s2 = "1";
    System.out.println(s == s2);

    String s3 = new String("1") + new String("1");
    s3.intern();
    String s4 = "11";
    System.out.println(s3 == s4);
}
```
打印结果是

* jdk6 下false false
* jdk7 下false true

### new String("abc")

使用这种方式一共会创建两个字符串对象（前提是 String Pool 中还没有 "abc" 字符串对象）。

* "abc" 属于字符串字面量，因此编译时期会在 String Pool 中创建一个字符串对象，指向这个 "abc" 字符串字面量；
* 而使用 new 的方式会在堆中创建一个字符串对象。

Stirng构造方法源码：
```java
public String(String original) {
    this.value = original.value;
    this.hash = original.hash;
}
```
可以看到在将一个字符串对象作为另一个字符串对象的构造函数参数时，并不会完全复制 value 数组内容，而是都会指向同一个 value 数组。

## 三、运算

### 参数传递
java的参数是以值传递的形式传入方法中，而不是引用传递。
引用对象传递的是对象的地址值。

### 隐式类型转换

java不能隐式执行向下转换，因为会丢失精度。但是可以隐式执行向上转换。
* 使用 += 或者 ++ 运算符可以执行隐式类型转换。
```java
short s1 = 1;
s1 = s1 +1;//错误 s1与int相加会先隐式向上转换成int后再加减，
            // 相加的结果为int不能向下转为short

s1 += 1;
s1++;
//上面的语句相当于将 s1 + 1 的计算结果进行了向下转型
```
## switch

从 Java 7 开始，可以在 switch 条件判断语句中使用 String 对象。
switch 不支持 long，是因为 switch 的设计初衷是对那些只有少数的几个值进行等值判断，如果值过于复杂，那么还是用 if 比较合适。

## 四、继承
### 访问权限
Java 中有三个访问权限修饰符：private、protected 以及 public，如果不加访问修饰符，表示包级可见。

可以对类或类中的成员（字段以及方法）加上访问修饰符。

* 类可见表示其它类可以用这个类创建实例对象。
* 成员可见表示其它类可以用这个类的实例对象访问到该成员；

如果子类的方法重写了父类的方法，那么子类中该方法的访问级别不允许低于父类的访问级别。这是为了确保可以使用父类实例的地方都可以使用子类实例，也就是确保满足里氏替换原则。

### 抽象类与接口

**1. 抽象类** 

抽象类和抽象方法都使用 abstract 关键字进行声明。抽象类一般会包含抽象方法，抽象方法一定位于抽象类中。  
抽象类和普通类最大的区别是，抽象类不能被实例化，需要继承抽象类才能实例化其子类。

**2. 接口**  

接口是抽象类的延伸，在 Java 8 之前，它可以看成是一个完全抽象的类，也就是说它不能有任何的方法实现。

<font color="blue">[从 Java 8 开始](https://blog.csdn.net/sun_promise/article/details/51220518)</font>，接口也可以拥有默认的方法实现和静态方法实现，这是因为不支持默认方法的接口的维护成本太高了。在 Java 8 之前，如果一个接口想要添加新的方法，那么要修改所有实现了该接口的类。

接口的成员（字段 + 方法）默认都是 public 的，并且不允许定义为 private 或者 protected。

接口的字段默认都是 static 和 final 的

**3. 比较**

* 从设计层面上看，抽象类提供了一种 IS-A 关系，那么就必须满足里式替换原则，即子类对象必须能够替换掉所有父类对象。而接口更像是一种 LIKE-A 关系，它只是提供一种方法实现契约，并不要求接口和实现接口的类具有 IS-A 关系
* 从使用上来看，一个类可以实现多个接口，但是不能继承多个抽象类。
* 接口的字段只能是 static 和 final 类型的，而抽象类的字段没有这种限制。
* 接口的成员只能是 public 的，而抽象类的成员可以有多种访问权限。

**4. 使用选择** 

使用接口：

- 需要让不相关的类都实现一个方法，例如不相关的类都可以实现 Compareable 接口中的 compareTo() 方法；
- 需要使用多重继承。

使用抽象类：

- 需要在几个相关的类中共享代码。
- 需要能控制继承来的成员的访问权限，而不是都为 public。
- 需要继承非静态和非常量字段。

在很多情况下，接口优先于抽象类。因为接口没有抽象类严格的类层次结构要求，可以灵活地为一个类添加行为。并且从 Java 8 开始，接口也可以有默认的方法实现，使得修改接口的成本也变的很低。

### super 访问父类的构造函数 访问父类的成员

### 重写与重载

**1. 重写（Override）** 

存在于继承体系中，指子类实现了一个与父类在方法声明上完全相同的一个方法。

为了满足里式替换原则，重写有有以下两个限制：

- 子类方法的访问权限必须大于等于父类方法；
- 子类方法的返回类型必须是父类方法返回类型或为其子类型。

使用 @Override 注解，可以让编译器帮忙检查是否满足上面的两个限制条件。

**2. 重载（Overload）** 

存在于同一个类中，指一个方法与已经存在的方法名称上相同，但是参数类型、个数、顺序至少有一个不同。

应该注意的是，返回值不同，其它都相同不算是重载。

## 五、Object通用方法

```java

public native int hashCode()

public boolean equals(Object obj)

protected native Object clone() throws CloneNotSupportedException

public String toString()

public final native Class<?> getClass()

protected void finalize() throws Throwable {}

public final native void notify()

public final native void notifyAll()

public final native void wait(long timeout) throws InterruptedException

public final void wait(long timeout, int nanos) throws InterruptedException

public final void wait() throws InterruptedException
```

### equals()
1. 等价关系
- 自反性 x.equals(x)//true
- 对称性 x.equals(y)==y.equals(x)
- 传递性 x.equals(y)//true,y.eqials(z)//true那么x.equals(z)//true
- 一致性  多次调用equals()方法结果一致

2. 等价与相等
- 对于基本类型没有equals方法，==是判断2个值是否相等
- 对于引用类型，==判断的是引用的是否是同一个对象，equals判断引用的对象是否等价

### hashCode()

hashCode() 返回散列值，而 equals() 是用来判断两个对象是否等价。等价的两个对象散列值一定相同，但是散列值相同的两个对象不一定等价。在覆盖equals方法时应总是要覆盖hashCode方法，保证等价的两个对象散列值也相等。

理想的散列函数应当具有均匀性，即不相等的对象应当均匀分布到所有可能的散列值上。这就要求了散列函数要把所有域的值都考虑进来。可以将每个域都当成 R 进制的某一位，然后组成一个 R 进制的整数。R 一般取 31，因为它是一个奇素数，如果是偶数的话，当出现乘法溢出，信息就会丢失，因为与 2 相乘相当于向左移一位。

```java
@Override
public int hashCode() {
    int result = 17;
    result = 31 * result + x;
    result = 31 * result + y;
    result = 31 * result + z;
    return result;
}
```
乘法溢出
[参考1](https://blog.csdn.net/z69183787/article/details/54692626)
[参考2](https://blog.csdn.net/qq_40284805/article/details/79532900)
为什么会内存溢出？int只能存储32位，除去首位位符号位(正数位0，负数位1)，只能存31位。当int增大(减小)达到最大位时再次操作时，会将末尾舍弃。一直增加后每位都为1，等于0。
```java
int de = 2147483647;
System.out.println(de);//2147483647
System.out.println(de+1);//-2147483648
```

### toString()

默认返回 ToStringExample@4554617c 这种形式，其中 @ 后面的数值为散列码的无符号十六进制表示。

### clone()

clone() 是 Object 的 protected 方法，它不是 public，一个类不显式去重写 clone()，其它类就不能直接去调用该类实例的 clone() 方法。要想使用该类的clone方法，该类必须要实现Cloneable。 
Cloneable 接口只是规定，如果一个类没有实现 Cloneable 接口又调用了 clone() 方法，就会抛出 CloneNotSupportedException。

**1.浅复制**

拷贝对象和原始对象的引用类型引用同一个对象。

**2.深复制**

拷贝对象和原始对象的引用类型引用不同对象。

**3.clone() 的替代方案**

使用 clone() 方法来拷贝一个对象即复杂又有风险，它会抛出异常，并且还需要类型转换。Effective Java 书上讲到，最好不要去使用 clone()，可以使用拷贝构造函数或者拷贝工厂来拷贝一个对象。

## static
**1. 静态内部类**

非静态内部类依赖于外部类的实例，而静态内部类不需要。静态内部类不能访问外部类的非静态的变量和方法。

**2.初始化顺序**
静态变量和静态语句块优先于实例变量和普通语句块，静态变量和静态语句块的初始化顺序取决于它们在代码中的顺序。  
存在继承的情况下，初始化顺序为：
* 父类（静态变量、静态语句块）
* 子类（静态变量、静态语句块）
* 父类（实例变量、普通语句块）
* 父类（构造函数）
* 子类（实例变量、普通语句块）
* 子类（构造函数）

## 七、反射
每个类都有一个 Class 对象，包含了与类有关的信息。当编译一个新类时，会产生一个同名的 .class 文件，该文件内容保存着 Class 对象。

类加载相当于 Class 对象的加载，类在第一次使用时才动态加载到 JVM 中。也可以使用 Class.forName("com.mysql.jdbc.Driver") 这种方式来控制类的加载，该方法会返回一个 Class 对象。

反射可以提供运行时的类信息，并且这个类可以在运行时才加载进来，甚至在编译时期该类的 .class 不存在也可以加载进来。

Class 和 java.lang.reflect 一起对反射提供了支持，java.lang.reflect 类库主要包含了以下三个类：

* Field ：可以使用 get() 和 set() 方法读取和修改 Field 对象关联的字段；
    - getFileds：获取所有public的成员变量，包括父类的
    - getDeclaredFields：获取所有的成员变量，但是不包括父类的
    - getFiled(String name)
    - getDeclaredField(String name)
* Method ：可以使用 invoke() 方法调用与 Method 对象关联的方法；
    - getDeclaredMethods：方法返回类或接口声明的所有方法，包括公共、保护、默认和私有的方法，但是不包括继承的方法。
    - getMethods：方法返回某个类的所有publie方法，包括继承类的公用方法。
    - getMethod(String name,Class<?>... parameterTypes)： 根据方法名和参数列表获取方法
* Constructor ：可以用 Constructor 创建新的对象。

**反射最重要的用途是开发各种通用框架 比如Spring Strtus2等**

**1. 判断是否为某个类的实例**

除了instanceOf还可以使用Class对象的isInstance(Object obj)

**2. 调用方法invoke()**

[深入理解invoke](https://www.sczyh30.com/posts/Java/java-reflection-2/#)
    invoke会首先进行权限检查 用setAccessible方法设置为true表示忽略权限规则，调用方法时无需检查权限（也就是说可以调用任意的private方法，违反了封装）。
```java
        Class<?> klass = String.class;
        Constructor<?> constructor = klass.getConstructor(String.class);
        Object object = constructor.newInstance("test");
        Method method = klass.getMethod("equals",Object.class);
        Object result = method.invoke(object,"test");
        System.out.println(result);
```

**3. 利用反射创建数组**
```java
    Class klass = Class.forName("java.lang.String");
    Object array = Array.newInstance(klass,25);
    Array.set(array,0,"0");
    Array.set(array,1,"1");
    Array.set(array,2,"2");
    Array.set(array,3,"3");
    System.out.println(Array.get(array,2));
```
**反射的优点：** 

*    **可扩展性**  ：应用程序可以利用全限定名创建可扩展对象的实例，来使用来自外部的用户自定义类。
*    **类浏览器和可视化开发环境**  ：一个类浏览器需要可以枚举类的成员。可视化开发环境（如 IDE）可以从利用反射中可用的类型信息中受益，以帮助程序员编写正确的代码。
*    **调试器和测试工具**  ： 调试器需要能够检查一个类里的私有成员。测试工具可以利用反射来自动地调用类里定义的可被发现的 API 定义，以确保一组测试中有较高的代码覆盖率。

**反射的缺点：** 

尽管反射非常强大，但也不能滥用。如果一个功能可以不用反射完成，那么最好就不用。在我们使用反射技术时，下面几条内容应该牢记于心。

*    **性能开销**  ：反射涉及了动态类型的解析，所以 JVM 无法对这些代码进行优化。因此，反射操作的效率要比那些非反射操作低得多。我们应该避免在经常被执行的代码或对性能要求很高的程序中使用反射。

*    **安全限制**  ：使用反射技术要求程序必须在一个没有安全限制的环境中运行。如果一个程序必须在有安全限制的环境中运行，如 Applet，那么这就是个问题了。

*    **内部暴露**  ：由于反射允许代码执行一些在正常情况下不被允许的操作（比如访问私有的属性和方法），所以使用反射可能会导致意料之外的副作用，这可能导致代码功能失调并破坏可移植性。反射代码破坏了抽象性，因此当平台发生改变的时候，代码的行为就有可能也随着变化。

## 八、异常
[Java 入门之异常处理](https://www.tianmaying.com/tutorial/Java-Exception)

Throwable所有异常的基类，异常分为两大类，错误```Error```和异常```Exception```。其中Error用来表示JVM无法处理的错误 如```OutOfMemoryError```，```NoClassDefFoundError```，```NoSuchFieldError```，```NoSuchMethodError```。
Exception分为两种：
* **受检异常** ：需要用 try...catch... 语句捕获并进行处理，并且可以从异常中恢复；
* **非受检异常** ：是程序运行时错误，例如除 0 会引发 Arithmetic Exception，此时程序崩溃并且无法恢复。

或者分为：
* **运行时异常** RuntimeException
* **非运行时异常**

![Throwable类图](../../../youdaonote-images/noteshare.html charset=utf-8)

`Error `:`Error`类对象由Java虚拟机生成并抛出，大多数错误与代码编写者所执行的操作无关。当出现这样的异常时，jvm一般会选择终止线程。    
`Exception`：重要的子类`RuntimeException`(运行时异常)，属于不检查异常，程序可以选择捕获处理，也可以选择不处理。产生该异常的原因一般是由程序逻辑错误引起的，程序应该从逻辑角度尽可能避免这类异常的发生。`RuntimeException`之外的异常都称为非运行时异常，必须进行处理的异常，不处理程序不能编译通过。

**finally**  
总是在try，catch执行完，方法返回之前执行。
```java
        try{
            System.out.print("try");
            throw new Exception("tes");
        }catch (Exception e){
            System.out.print("Exception");
            return "Exception";
        }finally {
            System.out.print("finally");
        }
        //输出 try Exception finally
```

**Java 7 ARM(Automatic Resource Management，自动资源管理)特征和多个catch块的使用**  
* Java 7的一个新特征是：一个catch子句中可以捕获多个异常，示例代码如下：
```java
catch(IOException | SQLException | Exception ex){
     logger.error(ex);
     throw new MyException(ex.getMessage());
}
```
* **自动资源管理**： 大多数情况下，当忘记关闭资源或因资源耗尽出现运行时异常时，我们只是用finally子句来关闭资源。这些异常很难调试，我们需要深入到资源使用的每一步来确定是否已关闭。因此，Java 7用try-with-resources进行了改进：在try子句中能创建一个资源对象，当程序的执行完try-catch之后，运行环境自动关闭资源。下面是这方面改进的示例代码：
```java
    try (MyResource mr = new MyResource()) {
        System.out.println("MyResource created in try-with-resources");
    } catch (Exception e) {
        e.printStackTrace();
    }
    // MyResource要实现AutoCloseable接口
```

## 九、泛型

* **泛型类**
```java
public class Box<T> {
    // T stands for "Type"
    private T t;
    public void set(T t) { this.t = t; }
    public T get() { return t; }
}
//使用
Box<Integer> integerBox = new Box<Integer>();
Box<String> stringBox = new Box<String>();
```
* **泛型方法**
```java
    public <H> H comple(){
        return null;
    }

    public <H> String comple1(H h){
        return h.toString();
    }
    // 调用
    对象.<String>comple();
    对象.<String>comple1("test")
    //或者在Java1.7/1.8利用type inference，让Java自动推导出相应的类型参数：
    对象.comple();
    String string = 对象.comple1("test")
```
* **边界符**  
? extends T 表示所有T的子类  
? super T 表示所有T的父类
* **PECS原则**
 [参考](http://www.importnew.com/24029.html)  
    * 使用extends界限符的，只能当做生产者（Producer）使用，只能从其中获取元素，不能添加，因为不知道当前泛型指向的是哪个子类，添加会出错。
    * 使用super界限符的，只能当做消费者（Consumer）使用，只能向其中添加元素，不能获取，因为不知道指向的是哪个父类，获取进行类型转换会出错。

* **类型擦除**    
类型擦除就是说Java泛型只能用于在编译期间的静态类型检查，然后编译器生成的代码会擦除相应的类型信息，这样到了运行期间实际上JVM根本就不知道泛型所代表的具体类型。这样做的目的是因为Java泛型是1.5之后才被引入的，为了保持向下的兼容性，所以只能做类型擦除来兼容以前的非泛型代码。

代码示例，编译时：
```java
    public class Node<T> {
        private T data;
        private Node<T> next;
        public Node(T data, Node<T> next) {
            this.data = data;
            this.next = next;
        }
        public T getData() { return data; }
        // ...
    }
```
到了运行时，代码转换成：
```java
    public class Node {
        private Object data;
        private Node next;
        public Node(Object data, Node next) {
            this.data = data;
            this.next = next;
        }
        public Object getData() { return data; }
        // ...
    }
```
这意味着不管声明Node<String>还是Node<Integer>，到了运行期间，JVM统统视为Node<Object>。可以通过指定界限符，解决这个问题  
上面代码修改为：
```java
    public class Node<T extends Comparable<T>> {
        private T data;
        private Node<T> next;
        public Node(T data, Node<T> next) {
            this.data = data;
            this.next = next;
        }
        public T getData() { return data; }
        // ...
    }
```
这样编译器就会将T出现的地方替换成Comparable而不再是默认的Object了：
```java
    public class Node {
        private Comparable data;
        private Node next;
        public Node(Comparable data, Node next) {
            this.data = data;
            this.next = next;
        }
        public Comparable getData() { return data; }
        // ...
    }
```

### 泛型带来的问题
* **问题一：** 在Java中不允许创建泛型数组，类似下面这样的做法编译器会报错
```java
List<Integer>[] arrayOfLists = new List<Integer>[2];  // compile-time error
```
假设我们支持泛型数组的创建，由于运行时期类型信息已经被擦除，JVM实际上根本就不知道new ArrayList<String>()和new ArrayList<Integer>()的区别。类似这样的错误假如出现才实际的应用场景中，将非常难以察觉。
```java
    Class c1 = new ArrayList<String>().getClass();
    Class c2 = new ArrayList<Integer>().getClass();
    System.out.println(c1 == c2); // true
```

* **问题二：**
对于泛型代码，Java编译器实际上还会偷偷帮我们实现一个Bridge method（桥接方法）。
```java
public class Node<T> {
    public T data;
    public Node(T data) { this.data = data; }
    public void setData(T data) {
        System.out.println("Node.setData");
        this.data = data;
    }
}
public class MyNode extends Node<Integer> {
    public MyNode(Integer data) { super(data); }
    public void setData(Integer data) {
        System.out.println("MyNode.setData");
        super.setData(data);
    }
}
```
可能理解jvm编译成：
```java
public class Node {
    public Object data;
    public Node(Object data) { this.data = data; }
    public void setData(Object data) {
        System.out.println("Node.setData");
        this.data = data;
    }
}
public class MyNode extends Node {
    public MyNode(Integer data) { super(data); }
    public void setData(Integer data) {
        System.out.println("MyNode.setData");
        super.setData(data);
    }
}
```
实际上不是这样的，以下代码会出现ClassCastException异常，提示String无法转换成Integer：
```java
MyNode mn = new MyNode(5);
Node n = mn; // A raw type - compiler throws an unchecked warning
n.setData("Hello"); // Causes a ClassCastException to be thrown.
// Integer x = mn.data;
```
实际上Java编译器对上面代码自动还做了一个处理（桥接方法）：
```java
class MyNode extends Node {
    // Bridge method generated by the compiler
    public void setData(Object data) {
        setData((Integer) data);
    }
    public void setData(Integer data) {
        System.out.println("MyNode.setData");
        super.setData(data);
    }
    // ...
}
```
* **问题三:**  
    Java泛型很大程度上只能提供静态类型检查，然后类型的信息就会被擦除，所以像    下面这样利用类型参数创建实例的做法编译器不会通过：
    ```java
    public static <E> void append(List<E> list) {
        E elem = new E();  // compile-time error
        list.add(elem);
    }
    ```
    如果某些场景需要利用类型参数创建实例，可以通过反射解决
    ```java
    public static <E> void append(List<E> list, Class<E> cls)   throws Exception {
        E elem = cls.newInstance();   // OK
        list.add(elem);
    }
    ```
* **问题四:**
无法对泛型代码直接使用instanceof关键字，因为Java编译器在生成代码的时候会擦除所有相关泛型的类型信息，正如我们上面验证过的JVM在运行时期无法识别出ArrayList<Integer>和ArrayList<String>的之间的区别:
    ```java
    public static <E> void rtti(List<E> list) {
        if (list instanceof ArrayList<Integer>) {  //  compile-time error
            // ...
        }
    }
    ```
    可以使用通配符重新设置bounds来解决这个问题：
    ```java
    public static void rtti(List<?> list) {
        if (list instanceof ArrayList<?>) {  // OK;instanceof  requires a reifiable type
            // ...
        }
    }
    ```

* **[10 道 Java 泛型面试题](https://cloud.tencent.com/developer/article/1033693)**

## 十、注解 
[参考](https://www.cnblogs.com/acm-bingzi/p/javaAnnotation.html)
* **说明**  
Annontation是Java5开始引入的新特征，Java 注解是附加在代码中的一些元信息，用于一些工具在编译、运行时进行解析和使用，起到说明、配置的功能。注解不会也不能影响代码的实际逻辑，仅仅起到辅助性的作用。

* **用处**
 
    * 1、生成文档。这是最常见的，也是java 最早提供的注解。常用的有@param @return 等
    * 2、跟踪代码依赖性，实现替代配置文件功能。比如Dagger 2 依赖注入，未来java 开发，将大量注解配置，具有很大用处;
    * 3、在编译时进行格式检查。如@override 放在方法前，如果你这个方法并不是覆盖了超类方法，则编译时就能检查出。

* **元注解**  
  java.lang.annotation 提供了四种元注解，专门注解其他的注解   
1. @Retention – 定义该注解的生命周期  
  *  RetentionPolicy.SOURCE :     在编译阶段丢弃。这些注解在编译结束之后就不再有任何意义，所以它们不会写入字节码。@Override, @SuppressWarnings都属于这类注解。  
  *   RetentionPolicy.CLASS : 在类加载的时候丢弃。在字节码文件的处理中有用。注解默认使用这种方式  
  *   RetentionPolicy.RUNTIME : 始终不会丢弃，运行期也保留该注解，因此可以使用反射机制读取该注解的信息。我们自定义的注解通常使用这种方式。  

  2. Target – 表示该注解用于什么地方。默认值为任何元素，表示该注解用于什么地方。可用的ElementType 参数包括
  * ElementType.CONSTRUCTOR: 用于描述构造器
  * ElementType.FIELD: 成员变量、对象、属性（包括enum实例）
  * ElementType.LOCAL_VARIABLE: 用于描述局部变量
  * ElementType.METHOD: 用于描述方法
  * ElementType.PACKAGE: 用于描述包
  * ElementType.PARAMETER: 用于描述参数
  * ElementType.TYPE: 用于描述类、接口(包括注解类型) 或enum声明

 3. @Documented – 一个简单的Annotations 标记注解，表示是否将注解信息添加在java 文档中。

 4. @Inherited – 定义该注释和子类的关系
     @Inherited 元注解是一个标记注解，@Inherited 阐述了某个被标注的类型是被继承的。如果一个使用了@Inherited 修饰的annotation 类型被用于一个class，则这个annotation 将被用于该class 的子类。
 
* **常见的注解**  
 Override（java编译器起到一个编译错误提示）  
 Deprecated（被Inherited 修饰的注解，表明不建议的方法或者废弃的方法）
@SuppressWarnings("unchecked")

## 十一、新特性
[参考](https://segmentfault.com/a/1190000004417830#articleHeader9)

* [Java 7 新特性](https://blog.csdn.net/qq_34755766/article/details/82877052)
    * **switch中添加对String类型的支持**
    * **数字字面量的改进 / 数值可加下划**

        Java7前支持十进制（123）、八进制（0123）、十六进制（0X12AB）
        Java7增加二进制表示（0B11110001、0b11110001）用0b或者0B表示；  
        Java7中支持在数字中间增加' _ '作为分隔符，分隔长int以及long
        （也支持double,float），显示更直观，如（12_123____456）。
        注意：_ 不能出现在数字的开头与结尾，以及表示意义的字符中间，
        如【0B_1001 错误】，小数点前后

    * **异常处理可以同时catch多个异常**

    java7 之前
    ```java
    try{
        ...
    }catch(SQLException e){
    ...
    }catch(FileNotFoundException e){...}
    ```
    java7之后
    ```java
    try{
        ...
    }catch(SQLException|FileNotFoundException e){
    ...
    }
    ```
    注意： 同时catch多个异常时 变量e自动为final不可以再在catch修改；
    
    * **try-with-resources 称为ARM（Automatic Resource Management），自动资源管理**
    
        java7之前
        ```java
            FileOutputStream fileOutputStream = null;
            try {
                BufferedReader bufferedReader1 = 
                new BufferedReader(new FileReader("D:\\Log.txt"));
                String s = bufferedReader1.readLine();
                System.out.println(s);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        ```
        Java7之后可以这样写
        ```java
        try (BufferedReader bufferedReader1 = 
        new BufferedReader(new FileReader("D:\\Log.txt"))){
                String s = bufferedReader1.readLine();
                System.out.println(s);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        ```
        第一种是在代码最后执行close，当try和close都出异常时，会走close代码  块的异常处理 ，如果close异常的代码块不抛出，那就会走try的catch处  理。
        第二种是在try执行后执行close操作，最后执行finally块。可以存在多个   类型，但是try中的类型必须实现java.lang.AutoCloseable接口，close的  顺序和定义顺序相反；当try 和 close都出错时会执行catch。
        exception.addSuppressed()可以记录被抑制的异常

    * **增强泛型推断（泛型实例化类型自动推断）**
    
        示例：List<String> list = new ArrayList<String>();  
    java7之后：List<String> list = new ArrayList<>();
    * **JSR203 NIO2.0（AIO）新IO的支持**
    * **JSR292与InvokeDynamic指令**
    * **Path接口、DirectoryStream、Files、WatchService（重要接口更新）**
    * **fork/join framework** 

* [Java 8 新特性](http://www.importnew.com/19345.html)

    * **Lambda表达式**  
    Lambda表达式（也叫做闭包），它允许我们将一个函数当作方法的参数（传递函数），或者说把代码当作数据。  
    最简单的Lambda表达式可以使用逗号分隔的参数列表、->符号和功能语句块来表示。示例：  
    ``` Arrays.asList( "a", "b", "d" ).forEach( e -> System.out.println( e ) );```  
    请注意到编译器会根据上下文来推测参数的类型，或者你也可以显示地指定参数类型，只需要将类型包在括号里。举个例子：  
    ``` Arrays.asList( "a", "b", "d" ).forEach( ( String e ) -> System.out.println( e ) ); ```  
    如果Lambda的功能语句太复杂，可以使用大括号包起来，如下：
        ```java
        String str = ",";
        Arrays.asList("a","b","c").forEach((e -> System.out.println(e + str)));
        ```
    
        上面str会默认为final（必须）    
    Lambda语句返回值，编译器会根据上下文自动推断返回值类型，可以会略return；
        ```java
        Arrays.asList("a","b","c").sort((e1,e2) -> e1.compareTo(e2));
        ```
        等价于
        ```java
        Arrays.asList("a","b","c").sort((e1,e2) -> {
            int i = e1.compareTo(e2);
            return i;
        });
        ```
    * **函数式接口**
    
        为了让现有的功能和lambda表达式友好兼容，出现函数式接口的概念。函数式接口是只有一个抽象方法的接口，像这样地，函数接口可以隐式地转换成lambda表达式。  
    
        java.lang.Runnable和java.util.concurrent.Callable是函数接口两个最好的例子。但是在实践中，函数接口是非常脆弱的，只要有人在接口里添加多一个方法，那么这个接口就不是函数接口了，就会导致编译失败。Java 8提供了一个特殊的注解@FunctionalInterface来克服上面提到的脆弱性并且显示地表明函数接口的目的（java里所有现存的接口都已经加上了@FunctionalInterface）。
    
    * **接口的默认方法和静态方法**  
    Java 8增加了两个新的概念在接口声明的时候：默认和静态方法。默认方法允许我们在接口里添加新的方法，而不会破坏实现这个接口的已有类的兼容性，也就是说不会强迫实现接口的类实现默认方法。  
        ```java
        default void test1(){
            System.out.println("test1");
        }
        ```  
        虽然默认方法很强大，但是使用之前一定要仔细考虑是不是真的需要使用默认方法，因为在层级很复杂的情况下很容易引起模糊不清甚至变异错误。
    * **方法引用**  
    方法引用提供了一个很有用的语义来直接访问类或者实例的已经存在的方法或者构造方法。结合Lambda表达式，方法引用使语法结构紧凑简明。不需要复杂的引用。 
    java8支持4种方法引用  
    **第一种方法引用是构造方法引用**，语法是Class::new,对于泛型语法是：Class::new,注意构造方法没有参数。  
    **第二种是静态方法引用**，语法是：Class::static_method,请注意这个静态方法只支持一个参数。  
    **第三种方法引用是类实例的方法引用**，语法是：Class::method请注意方法没有参数。  
    **第四种方法引用是引用特殊类的方法**，语法是：instance::method  
    实例：
        ```java
        public class Car {
            public static Car create( final Supplier< Car > supplier )   {
                return supplier.get();
            }

            public static void collide( Car car ) {
                System.out.println( "Collided " + car.toString() );
            }
            public static void collide(String str) {
                System.out.println( "Collided " +str);
            }
            public void follow( Car another ) {
                System.out.println( "Following the " +  another.toString()    );
            }

            public void repair() {
                System.out.println( "Repaired " + this.toString() );
            }

            public static void main(String[] args) {
                // 方法引用
                final Car car = Car.create( Car::new );
                final List< Car > cars = Arrays.asList( car );
                cars.forEach(Car::collide);
                Arrays.asList("a","b","c").forEach(Car::collide);
                cars.forEach( Car::repair );
                Car police = Car.create( Car::new );
                cars.forEach( police::follow );
            }
        }
        ```   
    * **重复注释**  
    自从Java 5支持注释以来，注释变得特别受欢迎因而被广泛使用。但是有一个限制，同一个地方的不能使用同一个注释超过一次。 Java 8打破了这个规则，引入了重复注释，允许相同注释在声明使用的时候重复使用超过一次。
    重复注释本身需要被@Repeatable注释。  
    示例：
        ```java
        public class RepeatingAnnontations {

            @Target(ElementType.TYPE)
            @Retention(RetentionPolicy.RUNTIME)
            public @interface Filters{
                Filter[] value();
            }

            @Target(ElementType.TYPE)
            @Retention(RetentionPolicy.RUNTIME)
            @Repeatable(Filters.class)
            public @interface Filter{
                String value();
            }

            @Filter( "filter1" )
            @Filter( "filter2" )
            public interface Filterable {
            }

            public static void main(String[] args) {
                Class zlass = Filterable.class;
                Filter[] annotationsByType = (Filter[])         zlass.getAnnotationsByType(Filter.class);
                for (Filter annotation : annotationsByType) {
                    System.out.println( annotation.value() );
                }
            }
        }
        ```
        注释Filter被@Repeatable( Filters.class )注释。Filters 只是一个容器，它持有Filter, 编译器尽力向程序员隐藏它的存在。通过这样的方式，Filterable接口可以被Filter注释两次。
    * **更好的类型推断**  
    * **注解的扩展**  
    Java 8扩展了注解可以使用的范围，现在我们几乎可以在所有的地方：局部变量、泛型、超类和接口实现、甚至是方法的Exception声明。  
    Java 8 新增加了两个注解的程序元素类型ElementType.TYPE_USE 和ElementType.TYPE_PARAMETER ，这两个新类型描述了可以使用注解的新场合。注解处理API（Annotation Processing API）也做了一些细微的改动，来识别这些新添加的注解类型。
    * **参数名字**  
    java8将参数名称保存在classde字节码里通过使用反射API和Parameter.getName() 方法可以获取参数名，前提是使用java编译命令javac的–parameters参数  
    * **Optional**防止空指针异常  
    * **Stream**  
    Stream API让集合处理简化了很多。Stream操作被分为中间操作和终点操作。  
    **中间操作**返回一个新的Stream。这些中间操作是延迟的，执行一个中间操作比如filter实际上不会真的做过滤操作，而是创建一个新的Stream，当这个新的Stream被遍历的时候，它里头会包含有原来Stream里符合过滤条件的元素。  
    **终点操作**比如说forEach或者sum会遍历Stream从而产生最终结果或附带结果。终点操作执行完之后，Stream管道就被消费完了，不再可用。在几乎所有的情况下，终点操作都是即时完成对数据的遍历操作。

    * **日期时间API**  
    **第一个类是Clock**,Clock使用时区来访问当前的instant, date和time。Clock类可以替换 System.currentTimeMillis() 和 TimeZone.getDefault().
    示例：  
        ```java
        final Clock clock = Clock.systemUTC();
        System.out.println( clock.instant() );//    2014-04-12T15:19:29.282Z
        System.out.println( clock.millis() );//1397315969360
        ```  
        **LocalTime和LocalDate**，LocalDate只保存ISO-8601日期系统的日期部分，有时区信息；LocalTime只保存ISO-8601日期系统的时间部分，没有时区信息。LocalDate和LocalTime都可以从Clock创建。  
        **LocalDateTime**类合并了LocalDate和LocalTime，它保存有ISO-8601日期系统的日期和时间，但是没有时区信息。
        ```java
        LocalDate date = LocalDate.now();
        LocalDate dateFromClock  = LocalDate.now(clock);
        LocalTime time = LocalTime.now();
        LocalTime timeFromClock = LocalTime.now(clock);
        LocalDateTime localDateTime = LocalDateTime.now();
        ```  
        **ZonedDateTime** 保存有ISO-8601日期系统的日期和时间，以及时区信息。  
        ```java
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        ZonedDateTime zonedDateTime1 = ZonedDateTime.now(clock);
        ZonedDateTime zonedDateTime2 = ZonedDateTime.now(ZoneId.of("America/Los_Angeles"));
        /*
        2019-02-24T20:27:27.610+08:00[Asia/Shanghai]
        2019-02-24T12:27:27.610Z
        2019-02-24T04:27:27.610-08:00[America/Los_Angeles]
        */
        ```
        **Duration**持有的时间精确到纳秒。它让我们很容易计算两个日期中间的差异。
        ```java
        LocalDateTime from = LocalDateTime.of( 2014, Month.APRIL, 16, 0, 0, 0 );
        LocalDateTime to = LocalDateTime.of(2015,Month.APRIL,18,0,0,0);
        Duration duration = Duration.between(from,to);
        System.out.println("Duration in days: "+duration.toDays());
        System.out.println( "Duration in hours: " + duration.toHours() );
        ```
    * **Nashorn javascript引擎**  
    Java8提供了一个新的Nashorn javascript引擎，它允许在JVM上运行特定的javascript应用。
        ```java
        ScriptEngineManager scriptEngineManager = new   ScriptEngineManager();
            ScriptEngine javaScript =   scriptEngineManager.getEngineByName("JavaScript");
            System.out.println(javaScript.getClass().getName());
            System.out.println(javaScript.eval("function f(){return 1;  } f()+2"));;
        ```
    * **Base64**(一种加密解密方式)  
    对Base64的支持最终成了Java 8标准库的一部分。示例：  
    ```java
        String str = "Hello Base64!";
        byte[] encode = Base64.getEncoder().encode(str.getBytes(StandardCharsets.UTF_8));
        System.out.println(encode);
        String encodeString = Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
        System.out.println(encodeString);
        byte[] decode = Base64.getDecoder().decode(encodeString);
        String text = new String(decode,StandardCharsets.UTF_8);
        System.out.println(text);
    ```
    新的Base64API也支持URL和MINE的编码解码。  
    (Base64.getUrlEncoder() / Base64.getUrlDecoder(), Base64.getMimeEncoder() / Base64.getMimeDecoder()).
    * **并行数组**  
    Java 8新增加了很多方法支持并行的数组处理。最重要的大概是parallelSort()这个方法显著地使排序在多核计算机上速度加快。
    ```java
        long[] arr = new long[2000];
        Arrays.parallelSetAll(arr,index -> ThreadLocalRandom.current().nextInt(2000));
        Arrays.stream(arr).limit(10).forEach(x-> System.out.println(x));
        System.out.println("===========");
        //排序
        Arrays.parallelSort(arr);
        Arrays.stream(arr).limit(10).forEach(x-> System.out.println(x));
    ```
    这一小段代码使用parallelSetAll() t方法填充这个长度是2000的数组，然后使用parallelSort() 排序。
    * **并发**（重要）  

    在新增Stream机制与lambda的基础之上，在java.util.concurrent.ConcurrentHashMap中加入了一些新方法来支持聚集操作。同时也在java.util.concurrent.ForkJoinPool类中加入了一些新方法来支持共有资源池（common pool）。

    新增的java.util.concurrent.locks.StampedLock类提供一直基于容量的锁，这种锁有三个模型来控制读写操作（它被认为是不太有名的java.util.concurrent.locks.ReadWriteLock类的替代者）。

    在java.util.concurrent.atomic包中还增加了下面这些类：  
    DoubleAccumulator  
    DoubleAdder  
    LongAccumulator  
    LongAdder  

    * **Nashorn引擎：jjs**
    * **类依赖分析工具：jdeps**  

    Jdeps是一个功能强大的命令行工具，它可以帮我们显示出包层级或者类层级java类文件的依赖关系。它接受class文件、目录、jar文件作为输入，默认情况下，jdeps会输出到控制台。
    * **JVM的新特性**

    JVM内存永久区已经被metaspace替换（JEP 122）。JVM参数 -XX:PermSize 和 –XX:MaxPermSize被XX:MetaSpaceSize 和 -XX:MaxMetaspaceSize代替。
## 最后 Java与C++的区别

* Java是纯粹的面向对象语言，所有的对象都继承自java.lang.Object，C++为了兼容 C 即支持面向对象也支持面向过程。
* Java通过虚拟机从而实现跨平台特性，但是C++依赖于特定的平台。
* Java没有指针，它的引用可以理解为安全指针，而C++具有和C一样的指针。
* Java支持自动地垃圾回收，而C++需要手动回收。
* Java不支持多重继承，只能通过实现多个接口来达到目的，而C++支持多重继承。
* Java 不支持操作符重载，虽然可以对两个 String 对象执行加法运算，但是这是语言内置支持的操作，不属于操作符重载，而 C++ 可以。
* Java 的 goto 是保留字，但是不可用，C++ 可以使用 goto。
* Java 不支持条件编译，C++ 通过 #ifdef #ifndef 等预处理命令从而实现条件编译。