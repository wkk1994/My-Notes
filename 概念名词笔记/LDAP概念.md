# LDAP概念

## 目录服务

目录是一个为查询、浏览和搜索而优化的专业分布式数据库，它呈树状结构组织数据，就好象Linux/Unix系统中的文件目录一样。目录数据库和关系数据库不同，它有优异的读性能，但写性能差，并且没有事务处理、回滚等复杂功能，不适于存储修改频繁的数据。所以目录天生是用来查询的，就好象它的名字一样。
目录服务是由目录数据库和一套访问协议组成的系统。类似以下的信息适合储存在目录中：企业员工信息，如姓名、电话、邮箱等；公用证书和安全密钥；公司的物理设备信息，如服务器，它的IP地址、存放位置、厂商、购买时间等；

## LDAP

全称Lightweight Directory Access Protocol，轻量目录访问协议。

### LDAP的基本模型

* 目录树：在一个目录服务系统中，整个目录信息集可以表示为一个目录信息树，树中的每个节点是一个条目。
* 条目：每个条目就是一条记录，每个条目有自己的唯一可区别的名称（DN）。
* 对象类：与某个实体类型对应的一组属性，对象类是可以继承的，这样父类的必须属性也会被继承下来。
* 属性：描述条目的某个方面的信息，一个属性由一个属性类型和一个或多个属性值组成，属性有必须属性和非必须属性。

| 关键字 | 英文全称 | 含义 |
| -- | -- | -- |
| dc | Domain Component | 域名的部分，其格式是将完整的域名分成几部分，如域名为example.com变成dc=example,dc=com（一条记录的所属位置） |
| uid | User Id | 用户ID songtao.xu（一条记录的ID）|
| ou | Organization Unit | 组织单位，组织单位可以包含其他各种对象（包括其他组织单元），如“oa组”（一条记录的所属组织）|
| cn | Common Name | 公共名称，如“Thomas Johansson”（一条记录的名称）|
| sn | Surname | 姓，如“许” |
| dn | Distinguished Name | “uid=songtao.xu,ou=oa组,dc=example,dc=com”，一条记录的位置（唯一）|
| rdn | Relative dn | 相对辨别名，类似于文件系统中的相对路径，它是与目录树结构无关的部分，如“uid=tom”或“cn= Thomas Johansson”|

## OpenLDAP 目录架构介绍

OpenLDAP目录架构常用的分为两种：一种为互联网命名组织架构，另一种是为企业级命名组织架构。

LDAP 的目录信息是以树形结构进行存储的，在树根一般定义国家（c=CN）或者域名（dc=com），其次往往定义一个或多个组织（organization，o）或组织单元（organization unit，ou）。一个组织单元可以包含员工、设备信息（计算机/打印机等）相关信息。例如uid=babs，ou=People，dc=example，dc=com。

互联网命名组织架构:

![互联网命名组织架构](https://upload-images.jianshu.io/upload_images/11727607-efb18b4982e1d7ba.png?imageMogr2/auto-orient/strip|imageView2/2/w/411/format/webp)

企业级命名组织架构:

![企业级命名组织架构](https://upload-images.jianshu.io/upload_images/11727607-14adc567eac6aee8.png?imageMogr2/auto-orient/strip|imageView2/2/w/438/format/webp)

## 安装

[安装参考1](https://cloud.tencent.com/developer/article/1490857)
[安装参考2](https://www.cnblogs.com/wmht/p/11162294.html#autoid-0-4-4)

## 相关命令

### ldapsearch查询命令

* -x: 启用简单认证，通过-D dn -w 密码的方式认证
* -D: 服务器DN
* -w: 服务器DN的密码
* -b: 指定要查询的根节点，支持正则表达式
* -H: 指定连接的服务器

示例：`ldapsearch -x -D cn=admin,dc=test,dc=com -w admin123 -H ldapi:/// -b ou=people,dc=test,dc=com`

### ldapadd添加节点

* -x: 启用简单认证，通过-D dn -w 密码的方式认证
* -D: 服务器DN
* -w: 服务器DN的密码
* -H: 指定连接的服务器
* -f: 指定要修改的文件

示例：`ldapadd -x -D cn=admin,dc=test,dc=com -w admin123 -H ldapi:/// -f addUser.ldif`

[OpenLDAP 常用命令](https://sitoi.cn/posts/5308.html#ldapmodify)