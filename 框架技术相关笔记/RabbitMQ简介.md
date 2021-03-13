[toc]

# RabbitMQ

## AMQP协议

Advanced Message Queuing Protocol,高级消息队列协议，提供统一消息服务的应用层标准。是应用层协议的一个开放标准,为面向消息的中间件设计。消息中间件存在的意义在于组件之间的解耦，发送方不需要知道使用者的存在，只负责发送消息，对于使用者也是如此。AMQP的主要特征是面向消息、队列、路由（包括点对点和发布/订阅）、可靠性、安全。

## 简介

RabbitMQ是一个开源的AMQP协议实现，使用Erlang编写。

## 基本概念

### vhost（虚拟主机）

虚拟主机：一个虚拟主机持有一组交换机、队列和绑定。虚拟主机的作用在于进行权限管控，rabbitmq默认有一个虚拟主机"/"。

* `rabbitmqctl add_vhost` 添加虚拟主机

### Queue

队列，用于存储消息。

### Message acknowledgment（消息确认）

实际应用中，消费者消费消息时，可能出现宕机，导致消息消费失败而丢失。为了避免这种情况，可以在消息消费完成后发送一个确认消息给RabbitMQ，RabbitMQ收到消息后，将消息从Queue中删除。

消息确认的模式：

* AcknowledgeMode.NONE：自动确认
自动确认，会在消息发送给消费者后立即确认，如果消费者执行抛出异常，相当于消费者没有成功处理消息，但是消息会丢失。

* AcknowledgeMode.AUTO：根据情况确认
按情况确认，会根据方法的执行情况来决定是否确认还是拒绝（是否重新入queue）
  
  * 如果消息成功被消费（成功的意思是在消费的过程中没有抛出异常），则自动确认
  * 当抛出 AmqpRejectAndDontRequeueException 异常的时候，则消息会被拒绝，且 requeue = false（不重新入队列）
  * 当抛出 ImmediateAcknowledgeAmqpException 异常，则消费者会被确认
  * 其他的异常，则消息会被拒绝，且 requeue = true（如果此时只有一个消费者监听该队列，则有发生死循环的风险）。可以通过 setDefaultRequeueRejected（默认是true）去设置 

* AcknowledgeMode.MANUAL：手动确认
动确认则当消费者调用 ack、nack、reject 几种方法进行确认，手动确认可以在业务失败后进行一些操作，如果消息未被 ACK 则会发送到下一个消费者

#### 消息确认处理

* 方式一

    * 设置消息确认模式

    ```yml
    spring:
      rabbitmq:
        listener:
          simple:
            acknowledge-mode: manual #消息手动确认
    ```

    * 消息监听

    ```java
    @RabbitListener(queues = "direct_queue")
        public void directQueueListener(Channel channel, Message message){
            String body = new String(message.getBody());
            System.out.println("监听到消息："+body);
            try {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    ```

* 方式二

```java
public RabbitListenerContainerFactory<?> simpleRabbitListenerContainerFactory(ConnectionFactory connectionFactory){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);//NONE 自动确认 MANUAL手动确认
        return factory;
}
// 消息监听需要 指定listener监听时的connection
@RabbitListener(queues = "direct_queue", containerFactory = "simpleRabbitListenerContainerFactory")
    public void directQueueListener(Channel channel, Message message){
        String body = new String(message.getBody());
        System.out.println("监听到消息："+body);
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
}
```

* channel.basicAck(deliveryTag,multiple) 消息确认，只有执行成功才可以确认，否则消息会丢失
    * deliveryTag：条消息的唯一标识 ID，是一个单调递增的正整数，delivery tag 的范围仅限于 Channel
    * multiple：批量确认标志，为true表示一次性确认 deliveryTag 小于等于传入值的所有消息
* channel.basicReject(deliveryTag,multiple) 消息确认为死信，消息会被丢弃，不会重回队列
* channel.basicNack(deliveryTag,multiple,requeue); 拒绝消息，requeue为true会重新进入队列，不会丢弃

### 消息发送确认

#### ConfirmCallback

通过实现 ConfirmCallback 接口，消息发送到 Broker 后触发回调，确认消息是否到达 Broker 服务器，也就是只确认是否正确到达 Exchange 中，连接不上服务器会直接抛出错误不会发送确认

* 开启publisher-confirms
```yml
spring:
  rabbitmq:
    publisher-confirms: true
```

或

```java
connectionFactory.setPublisherConfirms(true);
```

* 实现RabbitTemplate.ConfirmCallback

