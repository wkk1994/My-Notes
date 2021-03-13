# dubbo学习笔记
> dubbo基础

1.dubbo简介

    dubbo是一个分布式服务框架 RPC

2.涉及的知识

    远程调用：RMI hassion webservice thrift
    通信交互：http mina netty
    序列化： hession2 java json 
    容器：jetty spring
    多线程：异步  线程池
    负载均衡：zookeeper

3.服务注册

> 生产者

    spring容器加载-->服务提供方配置加载-->主线程等待
                    dubbo容器启动  服务注册
                    

                     