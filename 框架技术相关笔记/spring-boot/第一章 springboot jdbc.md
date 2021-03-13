# spring-boot jdbc

代码位置： spring-boot-learn/jdbc

## 直接配置所需的bean

* 数据源相关
  * DataSource（根据选择的数据源实现决定）
* 事务相关（可选）
  * PlatformTransactionManager（DataSourceTransactionManager）
  * TransactionTemplate
* 操作相关（可选）
  * JdbcTemplate

## 对应的自动配置bean

* DataSourceAutoConfiguration  配置DataSource
* DataSourceTranscationMangerAutoConfiguration 配置DataSourceTransactionManager
* JdbcTemplateAutoConfiguration 配置JdbcTemplate

以上的配置都是需要时才会配置

## 数据源相关配置属性

* 通用

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver #可选
```

* 初始化内嵌数据库

```properties
spring.datasource.initialization-mode=always|embedded|never
spring.datasource.data=data.sql
spring.datasource.schema=schema.sql
spring.datasource.platform=all|oracle|h2|mysql|postgreql
```

## 多数据源配置

与SpringBoot协同工作，可以选择下面中的一种使用

* 配置@Primary类型的Bean
    将它作为一个主要的数据源使用
* 排除SpringBoot的自动配置
    排除DataSourceAutoConfiguration，DataSourceTranscationMangerAutoConfiguration，JdbcTemplateAutoConfiguration

示例：

```java
    @Bean
    @ConfigurationProperties(value = "db1.datasource")
    public DataSourceProperties db1DataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource db1DataSource() {
        DataSourceProperties dataSourceProperties = db1DataSourceProperties();
        log.info("db1 DataSource : {}", dataSourceProperties.getUrl());
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    public PlatformTransactionManager db1PlatformTransactionManager(DataSource db1DataSource) {
        return new DataSourceTransactionManager(db1DataSource);
    }

    @Bean
    public JdbcTemplate db1JdbcTemplate(DataSource db1DataSource) {
        return new JdbcTemplate(db1DataSource);
    }

    @Bean
    @ConfigurationProperties(value = "db2.datasource")
    public DataSourceProperties db2DataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource db2DataSource() {
        DataSourceProperties dataSourceProperties = db2DataSourceProperties();
        log.info("db2 DataSource : {}", dataSourceProperties.getUrl());
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    public PlatformTransactionManager db2PlatformTransactionManager(DataSource db2DataSource) {
        return new DataSourceTransactionManager(db2DataSource);
    }

    @Bean
    public JdbcTemplate db2JdbcTemplate(DataSource db2DataSource) {
        return new JdbcTemplate(db2DataSource);
    }
```

```properties
db1.datasource.url=jdbc:h2:mem:testdb1
db1.datasource.username=sa
db1.datasource.password=
db1.datasource.driver-class-name=org.h2.Driver

db2.datasource.url=jdbc:h2:mem:testdb2
db2.datasource.username=sa
db2.datasource.password=
db2.datasource.driver-class-name=org.h2.Driver
```

## 数据源

### HikariCP（日本数据源，意为光）

**HiKariCP为什么那么快？**

* 字节码级别的优化（很多方法是通过JavaAssist生成，在运行时生成）
* 大量小的改进
  * 用FastStatementList代替ArrayList
  * 无锁集合ConcurrentBag
  * 代理类的优化（比如使用invokestatic代替invokevirtual）

### Druid

[Druid文档](https://github.com/alibaba/druid/wiki/%E9%A6%96%E9%A1%B5)
[druid-spring-boot-starter](https://github.com/alibaba/druid/tree/master/druid-spring-boot-starter)

Druid连接池阿里巴巴开源的数据库连接池，为监控而生，内置强大的监控功能，监控特性不影响性能。功能强大，能防止sql注入，内置的Logging能诊断Hack应用的行为。

* 详细的监控
* ExceptionSorter，对主流数据库的返回码都有支持
* SQL防注入
* 内置加密配置
* 众多扩展点，方便进行定制

**一些配置信息**

* Filter
  * spring.datasource.druid.filters=stat,config,wall,log4j（默认全部使用）
* 密码加密
  * spring.datasource.password=<加密密码>
  * spring.datasource.druid.filter.config.enabled=true
  * spring.datasource.druid.connection-properties=config.decrypt=true;config.decrypt.key=\<public-key>

* SQL防注入
  * spring.datasource.druid.filter.wall.enabled=true
  * spring.datasource.druid.filter.wall.db-type=h2
  * spring.datasource.druid.filter.wall.config.delete-allow=false
    不能删除数据
  * spring.datasource.druid.filter.wall.config.drop-table-allow=false
    不能drop操作

* ConfigTools是druid的加密类

**Druid Filter**

* 用于定制连接池操作的各个环节
* 可以继承FilterEventAdapter以便实现Filter
* 修改META-INF/druid-filter.properties增加Filter配置

示例：

* 实现FilterEventAdapter

```java
@Slf4j
public class ConnectionLogFilter  extends FilterEventAdapter {
    @Override
    public void connection_connectBefore(FilterChain chain, Properties info) {
        log.info("BEFORE CONNECTION!");
    }

    @Override
    public void connection_connectAfter(ConnectionProxy connection) {
        log.info("AFTER CONNECTION!");
    }
}
```

* druid-filter.properties引入Filter

```text
druid.filters.conn=com.wkk.learn.springboot.datasource.ConnectionLogFilter
```

* application.yml配置文件指定filter配置

```yml
spring.datasource.druid.filters=conn
```

**Filter简介**

* logFilter: 日志过滤器，主要包含log4j：Log4jFilter log4j对应实现；log4j2：Log4j2Filter log4j2对应实现；slf4j：Slf4jLogFilter slf4对应实现等。
* StatFilter
  别名：`stat`，用于统计监控信息，提供的功能有慢SQL记录，SQL合并。
  相关属性配置：
  * `spring.datasource.druid.filter.stat.db-type`: 指定数据库类型，不指定时从dataSource获取dataType
  * `spring.datasource.druid.filter.stat.log-slow-sql`: 是否启用慢查询日志，默认false
  * `spring.datasource.druid.filter.stat.slow-sql-millis`: 慢查询时间，默认3s
  * `spring.datasource.druid.filter.stat.merge-sql`: 是否启用SQL合并，默认false

* ConfigFilter
  别名`config`，ConfigFilter只负责解密, 和下载远程的配置文件。
  * 解密
  只能解密password，需要在`connectionProperties`中配置config.decrypt=true和config.decrypt.key=公钥key
  * 下载远程配置文件
  需要在`connectionProperties`中配置config.file=http://localhost:8080/remote.propreties;，会在ConfigFilter初始化时从远程获取配置文件信息。支持的属性com.alibaba.druid.pool.DruidDataSourceFactory.config(DruidDataSource dataSource, Map<?, ?> properties);

* EncodingConvertFilter
  别名`encoding`，由于历史原因，一些数据库保存数据的时候使用了错误编码，需要做编码转换。需要在`connectionProperties`中配置`clientEncoding=UTF-8;serverEncoding=ISO-8859-1`，这样就可以指定客户端编码和服务端编码，每次执行获取数据和插入数据时都会执行对应的decode和encode操作。

* [WallFilter](https://www.bookstack.cn/read/Druid/ffdd9118e6208531.md)
  别名`wall`，WallFilter的功能是防御SQL注入攻击。它是基于SQL语法分析，理解其中的SQL语义，然后做处理的，智能，准确，误报率低。

## Sprin的JDBC操作

* core，JdbcTemplate等核心接口和类
* datasource，数据源相关辅助
* object，将基本的JDBC操作封装成对象
* support，错误码等其他辅助工具

**JdbcTemplate的方法**

* query
* queryForObject
* queryForList
* update
* execute

## 事务抽象

Spring提供了统一的事务模型，不管是使用JDBC/myBatis/JTA/DataSource，都可以使用统一的API进行事务操作。

**事务抽象的核心接口**

* PlatformTransactionManager 事务管理接口
   Spring根据不同的链接方式提供不同的事务实现
  * DataSourceTransactionManager
  * HibernateTransactionManager
  * JtaTransactionManager

* TransactionDefinition 定义事务属性接口
   主要的属性有：
  * propagation 事务的传播方式

   |传播性|值|描述|
   |--|--|--|
   |PROPAGATION_REQUIRED|0|当前有事务就用当前的事务，没有事务就创建新的事务|
   |PROPAGATION_SUPPORTS|1|事务可有可无，不是必须的|
   |PROPAGATION_MANDATORY|2|一定要有事务，不然就会抛出错误|
   |PROPAGATION_REQUIRES_NEW|3|无论是否有事务都会开启一个新事务|
   |PROPAGATION_NOT_SUPPORTED|4|不支持事务按非事务方式运行|
   |PROPAGATION_NEVER|5|不支持事务，如果有事务就会抛出异常|
   |PROPAGATION_NESTED|6|如果有事务就在当前事务里再起一个事务|

    PROPAGATION_REQUIRES_NEW，始终启动一个新事务，两个事务没有关联
    PROPAGATION_NESTED，两个事务有关联，外部事务回滚，内嵌事务也会回滚

  * isolation  事务的隔离级别，默认值ISOLATION_DEFAULT，取决于数据库的设置

   |隔离性|值|脏读|不可重复读|幻读|
   |--|--|--|--|--|
   |ISOLATION_READ_UNCOMMITTED|1|√|√|√|
   |ISOLATION_READ_COMMITTED|2||√|√|
   |ISOLATION_REPEATABLE_READ|4|||√|
   |ISOLATION_SERIALIZABLE|8||||

  * timeout 事务的超时事件
  * readOnly 事务是否是只读

### 编程式事务

* TransactionManager
  * TransactionCallback
  * TranscationCallbackWithoutResult
* PlatformTransactionManager
  * 可以传入TransactionDefinition定义事务的特性

### 声明式事务

* 开启事务的方式
  * 注解的方式，在启动类上添加注解@EnableTransactionManager，**不添加也是启用的**
  * xml的方式，在xml配置文件中添加\<tx:annotation-driven/>

* 配置项说明：
  * proxyTargetClass 表示是否创建基于子类（CGlib）的代理方式，而不是基于Java标准的接口代理。对于没有实现接口的直接调用设置为true，默认值false。
  * mode 事务拦截的方式，默认值AdviceMode.PROXY，PROXY模式仅允许通过PROXY拦截调用。同一个类中的本地调用不能以这种方式被拦截。
  * order 事务拦截器的执行顺序，默认最低级别。
* 在需要的方法或类上添加@Transaction注解

## JDBC异常的抽象

Spring会将数据操作的异常转换为DataAccessException，无论使用哪一种数据访问方式，都能使用一样的异常。

**Spring数据库错误码的识别**

Spring通过SQLErrorCodeSQLExceptionTranslator解析错误码

* ErrorCode定义
  * /org/springframework/jdbc/support/sql-error-codes.xml
  * classpath下的sql-error-codes.xml

* 自定义错误码异常

  * 继承对应要自定义的异常类
  
  ```java
  public class CustomDuplicatedKeyException extends DuplicateKeyException {
    public CustomDuplicatedKeyException(String msg) {
        super(msg);
    }

    public CustomDuplicatedKeyException(String msg, Throwable cause) {
        super(msg, cause);
    }
  }
  ```

  * resources下添加sql-error-codes.xml，指定自定义异常

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN 2.0//EN" "https://www.springframework.org/dtd/spring-beans-2.0.dtd">
  <!--
	- Default SQL error codes for well-known databases.
	- Can be overridden by definitions in a "sql-error-codes.xml" file
  - in the root of the class path.
	-
	- If the Database Product Name contains characters that are invalid
	- to use in the id attribute (like a space) then we need to add a property
	- named "databaseProductName"/"databaseProductNames" that holds this value.
	- If this property is present, then it will be used instead of the id for
	- looking up the error codes based on the current database.
	-->
  <beans>
    <bean id="H2" class="org.springframework.jdbc.support.SQLErrorCodes">
      <property name="badSqlGrammarCodes">
  			<value>42000,42001,42101,42102,42111,42112,42121,42122,42132</value>
  		</property>
  		<property name="duplicateKeyCodes">
  			<value>23001,23505</value>
  		</property>
  		<property name="dataIntegrityViolationCodes">
  			<value>22001,22003,22012,22018,22025,23000,23002,23003,23502,23503,23506,23507,23513</  value>
  		</property>
  		<property name="dataAccessResourceFailureCodes">
  			<value>90046,90100,90117,90121,90126</value>
  		</property>
  		<property name="cannotAcquireLockCodes">
  			<value>50200</value>
  		</property>
  		<property name="customTranslations">
  			<bean class="org.springframework.jdbc.support.CustomSQLErrorCodesTranslation">
  				<property name="errorCodes" value="23001,23505"></property>
  				<property name="exceptionClass" value="com.wkk.learn.springboot.errorcode.  CustomDuplicatedKeyException"></property>
  			</bean>
  		</property>
  	</bean>
  </beans>
  ```
