# 第 25 课 分布式消息--Kafka消息中间件

## 1.Kafka概念和入门

### 什么是Kafka

Kafka是一种分布式、基于发布/订阅的消息系统。主要设计目的如下：

* 以时间复杂度O(1)的方式提供消息的持久化能力，即使对TB以上的数据也能保证常数时间复杂度的访问性能。
* 高吞吐量，即使在非常廉价的商用机器上也能做到单机支持每秒 100K 条以上消息 的传输。
* 支持 Kafka Server 间的消息分区及分布式消费，同时保证每个 Partition 内的消 息顺序传输。
* 同时支持离线数据处理和实时数据处理。
* Scale out：支持在线水平扩展。

### Kafka的基本概念

* Broker：Kafka集群中包含一个或多个服务器，每个服务器称为Broker，可以看做是代理。
* Topic：每条发布到 Kafka 集群的消息都有一个类别，这个类别被称为 Topic。 （物理上不同 Topic 的消息分开存储，逻辑上一个 Topic 的消息虽然保存于一个或 多个 broker 上，但用户只需指定消息的 Topic 即可生产或消费数据而不必关心数 据存于何处）。
* Partition：Partition是物理上的概念，每个Topic可以包含一个或多个Patition。
* Producer：负责发送消息到Kafka broker。
* Consumer：消息消费者，向Kafka broker读取消息客户端。
* Consumer Group：每个Consumer属于一个特定的Consumer Group（可为每个 Consumer 指定 group name，若不指定 group name 则属于默认的 group）。对于一个Consumer Group内的Consumer来说Topic相当于Queue。

### 单机部署结构

