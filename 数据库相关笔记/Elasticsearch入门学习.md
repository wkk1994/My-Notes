[TOC]

# Elasticsearch学习

一个基于Lucene的搜索服务器

## 1.基础概念

* index（索引）：含有相同属性的文档集合（相当于数据库）
* type（类型）：索引可以定义一个或多个类型，文档必须属于一个类型（相当于表）
* document（文档）：文档是可以被索引的基本数据单位（相当于数据）
* 分片： 每个索引都有多个分片，每个分片是一个Lucene索引，最多可以存储20亿条数据，最大不要超过50G，在10到30G之间最好
* 备份： 拷贝一份分片就完成了分片的备份
* 6.0.0版本以后只支持单个index下只有一个type，[原因](https://elasticsearch.cn/m/article/337)。
* ES不是可靠的存储系统，有丢数据风险
* ES不是实时系统，数据写入和删除不会立即完成，而是先写入trans log，后续通过后台线程将内存中的存入存储引擎，默认后台线程1秒执行一次，可以设置。对大数据存储的时候可以先设置为-1（不执行），写入完成后再设置为1，这样可以提高写入速度
* Spring使用ES[参考](https://blog.csdn.net/antao592/article/details/52872854)

## 2.索引原理分析[参考](https://www.cnblogs.com/dreamroute/p/8484457.html)

## 3.查询[参考](http://www.cnblogs.com/xing901022/p/5994210.html)
1.bool查询
  
    包括四个子查询must mustnot filter should

- must 必须满足must子句条件，参与计算分值
- mustnot 必须不满足mustnot子句条件
- filter 必须满足filter子句条件，但是不计算分值
- should 可能满足should子句条件，minimum_should_match参数定义了至少满足几个子句。
- [其他参考](https://www.jianshu.com/p/6333940621ec)

## 4.ELK总体架构
- E:ES
- L:Logstash
    
    Logstash是开源的服务器端数据处理管道，能够同时从多个来源采集数据、格式化数据，然后将数据发送到es进行存储。

- K:Kibana

    Kibana能够可视化Elasticsearch 中的数据并操作。

- B:Beat

    基于go语言写的轻量型数据采集器，读取数据，迅速发送到Logstash进行解析，亦或直接发送到Elasticsearch进行集中式存储和分析。

## 5.与Spring集成
* 1.spring.xml配置
```xml
<!-- ElasticSearch配置 -->
<bean id="transportClient"class="cn.sh.sstic.common.elastic.TransportClientFactoryBean">
	<property name="clusterName" value="test-master"/>
	<property name="clusterNodes" value="127.0.0.1:9300"/>
</bean>
```

* 2.FactoryBean
```java
public class TransportClientFactoryBean implements FactoryBean<TransportClient>, InitializingBean, DisposableBean {

    private String clusterNodes = "127.0.0.1:9300";//es地址
    private String clusterName = "elasticsearch";//集群名称
    private Boolean clientTransportSniff = true;//为true来使客户端去嗅探整个集群的状态
    private Boolean clientIgnoreClusterName = Boolean.FALSE;//设置为true来忽略集群名称验证连接的节点
    private String clientPingTimeout = "5s";//等待节点ping返回的时间，默认为5秒
    private String clientNodesSamplerInterval = "5s";//检验已连接节点列表的活跃性的间隔时间，默认为5秒
    private TransportClient client;
    static final String COLON = ":";
    static final String COMMA = ",";

    @Override
    public void afterPropertiesSet() throws Exception {
        buildClient();
    }

    private void buildClient() throws UnknownHostException {
        client = new PreBuiltTransportClient(settings());
        for (String clusterNode : clusterNodes.split(COMMA)) {
            String hostName = substringBeforeLast(clusterNode, COLON);
            String port = substringAfterLast(clusterNode, COLON);
            client.addTransportAddress(new TransportAddress(
            InetAddress.getByName(hostName), Integer.valueOf(port)));
        }
    }

    private Settings settings() {
        return Settings.builder().put("cluster.name", clusterName)
                .put("client.transport.sniff", clientTransportSniff)
                .put("client.transport.ignore_cluster_name", clientIgnoreClusterName)
                .put("client.transport.ping_timeout", clientPingTimeout)
                .put("client.transport.nodes_sampler_interval", clientNodesSamplerInterval)
                .build();
    }

    @Override
    public void destroy() throws Exception {
        if (client != null) {
            client.close();
        }
    }

    @Override
    public TransportClient getObject() throws Exception {
        return client;
    }

    @Override
    public Class<?> getObjectType() {
        return TransportClient.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    // get set ...

}
```
* 3.通过注入TransportClient操作ES [参考](https://www.elastic.co/guide/en/elasticsearch/client/java-api/6.4/java-docs.html)