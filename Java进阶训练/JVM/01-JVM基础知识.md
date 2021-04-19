# JVM基础知识

## JVM基础知识

### JVM的定义

JVM规范是Java虚拟机规范的简称，是JVM规范的定义，包括内存模型、类加载机制等。

JVM（Java Virtual Machine）是Java虚拟机的简称。它是一个虚构出来的计算机，是通过在实际的计算机上仿真模拟各种计算机功能来实现的。

JVM是跨语言的平台，只和class文件相关，和语言无关，任何符合规范的class都能被JVM执行。

常见的JVM有：

* Sun公司的HotSpot是目前使用范围最广的Java虚拟机。Hotspot8开始收费，但是有openJdk的开源版本在维护。

* IBM公司的J9 VM 是一个高性能的企业级 Java 虚拟机。

* BEA公司的JRockit。

* azul公司的Zing是一个高性能的商业Java虚拟机。

### JDK、JRE和JVM的关系

JDK = JRE + 开发工具

JRE = JVM + 类库

三者在开发Java程序时的交互关系：简单来说，通过JDK开发完程序，编译以后，可以打包分发给其他装有JRE的机器上运行。而运行的程序，则通过java命令启动一个JVM实例，代码逻辑都执行在这个JVM实例上。

### 编程语言的分类

把各种编程语言从底向上划分为最基本的三大类：机器语言、汇编语言和高级语言。

* 机器语言：这种语言主要是利用二进制编码进行指令的发送，能够被计算机快速识别，其灵活性相对较高，且执行速度较为可观，机器语言和汇编语言之间相识性较高，但是由于局限性，在使用上存在一定的约束性。

* 汇编语言：主要是以缩写因为作为标识符进行编写，运行汇编语言编写的一般都是较为简练的小程序，其在执行方面较为便利，但汇编语言在程序方面较为冗长，所以具有较高的出错率。

* 高级语言：所谓的高级语言其实是多种编程语言结合之后的总称，可以对多条指令进行整合，将其变成单条指令完成传输，其在操作细节指令以及中间过程等方面都得到了适当的简化，所以，整个程序更为简捷，具有较强的操作性。

简单来说：机器语言是可以直接给机器执行的二进制指令，每种CPU平台都有对应的机器语言；而汇编语言则相当于是给机器执行的指令，按照人的理解的助记符表示，这样代码就很长，但是性能也好；高级语言就是为了方便人来理解，进而快速设计和实现程序代码，一般跟机器语言和汇编语言的指令已经没有关系了，代码编写完成后通过编译或者解释，转换成汇编语言或机器码，之后再传递给计算机去执行。

### 高级语言的分类

* 按照有无虚拟机来划分

* 按照变量是不是有确定的类型，还是类型可以随意变化来划分

* 按照是编译执行还是解释执行来划分

* 按照语言特点来划分

**Java是一种面向对象、静态类型、编译执行，有VM/GC和运行时、跨平台的高级语言。**

### 编程语言的跨平台

C++是源码跨平台，在不同的平台上需要重新编译才能执行。
Java是二进制跨平台，只需要一次编译，二进制文件可以在不同的平台上执行。

### Java、C++、Rust的区别

* C/C++完全相信而且惯着程序员，让大家自行管理内存，所以可以编写很自由的代码，
但一个不小心就会造成内存泄漏等问题导致程序崩溃。

* Java/Golang完全不相信程序员，但也惯着程序员。所有的内存生命周期都由JVM/运行
时统一管理。 在绝大部分场景下，你可以非常自由的写代码，而且不用关心内存到底是
什么情况。 内存使用有问题的时候，我们可以通过JVM来信息相关的分析诊断和调整。
这也是本课程的目标。带来的问题可能就是JVM在GC时的等待。stop the world。

* Rust语言选择既不相信程序员，也不惯着程序员。 让你在写代码的时候，必须清楚明白
的用Rust的规则管理好你的变量，好让机器能明白高效地分析和管理内存。 但是这样会
导致代码不利于人的理解，写代码很不自由，学习成本也很高。

### 编译执行和解释执行

编译执行：通过编译器将程序语句翻译成机器指令然后执行。如C++，需要在不同的平台上编译成对应的机器指令才能执行。
解释执行：通过解释器将程序语句进行解释执行，不需要翻译成机器指令。如python，java。

**Java是解释执行的?**

这个说法不太准确，在开发Java源码时，首先通过javac编译成字节码（bytecode），然后在运行时通过Java虚拟机（JVM）内嵌的解释器将字节码转换成最终的机器码。但是常见的JVM，比如Hotspot JVM，都提供了JIT（Just In Time）编译器，也就是通常所说的动态编译器，JIT能够在运行时将热点代码编译成机器码，这种情况下部分热点代码就属于编译执行了，而不是解释执行。

**JVM编译执行和解释执行**

JVM默认是混合执行，通过参数`-Xint`可以强行指定只使用解释模式；`-Xcomp`强行指定使用编译执行；`-Xmixed`指定混合模式，即默认的方式，解释+编译。

参数`-XX:CompileThreshold`指定了方法或循环到达指定的值时会触发即时编译。默认C1为1500，C2为10000。

**CodeCache**

CodeCache是热点代码编译后的存放的位置，位于堆外内存。`-XX:InitialCodeCacheSize`和`-XX:ReservedCodeCacheSize`参数指定了CodeCache的内存大小。

-XX:InitialCodeCacheSize：CodeCache初始内存大小，默认2496K；
-XX:ReservedCodeCacheSize：CodeCache预留内存大小，默认48M（分层编译开启默认240M）。

**当CodeCache满了之后，已经被编译的代码还会以本地代码方式执行，但后面没有编译的代码只能以解释执行的方式运行。**