```java
@Component
public class CustomConfirmCallback implements RabbitTemplate.ConfirmCallback{

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        System.out.println("消息唯一标识："+correlationData);
        System.out.println("确认结果："+ack);
        System.out.println("失败原因："+cause);
    }
}
```

* 指定 ConfirmCallback

```java
rabbitTemplate.setConfirmCallback(customConfirmCallback);
```

#### ReturnCallback

通过实现 ReturnCallback 接口，消息失败返回，比如路由不到队列时触发回调

* 开启publisher-returns

```yml
spring:
  rabbitmq:
    publisher-returns: true
```
或

```java
connectionFactory.setPublisherReturns(true);
```
* 实现RabbitTemplate.ReturnCallback

```java
@Component
public class CustomReturnCallback implements RabbitTemplate.ReturnCallback{

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("消息主体 message : "+message);
        System.out.println("消息主体 message : "+replyCode);
        System.out.println("描述："+replyText);
        System.out.println("消息使用的交换器 exchange : "+exchange);
        System.out.println("消息使用的路由键 routing : "+routingKey);
    }
}
```

* 指定 ReturnCallback

```java
rabbitTemplate.setReturnCallback(customReturnCallback);
```

### Message durability（消息持久化）

如果希望RabbitMQ重启后，消息仍不丢失，可以在创建队列的时候选择消息持久化。但是仍然会存在小概率丢失事件（比如RabbitMQ服务器已经接收到生产者的消息，但还没来得及持久化该消息时RabbitMQ服务器就断电了），可以通过事务将这些小概率事件管理起来，**但是事务会影响RabbitMQ的QPS**

### Prefetch count

允许为每个consumer指定最大的unacked messages数目。指定每个consumer从Rabbit一次从中获取多少条消息缓存在client，缓存区满了，Rabbit将停止发送新的message到该consumer中。同时该队列的unacked就为缓存中的message数量。

### Exchange（交换机）

交换机，在实际使用中消息都会先发送到Exchange，再由Exchange路由到一个或者多个Queue

#### 交换机类型

* fanout
意为广播，会将Exchange上的消息路由到所有与它绑定的Queue上。

* direct
将消息路由到那些binding key与routing key完全匹配的Queue中。

* topic
意为主题，将消息路由到与binding key相匹配的routing key的Queue中。这里的匹配是模糊匹配。匹配规则与约定：

    * routing key为一个句点号“ . ”分隔的字符串，如stock.usd.nyse，quick.orange.rabbit
    * binding key与routing key一样也是句点号“ . ”分隔的字符串
    * binding key中可以存在两种特殊字符“ * ”与“#”，用于做模糊匹配，其中“ * ”用于匹配一个单词，“#”用于匹配多个单词（可以是零个） 
    * 虽然一个Queue上的多个binding key相匹配，但是只会给这个Queue发送一次消息。
* headers
headers类型的Exchange不依赖于routing key与binding key的匹配规则来路由消息，而是根据发送的消息内容中的headers属性进行匹配。 在绑定Queue与Exchange时指定一组键值对；当消息发送到Exchange时，RabbitMQ会取到该消息的headers（也是一个键值对的形式），对比其中的键值对是否完全匹配Queue与Exchange绑定时指定的键值对；如果完全匹配则消息会路由到该Queue，否则不会路由到该Queue

### routing key（路由key）

消息发送到Exchange中，都会指定一个routing key，Exchange根据routing key再将消息路由到Queue

### Binding

将Queue与Exchange关联起来，需要指定binding key。这样Exchange会根据routing key与binding key的关系将消息进行路由。（有些类型的交换机不需要指定routing key，如headers）

### RPC

MQ属于异步的消息处理，如果需要等待服务器处理完成，再进行接下来的业务处理，就需要RPC（Remote Procedure Call，远程过程调用）。

#### 同步RPC

从版本3.4.0开始，RabbitMQ服务器现在支持直接回复。直接回复是同步的会阻塞后面的请求。

* 发送消息，等待返回

```java
@Test
public void sendSyncRpcMessage(){
    String result = (String)rabbitTemplate.convertSendAndReceive("rpc_sync_queue", "sync消息");
    System.out.println("响应： "+result);

}
```

* 监听消息并返回

```java
@RabbitListener(queues = "rpc_sync_queue")
public String rpcSyncQueueListener(Channel channel, Message message){
    String body = new String(message.getBody());
    System.out.println("rpc_sync_queue监听到消息："+body);
    int millis = (int) (Math.random() * 2 * 1000);
    try {
        Thread.sleep(millis);
    } catch (InterruptedException e) {
    }
    return body + " sleep for " + millis + " ms";
}

```

#### 异步RPC

