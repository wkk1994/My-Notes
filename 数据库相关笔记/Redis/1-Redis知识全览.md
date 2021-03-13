# Redis知识全览

## Redis 知识全景图

Redis 知识全景图主要包含“两大维度，三大主线”。

![Redis 知识全景图](https://static001.geekbang.org/resource/image/79/e7/79da7093ed998a99d9abe91e610b74e7.jpg)

两大维度，就是指系统纬度和应用纬度。三大主线，也就是指高性能、高可靠和高可扩展（简称三高）。

从系统维度上说，需要了解 Redis 的各项关键技术的设计原理，这些能够为判断和推理问题打下坚实的基础。

三大主线的分类：

* 高性能主线：包括线程模型、数据结构、持久化、网络框架；
* 高可靠主线：包括主从复制、哨兵机制；
* 高可扩展主线：包括数据分片、负载均衡。

## Redis问题画像

![Redis问题画像](https://static001.geekbang.org/resource/image/70/b4/70a5bc1ddc9e3579a2fcb8a5d44118b4.jpeg)