[具体参考](https://zhuanlan.zhihu.com/p/81941373)

### Hotspot的即时编译

在 HotSpot 虚拟机中，内置了两个 JIT，分别为 C1 编译器和 C2 编译器，这两个编译器的编译过程是不一样的。C1 编译器是一个简单快速的编译器，主要的关注点在于局部性的优化，适用于执行时间较短或对启动性能有要求的程序，例如，GUI 应用对界面启动速度就有一定要求。C2 编译器是为长期运行的服务器端应用程序做性能调优的编译器，适用于执行时间较长或对峰值性能有要求的程序。根据各自的适配性，这两种即时编译也被称为 Client Compiler 和 Server Compiler。

Java7引入分层编译，这种方式综合了C1的启动性能和C2的峰值性能。可以通过参数"-client"、"-server"强制指定虚拟机的即时编译模式。在Java8默认开启分层编译，-client 和 -server 的设置已经是无效的了。如果只想开启 C2，可以关闭分层编译（-XX:-TieredCompilation），如果只想用 C1，可以在打开分层编译的同时，使用参数：-XX:TieredStopAtLevel=1。

**热点探测**

在 HotSpot 虚拟机中的热点探测是 JIT 优化的条件，热点探测是基于计数器的热点探测，采用这种方法的虚拟机会为每个方法建立计数器统计方法的执行次数，如果执行次数超过一定的阈值就认为它是“热点方法” 。

方法调用计数器和回边计数器分别用来统计方法被调用的次数和一个方法中循环体代码执行的次数。

**参考[深入JVM即时编译器JIT，优化Java编译](https://time.geekbang.org/column/article/106953)**

## Java字节码技术

### 什么是字节码

Java之所以可以“一次编译，到处运行”，一是因为JVM针对各个平台、操作系统进行了定制，二是因为无论在什么平台，都可以编译生成固定格式的字节码（.class）供JVM使用。字节码文件是由十六进制组成，而JVM以两个十六进制为一组，即以字节为单位进行读取。

(一个十六进制对应四个进制，1111 -> F)

### Java字节码指令

Java字节码（byte code）由单字节（byte）的指令组成，理论上最多支持256个操作码，实际上Java只使用了200左右的操作码，还有一些操作码则保留给调试操作。

[字节码指令解释对照](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html)

### 字节码的结构

字节码结构包括：魔数，版本号，常量池，访问标志，当前类名，父类名称，接口信息，字段表，方法表，附加属性表。

[具体内容参考](https://tech.meituan.com/2019/09/05/java-bytecode-enhancement.html)

### 解读字节码清单

对编译后的class文件，通过`javap`命令可以查看对应类的字节码内容。

> tips: `javac`不指定-d参数编译后生成的.class文件默认和源代码在同一个目录。指定-g参数可以在编译后生成更多的调试信息，比如保留变量名称。
`javap`添加‐verbose参数可以显示更多的附加信息，如常量池，版本信息。

HelloByteCode.java:

```java
package demo.java.bytecode;

public class HelloByteCode {
  public HelloByteCode() {
  }

  public HelloByteCode(String str) {

  }
  public static void main(String[] args) {
    HelloByteCode obj = new HelloByteCode("123");
  }

}
```

`javap -c -verbose HelloByteCode.class`输出信息：

```java
Classfile /JVM/HelloByteCode.class
  Last modified 2020-10-16; size 398 bytes
  MD5 checksum a366cc97cd2e5658cc445db6fd707cae
  Compiled from "HelloByteCode.java"
public class demo.java.bytecode.HelloByteCode
  minor version: 0
  major version: 52
  flags: ACC_PUBLIC, ACC_SUPER
Constant pool:
   #1 = Methodref          #5.#15         // java/lang/Object."<init>":()V
   #2 = Class              #16            // demo/java/bytecode/HelloByteCode
   #3 = String             #17            // 123
   #4 = Methodref          #2.#18         // demo/java/bytecode/HelloByteCode."<init>":(Ljava/lang/String;)V
   #5 = Class              #19            // java/lang/Object
   #6 = Utf8               <init>
   #7 = Utf8               ()V
   #8 = Utf8               Code
   #9 = Utf8               LineNumberTable
  #10 = Utf8               (Ljava/lang/String;)V
  #11 = Utf8               main
  #12 = Utf8               ([Ljava/lang/String;)V
  #13 = Utf8               SourceFile
  #14 = Utf8               HelloByteCode.java
  #15 = NameAndType        #6:#7          // "<init>":()V
  #16 = Utf8               demo/java/bytecode/HelloByteCode
  #17 = Utf8               123
  #18 = NameAndType        #6:#10         // "<init>":(Ljava/lang/String;)V
  #19 = Utf8               java/lang/Object
{
  public demo.java.bytecode.HelloByteCode();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 4: 0
        line 5: 4

  public demo.java.bytecode.HelloByteCode(java.lang.String);
    descriptor: (Ljava/lang/String;)V
    flags: ACC_PUBLIC
    Code:
      stack=1, locals=2, args_size=2
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 7: 0
        line 9: 4

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
      stack=3, locals=2, args_size=1
         0: new           #2                  // class demo/java/bytecode/HelloByteCode
         3: dup
         4: ldc           #3                  // String 123
         6: invokespecial #4                  // Method "<init>":(Ljava/lang/String;)V
         9: astore_1
        10: return
      LineNumberTable:
        line 11: 0
        line 12: 10
}
SourceFile: "HelloByteCode.java"
```

可以看到class的文件信息：最后修改时间、MD5、版本号。

* `minor version: 0 major version: 52`：显示的是使用的java版本号，minor是小版本号，major是大版本号，52对应的就是jdk8.

* `ACC_PUBLIC, ACC_SUPER`：表示的是类的访问权限。`ACC_PUBLIC`表示的这个类是public类。`ACC_SUPER`标志，是在JDK1.0的BUG修正中引入的，用来修正`invokespecial`指令调用super类方法的问题，从Java1.1开始，编译器一般会自动生成`ACC_SUPER`标志。

* `Constant pool`：表示的常量池的内容。

* `#1 = Methodref   #5.#15 // java/lang/Object."<init>":()V`解读：

  * `#1`常量编号，该文件中其他地方可以引用。
  * `=`等号就是分隔符。
  * `Methodref`表示这个常量执行的是一个方法；具体指向的类是`#5`，指向的方法是`#15`。

* 方法信息：

  ```java
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
      stack=3, locals=2, args_size=1
  ```
  
  * `([Ljava/lang/String;)V`：其中小括号内表示方法的入参；左方括号表示数组；L表示对象；后面的java/lang/String 就是类名称；小括号后面的 V 则表示这个方法的返回值是 void；
  * `flags: ACC_PUBLIC, ACC_STATIC`：表示方法的访问标志。
  * `stack=2, locals=2, args_size=1`：表示执行该方法需要的栈（stack）深度，局部变量表保留的的槽位数，还有方法的参数；如果是实例方法会默认传入一个this引用，所以实例方法默认就有一个参数和一个本地变量。

* 方法执行信息：

  ```java
    0: new           #2   // class demo/java/bytecode/HelloByteCode
    3: dup
    4: ldc           #3   // String 123
    6: invokespecial #4   // Method "<init>":(Ljava/lang/String;)V
    9: astore_1
    10: return
  ```

  * 序号表示指令存储的索引。
  * 后面英文表示的具体的指令。`new`表示创建对象，但没有调用构造方法；`invokespecial`指令用来调用某些特殊方法的, 当然这里调用的是构造函数；`dup`指令用于复制栈顶的值。

### 字节码指令

根据指令的性质，主要分为四个大类：

1. 栈操作指令，包括与局部变量交互的指令
2. 程序流程控制指令
3. 对象操作指令，包括方法调用指令
4. 算术运算以及类型转换指令

此外还有一些执行专门任务的指令，比如同步(synchronization)指令，以及抛出异常 相关的指令等等。下文会对这些指令进行详细的讲解。

**常见的指令**

* `dup`：表示复制栈顶的值。
* `pop`：表示冲栈中删除最顶部的值。
* `dup_x1`：表示复制栈顶元素的值，并压入原来值的后面一个值的后面。
* `dup2_x1`：表示复制栈顶的两个元素的值，并压入原来值的后面一个值的后面。
* `invokestatic`：调用某个类的静态方法，这是方法调用指令中最快的一个。
* `invokevirtual`：如果是具体类型的目标对象，invokevirtual 用于调用公共，受保护和package 级的私有方法。
* `invokeinterface`：当通过接口引用来调用方法时，将会编译为invokeinterface指令。
* `inovkespecial`：可以直接定位，不需要多态的方法private方法、构造方法。
* `invokedynamic`：JDK7新增加的指令，是用来实现“动态类型语言”的支持，lambda表达式或者反射或者其他动态语言scala、kotlin，或者CGlib ASM动态产生的class会用到的指令。

...

## Java类加载器

### 类的生命周期

类的生命周期包括类的加载，使用，卸载。

类的加载过程包括了加载、校验、准备、解析、初始化五个阶段。其中校验、准备、解析又统称为连接。

![类的生命周期](https://note.youdao.com/yws/api/personal/file/WEBb88be26dc016de41cfdded0995ea0947?method=download&shareKey=d4e299adcc1d10823233247240a38528)

#### 加载(Loading)

加载阶段，就是通过类的全限定名，来获取二进制的class字节流。如果找不到class就会抛出`NoClassDefFoundError`。

#### 校验(Verification)

链接的第一个过程就是校验，目的是确保class文件里的字节流信息是符合当前虚拟机要求的，不会危害虚拟机的安全。过程中可能会抛出 `VerifyError`，`ClassFormatError`或 `UnsupportedClassVersionError`。

#### 准备(Preparation)

准备阶段，会创建类的静态变量，并且将其初始化为默认值（null或者0），并在方法区中分配这些变量所使用的内存空间。

如`public static ing i = 1`，在准备阶段i的值会被初始化为0；而`public final static ing i`，i的值会被初始化为1。

#### 解析(Resolution)

解析阶段就是虚拟机将常量池内的符号引用替换为直接引用的过程，解析动作主要针对类或接口、字段、类方法、接口方法、方法类型、方法句柄和调用限定符，7类符号引用进行。

例如将类中对String的符号引用转换成直接引用。**如果这个时候引用的类未加载需要加载**

符号引用就是一组符号来描述目标，可以是任何字面量。直接引用就是直接指向目标的指针、相对偏移量或一个间接定位到目标的句柄。

**解析阶段发生的时间是不一定的，可以在类被加载器加载时或者是在使用一个符号引用前。**

### 初始化(Initializing)

初始化阶段，主要对类变量进行初始化，如执行静态代码块，执行static静态变量赋值语句。

JVM规定，必须在类首次主动使用时才能执行类初始化。类的主动使用包括一下六种情况：

* 创建类的实例，也就是new的方法。
* 访问某个类或接口的静态变量，或者对该静态变量复值。
* 调用类的静态方法。
* 反射（如Class.forName("Test")）。
* 初始化某个类的子类时，则其父类也要初始化。
* Java虚拟机启动时被标明为启动类的类（ JavaTest），直接使用 java.exe命令来运行某个主类

下面几种情况不会执行类的初始化：

* 通过子类引用访问父类的静态字段，只会出发父类初始化，子类初始化不会触发。
* 定义对象数组不会触发类初始化。
* 常量在编译期间会存入调用类的常量池中，本质上并没有直接引用定义常量的类，不会触发定义常量所在的类。`static final`。
* 通过类名获取Class对象，不会触发类的初始化，Hello.class不会让Hello类初始化。
* 通过Class.forName加载指定类时，如果指定参数initialize为false时，也不会触发类初始化，其实这个参数是告诉虚拟机，是否要对类进行初始化。如Class.forName("jvm.Hello", false, null)。
* 通过ClassLoader默认的loadClass方法，也不会触发初始化动作。

### 类加载器

类的加载通过类加载器（ClassLoader）来完成，系统自带的类加载器分为三种：

* 启动类加载器（BootstrapClassLoader）：负责加载存放在JDK\lib(JDK代表JDK的安装目录，下同)下，或被 -Xbootclasspath参数指定的路径中的，并且能被虚拟机识别的类库（如rt.jar，所有的java.开头的类均被 BootstrapClassLoader加载）。启动类加载器是使用C++实现的，无法被Java程序直接引用的。比如：java.lang.String是被启动类加载器加载的，但是通过String.class.getClassLoader()获得是null。

* 扩展类加载器（ExtClassLoader）：由sun.misc.Launcher$ExtClassLoader实现，负责加载JDK\lib\ext目录中，或者由 java.ext.dirs系统变量指定的路径中的所有类库（如javax.开头的类），代码中直接获取它的父加载器为null（因为无法拿到启动类加载器）。

* 应用类加载器（AppClassLoader）：由 sun.misc.Launcher$AppClassLoader来实现，负责在JVM启动时加载来自Java命令的­ classpath或者­cp选项、java.class.path系统属性指定的jar包和类路径。在应用程序代码里可以通过ClassLoader的静态方法getSystemClassLoader()来获取应用类加载器。如果没有特别指定，则在没有使用自定义类加载器情况下，用户自定义的类都由此加载器加载。

它们之间的层次关系是：启动类加载器是扩张类加载器的上级类加载器，扩张类加载器是应用类加载器的上级类加载器，应用类加载器是自定义类加载器的上级类加载器。

### JVM类加载机制

* 双亲委派：当一个类加载器需要加载一个类时，会先让它的父类加载器去加载，如果父类加载器不能加载，再由自己加载。

* 负责依赖/全盘负责：在加载一个类时，如果它还依赖其他类，没有显示指定依赖类的加载器时，由当前类的加载器负责去加载依赖的其他类。

* 缓存加载：为了提升加载效率，消除重复加载，一旦某个类被一个类加载器加载，那么它会缓存这个加载结果，不会重复加载。

### 双亲委派机制

* 双亲委派机制的流程

  * 1、当 AppClassLoader加载一个class时，它首先不会自己去尝试加载这个类，而是把类加载请求委派给父类加载器ExtClassLoader去完成。

  * 2、当 ExtClassLoader加载一个class时，它首先也不会自己去尝试加载这个类，而是把类加载请求委派给BootStrapClassLoader去完成。

  * 3、如果 BootStrapClassLoader加载失败（例如在 $JAVA_HOME/lib里未查找到该class），会使用 ExtClassLoader来尝试加载；

  * 4、若ExtClassLoader也加载失败，则会使用 AppClassLoader来加载，如果 AppClassLoader也加载失败，则会报出异常 ClassNotFoundException

* 双亲委派机制的意义

  * 系统类防止内存中出现多份同样的字节码

  * 保证Java程序安全稳定运行

### 破坏双亲委派机制

* 双亲委派模型的第一次“被破坏”其实发生在双亲委派模型出现之前，即JDK 1.2面世以前。后来设计的双亲委派模型，为了兼容已有的代码，无法使用技术的手段避免loadClass()被子类覆盖，所有新添加了一个protected方法findClass()，引导用户编写类加载器时覆盖这个方法。

* 双亲委派模型的第二次“被破坏”是由这个模型自身的缺陷导致的，双亲委派很好地解决了各个类加载器协作时基础类型的一致性问题（越基础的类由越上层的加载器进行加载），但是如果存在基础类型需要使用用户代码，就不好处理了。引入了一个不太优雅的设计：线程上下文类加载器(Thread Context ClassLoader)，这个类加载器可以通过Thread#setContextClassLoader方法进行设置，如果创建线程时还未设置，它将会从父线程中继承一个，如果在应用程序的全局范围内 都没有设置过的话，那这个类加载器默认就是应用程序类加载器。这样，在基础类型使用到用户代码时就可以通过这个类加载器进行加载。

  举例：JNDI服务使用这个线程上下文类 加载器去加载所需的SPI服务代码，这是一种父类加载器去请求子类加载器完成类加载的行为，这种行为实际上是打通了双亲委派模型的层次结构来逆向使用类加载器，已经违背了双亲委派模型的一般性原则。Java中涉及SPI的加载基本上都采用这种方式来完成，例如JNDI、 JDBC、JCE、JAXB和JBI等。
  
* 双亲委派模型的第三次“被破坏”，是用户自定义的ClassLoader实现loadClass()方法，打破双亲委派机制。对于一些代码热部署场景，需要打破双亲委派模型，比如OSGi；tomcat都有自己的模块指定classloader，可以加载同一类库的不同版本。

### 类加载的方式

* 命令行启动应用时候由JVM初始化加载
* 通过Class.forName()方法动态加载，Class.forName()在加载类时，可以指定是否初始化和类加载器。
* 通过ClassLoader.loadClass()方法动态加载，通过这种方式加载的类不会执行初始化。

> jvm规范没有规定什么时候加载类，但是严格规定类什么时候初始化。

### 自定义类加载器

自定义类加载器只要继承java.lang.ClassLoader，实现findClass方法就好了。

```java
public class CustomClassLoader extends ClassLoader{

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String helloClassBase64 = "yv66vgAAADQAHwoABgARCQASABMIABQKABUAFgcAFwcAGAEABjxpbml0PgEAAygpVgEABENvZGUBAA9MaW5lTnVtYmVyVGFibGUBABJMb2NhbFZhcmlhYmxlVGFibGUBAAR0aGlzAQAvTGNvbS93a2svZGVtby9qdm0vY2xhc3Nsb2FkZXIvSGVsbG9DbGFzc0xvYWRlcjsBAAg8Y2xpbml0PgEAClNvdXJjZUZpbGUBABVIZWxsb0NsYXNzTG9hZGVyLmphdmEMAAcACAcAGQwAGgAbAQASSGVsbG8gQ2xhc3NMb2FkZXIhBwAcDAAdAB4BAC1jb20vd2trL2RlbW8vanZtL2NsYXNzbG9hZGVyL0hlbGxvQ2xhc3NMb2FkZXIBABBqYXZhL2xhbmcvT2JqZWN0AQAQamF2YS9sYW5nL1N5c3RlbQEAA291dAEAFUxqYXZhL2lvL1ByaW50U3RyZWFtOwEAE2phdmEvaW8vUHJpbnRTdHJlYW0BAAdwcmludGxuAQAVKExqYXZhL2xhbmcvU3RyaW5nOylWACEABQAGAAAAAAACAAEABwAIAAEACQAAAC8AAQABAAAABSq3AAGxAAAAAgAKAAAABgABAAAACAALAAAADAABAAAABQAMAA0AAAAIAA4ACAABAAkAAAAlAAIAAAAAAAmyAAISA7YABLEAAAABAAoAAAAKAAIAAAALAAgADAABAA8AAAACABA=";
        byte[] decode = Base64.getDecoder().decode(helloClassBase64);
        return defineClass(name, decode, 0, decode.length);
    }

    public static void main(String[] args) throws Exception {
        Class<?> aClass = new CustomClassLoader().loadClass("com.wkk.demo.jvm.classloader.HelloClassLoader");
        aClass.newInstance();
    }
}
```

> 系统自带的ClassLoader是不是单例的。
> 不同的自定义ClassLoader是互相隔离的。同一个ClassLoader的不同实例对象也是相互隔离的。比如使用同一个ClassLoader的不同对象进行加载类，类会被加载两次（初始化也是两次）。
> 比较两个类是否“相等”，只有在这两个类是由同一个类加载器加载的前提下才有意义，否则，即使这两个类来源于同一个 Class文件，被同一个Java虚拟机加载，只要加载它们的类加载器不同，那这两个类就必定不相等。这里的相等包括调用类对象的equals()、isAssignableFrom()、isInstance()方法返回的结果，以及instanceof关键字的判定。

### 一些实用技巧

#### 如何排查找不到Jar包的问题

通过一下代码可以查看类加载器加载的jar列表

```java
/**
 * @Description 输出类加载器加载的jar列表
 * @Author Wangkunkun
 * @Date 2020/10/18 20:25
 */
public class JvmClassLoaderPrintPath {

    public static void main(String[] args) {
        // 启动类加载器
        URL[] urLs = Launcher.getBootstrapClassPath().getURLs();
        System.out.println("启动类加载器");
        for (URL urL : urLs) {
            System.out.println("==> " + urL.toExternalForm());
        }
        // 扩展类加载器
        System.out.println("扩展类加载器");
        printClassLoader("扩展类加载器", (URLClassLoader) JvmClassLoaderPrintPath.class.getClassLoader().getParent());
        // 启动类加载器
        System.out.println("启动类加载器");
        printClassLoader("启动类加载器", (URLClassLoader) JvmClassLoaderPrintPath.class.getClassLoader());

    }

    public static void printClassLoader(String name, URLClassLoader urlClassLoader) {
        if(urlClassLoader != null) {
            System.out.println(name + " ClassLoader -> " + urlClassLoader);
            URL[] urLs = urlClassLoader.getURLs();
            for (URL urL : urLs) {
                System.out.println("==> " + urL.toExternalForm());
            }
        }

    }
}
```

#### 如何排查类的方法不一致的问题

如果确定一个jar或者class已经在classpath里了，但是总是提示`java.lang.NoSuchMethodError`，出现这样的情况可能是加载了错误的或者重复加载了不同版本的jar包，不同的jar包里面的方法可能不同，可以根据上面的输出信息进行排查。

#### 怎么看到加载了哪些类，以及加载顺序

只需要在类的启动命令行参数上加上`-XX:+TraceClassLoading`或者`-verbose`，就可以输出类的加载顺序和加载了哪些类。

```text
bin % java -XX:+TraceClassLoading Hello
[0.003s][warning][arguments] -XX:+TraceClassLoading is deprecated. Will use -Xlog:class+load=info instead.
[0.006s][info   ][class,load] opened: /Library/Java/JavaVirtualMachines/jdk-11.0.8.jdk/Contents/Home/lib/modules
[0.011s][info   ][class,load] java.lang.Object source: jrt:/java.base
[0.012s][info   ][class,load] java.io.Serializable source: jrt:/java.base
[0.012s][info   ][class,load] java.lang.Comparable source: jrt:/java.base
[0.012s][info   ][class,load] java.lang.CharSequence source: jrt:/java.base
[0.012s][info   ][class,load] java.lang.String source: jrt:/java.base
[0.012s][info   ][class,load] java.lang.reflect.AnnotatedElement source: jrt:/java.base
[0.012s][info   ][class,load] java.lang.reflect.GenericDeclaration source: jrt:/java.base
[0.012s][info   ][class,load] java.lang.reflect.Type source: jrt:/java.base
...
```

从上面的信息，可以清楚看到类的加载先后顺序，以及是从哪个jar加载的，这样排查类加载的问题非常方便。

#### 怎么调整或修改ext和本地加载路径

直接执行Java命令时，默认也会加载非常多的jar包，可以通过添加参数`-Dsun.boot.class.path`指定启动类加载器要加载的jar包。参数`-Djava.ext.dirs`指定扩展类加载器要加载的jar包。

#### 怎么运行期加载额外的jar包或者class呢

有的时候在运行期间，可能要再额外去加载一些jar或者类，这个时候有两种解决方法。

一种是前面提到的自定义类加载器的方式。还有一种是直接在当前的应用加载器里，使用URLClassLoader类的方法addURL，不过这个方法是protected的，需要反射处理一 下，然后又因为程序在启动时并没有显示加载Hello类，所以在添加完了classpath以后，没法直接显式初始化，需要使用Class.forName的方式来拿到已经加载的Hello类 （Class.forName("jvm.Hello")默认会初始化并执行静态代码块）。代码如下：

```java
public class JvmAppClassLoaderAddURL {
    public static void main(String[] args) {
        String appPath = "file:/Users/Desktop/";
        ClassLoader classLoader = JvmAppClassLoaderAddURL.class.getClassLoader();
        try {
            Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);
            URL url = new URL(appPath);
            addURL.invoke(classLoader, url);
            Class.forName("HelloByteCode");
        } catch (NoSuchMethodException | MalformedURLException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
```

## JVM运行时数据区

Java虚拟机在执行Java程序时会将内存划分为不同的数据区域。这些数据区域有些是随着虚拟机启动而创建，虚拟机退出才被销毁；有些区域随着用户线程的启动和结束而创建和销毁。

JVM内存结构主要有三大块：堆内存，方法区和栈。堆内存是JVM中最大的一块内存结构，也是垃圾回收主要工作的地方，主要由年轻代和老年代组成，年轻代又分为三部分：Eden空间，FormSurvivor空间，To Survivor空间，默认情况下按照8:1:1非比例分配。

[内存实例图](https://www.processon.com/view/5c8860fbe4b02ce2e88bc422?fromnew=1)

JDK8开始将方法区放到了元数据区。

### PC(程序计数器)

线程私有，一块较小的内存空间，指向当前线程正在执行的字节码指令的地址、行号。

在Java虚拟机的概念模型里，字节码解释器工作时就是通过改变这个计数器的值来选取下一条需要执行的字节码指令，它是程序控制流的指示器，分支、循环、跳转、异常处理、线程恢复等基础功能都需要依赖这个计数器来完成。如果线程正在执行的是一个java方法，这个计数器记录的是正在执行的虚拟机字节码指令的地址；如果正在执行的是本地（native）方法，这个计数器则应为空（Undfined）。**这个内存区域是唯一一个在《Java虚拟机规范》中没有规定任何OutOfMemoryError情况的区域。**

### JVM Stack(Java虚拟机栈)

Java虚拟机栈是线程私有的，它的生命周期与线程相同。每启动一个线程，JVM就会在栈空间分配对应的线程栈，线程栈也叫做Java方法栈；线程中每执行一个方法，就会在线程栈中同步创建一个栈帧，用来存储局部变量表，操作数栈、动态连接、方法出口等信息。每一个方法被调用直到完毕的过程，就对应着一个栈帧在线程栈中从入栈到出栈的过程。

在Java虚拟机规范中，对这个区域规定了两种异常状况：如果线程请求的栈深度大于虚拟机所允许的深度，将抛出StackOverflowError异常；如果虚拟机栈可以动态扩展，当扩展时无法申请到足够的内存时会抛出OutOfMemoryError异常。

**Frame(栈帧)**

**栈帧**只是一个逻辑上的概念，具体的大小在方法编写完成后就基本上确定了，每个方法对应一个栈帧。

* Local Variable Table 本地变量表：存储方法执行过程中的局部变量，包括基本数据类型、对象引用和returnAddress类型。
* Operand Stack 操作数栈
* Dynamic Linking 动态链接
* Return address 返回地址

一个Java方法的动态执行过程：

```java
void foo(){
    int a = 1;
    int b = 2;
    int c = (a + b) * 5;
}
```

![jvm-run](https://i.loli.net/2021/02/22/f7OePLDNCBAn68w.gif)

### Native Method Stack(本地方法栈)

本地方法栈和虚拟机栈的作用类似，只不过本地方法栈是在执行本地方法时使用的。与虚拟机栈一样，本地方法栈也会在栈深度溢出或者栈扩展失 败时分别抛出StackOverflowError和OutOfMemoryError异常。

### Heap(堆)

堆内存是所有线程共用的内存空间。不同的JVM在具体实现一般会有各种优化。逻辑上的Java堆划分为堆（Heap）和非堆（Non Heap）。

堆（Heap）是存放对象的位置，也是所有线程共享的，GC主要工作的地方，所以也称为GC Heap。GC一般使用的是分代回收算法，因为在程序中分配的对象要么存活的很久，要么用过就会丢弃。所以JVM将堆划分为年轻代（Young generation）和老年代（Old generation）两部分。

年轻代还划分为新生代（Eden space）、存活区0（Survivor space简称S0）和存活区1（Survivor space简称S1）三部分。不论在任何时刻S0和S1总有一个是空的，S0和S1一般很小，也不浪费空间。

> 在G1收集器出现之前，HotSport虚拟机内部的垃圾收集器都是基于“典型分代”来设计的。所以有上面的老年代，新生代。G1收集器开始淡化了这种分代的概念，将堆内存分成2000多块，每块表示不同的意义。

### Method Area(方法区)

在JVM规范中将方法区描述为堆的一个逻辑部分，所以也称做非堆（Non Heap），目的是与Java堆区分开来。方法区用来存放虚拟机加载的类信息、常量、静态变量、即时编译器编译后的代码。

Hotspot对方法区的实现：

在JDK1.7之前方法区的实现是永久代(Perm Space)，字符串常量位于Perm Space，FGC不会清理，大小启动的时候指定，不能变。

从JDK1.8开始用元数据区(Meta Space)取代了永久代，字符串常量位于堆，会触发FGC清理，不设定的话，最大就是物理内存。

> **jdk1.7把字符串常量池从永久代中剥离出来，存放在堆空间中。** 因为如果在程序中生成大量的字符串，会导致永久代OOM。jdk1.6环境下：Exception in thread "main" java.lang.OutOfMemoryError: PermGen space，jdk1.7环境下：Exception in thread "main" java.lang.OutOfMemoryError: Java heap space

**为什么用元数据区取代永久代？**

* 字符串存在永久代中，容易出现性能问题和内存溢出。
* 永久代有-XX:MaxPermSize的上限，即使不设置也有默认大小，这会限制永久代的大小，有些类及方法的信息等比较难确定其大小，因此永久代的大小指定就比较困难，太小容易出现永久代溢出，太大则容易导致老年代溢出。
* 永久代会为 GC 带来不必要的复杂度，并且回收效率偏低。

**元数据区是使用的本地内存来实现的**

### Run-Time Constant Pool(运行时常量池)

运行时常量池是方法区的一部分，Class文件中除了有类的版本、字段、方法、接口等描述信息外，还有一项信息是常量池表（Constant Pool Table），用于存放编译期生成的各种字面量与符号引用，这部分内容将在类加载后存放到方法区的运行时常量池中。

运行时常量池具有动态性，并非Class文件中定义常量才会进入运行时常量池，运行期间也可以将新常量放入运行时常量池，常见的是调用String的intern()方法，可以将常量放入常量池。

**当常量池无法再申请到内存时会抛出OutOfMemoryError。**

### Direct Memory(直接内存)

直接内存（Direct Memory）并不是虚拟机运行时数据区的一部分，也不是《Java虚拟机规范》中定义的内存区域。但是这部分内存也被频繁地使用，而且也可能导致OutOfMemoryError异常出现。

在JDK1.4中新加入了NIO（New Input/Output）类，它可以使用Native函数直接分配堆外内存，然后通过一个存储在Java堆里面的DirectByteBuffer对象作为这块内存的引用进行操作。这样能在一些场景中显著提高性能，因为避免了在Java堆和Native堆中来回复制数据。

直接内存的分配不受Java堆大小的限制，但是还是受到本机内存大小的限制的。所以在配置虚拟机参数的时候不能忽略直接内存，否则各个内存区域总和大于物理内存限制，从而导致动态扩展时出现OutOfMemoryError异常。

## Java内存模型（JMM）

### 为什么会存在JMM规范

因为在不同的硬件生产商和不同的操作系统下，内存访问逻辑有一定的差异，可能造成的现象就是，当程序在某个系统环境下运行良好并且线程安全，换个系统环境可能就出现线程不安全问题。JMM规范明确定义了不同线程之间，通过哪些方式，在什么时候可以看见其他线程保存到共享变量中的值，以及在必要时，如何对共享变量的访问进行同步。这样的好处是屏蔽各种硬件平台和操作系统之间的内存访问差异，实现了Java并发程序真正的跨平台。

**简单来说就是屏蔽不同硬件或操作系统下，内存访问的差异，实现应用在各种平台下运行都能到达一致的内存访问效果。**

### 缓存一致性

由于计算器的存储设备和处理器的运算速度有着好几个数量级的差距，所以现代计算机不得不引入了一层或多层读写速度更接近处理器的高速缓存，来作为处理器和存储设备之前的缓冲。在运算时会先将数据复制到缓存中，让运算能快速运行，当运算结束后再将数据同步回内存中，这样处理器无需等待缓慢的内存读写了。

目前处理器的实现上，一般有三个高速缓存分别是：L1 cache、L2 cache和L3 cache，其中L1 cache和L2 cache是同一个物理核上所有逻辑核共享的，L3 cache是同一个处理器上所有物理核共享的。

基于高速缓存的存储交互很好地解决了处理器与内存速度之间的矛盾，但是也为计算机系统带来更高的复杂度，它引入了一个新的问题：缓存一致性（Cache Coherence）。为了解决缓存一致性问题，需要各个处理器都遵循一些缓存一致性协议，常见的缓存一致性协议有inter的MESI、MSI、MOSI。

#### 缓存行

读取缓存是以缓存行为单位，缓存行大小一般为64byte。

#### 伪共享问题

位于同一个缓存行的数据被不同的cpu锁定/使用，产生相互影响的伪共享问题。

伪共享会影响程序效率，一般的解决方式是使用缓存行对齐，让两个数据位于不同的缓存行上。

示例代码：

[CacheLineTest.java](https://github.com/wkk1994/learn-demos/blob/master/demos/src/main/java/com/wkk/demo/javaconcurrent/CacheLineTest.java)

### 工作内存和主内存

JMM规定了内存主要分为主内存和工作内存两种。如果Java线程每次读取和写入变量都直接操作主内存，对性能影响比较大，所以每条线程都拥有自己的工作内存，工作内存中的变量是主内存中的一份拷贝，线程对变量的读取和写入，直接在工作内存中操作，而不能直接去操作主内存中的变量。这样带来的问题是，如果多个线程修改共享对象就会出现线程不安全问题。因此JMM规范制定了一套标准来保证开发者在编写多线程程序时，能够控制什么时候内存会被同步给其他线程。

#### 内存交互操作

内存交互操作有8种，虚拟机实现必须保证每一个操作都是原子的，不可再分的（对于double和long类型的变量来说，load、store、read和write操作在某些平台上允许例外）

* lock（锁定）：作用于主内存的变量，把一个变量标识为线程独占状态
* unlock（解锁）：作用于主内存的变量，它把一个处于锁定状态的变量释放出来，释放后的变量才可以被其他线程锁定
* read（读取）：作用于主内存变量，它把一个变量的值从主内存传输到线程的工作内存中，以便随后的load动作使用
* load（载入）：作用于工作内存的变量，它把read操作从主存中变量放入工作内存中
* use（使用）：作用于工作内存中的变量，它把工作内存中的变量传输给执行引擎，每当虚拟机遇到一个需要使用到变量的值，就会使用到这个指令
* assign（赋值）：作用于工作内存中的变量，它把一个从执行引擎中接受到的值放入工作内存的变量副本中
* store（存储）：作用于主内存中的变量，它把一个从工作内存中一个变量的值传送到主内存中，以便后续的write使用
* write（写入）：作用于主内存中的变量，它把store操作从工作内存中得到的变量的值放入主内存的变量中

JMM对这八种指令，制定了如下规则：

* 不允许read和load，store和write操作之一单独出现，即使用了read必须load，使用了store必须write。
* 不允许线程丢弃它最近的assign，即工作变量的数据改变之后，必须告诉主内存。
* 不允许线程将没有assign的工作变量同步回主内存。
* 一个新的变量必须在主内存中诞生，不允许工作内存直接使用一个未被初始化的变量，就是对变量实施use、store操作之前，必须经过assgin和load操作。
* 一个变量同一时间只有一个线程能对其进行lock。多次lock后，必须执行相同次数的unlock才能解锁。
* 如果对一个变量进行lock操作，会清空所有工作内存中此变量的值，在执行引擎使用这个变量前，必须重新load或assign操作初始化变量的值。
* 如果一个变量没有被lock，就不能对其进行unlock操作。也不能unlock一个被其他线程锁住的变量。
* 对一个变量进行unlock操作之前，必须把此变量同步回主内存。

通过这些规则就可以确定哪些操作是线程安全的，哪些是线程不安全的。

### 针对long和double型变量的特殊规则

Java内存模型虽然规定了上面八种操作都具有原子性，但是对于64位的数据类型（double/long），允许JVM将没有被volatile修饰的64位数据类型的读写操作划分为两次32位的操作来进行，这就是所谓的“long和double的非原子性协定”（Non-Atomic Treatment of double and long Variables）。

这样可能出现的问题是，线程读到其他线程修改的半个数据的值，不是完整的。在目前主流平台下商的64位Java虚拟机中并不会出现非原子性访问行为，并且实际测试中极少出现这种情况，可以忽略不计。

### 指令重排序

在执行程序时为了提高性能，编译器和处理器常常会对指令做重排序。重排序分为三类：

* 编译器优化的重排序，编译器在不改变单线程程序语义的前提下，可以重新安排语句的执行顺序。
* 指令级并行的重排序，现代处理器采用指令级并行技术（ILP）将多条指令重叠执行。如果不存在数据依赖性，处理器可以改变语句对应机器指令的执行顺序。
* 内存系统的重排序，由于处理器使用缓存和读写缓冲区，这使得加载和存储看上去可能是在乱序执行。

#### 如何证明指令重排序

```java
public static int a = 0;
public static int b = 0;
public static int x = 0;
public static int y = 0;

/**
 * 如果不存在指令重排序，不会出现x=0,y=0的情况
 */
public static void orderingTest() {
    long beginTime = System.currentTimeMillis();
    CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
    int i = 0;
    for (;;) {
        a = 0; b = 0; x = 0; y = 0;
        i++;
        Thread one = new Thread() {
            @Override
            public void run() {
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                a = 1;
                x = b;
            }
        };
        Thread two =  new Thread() {
            @Override
            public void run() {
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                b = 1;
                y = a;
            }
        };
        one.start();
        two.start();
        try {
            one.join();
            two.join();
            //System.out.println(String.format("第%s次执行的情况，x=%s, y=%s", i, x, y));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        if(x == 0 &&  y == 0) {
            System.out.println(endTime - beginTime);
            System.out.println(String.format("第%s次出现了乱序执行的情况，x=%s, y=%s", i, x, y));
            return;
        }
    }
}
```

#### as-if-serial语义

as-if-serial语义的意思是不管怎么重排序，单线程运行的执行结果始终不变。

#### happens before原则(先行发生原则)

JVM规定的先行发生原则，让一个操作无需控制就可以先于另一个操作发生。有8条，感觉都像废话。

https://github.com/CyC2018/CS-Notes/blob/master/notes/Java%20%E5%B9%B6%E5%8F%91.md#%E5%85%88%E8%A1%8C%E5%8F%91%E7%94%9F%E5%8E%9F%E5%88%99

### 合并写

当CPU执行存储指令（store）时，它会尝试将数据写到离CPU最近的L1缓存。如果此时出现缓存未命中，CPU会访问下一级缓存。此时，无论是英特尔还是许多其它厂商的CPU都会使用一种称为“合并写（write combining）”的技术。

就是在请求L2缓存尚未完成时，将待存储的数据先写到处理器中一个和缓存行大小的缓存区中，在后续的写操作提交到L2缓存之前，可以进行缓冲区写合并。

**对程序的影响**

在程序中如果可以在合并写缓存区被传输到外部缓存之前将其写满，那么将大大提高各级传输总线的效率。

这些缓冲区的数量是有限的，且随CPU模型而异。例如在Intel CPU中，同一时刻只能拿到4个。这意味着，在一个循环中，你不应该同时写超过4个不同的内存位置，否则你将不能享受到合并写（write combining）的好处。

合并写示例代码：[WriteCombining.java](https://github.com/wkk1994/learn-demos/blob/master/demos/src/main/java/com/wkk/demo/javaconcurrent/WriteCombining.java)

### 内存屏障

CPU中的内存屏障是一个CPU指令，它的作用用来禁止指令重拍序。

JMM引入内存屏障机制，用于控制可见性和指令重排序。内存屏障是一个规范，不同的虚拟机实现不同。内存屏障另一个作用是强制更新一次不同CPU的缓存。

**JMM规范的四种屏障类型**

* LoadLoad屏障：对于这样的语句Load1; LoadLoad; Load2，在Load2及后续读取操作要读取的数据被访问前，保证Load1要读取的数据被读取完毕。
* StoreStore屏障
* LoadStore屏障
* StoreLoad屏障：同时具有上面三种屏障的效果，可以用来代替另外三种内存屏障。

**硬件内存屏障（以X86为例）**

* Store屏障，是x86上的`sfence`指令，强制在sfence指令前的写操作当必须在sfence指令后的写操作前完成，并把store缓冲区的数据都刷到CPU缓存。

* Load屏障，是x86上的`ifence`指令，在lfence指令前的读操作当必须在lfence指令后的读操作前完成。

* Full屏障，是x86上的`mfence`指令，结合了load和store屏障的功能。

### 如何禁止指令冲重排序

### JMM的特征

#### 原子性

大致可以认为基本类型的访问、读写都是原子性的（long和double类型的例外，但是基本上不会发生）。如果要保证更大范围的原子性，可以通过lock和unlock操作来保证。

实现更大范围的原子性的方式：

* 使用原子类，如AtomicInteger；
* 使用synchronized互斥锁来保证原子性。

#### 可见性

可见性是指当一个线程修改了共享变量后，其他线程能立刻得知这个变更。

保证可见性的方式：

* volatile，被volatile修饰的变量在每次使用时都从主内存中读取，以此来保证可见性，但是不能保证原子性。
* synchronized，对一个变量执行 unlock 操作之前，必须把变量值同步回主内存。
* final，被 final 关键字修饰的字段在构造器中一旦初始化完成，并且没有发生 this 逃逸（其它线程通过 this 引用访问到初始化了一半的对象），那么其它线程就能看见 final 字段的值

#### 有序性

如果在本线程内观察，所有的操作都是有序的；如果在一个线程中观察另一个线程， 所有的操作都是无序的，无序是因为发生了指令重排序。

保证有序性的方式：

* volatile 关键字通过添加内存屏障的方式来禁止指令重排，即重排序时不能把后面的指令放到内存屏障之前。
* synchronized ，它保证每个时刻只有一个线程执行同步代码，相当于是让线程顺序执行同步代码。

## 对象分析

不同的虚拟机中对象的分布，布局，访问方式不同，这里都以Hotspot为例。

### 对象的内存存储布局

在Hotspot虚拟机里，对象在堆内存中的存储布局分为：对象头、实例数据、对齐填充。

![对象内存结构](https://pic3.zhimg.com/80/v2-961b928cf04da5ce629ee08fd767191e_1440w.jpg)

* 对象头

  对象头包含三部分内容，一部分是和对象运行时数据相关的Mark Word，长度在32位虚拟机和64位虚拟机（未开启压缩指针）上分别是32bit和64bit；另一部分是类型指针，指向该对象的类型元数据的指针，通过这个指针可以确定它是哪个类的实例，未开启类型指针压缩时长度为8byte；如果对象是一个数组，还有一个记录数组长度的数据，大小为4byte。

  * ClassPointer指针：开启类型指针压缩为4字节，不开启为8字节。（-XX:+UseCompressedClassPointers为开启类型指针压缩）

  * Mark Word

     存储了对象的哈希码、GC分代年龄、锁状态标志、线程持有的锁、偏向线程ID等数据，在对象不同的锁状态下存储的数据不同。

     对象头的Mark Word：

    ![对象头的MarkWord](https://note.youdao.com/yws/api/personal/file/WEB4f09b26fb61180c458bfb41bb90a84b2?method=download&shareKey=56ea9804c0f0db8ae48b8edef50a762d)

* 实例数据

  存储代码中定义的各种类型的字段，包括从父类继承下来的和自己定义的字段。实例数据的存储顺序受虚拟机的分配策略参数和字段在源码中定义顺序的影响。对于引用类型如果开启指针压缩为4字节，不开启为8字节。（-XX:+UseCompressedOops为开启指针压缩）。

* 内存填充（对齐填充）

  不是必须存在的，由于HotSpot虚拟机的自动内存管理系统要求对象起始地址必须是8字节的整数倍，换句话说就是任何对象的大小都必须是8字节的整数倍。对象头部分已经被精心设计成正好是8字节的倍数（1倍或者 2倍），因此，如果对象实例数据部分没有对齐的话，就需要通过对齐填充来补全。

**-XX:+UseCompressedClassPointers和-XX:+UseCompressedOops在32位系统下默认开启，64位系统默认不开启，所以占8个字节，但是jdk8已经默认开启了。**

### 对象的创建过程

1. 虚拟机遇到一个new指令时，会对符号引用进行解析，并检查这个是否能够在常量池中找到一个类的符号引用，并且检查这个符号引用的类是否已经被加载、解析和初始化过；如果在常量池中找不到对应的类的符号引用，那么先执行相应类的加载过程。

2. 类加载检查通过后，会为对象分配内存空间，类所需的内存空间是在类加载完成就可以确定的，分配内存空间的方式常有指针碰撞和空闲列表，前者适合连续的内存空间，后者适合不连续的内存空间。

3. 内存分配完成之后，虚拟机必须将分配到的内存空间（对象头除外）都初始化为零值。这步操作保证了对象的实例字段在Java代码中可以不赋初始值就直接使用，使程序能访问到这些字段的数据类型所对应的零值。

4. 接下来虚拟机会对对象进行必要的设置，比如对象的类型指针设置、对象的GC分代年龄等信息，这些信息都存储在对象的对象头中，并且根据当前虚拟机的的运行状态的不同，如是否使用偏向锁、启用偏向锁延迟时间等，对象头有不同的设置方式。

5. 一般new指令后面都会跟随invokespecial指令，这是因为Java编译器在遇到new关键字的地方会同时生成这两个指令，但如果直接通过其他方式产生的则不一定如此。所以一般来说，最后会执行`<init>()`方法，也就是对象的构造方法，调用构造方法会一直上溯到Object类。至此，一个真正可用的对象才算完全被构造出来。

### 对象的访问定位

通过对象引用怎么去定位、访问到堆中对象的具体位置，这个是由虚拟机实现而定的。常见的访问方式有句柄和直接指针两种。

**句柄**

Java会在堆中划出一块内存作为句柄池，reference中存储的就是对象的句柄，而句柄中包含了对象的实例数据和类型数据各自的具体地址信息。

**直接指针访问**

Java堆中的对象的内存布局需要有位置存储类型数据相关的信息，reference中直接存储对象地址。

**两者的比较**

使用句柄来访问的最大好处就是reference中存储的是稳定句柄地址，在对象被移动（垃圾收集时移动对象是非常普遍的行为）时只会改变句柄中的实例数据指针，而reference本身不需要被修改。

使用直接指针来访问最大的好处就是速度更快，它节省了一次指针定位的时间开销，由于对象访 问在Java中非常频繁，因此这类开销积少成多也是一项极为可观的执行成本，Hotspot使用的直接指针的方式进行对象访问。

### 对象的分配过程

// TODO

### 查看对象内存大小和布局

可以通过JOL(Java Object Layout)查看对象内存布局和大小，或者通过Instrumentation.getObjectSize()估算对象内存大小。

```xml
<dependency>
    <groupId>org.openjdk.jol</groupId>
    <artifactId>jol-core</artifactId>
    <version>0.14</version>
</dependency>
```

### 包装类型和基本类型的区别

对于int的包装类型Integer，占用16字节，而int只占用4字节，少了2倍。
对于long类型的包装类型Long，占用24字节，而long只占用8字节，也是少了2倍。

### 二维数组使用注意

在二维数组int[num1][num2]中，每个嵌套的数组int[num2]都是一个单独的Object，会占用额外的16字节的空间，所以当维度特别大的时候（即num1特别大），内存开销会更大。
举例：int[128][2]会占用3072字节，而int[2][128]只占用1056字节

## 参考

* [java内存模型JMM理解整理](https://www.cnblogs.com/null-qige/p/9481900.html)
* [关于Jvm知识看这一篇就够了](https://zhuanlan.zhihu.com/p/34426768)
* [指令重排序](https://www.jianshu.com/p/c6f190018db1)
* [内存屏障](https://ifeve.com/memory-barriers-or-fences/)