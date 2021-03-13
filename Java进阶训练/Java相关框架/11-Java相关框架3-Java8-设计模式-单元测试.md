# 第11课 Java相关框架3-Java8-设计模式-单元测试

## 1.Java8 Lambda

### 什么是Lambda

Lambda表达式是一个匿名函数，在使用函数时不需要声明一个匿名类，可以简化写法，直接使用Lambda表达式的方式使用函数。实际在运行时，java会创建一个匿名内部类。

写法：

* 不需要参数，返回值为5：`() -> 5`
* 接收一个参数（数字类型），返回其2倍的值：`x -> 2*x`
* 接收两个参数并返回他们的差值：`(x, y) -> x – y`
* 接收两个int参数，并返回他们的和：`(int x, int y) -> x + y`
* 接受一个 string 对象,并在控制台打印,不返回任何值(看起来像是返回void)：`(String s) -> System.out.print(s)`

**为什么Lambda中引用的外部变量必须式final的？**

Lambda的方式实际上是闭包，闭包的作用域不能修改外部的变量，否则就不符合闭包的规则了。所以必须final。

### 函数式接口

可以使用Lambda的前提是，接口中只能有一个抽象方法。为了实现这个功能出现了函数式接口的概念。函数式接口是只有一个抽象方法的接口，像这样地，函数接口可以隐式地转换成lambda表达式。

> @FunctionalInterface注解，限定当前的接口只能有一个抽象方法

### 方法引用

方法引用也是Java8的新特性，可以直接访问类或者实例已经存在的方法或者构造方法。结合Lambda表达式，方法引用使语法结构紧凑简明。不需要复杂的引用。

java8支持4种方法引用：

* 构造方法引用：语法：Class::new，**构造方法不能有参数**。
* 静态方法引用：Class::static_method，**静态方法只支持一个参数**。
* 类实例的方法引用：Class::method，**引用的方法不能有参数**。
* 引用特殊类的方法：instance::method，**引用的方法只支持一个参数**。

> 闭包

## 2.Java8 Stream

### Java泛型

什么是泛型？

java是伪泛型，通过类型擦除实现。通过反射可以获得泛型的定义的类型。

\<T extends Serializable&Comparable&Collection> 很神奇的操作

> invokedynamic指令，lambda性能疑问。

### 什么是流

Stream(流)是一个来自数据源的元素队列并支持聚合操作。

* 元素：特定类型的对象，形成一个队列。Java中的Stream并不会存储元素，而是按需计算。
* 数据源：流的来源，可以是集合、数组、I/O channel等。
* 聚合操作类似SQL语句一样的操作，比如filter，map，reduce，find，match，sorted等。

**和以前的Collection操作不同，Stream操作还有两个基础的特征：**

* Pipelinling: 中间操作都会返回流对象本身。这样多个额操作可以串联成一个管道。
* 内部迭代：以前对集合遍历都是通过Iterator或者For-Each的方式，显示的在集合外部进行迭代，这叫做外部迭代。Stream提供了内部迭代的方式，通过访问者模式（Visitor）实现。

### 创建流的方式

集合类可以直接调用stream()方法。
数组可以通过Arrays.stream()创建Stream。
多个对象可以通过Stream.of()创建Stream，实际上也是调用的Arrays.stream()。

### Stream操作

**中间操作：**

* 1、选择与过滤
  * filter(Predicate p): 接收Lambda， 根据传入的条件从流中排除某些元素。
  * distinct(): 根据流所生成元素的hasCode()和equals()去除重复元素。
  * limit(long maxSize) 截断流，使其元素不超过给定数量。
  * skip(long n): 跳过元素，返回一个扔掉了前 n 个元素的流。若流中元素不足 n 个，则返 回一个空流。
* 2、映射
  将流中的元素从一个对象转换成另一个对象。
  * map(Function f) 接收 Lambda ， 将元素转换成其他形式或提取信息;接收一个函数作为参数，
该函数会被应用到每个元素上，并将其映射成一个新的元素。
  * mapToDouble(ToDoubleFunction f) 接收一个函数作为参数，该函数会被应用到每个元素上，
