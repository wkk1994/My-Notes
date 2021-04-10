# mysql数据库导入导出

## mysqldump

官方自带的逻辑备份工具。当 mysqldump 使用参数–single-transaction 的时候，导数据之前就会启动一个事务，来确保拿到一致性视图。

常用语法:
   mysqldump[options] database [tables,多个表用空格隔开]
               --databases [options] BD1 [DB2..多个库用空格隔开]
               --all-databases [options]
### 导出数据

* 导出数据和表结构
mysqldump -u用户名 -p密码 数据库名 > 数据库名.sql
示例： mysqldump -u root -p test > test.sql

* 只导出表结构
mysqldump -u用户名 -p密码 -d 数据库名 > 数据库名.sql
示例：mysqldump -u root -p -d test > test.sql

* 只导出表数据
mysqldump -u用户名 -p密码 -t 数据库名 > 数据库名.sql
示例：mysqldump -u root -p -t test > test.sql

* 导出一个或多个表结构
mysqldump -u 用户名 -p -d 数据库名 表名1 表名2 > 导出的文件名
示例：mysqldump -u root -p -d test test_user index_t > test_user.sql

* 导出一个或多个表结构和数据
mysqldump -u 用户名 -p 数据库名 表名1 表名2 > 导出的文件名
示例：mysqldump -u root -p test test_user index_t > test_user.sql

* 开启一致性视图
mysqldump -u 用户名 -p --single-transaction 数据库名 表名1 表名2 > 导出的文件名

* 导出远程数据库
mysqldump -h 远程IP地址 -u 用户名 -p 数据库名 表名1 表名2 > 导出的文件名

mysqldump --column-statistics=0 -h 47.102.107.218 -u mysql -p --single-transaction stcsm_user > stcsm_user.sql

mysqldump --column-statistics=0 -h 127.0.0.1 -u mysql -p --single-transaction stcsm_user > stcsm_user-2021-04-06.sql

### 导入数据

1、首先建空数据库
mysql>create database test_bak;

2、导入数据库
方法一：
（1）选择数据库
mysql>use abc;
（2）设置数据库编码
mysql>set names utf8;
（3）导入数据（注意sql文件的路径）
mysql>source /home/abc/test_user.sql;
方法二：
mysql -u用户名 -p密码 数据库名 < 数据库名.sql
示例： mysql -u root -p test_bak < test_user.sql;

## mysqlpump

MySQL从5.7开始推出了mysqlpump工具

与mysqldump相比的优势：

* 基于表并行备份数据库和数据库中的对象的，加快备份过程。（--default-parallelism）
* 更好的控制数据库和数据库对象（表，存储过程，用户帐户）的备份。
* 备份用户账号作为帐户管理语句（CREATE USER，GRANT），而不是直接插入到MySQL的系统数据库。
* 备份出来直接生成压缩后的备份文件。
* 备份进度指示，观察备份进度更直观。
* 重新加载（还原）备份文件，先建表后插入数据最后建立索引，减少了索引维护开销，加快了还原速度。
* 备份可以排除或则指定数据库。

注意：

* 官方表示在mysql5.7.11之前无法保证数据的一致性，所以5.7.11之前该工具基本无法使用
* mysqlpump的多线程备份是基于表的，当数据库中有很多张小表个别几张超大表的时候，mysqlpump的备份速度其实还不如mysqldump。

参数使用：

```text
 -u                          备份用户名
 -p                          指定用户名密码
 -P                          连接数据库端口
 -h                          指定数据库地址
 --login-path=#              可以支持面密码备份数据
 -A, --all-databases 	     备份所有数据库
 --character-sets-dir=name   指定备份数据导出字符集
 --compress-output=name      将备份数据压缩输出，目前支持的压缩算法有LZ4和ZLIB
 -B, --databases             指定备份数据库，多个库之间用逗号分隔
 --default-parallelism=#     备份并行线程数，默认为2，若指定该参数为0，则表示不使用并行备份。
 --defer-table-indexes       延迟创建索引，将全部数据备份结束后再创建索引，默认开启。使用mysqldump备份时会先创建表和索引，然后加载原数据，资源消耗不仅有备份还有对二级索引的维护
 --exclude-databases=name    备份时排除该参数指定的数据库，多个数据库之间使用,分隔
 --exclude-tables=name       备份时排除该参数指定的表，多个表之间使用,分隔
 --include-databases=name    备份指定数据库，多个数据库之间使用,分隔
 --include-tables=name       备份指定表，多个表之间使用,分隔
 --parallel-schemas=[N:]db_list    指定并行备份的库，多个库之间用逗号分隔。也可以直接指定备份该库启用的线程队列数，若不指定则有-default-parallelism参数决定，默认为2。
 -d, --skip-dump-rows        只备份表结构，不备份数据
 --users                     备份数据库用户，备份形式为create user ... ,grant ....
                             如果只需要备份数据库账号可以使用 mysqlpump --exclude-databases=% --users
 --watch-progress            显示备份进度，默认开启
 --single-transaction        对于innodb表，在备份开始的时候会开启一个事物，并且设置隔离级别为RR，保证备份数据的一致性。备份期间应避免DDL。
                             在MySQL5.7.11之前，--defaut-parallelism大于1的时候和该参数互斥，所以必须使用--default-parallelism=0。
                             MySQL 5.7.11后解决了--single-transaction和--default-parallelism互斥的问题。
```

### 导出数据

* mysqlpump压缩备份数据库 三个并发线程备份

  mysqlpump -uroot -p -h127.0.0.1 --single-transaction --default-character-set=utf8 --compress-output=LZ4 --default-parallelism=3 -B test > test.sql.lz4

* mysqldump备份压缩数据库 单个线程备份，gzip的压缩率比LZ4的高
  mysqlpump -uroot -p -h127.0.0.1 --single-transaction --default-character-set=utf8 --compress-output=LZ4 --default-parallelism=3 -B test > test.sql.lz4
