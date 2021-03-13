# nvarchar2和varchar区别
> 1.从使用角度来看区别在于：NVARCHAR2在计算长度时和字符集相关的<br>
> 2.varchar2是Oracle提供的独特的数据类型oracle保证在任何版本中该数据类型向上和向下兼容<br>
>3.nvarchar/nvarchar2适用于存放中文 <br>
>4.nvarchar2基本上等同于nvarchar，不同在于nvarchar2中存的英文字母也占两个字节
>5.nvarchar是使用自己的统一编码 再将数据插进表中，不存在更换编码出现问题