* 客户端发送请求时，在消息中定义2个属性：reply_to，设置为回调队列，correlation_id，设置为每个请求的唯一值。
* 服务器端收到消息并处理完成后，将产生一条应答消息到reply_to指定的Queue上，同时带上correlation_id属性。
* 客户端之前已订阅replyTo 指定的Queue ，从中收到服务器的应答消息后，根据其中的correlationId 属性分析哪条请求被执行了，根据执行结果进行后续业务处理

**需要自己去判断replyTo转发到replyTo对应的queue？？**

### Spring Cloud中RabbitMQ配置属性表

https://blog.csdn.net/en_joker/article/details/80103519

### 延迟队列的实现方式

#### 一个队列上的延迟时间相同的实现（方式一）

实现原理是将延迟消息先发送到一个临时队列（死信队列），当到**队首消息**达到过期时间时会将消息确认为死信，根据临时队列的`x-dead-letter-exchange` `x-dead-letter-routing-key`将消息转发到处理队列上去。

* 延迟队列定义：

```java
    /**
     * 公示结束通知延迟队列--专家
     * @return
     */
    @Bean
    public Queue expertPublicityDelayEndQueue() {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-dead-letter-exchange", SystemConstant.PUBLICITY_END_EXCHANGE);
        args.put("x-dead-letter-routing-key", SystemConstant.PUBLICITY_END_ROUTE_KEY_EXPERT);
        return new Queue(SystemConstant.PUBLICITY_DELAY_END_QUEUE_EXPERT, true, false, false, args);
    }

    /**
     * 公示结束通知延迟队列和交换机绑定--专家
     * @return
     */
    @Bean
    Binding bindingPublicityDelayEndQueue() {
        return BindingBuilder.bind(expertPublicityDelayEndQueue()).to(publicityDelayEndExchange())
                .with(SystemConstant.PUBLICITY_DELAY_END_ROUTE_KEY_EXPERT);
    }

    /**
     * 公示结束交换机
     * @return
     */
    @Bean
    DirectExchange publicityEndExecuteExchange() {
        return new DirectExchange(SystemConstant.PUBLICITY_END_EXCHANGE);
    }

    /**
     *  公示结束队列--专家
     * @return
     */
    @Bean
    public Queue expertPublicityEndQueue() {
        return new Queue(SystemConstant.PUBLICITY_END_QUEUE_EXPERT, true, false, false);
    }

    /**
     * 公示结束队列和交换机绑定
     * @return
     */
    @Bean
    Binding bindingPublicityEndQueue() {
        return BindingBuilder.bind(expertPublicityEndQueue()).to(publicityEndExecuteExchange())
                .with(SystemConstant.PUBLICITY_END_ROUTE_KEY_EXPERT);
    }
```

* 过期时间定义
在定义队列的时候添加属性`x-message-ttl`指定全部消息的过期时间，或者
发送消息时指定单个消息的过期时间`expiration`。同时指定时以最小的为准。

**注意：** 使用这样的延迟队列实现方式，队列只会轮询队首的消息，当队首的消息到达过期时间才会确认为死信，接着轮询下一个消息。即使队首的消息没到过期时间，其他消息到达过期时间也不会被确认死信。

#### 使用rabbitmq_delayed_message_exchange插件实现延迟队列（方式二）

[参考](https://blog.csdn.net/liyongbing1122/article/details/81225761)

优点：可以消息可以指定任意延迟时间，没有上述方式的局限性。  
缺点：这个插件是实验性的，认识到其局限性就可以用于生产（github）。我不选择的原因，不能直观查看到当前有多少个延迟消息，exchanage上看不到？？？

**注意：** 这个解决方式有个bug，当使用ReturnCallback时，会一直返回信息NO_ROUTE

#### 任意延迟时间我的解决方式（方式三）

由于延迟消息数量并不多，每次发送延迟消息会新建一个延迟删除队列（通过设置`x-expires`值实现），以此解决方式一的局限性。
当数据量比较大的时候，可以通过定时轮询数据库，将最接近需要延迟的数据取出，再新建一个延迟删除队列。

#### 探寻最好的解决方式？？？

## 参考资料

- [spring-amqp-rpc](https://github.com/cbwleft/spring-amqp-rpc)
- [我为什么要选择RabbitMQ ，RabbitMQ简介，各种MQ选型对比](https://www.sojson.com/blog/48.html)
- [RabbitMQ：消息发送确认 与 消息接收确认（ACK）](https://www.jianshu.com/p/2c5eebfd0e95)
- [Java 使用RabbitMQ插件实现延时队列](https://blog.csdn.net/liyongbing1122/article/details/81225761)

## 单词

RabbitMQ