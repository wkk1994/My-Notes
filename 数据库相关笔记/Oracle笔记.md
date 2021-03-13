### 数据库相关零星记录

#### oracle函数
> **intersect ; minus; union**

- "A minus B" 用于将结果集B中含有的记录从结果集A中移除，即用于获取存在于结果集A中而不存在于结果集B中的记录；“A union B”和“A union all B”用于获取结果集A和结果集B的并集，其中前者将只保留重复记录中的一条，而后者将保留所有的记录；“A intersect B”用于获取结果集A和结果集B共有的记录，即它们的交集。 
<font color="red" >注意：需要结果集A和结果集B拥有相同的结构，即它们的列数要一致，且每列的数据类型也需要一致，否则没法进行比较</font>
- **与in的比较：** in只适用与单个字段的比较

> **nvl**

- nvl(value1,value2) 若value为null则返回value2的值，可以嵌套使用 nvl(value1,nvl(value2,value3))

> **decode**

- decode(条件,值1,返回值1,值2,返回值2,...值n,返回值n,缺省值)
- 示例：比较大小 decode(sign(变量1-变量2),-1,变量1,变量2);
- **sign()** 根据某个值是0、正数还是负数，分别返回0、1、-1

#### 数据库备份
https://www.cnblogs.com/login2012/p/5895987.html#_label2o

#### 线程阻塞解决方案：
[参考](http://www.cnblogs.com/kerrycode/p/4034231.html)
* 首先得到被锁对象的session_id
```sql 
select session_id from v$locked_object;
```
* 通过上面得到的session_id去取得v$session的sid和serial#，然后对该进程进行终止。
```sql
SELECT sid, serial#, username, osuser FROM v$session where sid = 22;
```
* 最后杀会话
三种方式：
    * ```sql alter system kill session 'sid,serial';```
    
        实际上不是真正的杀死会话，它只是将会话标记为终止。等待PMON进程来清除会话。
    * ```sql alter system disconnect session 'sid,serial#';```
    
        杀掉专用服务器(DEDICATED SERVER)或共享服务器的连接会话，它等价于从操作系统杀掉进程。它有两个选项POST_TRANSACTION和IMMEDIATE， 其中POST_TRANSACTION表示等待事务完成后断开会话，IMMEDIATE表示中断会话，立即回滚事务。

        SQL> alter system disconnect session 'sid,serial#' post_transaction;

        SQL> alter system disconnect session 'sid,serial#' immediate;
    *  KILL -9 SPID(Linux)或orakill ORACLE_SID spid (Windows)
    
    可以使用下面SQL语句找到对应的操作系统进程SPID，然后杀掉。当然杀掉操作系统进程是一件危险的事情，尤其不要误杀。
    ```sql
    select s.inst_id,s.sid,s.serial#,p.spid,s.username,s.program
    from gv$session s join gv$process p on p.addr = s.paddr and 
    p.inst_id = s.inst_id where s.type != 'BACKGROUND';
    ```
    
在数据库如果要彻底杀掉一个会话，尤其是大事务会话，最好是使用alter system disconnect session immediate或使用下面步骤：

1.首先在操作系统级别Kill掉进程。  
2.在数据库内部KILL SESSION