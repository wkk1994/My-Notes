# Spring泛型处理

## Java泛型基础

泛型类型：是在类型上参数化的泛型或接口，比如Collection\<E>中的E。

泛型的使用场景：

* 编译时强类型检查
* 避免类型强转
* 实现通用算法：不太明白？

泛型类型擦写：类型擦写确保不会为了参数化类型创建新类，这样泛型能确保不会有运行时的负载。比如创建一个new ArrayList\<String>()时，在运行期不必为此动态创建一个new ArrayList\<String>()的子类，来保证类型安全，直接使用new ArrayList\<Object>()，省去了动态创建新类的开销。为了实现类型擦写，编译器将类型擦写应用于：

* 将泛型类型中的所有类型参数替换为其边界，如果类型参数是无边界的，则将其替换为Object。因此，生成的字节码只包含普通类、接口和方法。
* 必要时插入类型转换以保持类型安全：在获取泛型中的数据时，编译时会插入强制类型转换代码，以保证类型安全。比如: `System.out.println((String)collection.get(1))`，类型转换时编译时编译器自动加上的。
* 生成桥接方法以保留扩展泛型类型中的多态性：桥接方法可以参考Java基础笔记中的泛型。

Java泛型示例：[GenericDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/generic/src/main/java/com/wkk/learn/spring/ioc/generic/GenericDemo.java)

## Java5类型接口

Java5引入泛型之后，新增了与泛型相关的反射接口和类，最主要的是java.lang.reflect.Type。它的派生类或接口有：

* java.lang.Class: Java 类 API，如 java.lang.String
* java.lang.reflect.GenericArrayType: 泛型数组类型，比如T[]
* java.lang.reflect.ParameterizedType: 泛型参数类型，如Collection\<E>
* java.lang.reflect.TypeVariable: 泛型类型变量，如Collection\<E>中的E
* java.lang.reflect.WildcardType: 泛型通配类型，就是有边界的泛型类型

Java泛型反射API：

* 泛型信息（Generics Info）: java.lang.Class#getGenericInfo()
* 泛型参数（Parameters）: java.lang.reflect.ParameterizedType
* 泛型父类（Super Classes）: java.lang.Class#getGenericSuperclass()
* 泛型接口（Interfaces）: java.lang.Class#getGenericInterfaces()
* 泛型声明（Generics Declaration）: java.lang.reflect.GenericDeclaration

Java泛型API示例：[GenericAPIDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/generic/src/main/java/com/wkk/learn/spring/ioc/generic/GenericAPIDemo.java)

## Spring泛型类型辅助类

核心API：org.springframework.core.GenericTypeResolver

* 版本支持：2.5.1+
* 处理类型（Type）相关的方法
  * resolveReturnType：获取方法返还值类型。
  * resolveType
* 处理泛型参数类型（ParameterizedType）的相关方法
  * resolveReturnTypeArgument
  * resolveTypeArgument
  * resolveTypeArguments
* 处理泛型类型变量（TypeVariable）相关方法
  * getTypeVariableMap

Spring泛型类型处理示例：[GenericTypeResolverDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/generic/src/main/java/com/wkk/learn/spring/ioc/generic/GenericTypeResolverDemo.java)

> 在获取泛型参数类型时，泛型参数必须具体化才能获取到值，因为泛型参数具体化，字节码才有值。

## Spring泛型集合类型辅助类

核心API：org.springframework.core.GenericCollectionTypeResolver

使用GenericCollectionTypeResolver类可以获取到集合类型中泛型的具体化，比如List\<String>，返回的是String.class。

* 版本支持：[2.0, 4.3]
* 替换实现：org.springframework.core.ResolvableType，5.0版本开始使用ResolvableType替换实现。
* 处理 Collection 相关
  * getCollection*Type
* 处理 Map 相关
  * getMapKey*Type
  * getMapValue*Type

Spring泛型集合类型处理示例：[GenericCollectionTypeResolverDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/generic/src/main/java/com/wkk/learn/spring/ioc/generic/GenericCollectionTypeResolverDemo.java)

## Spring方法参数封装

核心 API - org.springframework.core.MethodParameter

MethodParameter既可以表示方法参数也可以表示构造器参数，在SpringMVC中又用来表示返回值类型，但是一个MethodParameter实例只能表示一种情况。

* 起始版本：2.0+
* 元信息：
  * 关联的方法 - Method
  * 关联的构造器 - Constructor
  * 构造器或方法参数索引 - parameterIndex
  * 构造器或方法参数类型 - parameterType
  * 构造器或方法参数泛型类型 - genericParameterType
  * 构造器或方法参数参数名称 - parameterName，在jdk8之前，接口中的方法参数名称在编译后是不保存的，在jdk8之后可以在编译时添加参数，保留方法参数。
  * 所在的类 - containingClass

## Spring 4.0泛型优化实现ResolvableType

核心API：org.springframework.core.ResolvableType

* 起始版本：4.0
* GenericTypeResolver和GenericCollectionTypeResolver的替代者
* 工厂方法：for*方法：通过工厂方法获取到ResolvableType示例
  * forClass(Class)
  * forField(Field)
  * forMethodParameter
  * forType
* 转换方法：as*方法：转换成指定的类型
  * asMap：转换成Map类型，必须实现了Map，否则为？
  * asCollection：转换为Collection类型，必须实现了Collection，否则为？
* 处理方法：resolve*方法：用来转换成指定的Raw Type类型

ResolvableType示例：[ResolvableTypeDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/generic/src/main/java/com/wkk/learn/spring/ioc/generic/ResolvableTypeDemo.java)

## ResolvableType的局限性

ResolvableType接口即使再强大，也无法跳出Java泛型语言特性的一些局限性，这些局限性有：

* 局限一：ResolvableType无法处理泛型擦写
* 局限二：ResolvableType无法处理非具体化的ParameterizedType

实际上ResolvableType是基于Java泛型API的基础上做了一些封装和优化，简化了API的调用，以及去除了一些不必要的API。

## 面试题

* Java 泛型擦写发生在编译时还是运行时？

  编译时会进行类型检查，生成的字节码中没有泛型了，我的理解就是运行时擦写。

* 请介绍 Java 5 Type 类型的派生类或接口？

  * java.lang.Class
  * java.lang.reflect.GenericArrayType
  * java.lang.reflect.ParameterizedType
  * java.lang.reflect.TypeVariable
  * java.lang.reflect.WildcardType

* 请说明ResolvableType的设计优势？

  * 简化 Java 5 Type API 开发，屏蔽复杂 API 的运用，如ParameterizedType
  * 不变性设计（Immutability）：ResolvableType的很多内部属性都是final的。
  * Fluent API 设计（Builder 模式），链式（流式）编程，每个方法在调用后都会返回一个新的/老的ResolvableType实例。