产生一个新的 DoubleStream。
  * mapToInt(ToIntFunction f) 接收一个函数作为参数，该函数会被应用到每个元素上，产生一个新
的 IntStream。
  * mapToLong(ToLongFunction f) 接收一个函数作为参数，该函数会被应用到每个元素上，产生
一个新的 LongStream。
  * flatMap(Function f) 接收一个函数作为参数，将流中的每个值都换成另一个流，然后把所有流连接成一个流。

* 3、排序 sorted() 产生一个新流，其中按自然顺序排序；sorted(Comparator comp) 产生一个新流，其中按比较器顺序排序

**终止操作：**

终止操作之后可能不再是一个流了。

* 1.查找与匹配
  * allMatch(Predicate<? super T> predicate): 检查全部元素是否匹配指定条件。
  * anyMatch(Predicate<? super T> predicate): 检查是否有元素匹配指定条件。
  * noneMatch(Predicate<? super T> predicate): 检查是否没有元素匹配指定条件。
  * findFirst(): 返回第一个元素。
  * findAny(): 返回当前流中的任意元素。
  * count(): 返回流中元素个数。
  * max(Comparator<? super T> comparator): 返回流中最大的元素。
  * min(Comparator<? super T> comparator): 返回流中最小的元素。

* 2.归约reduce: 将元素进行合计等操作，需要初始值。
* 3.收集collect: 
  * toList List<T> 把流中元素收集到 List
  * toSet Set<T> 把流中元素收集到 Set
  * toCollection Collection<T> 把流中元素收集到创建的集合
  * count 计算流中元素的个数
  * summaryStatistics 统计最大最小平均值

  ```java
  // (a, b) -> 表示当key重复的时候怎么处理，不添加这个会在有重复key的时候出现错误。
  System.out.println("collect : " + list.stream().collect(Collectors.toMap(t -> t, t -> t+"123", (a, b) -> a, HashMap::new)));

  System.out.println("collect : " + list.stream().collect(Collectors.toMap(t -> t, t -> t+"123")));

  System.out.println("collect.summarizingInt : " + list.stream().collect(Collectors.summarizingInt(Integer::valueOf)));
  ```

* 4.迭代forEach。

#### 并行Stream

通过parallelStream()方法可以启用并行stream，但是只有部分操作支持并行stream，不支持的还是默认走单线程的。

## 3.Lombok(轮牧布克)

基于字节码增强，编译期处理。需要IDE支持。

```xml
<dependency>
  <groupId>org.projectlombok</groupId>
  <artifactId>lombok</artifactId>
  <version>1.18.12</version>
</dependency>
```

@Setter @Getter
@Data
@XXXConstructor
@Builder
@ToString
@Slf4j

## 4.Guava

### 什么是Guava

Guava是基于开源的java库，对JAVA库的扩展，其中包含谷歌正在由他们很多项目使用的很多核心库。这个库是为了方便编码，并减少编码错误。这个库提供用于集合，缓存，支持原语，并发性，常见注解，字符串处理，I/O 和验证的实用方法。

Guava的好处：

* 标准化：Guava库是由谷歌托管。
* 高效可靠：快速有效的扩展了JAVA标准库。
* 优化：Guava 库经过高度的优化。

```xml
<dependency>
  <groupId>com.google.guava</groupId>
  <artifactId>guava</artifactId>
  <version>29.0-jre</version>
</dependency>
```

> JDK8 里的一些新特性源于 Guava。

### 集合[Collections]

Guava对JDK集合的扩展，是最成熟和为人所知的部分。

* 不可变集合：用不可变的集合进行防御性编程和性能提升。

  ```java
  ImmutableSet<String> of = ImmutableSet.of("c", "b", "a", "b", "f");
  ```

  相关类：ImmutableSet、ImmutableMap、ImmutableList。

* 新集合类：
  * multisets：可重复value的set。
  * multimaps：可以重复key值的map，重复key对应的value用一个list保存value。
  * tables：类似表的形式二维数据保存与展示。
  * bidirectional maps：支持双向映射，可以通过value查找key。
* 强大的集合工具类：提供 java.util.Collections 中没有的集合工具。
* 扩展工具类：让实现和扩展集合类变得更容易，比如创建 Collection 的装饰器，或实现迭代器。

