# grant 和 revoke 命令的执行逻辑

## 创建用户的逻辑

```sql
create user 'ua'@'%' identified by 'pa';
```

上面的语句的语义是创建一个用户'ua'@'%'，密码是 pa；在MySQL里用户名（user）+地址（host）才表示一个用户，ua@ip1 和 ua@ip2 代表的是两个不同的用户，'%'表示所有ip都可以使用这个用户连接。

这条命令的执行过程：

* 磁盘上：往mysql.user表插入一行数据，由于没有指定权限，所以这行数据所有表示权限的字段的值都是"N"；
* 内存里：往数组acl_users里面插入一个acl_users对象，这个对象的access字段值为0。

## 用户权限范围

在 MySQL 中，用户权限是有不同的范围的。

![权限范围](http://ww1.sinaimg.cn/large/d1885ed1ly1g0ab2twmjaj21gs0js78u.jpg)

### 全局范围

全局权限，作用于整个 MySQL 实例，这些权限信息保存在 mysql 库的 user 表里。

**授权语句**

```sql
grant all privileges on *.* to 'ua'@'%' with grant option;
```

上面语句的语义是给ua用户赋予一个最高权限。在MySQL中执行过程：

* 磁盘上：将mysql.user表中，用户'ua'@'%'这一行的所有表示权限的字段都改为"Y"；
* 内存里：从数组acl_users中找到用户'ua'@'%'对应的对象，将access值（权限位）修改为二进制的"全1"。

在这个 grant 命令执行完成后，如果有新的客户端使用用户名 ua 登录成功，MySQL 会为新连接维护一个线程对象，然后从 acl_users 数组里查到这个用户的权限，并将权限值拷贝到这个线程对象中。之后在这个连接中执行的语句，所有关于全局权限的判断，都直接使用线程对象内部保存的权限位。

所以：

* grant 命令对于全局权限，同时更新了磁盘和内存。命令完成后即时生效，接下来新创建的连接会使用新的权限。
* 对于一个已经存在的连接，它的全局权限不受 grant 命令的影响。

**注意：**一般在生产环境要合理控制用户权限范围，不会轻易赋予用户全部权限，即使赋予全部权限也是指定ip连接的。

**收回权限**

```sql
revoke all privileges on *.* from 'ua'@'%';
```

在MySQL中的执行过程：

* 磁盘上，将 mysql.user 表里，用户'ua'@'%'这一行的所有表示权限的字段的值都修改为"N"；
* 内存里，从数组 acl_users 中找到这个用户对应的对象，将 access 的值修改为 0。

### db 权限

除了全局权限，MySQL也支持库级别的权限定义。

**授权语句**

```sql
grant all privileges on db1.* to 'ua'@'%' with grant option;
-- 即使授予的数据库没创建也会授权成功
grant super on *.* to 'ua'@'%' identified by 'pa';
-- 如果用户'ua'@'%'不存在，就创建这个用户，密码是 pa；如果用户'ua'@'%'已经存在，就将密码修改成 pa。
```

上面语句的语义是给用户'ua'@'%'授予db1库的全部权限。基于库的权限记录保存在 mysql.db 表中，在内存里则保存在数组 acl_dbs 中。这条 grant 命令做了如下两个动作：

* 磁盘上：往mysql.db表中插入一行记录，所有权限字段设置为"Y"；
* 内存里：增加一个对象到数组acl_dbs中，这个对象的权限位为"全1"。

每次需要判断一个用户对一个数据库读写权限的时候，都需要遍历一次 acl_dbs 数组，根据 user、host 和 db 找到匹配的对象，然后根据对象的权限位来判断。也就是说，grant 修改 db 权限的时候，是同时对磁盘和内存生效的。

**如果当前会话已经处于某一个 db 里面，之前 use 这个库的时候拿到的库权限会保存在会话变量中。**

### 表权限和列权限

除了 db 级别的权限外，MySQL 支持更细粒度的表权限和列权限。其中，表权限定义存放在表 mysql.tables_priv 中，列权限定义存放在表 mysql.columns_priv 中。这两类权限，组合起来存放在内存的 hash 结构 column_priv_hash 中。

```sql
grant all privileges on db1.t1 to 'ua'@'%' with grant option;
grant select(id), insert (id,a) on mydb.mytbl to 'ua'@'%' with grant option;
```

跟 db 权限类似，这两个权限每次 grant 的时候都会修改数据表，也会同步修改内存中的 hash 结构。因此，对这两类权限的操作，也会马上影响到已经存在的连接。

## flush privileges

`flush privileges` 命令会清空 acl_users 数组，然后从 mysql.user 表中读取数据重新加载，重新构造一个 acl_users 数组。也就是说，以数据表中的数据为准，会将全局权限内存数组重新加载一遍。同样地，对于 db 权限、表权限和列权限，MySQL 也做了这样的处理。也就是说，如果内存的权限数据和磁盘数据表相同的话，不需要执行 flush privileges。而如果都是用 grant/revoke 语句来执行的话，内存和数据表本来就是保持同步更新的。所以在正常情况下，grant/revoke 语句之后不需要执行`flush privileges`。

### flush privileges的使用场景

当数据表中的权限跟内存中的权限数据不一致的时候，`flush privileges`语句可以用来重建内存数据，达到一致的状态。

这种不一致往往是由不规范的操作导致的，比如直接用 DML 语句操作系统权限表。这样的不规范操作也可能产生用户仍可以正常登陆，但是授予用户权限等操作会提示失败。

## 记录

* `grant all privileges on `db%`.* to ... `表示所有以db为前缀的库，但是不建议这样操作。
