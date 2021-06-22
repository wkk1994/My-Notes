# Spring注解

## Spring注解驱动编程发展历程

* 注解驱动启蒙时代：Spring 1.X

  Spring 1.x版本处于03年之间，开始还是兼容的JDK1.2，没有支持注解的特性。
  代表注解：@Transactional：Spring1.2版本引入。

* 注解驱动过渡时代：Spring 2.X

  Spring 2.X版本虽然没有强制要求使用JDK1.5，但是已经开始兼容JDK1.5了。
  注解举例：@Repository、@Component、@Service

* 注解驱动黄金时代：Spring 3.X

  开始引入大量的注解，注解举例：@Bean、@Lazy、@Primary、@Configuration

* 注解驱动完善时代：Spring 4.X

  基本完成了注解驱动的编程模型。
  注解举例：@Conditional

* 注解驱动当下时代：Spring 5.X

  没有引入新注解，更多的是性能优化。
  注解举例：@Indexed：对注解扫描进行优化，可以在编译时就确定有哪些Bean。

## Spring核心注解场景分类

Spring模式注解：

* @Repository： 数据仓储模式注解
* @Component：通用组件模式注解，基本上所有的模式注解都是来自于它的派生。
* @Service：服务模式注解
* @Controller：Web控制模式注解
* @Configuration：配置类模式注解

装配注解：

* @ImportResource：替换XML中的\<import>
* Import：导入Configuration类
* ComponentScan：扫描指定包下的Spring模式注解类

依赖注入注解：

* @Autowired：Bean的依赖注入，支持多种依赖查找方式
* @Qualifier：细粒度的@Autowired依赖查找

