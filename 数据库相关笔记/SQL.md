## SQL 

> distinct 去除重复数据

    SELECT DISTINCT 列名称 FROM 表名称

> insert 

    INSERT INTO 表名称 VALUES (值1, 值2,....)
    INSERT INTO table_name (列1, 列2,...) VALUES (值1, 值2,....)

> update 

    UPDATE 表名称 SET 列名称 = 新值 WHERE 列名称 = 某值

> delete

    DELETE FROM 表名称 WHERE 列名称 = 值

> TOP 子句用于规定要返回的记录的数目。

    并非所有的数据库系统都支持 TOP 子句。
    SQL Server SELECT TOP number|percent column_name(s) FROM table_name
    Oracle SELECT column_name(s) FROM table_name WHERE ROWNUM <= number         
    MySql SELECT column_name(s) FROM table_name LIMIT number//startRow,pageSize;   

> 通配符

    %	替代一个或多个字符
    _	仅替代一个字符
    [charlist]	字符列中的任何单一字符  Oracle 不支持
    [^charlist]或者[!charlist] 不在字符列中的任何单一字符 Oracle 不支持

> between

    SELECT column_name(s) FROM table_name WHERE column_name BETWEEN value1 AND value2
    不同的数据库对 BETWEEN...AND 操作符的处理方式是有差异的 可能包括（不包括）value1 value2; Oracle value1 value2都包括

> JOIN

    JOIN: 如果表中有至少一个匹配，则返回行
    LEFT JOIN: 即使右表中没有匹配，也从左表返回所有的行
    RIGHT JOIN: 即使左表中没有匹配，也从右表返回所有的行
    FULL JOIN: 只要其中一个表中存在匹配，就返回行
    INNER JOIN 与 JOIN 是相同的。

> UNION

    select colnum1 from table1 union select colnum2 from table2;
    UNION 操作符用于合并两个或多个 SELECT 语句的结果集。
    UNION 内部的 SELECT 语句必须拥有相同数量的列。列也必须拥有相似的数据类型。同时，每条 SELECT 语句中的列的顺序必须相同。
    UNION 命令只会选取不同的值
    允许重复的值，请使用 UNION ALL。

> select into

    语句从一个表中选取数据，然后把数据插入另一个表中
    SELECT column_name(s) INTO new_table_name [IN externaldatabase] FROM old_tablename
    * oracle不支持 oracle 使用 
    1. 新增一个表，通过另一个表的结构和数据
        create table tab1 as select * from tab2;
    2. 如果表存在
        insert into tab1 select * from tab2;
    3. 同一个表中，将A字段的指赋给B字段：
        update table_name set columnB =  columnA;
    4. 将一个表的字段数据插入到另一个表的字段数据中（可以延伸多个表的多个字段，插入同一个表的多个字段）
        insert into tab1(columnA,columnB)  select columnC,columnD from tab2;

> create 

    1. CREATE DATABASE database_name; 用于创建数据库。
    2. CREATE TABLE table_name;来添加数据库表

> 约束 
    
    not null; unique;  primary key; foreign key; check; default; 

> create index

    唯一索引
    CREATE UNIQUE INDEX index_name ON table_name (column_name)
    以降序索引某个列中的值，可以在列名称之后添加保留字 DESC：

>   DROP 删除索引、表和数据库 

    DROP TABLE table_name  删除表（表的结构、属性以及索引也会被删除）
    TRUNCATE TABLE table_name 仅仅删除表格中的数据
    DROP DATABASE 数据库名称   删除数据库

> ALTER TABLE 语句用于在已有的表中添加、修改或删除列

    ALTER TABLE table_name ADD column_name datatype
    ALTER TABLE table_name DROP COLUMN column_name

> create viwe

    CREATE VIEW view_name AS SELECT column_name(s) FROM table_name WHERE condition
    视图总是显示最近的数据。每当用户查询视图时，数据库引擎通过使用 SQL 语句来重建数据

> 对日期的处理

> IS NOT NULL  IS NULL 

> ISNULL()、NVL()、IFNULL() 和 COALESCE() 函数

    ISNULL(colnum,为null返回值) SQL Server / MS Access支持
    NVL(colnum,为null返回值) Oracle支持
    IFNULL(colnum,为null返回值)  MySql支持
    COALESCE(colnum,为null返回值) Oracle支持  MySql支持

> <a href='http://www.w3school.com.cn/sql/sql_datatypes.asp'>数据类型</a>

> having 

> drop truncate 和 delete 的区别

    truncate table命令将快速删除数据表中的所有记录，但保留数据表结构。这种快速删除与delete from 数据表的删除全部数据表记录不一样，delete命令删除的数据将存储在系统回滚段中，需要的时候，数据可以回滚恢复，而truncate命令删除的数据是不可以恢复的 
    TRUNCATE之后的自增字段从头开始计数了，而DELETE的仍保留原来的最大数值 
    不同点
    1. truncate和 delete只删除数据不删除表的结构(定义) 
    drop语句将删除表的结构被依赖的约束(constrain),触发器(trigger),索引(index); 依赖于该表的存储过程/函数将保留,但是变为invalid状态. 
    2. delete语句是dml,这个操作会放到rollback segement中
    truncate,drop是ddl, 操作立即生效,原数据不放到rollback segment中,不能回滚. 操作不触发trigger. 
    3.速度,一般来说: drop>; truncate >; delete
    实例
    drop table tablename;
    truncate table wap_cms_cp_user;
    delete from  tablename;

>什么是Rollback Segment

Rollback Segments是在你数据库中的一些存储空间，它用来临时的保存当数据库数据发生改变时的先前值，Rollback Segment主要有两个目的：

1. 如果因为某种原因或者其他用用户想要通过ROLLBACK声明来取消一个人的数据操作，数据就会复原到之前为改变时的值。这种情况只在transaction的过程中有效，如果用户执行了COMMIT命令，那么ROLLBACK SEGMENT里面的值就会标识为失效的，数据改变就将永久化。
2. 另一个目的是当有并发的session访问了一个数据值改变但事务还没有提交的表。如果一个SELECT语句开始读取一个表同时一个事务也在修改这个表的值，那么修改前的值就会保存到rollback segment里面，SELECT语句也是从ROLLBACK SEGMENT里面读取表的值。
