
# 分布式ID生成算法

## UUID

32位16进制数字组成，以连字符-分为5段，形式为8-4-4-4-12的32个字符。

### UUID的组成

UUID由以下几部分的组合：

* 当前日期和时间，UUID的第一个部分与时间有关，如果你在生成一个UUID之后，过几秒又生成一个UUID，则第一个部分不同，其余相同。
* 时钟序列。
* 全局唯一的IEEE机器识别号，如果有网卡，从网卡MAC地址获得，没有网卡以其他方式获得。

`xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx`

数字 M的四位表示 UUID 版本，当前规范有5个版本，M可选值为1, 2, 3, 4, 5 ；

数字 N的一至四个最高有效位表示 UUID 变体( variant )，有固定的两位10xx因此只可能取值8, 9, a, b

生成方式多种多样，业界公认的是五种，分别是uuid1,uuid2,uuid3,uuid4,uuid5。目前使用最广泛的UUID是微软的 GUID。

* uuid1: 基于时间戳、机器MAC地址生成。由于使用MAC地址，可以保证全球范围的唯一性。（有暴露机器地址问题） 常用
* uuid2: 只基于时间戳，不常用。
* uuid3: 基于namespace和一个自定义字符串，不常用。
* uuid4: 只基于随机数，最常用，但不推荐，重复几率不太能让人接受。
* uuid5: 只基于namespace，不常用。

Java UUID的生成`UUID.randomUUID()`默认为uuid4

### 优缺点

**优点：**

* 本地生成，性能极佳，无网络销号。
* 全局唯一

**缺点：**

* 存储麻烦。16字节128位，通常以36长度的字符串表示，很多场景不适用
* 通常是字符串，非自增，无序，不利于做主键。每次插入都会对B+tree结构进行修改
* 破解相对困难，但是也不安全。参考"梅丽莎病毒事件，病毒作者制作的UUID包含Mac地址，被警方破解后，直接定位，抓捕归案😝"

## 数据库主键自增(Flicker)

基于数据库主键自增的方案，名为 Flicker。主要是利用MySQL的自增主键来实现分布式ID。

### 实现方式

1. 创建数据库

```sql
create database `flicker`;
```

2. 创建一张表：sequence_id

```sql
create table sequence_id
(
    id bigint(20) unsigned NOT NULL auto_increment,
    stub char(10) NOT NULL default '',
    PRIMARY KEY (id),
    UNIQUE KEY stub (stub)
)ENGINE=InnoDB;
```

说明：

* stub: 票根，对应需要生成 Id 的业务方编码，可以是项目名、表名甚至是服务器 IP 地址。
* stub 要设置为唯一索引

3. 获取ID

```sql
REPLACE INTO sequence_id (stub) VALUES ('test');
SELECT LAST_INSERT_ID();
```

说明：

* `REPLACE INTO`：当插入的数据中和已有的唯一字段的数据重复时，替换原来数据。
* `SELECT LAST_INSERT_ID()`：返回最后一个insert/update时AUTO_INCREMENT列设置的值，必须和insert/update在同一个连接中。

### 改进

单实例节点挂了就会导致整个服务不能使用，可以通过运行多个实例解决。

第一台服务器：

```sql
set @@auto_increment_offset=1;--起始值
set @@auto_increment_increment=2;--步长
```

第二台服务器：

```sql
set @@auto_increment_offset=2;--起始值
set @@auto_increment_increment=2;--步长
```

当两台都OK的时候，随机取其中的一台生成ID；若其中一台挂了，则取另外一台生成ID。

### 优缺点

**优点：**

* 简单。充分利用了数据库自增 ID 机制，生成的 ID 有序递增。
* ID递增

**缺点：**

* 水平扩展困难，已经定义好步长后，添加机器困难。
* 并发量不大
* 安全系数低

## 利用Redis实现分布式ID

Redis为单线程的，所以操作为原子操作，利用 incrby命令可以生成唯一的递增ID。
集群部署时和Mysql类似，设置步长为集群数。

### 优缺点

**优点：**

* 性能显然高于基于数据库的 Flicker方案
* ID递增

**缺点：**

* 水平扩展困难
* Redis集群宕机可能会产生重复的id
* 易破解

## snowflake（雪花算法）

SnowFlake所生成的ID一共分成四部分：

![snowflakeID结构](../../../youdaonote-images/EAD8BE6709054E599171360401B6ECD5.jpeg)

* 第一位 占用1bit，其值始终是0，没有实际作用。
* 时间戳 占用41bit，精确到毫秒，总共可以容纳约69年的时间。
* 工作机器id 占用10bit，其中高位5bit是数据中心ID（datacenterId），低位5bit是工作节点ID（workerId），做多可以容纳1024个节点。
* 序列号 占用12bit，这个值在同一毫秒同一节点上从0开始不断累加，最多可以累加到4095。

SnowFlake算法在同一毫秒内最多可以生成1024 X 4096 =  4194304个全局唯一ID，这个数字在绝大多数并发场景下都是够用的。

### 优缺点

**优点：**

* 生成ID时不依赖于DB，完全在内存生成，高性能高可用。
* ID呈趋势递增，后续插入索引树的时候性能较好。
* 不太容易破解

**缺点：**

* 依赖机器的时间，如果机器时间不准或者回拨，可能导致重复

## 参考

* [图解各路分布式ID生成算法](https://mp.weixin.qq.com/s/qVN75AeN_5x-rn-wvgnBHg)
* [什么是SnowFlake算法？](https://cloud.tencent.com/developer/article/1477439)
