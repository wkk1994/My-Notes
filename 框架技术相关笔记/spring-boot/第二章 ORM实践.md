# O/R Mapping 实践

* 代码：spring-boot-learn/orm/*

## Spring Data JPA

**JPA**

JPA为对象关系映射提供了一种基于POJO的持久化模型，属于JAVA EE的标准之一，目的是简化数据持久化代码的开发，屏蔽不同持久化API的差异。比较好的实现为Hibernate。

**Spring Data**

Spring Data是Spring在保留底层存储特性的同时，提供相对一致的，基于Spring的编程模型

主要模块：

* Spring Data Commons
* Spring Data JDBC
* Spring Data JPA
* Spring Data Redis
* ... 

### JPA常用注解

**实体**

* @Entity 表示当前是一个实体
* @MappedSuperClass 表示当前实体是其他实体的父类
* @Table(name) 表明当前实体对应的表名

**主键**

* @Id
* @GeneratedValue(strategy,generator)
* @SequenceGenerator(name, sequenceName)

**映射**

* @Column(name, nullable, length, insertable, updatable)
  * insertable 是否可以插入
  * updatable 是否可以修改
* @JoinTable(name) 关联的表名
* @JoinColumn(name) 关联的列名

**关系**

* @OneToOne，@OneToMany，@ManyToOne，@ManyToMany
* @OrderBy

### JPA使用

* 在启动类上添加@EnableJpaRepositories注解，会自动扫描repository
* @NoRepositoryBean 定义这个bean不需要自动生成bean
* 实现Repository\<T, ID>接口
  * CrudRepository<T, ID>
  * PagingAndSortingRepository<T, ID>
  * JpaRepository<T, ID>

* 定义查询
  JPA会根据查询的方法名自动映射成对应的查询sql
  * find...By... / read...By.../ query...By.../ get...By...
  * count...By...
  * ...OrderBy...[Asc|Desc]
  * And/Or/IgnoreCase
  * Top/First/Distinct

* 分页查询
  * PagingAndSortingRepository<T, ID>
  * Pageable / Sort
  * Slice\<T> / Page\<T>

### RepositoryBean的生成

**？？？？**

## MyBatis

一个优秀的持久层框架，支持定制化SQL，存储过程和高级映射。

**JPA和MyBatis的适用场景：**

对于数据操作比较简单，表关系简单的场景可以使用JPA；对于复杂的表关系的处理，使用MyBatis可以定制化SQL，优化比较方便。
