# order by的工作方式

`select city,name,age from t where city='杭州' order by name limit 1000;`
sql语句中order by的执行流程。

## 全字段排序

对于上面的sql为了避免全表扫描，在city字段上添加索引。

通过`explain`命令，查看sql的执行情况，Extra字段中的"Using filesort"表示的是需要排序（使用文件排序）。MySQL会给每个线程分配一块内存用于排序，称为`sort_buffer`。

上面语句的执行大致过程为：
1.初始化sort_buffer，确定放入city、name、age这三个字段；
2.从索引city找到第一个满足city='杭州'条件的主键id；
3.根据主键id索引取出整行，取city、name、age三个字段的值，存入sort_buffer中；
4.从索引city取下一个记录的主键id；
5.重复3,4直到city不满足查询条件为止；
6.对sort_buffer中的数据按照字段name做快速排序；
7.按照排序结果取前1000行返回给客户端。

**sort_buffer_size：**

流程中的对name进行排序，可能在内存中完成，也可能需要使用外部排序，这取决于所需的内存和参数`sort_buffer_size`。sort_buffer_size，就是 MySQL 为排序开辟的内存（sort_buffer）的大小。如果要排序的数据量小于 sort_buffer_size，排序就在内存中完成。但如果排序数据量太大，内存放不下，则不得不利用磁盘临时文件辅助排序。

可以使用以下语句检查排序语句是否使用了临时文件:
```sql
/* 打开 optimizer_trace，只对本线程有效 */
SET optimizer_trace='enabled=on'; 
 
/* @a 保存 Innodb_rows_read 的初始值 */
select VARIABLE_VALUE into @a from  performance_schema.session_status where variable_name = 'Innodb_rows_read';
 
/* 执行语句 */
select city, name,age from t where city='杭州' order by name limit 1000; 
 
/* 查看 OPTIMIZER_TRACE 输出 */
SELECT * FROM `information_schema`.`OPTIMIZER_TRACE`;
 
/* @b 保存 Innodb_rows_read 的当前值 */
select VARIABLE_VALUE into @b from performance_schema.session_status where variable_name = 'Innodb_rows_read';
 
/* 计算 Innodb_rows_read 差值 */
select @b-@a;
```

查看 OPTIMIZER_TRACE 输出中的`number_of_tmp_files`查看是否使用了临时文件，以及临时文件的数量。`number_of_tmp_files`为0表示排序可以直接在内存中完成。
```json
"filesort_summary": {
  "rows": 300003,
  "examined_rows": 300003,
  "number_of_tmp_files": 1191,
  "sort_buffer_size": 261328,
  "sort_mode": "<sort_key, packed_additional_fields>"
}
```
`sort_mode` 里面的`packed_additional_fields` 的意思是，排序过程对字符串做了“紧凑”处理。即使 name 字段的定义是 varchar(16)，在排序过程中还是要按照实际长度来分配空间的。

## rowid 排序

当排序查询返回的字段很多，sort_buffer中方的字段太多，这样内存里能够同时放下的行数很少，要分成很多个临时文件，排序的性能会很差，这时候MySQL就会使用新的算法，只将排序字段和主键id放入sort_buffer。排序之后再通过主键id回表查询要返回的结果。

**参数`max_length_for_sort_data`** 控制用于排序的行数据的长度的一个参数。如果单行的长度超过这个值，MySQL 就认为单行太大，要换一个算法。

select @b-@a 这个语句的值变成 5000 了。
因为这时候除了排序过程外，在排序完成后，还要根据 id 去原表取值。由于语句是 limit 1000，因此会多读 1000 行。
`sort_mode` 变成了 <sort_key, rowid>，表示参与排序的只有 name 和 id 这两个字段。

**全字段排序和rowid 排序体现了 MySQL 的一个设计思想：如果内存够，就要多利用内存，尽量减少磁盘访问。**


## 如何避免排序

可以创建一个 city 和 name 的联合索引，这样读取到的数据已经是有序的不需要再进行排序了。
```sql
alter table t add index city_user(city, name);
```

使用explain命令查看Extra字段中没有Using filesort 了，也就是不需要排序了。