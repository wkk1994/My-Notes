# MySQL分区表

## 分区表是什么

分区表是将一个表的数据按照范围，HASH，列表等规则分成多个小部分进行管理。

* 创建范围分区表

    ```sql
    CREATE TABLE `test43` ( 
        `ftime` datetime NOT NULL, 
        `c` int(11) DEFAULT NULL,  KEY (`ftime`)
    ) ENGINE=InnoDB DEFAULT CHARSET=latin1
    PARTITION BY RANGE (YEAR(ftime))(
        PARTITION p_2017 VALUES LESS THAN (2017) ENGINE = InnoDB,
        PARTITION p_2018 VALUES LESS THAN (2018) ENGINE = InnoDB,
        PARTITION p_2019 VALUES LESS THAN (2019) ENGINE = InnoDB,
        PARTITION p_others VALUES LESS THAN MAXVALUE ENGINE = InnoDB
    );
    ```

* 新增分区

    ```sql
    alter table test43 add partition(
        partition p_2020 values less than (2020)
    );
    ```

* 删除分区

    ```sql
    alter table test43 drop partition p_2020;
    ```

* 拆分分区

    ```sql
    alter table test43 reorganize partition p_others into(
        partition s0 values less than(2020),
        partition s1 values less than(MAXVALUE)
    );
    ```

* 合并分区

    ```sql
    alter table test43 reorganize partition s0,s1 into (
        partition p4 values less than (MAXVALUE)
    );
    ```

**注意：如果表上存在唯一索引或者主键索引，分区字段，必须是表上所有的唯一索引（或主键索引）包含的字段的子集。** 

上面创建分区表后，这个表包含一个.frm文件和4个.ibd文件，每个分区对应一个.ibd文件。也就是说：对于引擎层来说，这是4个表；对于server层来说，这是1个表。

## 分区表的引擎层行为

```sql
-- session A                                                    session B
begin;
select * from test43 where ftime ='2017-05-01' for update;
                                                                insert into test43 values('2018-2-1', 1);(Query Ok)
                                                                insert into test43 values('2017-2-1', 1);(blocked)
```

在引擎层，认为这是不同的表，因此 MDL 锁之后的执行过程，会根据分区表规则，只访问必要的分区——被访问到的分区。

* 对于InnoDB引擎来说分区表实际上是不同的表，所以加锁规则是在不同的分区表上加锁，互不影响。

* 因为MyISAM 的表锁是在引擎层实现的，所以一个分区上加MDL读锁也不影响其他分区正常读，写。

## 分区表的 server 层行为

如果从 server 层看的话，一个分区表就只是一个表。

```sql
-- session A                                        session B
begin;
select * from test43 where ftime = '2018-4-1';
                                                    alter table test43 truncate partition p_2017;
```

可以看到，虽然 session B 只需要操作 p_2107 这个分区，但是由于 session A 持有整个表 test43 的 MDL 锁，就导致了 session B 的 alter 语句被堵住。分区表，在做 DDL 的时候，影响会更大。如果使用的是普通分表，那么当你在 truncate 一个分表的时候，肯定不会跟另外一个分表上的查询语句，出现 MDL 锁冲突。

## 分区策略

每当第一次访问一个分区表的时候，MySQL 需要把所有的分区都访问一遍。一个典型的报错情况是这样的：如果一个分区表的分区很多，比如超过了 1000 个，而 MySQL 启动的时候，open_files_limit 参数使用的是默认值 1024，那么就会在访问这个表的时候，由于需要打开所有的文件，导致打开表文件的个数超过了上限而报错。错误信息：`Can 't open file './test/t_myisam.frm'(errno 24 -- Too many open files)`。

现在Myisam引擎会出现这个错误，InnoDB引擎不会出现这个错误。

* MyISAM 分区表使用的分区策略，称为通用分区策略（generic partitioning），每次访问分区都由 server 层控制。通用分区策略，是 MySQL 一开始支持分区表的时候就存在的代码，在文件管理、表管理的实现上很粗糙，因此有比较严重的性能问题。

* InnoDB 引擎从 MySQL 5.7.9 开始，引入了本地分区策略（native partitioning）。这个策略是在 InnoDB 内部自己管理打开分区的行为。

    参数`innodb_open_files`限制Innodb能打开的表的数量，在InnoDB引擎打开文件超过 innodb_open_files这个值的时候，就会关掉一些之前打开的文件。InnoDB分区表使用了本地分区策略以后，即使分区个数大于open_files_limit ，打开InnoDB分区表也不会报“打开文件过多”这个错误，就是innodb_open_files这个参数发挥的作用。

从 MySQL 8.0 版本开始，就不允许创建 MyISAM 分区表了，只允许创建已经实现了本地分区策略的引擎。目前来看，只有 InnoDB 和 NDB 这两个引擎支持了本地分区策略。

## 分区表的应用场景

分区表的一个显而易见的优势是对业务透明，相对于用户分表来说，使用分区表的业务代码更简洁。

分区表可以很方便的清理历史数据。如果一项业务跑的时间足够长，往往就会有根据时间删除历史数据的需求。这时候，按照时间分区的分区表，就可以直接通过 alter table t drop partition …这个语法删掉分区，从而删掉过期的历史数据。这个 alter table t drop partition …操作是直接删除分区文件，效果跟 drop 普通表类似。与使用 delete 语句删除数据相比，优势是速度快、对系统影响小。

但是需要注意的是：

* 分区并不是越细越好。实际上，单表或者单分区的数据一千万行，只要没有特别大的索引，对于现在的硬件能力来说都已经是小表了。

* 分区也不要提前预留太多，在使用之前预先创建即可。比如，如果是按月分区，每年年底时再把下一年度的 12 个新分区创建上即可。对于没有数据的历史分区，要及时的 drop 掉。

* 实际使用时，分区表跟用户分表比起来，有两个绕不开的问题：一个是第一次访问的时候需要访问所有分区，另一个是共用 MDL 锁。