### 缓存[Caches]

本地缓存实现，支持多种缓存过期策略。

### 并发[Concurrency]

强大而简单的抽象，让编写正确的并发代码更简单。

### 字符串处理[Strings]

非常有用的字符串工具，包括分割、连接、填充等操作。

### 事件总线[EventBus]

发布订阅模式的组件通信，进程内模块间解耦。

实现A和B的解耦方式：SPI、callback、EventBus

### 反射[Reflection]

Guava的Java反射机制工具类。

## 5.设计原则

### 面向对象设计原则SOLID

* SRP：The Single Responsibility Principle,单一责任原则
* OCP：The Open Closed Principle,开放封闭原则
* LSP：The Liskov Substitution Principle,里氏替换原则
* ISP：The Interface Segregation Principle,接口隔离原则
* DIP：The Dependency Inversion Principle,依赖倒置原则

知道最少原则，KISS，高内聚低耦合。

### 编码规范、checkstyle

为什么需要编码规范：降低沟通的成本，减少bug或问题的产生。

常见的编码规范：

* Google 编码规范：https://google.github.io/styleguide/javaguide.html
* Alibaba 编码规范：https://github.com/alibaba/p3c
* VIP 规范：https://vipshop.github.io/vjtools/#/standard/

> 规范检查工具：
> jacoco、emma、coberuta底层
> sonar和checkstyle都是在findbugs上面的封装。

## 6.设计模式

### GoF 23设计模式

创建型：如何创建对象
结构型：如何封装组合对象
行为型

### 设计模式与反模式

模式的3个层次：解决方案层面（架构模式），组件模式（架构模式），代码层面（GoF设计模式）。

反模式：死用模式，都是反模式。

## 7.单元测试

### 什么是单元测试

针对类来组织，针对方法的测试。

单元测试 --> 集成测试 --> 自动化端到端测试 --> 手工回归测试

### 常见的单元测试工具

Junit -> TestCase,TestSuite,Runner

SpringTest

Mock技术：Mockito、easyMock

### 如何做单元测试

1. 单元测试方法应该每个方法是一个case，断言充分，提示明确
2. 单元测试要覆盖所有的corner case（极端情况）
3. 充分使用mock（一切皆可mock）
4. 如果发现不好测试，则说明业务代码设计存在问题，可以反向优化代码
5. 批量测试用例使用参数化单元测试
6. 注意测试是单线程执行
7. 合理使用before, after, setup准备环境
8. 合理使用通用测试基类
9. 配合checkstyle，coverage等工具
10. 制定单元测试覆盖率基线，通过插件可以设置单元测试覆盖率的预值，如果低于这个预值，测试失败。

### 单元测试的常见陷阱与经验

1. 尽量不要访问外部数据库等外部资源
2. 如果必须用数据库考虑用嵌入式DB+事务自动回滚
  如果依赖数据库，单元测试最好加上自动回滚。
3. 防止静态变量污染导致测试无效
4. 小心测试方法的顺序导致的不同环境测试失败
  很奇葩的问题，环境的更换可能导致测试执行的方法顺序不一致了。
5. 单元测试总时间特别长的问题

## Tips

## 参考

* [FunctionInterface的用法](https://www.cnblogs.com/bigbaby/p/12116886.html)
* [Lambda表达式如何演化，简化代码用法](https://www.zhihu.com/question/20125256/answer/324121308)
* [Lambda表达式如何演化，简化代码用法](https://www.cnblogs.com/bigbaby/p/12113741.html)
* Steam操作:https://www.jianshu.com/p/932ef18941fb https://www.jianshu.com/p/633f691f9afb https://developer.ibm.com/zh/articles/j-lo-java8streamapi/
* [Guava中文教程](http://ifeve.com/google-guava/)
* 编码规范
  * https://www.sohu.com/a/215755759_820120
  * https://zhuanlan.zhihu.com/p/87352004
* 设计模式
  * https://github.com/me115/design_patterns
  * https://github.com/quanke/design-pattern-java
* 单元测试
  * https://www.zhihu.com/question/27313846/answer/36132954

## 问题

* 为什么并行stream的map比非并行的效率很低。