![Kafka单机部署结构](https://note.youdao.com/yws/api/personal/file/WEB701ae3aaa7f2379e3cef74ef0d5c164f?method=download&shareKey=5fbed49d193f9bb575c3fd9ee369d156)

### 集群部署结构

![Kafka集群部署结构](https://note.youdao.com/yws/api/personal/file/WEBdcfa383d64d80c20cdc032293a8dcba1?method=download&shareKey=40bf08334b76250a6699244437056201)

### Topic和Partition

多Partition支持水平扩展和并行处理，顺序写入提升吞吐性能。

一个消息会发送到不同的Partition上，发送到哪个Partition是由客户端完成的。

> 客户端怎么知道发送到哪个Partition上？
> 单个机器上，不建议有大量的partition，也不建议有大量的topic，因为它们对应着一个个数据文件，都集中在单个机器上，可能形成随机IO。
> 如果消息量很大，partition也可以参考消费节点的数量。

### Partition和Replica

多副本可以避免单节点故障的时候partition的数据丢失。每个partition可以通过副本因子添加多个副本。在创建Topic的时候不仅可以指定Partition数量，还可以指定replica数量。多个副本只有一个是leader。

> 多副本的写入规则是什么？会影响partition的性能吗？
> Isr: In-Sync Replica，同步副本，状态和主副本一样。

### Topic特性

* topic是逻辑的概念，partition是才是物理概念，可以增加扩展性和性能。
* 通过顺序写入达到高吞吐。
* 多副本增加容错性。

> **在线添加partition，会造成kafka的性能抖动。**

> 因为一个Topic可以有多个partition，所以Kafka只能保证单个partition内消息的顺序性，不能保证多个partition的顺序性，如果一定要保证Topic内消息的顺序性，可以将Topic设置为只有一个partition。

## 2.Kafka的简单使用*

### 单机安装部署

### 单机部署测试

### Kafka自带的压力测试工具

* kafka-producer-perf-test.sh
  生产者压测脚本，常用参数：
  * --topic topicName：指定测试的topic名称。
  * --num-records 1000：发送的消息数量。
  * --throughput 1000：吞吐量限制，-1表示不限制。
  * --producer-props propsName=value：kafka相关配置，可以配置多个，如--producer-props bootstrap.servers=localhost:9002。
  * --producer.config configFile：指定kafka相关的配置文件地址。
  * --print-metrics：测试结束时打印指标信息，默认false。
  * --record-size 1000：一个消息的大小，--record-size和--payload-file必须有一个。
  * --payload-file filePath：消息内容的文件。
  
  使用示例：bin/kafka-producer-perf-test.sh --topic test1 --num-records 10000000 --record-size 1000 --throughput -1 --producer-props bootstrap.servers=localhost:9001,localhost:9002,localhost:9003

* kafka-consumer-perf-test.sh
  消费者压测脚本，常用参数：
  * --topic topicName：指定测试的topic名称。
  * --bootstrap-server localhost:9002：服务器地址，多个,分隔。
  * --consumer.config fileName：消费端配置文件地址。
  * --fetch-size 1048576：获取数据的大小，默认1048576，有疑问？是消息的大小还是消息的数量？
  * --from-latest：从最新的消息开始获取消息。
  * --group perf-consumer-19126：消费组id，默认perf-consumer-19126。
  * --messages 10000：要消费的消息数量。
  * --num-fetch-threads 1：获取消息的线程数量，已经被弃用并忽略，默认1。
  * --print-metrics：测试结束时打印指标信息。
  * --threads 10：处理消息的线程数量，已经被弃用并忽略，默认10。

  使用示例：bin/kafka-consumer-perf-test.sh --bootstrap-server localhost:9002,localhost:9001,localhost:9003 --topic test1 --fetch-size 1048576 --messages 100000 --threads 10| jq -R .|jq -sr 'map(./",")|transpose|map(join(": "))[]'

### Java中使用Kafka发送接收消息

Kafka是重客户端，各种拉取消息的相关控制都由客户端控制，Broker比较轻薄，只是记录消息的内容以及消息的位置等，这也是Kafka性能比较高的原因。

## 3.Kafka的集群配置*

### 集群安装部署

### 集群与多副本的说明

* Isr：In-Sync Replica，同步副本，状态和主副本一样。
* Rebalance：broker和consumer group的rebalance
* 热点分区：需要重新平衡。

## 4.Kafka的高级特性*

### 生产者-执行步骤

客户端发送消息需要实现序列化、分区、压缩操作。

* 序列化：消息在发送前进行本地序列化。
* 寻址：客户端需要确定消息发送到哪个partition上。
* 压缩：压缩消息。
* 发送消息：消息的发送可能会在本地进行批量发送的优化，这就涉及到吞吐和延迟的平衡。等待其他消息一起发送会提高延迟。

### 生产者-确认模式

通过确认模式可以在可靠性和发送消息的性能之间做权衡，一共有三种确认模式：

* ack=0：只发送不管有没有写入到broker。
* ack=1：写入到leader就认为写入成功。
* ack=-1/all：写入最小的副本数则认为写入成功，这样消息是最可靠的。

> 在实际生产中，可以指定少于指定的副本就禁止复制数据，停止接收消息，这样防止副本数太少，leader宕机时出现脑裂的情况。

### 生产者特性-同步发送

同步发送：

```java
KafkaProducer kafkaProducer = new KafkaProducer(pro);
ProducerRecord record = new ProducerRecord("topic", "key", "value");
Future future = kafkaProducer.send(record);
//同步发送方法1
Object o = future.get();
//同步发送方法2
kafkaProducer.flush();
```

### 生产者特性-异步发送

kafka客户端支持异步发送，在发送方法中添加回调函数，这样出错可以异步通知。异步发送可以指定发送数量的大小，这样达到这个数量就会发送一批，但是这样操作，如果达不到发送要求，其他消息就会一直等待。

```java
pro.put("linger.ms", "1");
pro.put("batch.size", "10240");
KafkaProducer kafkaProducer = new KafkaProducer(pro);
ProducerRecord record = new ProducerRecord("topic", "key", "value");
Future future = kafkaProducer.send(record);
//异步发送方法1
kafkaProducer.send(record, (metadata, exception) -> {
    if (exception == null) System.out.println("record = " + record); 
});
//异步发送方法2
kafkaProducer.send(record);
```

### 生产者特性-顺序保证

如何保证生产者发送消息的顺序性？

```java
// 这个参数设置为1，那么在生产者发送第一批消息时，不会有其他消息发送给broker，这样会影响生产者的吞吐量。
pro.put("max.in.flight.requests.per.connection", "1");

// 同步发送，保证发送的消息确定发送成功了。
kafkaProducer.send(record);
kafkaProducer.flush();
```

### 生产者特性-消息可靠性传递

**什么是消息的事务？**

对于生产者来说，消息的事务就是生产者保证在一个事务中发送的一批消息，要么全部发送成功，要么全部发送失败。
对于消费者来说，消息的事务就是消费者保证在一个事务中消费的一批消息，要么全部消费完成，要么全部未消费。即在消费消息的过程中，有部分消息没有确认消费，那么下次消费还会从事务开始的消息消费，不会从上次中断的消息开始消费。

```java
// enable.idempotence开始消息的幂等性，防止发送消息因网络抖动重试的时候消息重复。
// 这个设置默认会将acks设置为all
pro.put("enable.idempotence","true");
// 设置事务标志
pro.put("transaction.id","tx0001");
try {
    // 开启事务，这样接下来的消息发送都在一个事务中
    kafkaProducer.beginTransaction();
    ProducerRecord record = new ProducerRecord("topic", "key", "value");
    for (int i = 0; i < 100; i++) {
        kafkaProducer.send(record, (metadata, exception) -> { 
            if (exception != null) { 
                kafkaProducer.abortTransaction(); throw new KafkaException(exception.getMessage() + " , data: " + record); 
            } 
        });
    }
    kafkaProducer.commitTransaction(); 
} catch (Throwable e) { 
    kafkaProducer.abortTransaction();
}
```

> 事务消费者只能看到提交的消息，需要配置。

### 消费组-Consumer Group

**Consumer Group的定义**

一个Consumer Group内可以有多个Consumer实例。每个Consumer Group都有一个Group Id标识。
Consumer Group订阅的主题内的partition只能分给组内的某个Customer实例。
这个分区也可以被其他Customer Group消费。

虽然Kafka只支持发布/订阅的消息模型，但是可以通过Consumer Group这种特性实现点对点的消息模型或发布/订阅的消息模型。如果所有的Consumer都属于同一个Group，那么它实现的就是消息队列模型；如果所有Consumer实例分别属于不同的 Group，那么它实现的就是发布/订阅模型。

消费者组和Partition的关系：

![ConsumerGroup和Partition的关系](https://note.youdao.com/yws/api/personal/file/WEBcc240e74d496d00088c4b1c7c74ec6fb?method=download&shareKey=24a01d88f8f096cd23f5f463afc9c725)

**Rebalance**

Rebalance规定了一个Consumer Group下的所有Consumer如何达成一致，来分配订阅的Topic的每个分区。

**Rebalance触发的条件：**

* 组内成员发生变更。比如有新的Consumer加入或离开组，或者Consumer实例崩溃被踢出组。
* 订阅主题数发生变更。Consumer Group 可以使用正则表达式的方式订阅主题，比如 consumer.subscribe(Pattern.compile("t.*c")) 就表明该 Group 订阅所有以字母 t 开头、字母 c 结尾的主题。在 Consumer Group 的运行过程中，你新创建了一个满足这样条件的主题，那么该 Group 就会发生 Rebalance。
* 订阅主题的分区数发生变更。当分区数增加时，就会触发订阅该主题的所有 Group 开启 Rebalance。

**Rebalance过程对Consumer Group 消费过程有极大的影响，会停止Consumer Group消费消息，一个Consumer Group中的Consumer如果过多，那么Rebalance会很耗时。如果分配倾斜会出现闲的闲死，忙的忙死。目前社区对Rebalance无能无力，最好的方案是尽量避免Rebalance的发生。**

> **理想情况下，Consumer实例的数量应该等于该Group订阅主题的分区总数。**

### 消费者特性-Offset

offset（位移）是Kafka中用来记录消费者消费过程中的消息位置信息，也就是当前消费者消费消息到哪里了。

在老版本的Consumer Group中把Offset保存到Zookeeper中；新版本将Offset保存到Kafka内部的Topic中（__consumer_offsets）。

**为什么移动Offset保存的位置？**

ZooKeeper 这类元框架其实并不适合进行频繁的写更新，而 Consumer Group 的位移更新却是一个非常频繁的操作。这种大吞吐量的写操作会极大地拖慢 ZooKeeper 集群的性能，因此将Consumer的Offset保存到Zookeeper中是不合适的做法，所以新版本重新设计了Consumer Offset的存储位置。

#### Offset同步提交

Offset的保存操作可以调用接口进行同步提交。

```java
props.put("enable.auto.commit","false");
while (true) {
    //拉取数据
    ConsumerRecords poll = consumer.poll(Duration.ofMillis(100));
    poll.forEach(o -> {
        ConsumerRecord<String, String> record = (ConsumerRecord) o;
        Order order = JSON.parseObject(record.value(), Order.class);
        System.out.println("order = " + order);
        });
    consumer.commitSync();
}
```

#### Offset异步提交

```java
props.put("enable.auto.commit","false");
while (true) {
    //拉取数据
    ConsumerRecords poll = consumer.poll(Duration.ofMillis(100));
    poll.forEach(o -> {
        ConsumerRecord<String, String> record = (ConsumerRecord) o;
        Order order = JSON.parseObject(record.value(), Order.class);
        System.out.println("order = " + order);
    });
    consumer.commitAsync();
}
```

#### Offset自动提交

```java
props.put("enable.auto.commit","true");
props.put("auto.commit.interval.ms","5000");
while (true) {
    //拉取数据
    ConsumerRecords poll = consumer.poll(Duration.ofMillis(100));
    poll.forEach(o -> {
        ConsumerRecord<String, String> record = (ConsumerRecord) o;
        Order order = JSON.parseObject(record.value(), Order.class);
        System.out.println("order = " + order);
    });
}
```

#### Offset Seek

可以在订阅topic的时候，添加一个监听器，用来做offset的自定义保存，将offset保存到mysql等其他存储中，这样能做到offset的找回，在需要的时候重设offset。

方式一：

```java
props.put("enable.auto.commit","true");
//订阅topic
consumer.subscribe(Arrays.asList("demo-source"), new ConsumerRebalanceListener() {
    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        //实现自定义的保存offset的方式
        commitOffsetToDB();
    }
    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) { 
        partitions.forEach(topicPartition -> consumer.seek(topicPartition, getOffsetFromDB(topicPartition)));
    }
});

```

方式二：

```java
while (true) {
    //拉取数据
    ConsumerRecords poll = consumer.poll(Duration.ofMillis(100));
    poll.forEach(o -> { 
        ConsumerRecord<String, String> record = (ConsumerRecord) o;
        processRecord(record);
        saveRecordAndOffsetInDB(record, record.offset()); 
    });
}
```

## Kafka集群建议

### Kafka集群的建议

### 集群监控

## Tips

* broker、proxy、agent都是代理但是具体含义有差异。
* log4j都有参数可以设置日志刷到盘中的策略。
* MapStruct，解决对象与对象之间进行转换，自动生成set方法进行转换，[官网](https://mapstruct.org/)。
