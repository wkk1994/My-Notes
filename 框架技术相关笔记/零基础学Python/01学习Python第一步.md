# 学习Python第一步

## 安装python

从https://www.python.org/下载对应系统的版本，安装之后通过`python3 -V`验证是否安装成功

多版本python如何运行？

通过`python3.13 -V`可以指定执行Python的版本


## IDE

* pycharm: 重量级IDE，功能强大，适合一定经验的开发工作者
* vscode: 轻量级IDE，适合新手
* jupyter lab: 远程运行的网页ide，一般是数据科学家用来在GPU机器上，运行编写和运行代码

## 官方文档

https://docs.python.org/zh-cn/3/

标准库参考，可以查看内置的库支持，避免重复造轮子。

## 交互执行和非交互执行

* 非交互执行：通过`python3 hello.py`可以直接运行python代码，运行完结果就结束
* 交互执行：通过`python3 -i hello.py`可以进入交互运行，运行完hello.py后，还可以持续输入代码运行

```text
xujinxiu@xujinxiudeMBP python-test % python3 hello.py
hello world!
xujinxiu@xujinxiudeMBP python-test % python3 -i hello.py
hello world!
>>> print("11")
11
>>> print(1+1)
2
>>> 
```