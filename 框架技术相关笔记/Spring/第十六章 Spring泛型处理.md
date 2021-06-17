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

Spring泛型类型处理实例：[GenericTypeResolverDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/generic/src/main/java/com/wkk/learn/spring/ioc/generic/GenericTypeResolverDemo.java)

> 在获取泛型参数类型时，泛型参数必须具体化才能获取到值，因为泛型参数具体化，字节码才有值。