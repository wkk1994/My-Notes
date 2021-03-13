# 第七章 访问Web资源

## 使用RestTemplate访问Web资源

* 代码位置：webresources/resttemplate-demo

SpringBoot中没有自动配置RestTemplate，可以通过SpringBoot中提供的RestTemplateBuilder.build()创建一个RestTemplate。

**常用方法**

* GET 请求
  * getForObject() / getForEntity()
* POST 请求
  * postForObject() / postForEntity()
* PUT 请求
  * put()
* DELETE 请求
  * delete()

**构造URI**

除了手动指定uri的String地址，SpringBoot提供了对应的API，可以用来构造一个uri对象。

* UriComponentsBuilder 构造URI

  示例：
  
  ```java
    URI uri = UriComponentsBuilder.fromUriString("http://example.com/hotels/{hotel}")
        .queryParam("q", "{q}")
        .encode()
        .buildAndExpand("Westin", "123")
        .toUri();
    URI uri = UriComponentsBuilder.fromUriString("http://example.com/hotels/{hotel}?1={q}")
        .build("Westin", "123");
  ```

* ServletUriComponentsBuilder 基于Request请求构造URI

    ```java
    public class ServletUriComponentsBuilder extends UriComponentsBuilder {
        // ...
        public static ServletUriComponentsBuilder fromServletMapping(HttpServletRequest request) {
        ServletUriComponentsBuilder builder = fromContextPath(request);
        if (StringUtils.hasText(new UrlPathHelper().getPathWithinServletMapping(request))) {
          builder.path(request.getServletPath());
        }
        return builder;
      }
        // ...
    }
    ```

* MvcUriComponentsBuilder 通过指向Spring MVC控制器上的{@RequestMapping}方法，创建{@UriComponentsBuilder}的实例

    示例：

    ```java
    UriComponents uriComponents = MvcUriComponentsBuilder.fromMethodCall(on(BookingController.class).getBooking(21)).buildAndExpand(42);
    URI uri = uriComponents.encode().toUri();
    ```

**类型转换**

类型转换可以通过实现JsonSerializer / JsonDeserializer，并添加@JsonComponent注解完成

**解析泛型对象**

对于返回的对象是List时，`List<Coffee> list = restTemplate.getForEntity(uri, List.class)`不能使用List.class作为返回，因为在运行时List<Coffee>等同于List<Object>，所以请求不知道准确的响应是哪个而产生异常转换错误。

可以通过ParameterizedTypeReference<T>实现List结果的返回。

示例：

```java
URI uri = UriComponentsBuilder.fromHttpUrl("http://127.0.0.1:8080/coffee/").build().toUri();
ResponseEntity<List<Coffee>> exchange = restTemplate.exchange(uri, HttpMethod.GET, null, ptr);
log.info("exchange code : {}", exchange.getStatusCode());
exchange.getBody().forEach(entity -> {
  log.info("entity : {}", entity);
});
```

## RestTemplate支持的Http库

* 代码位置：webresources/resttemplate-demo

* 通用接口
  * ClientHttpRequestFactory
* 默认实现
  * SimpleClientHttpRequestFactory
* Apache HttpComponents
  * HttpComponentsClientHttpRequestFactory 现在改名为HttpClinet
* Netty
  * Netty4ClientHttpRequestFactory
* OkHttp
  * OkHttp3ClientHttpRequestFactory

默认的RestTemplateBuilder.build()方法会在构建RequestFactory时检查是否存在其他RequestFactory，不存在时就默认构建一个SimpleClientHttpRequestFactory

org.springframework.boot.web.client.RestTemplateBuilder#buildRequestFactory

```java
 /**
  * Build a new {@link ClientHttpRequestFactory} instance using the settings of this
  * builder.
  * @return a {@link ClientHttpRequestFactory} or {@code null}
  * @since 2.2.0
  */
  public ClientHttpRequestFactory buildRequestFactory() {
    ClientHttpRequestFactory requestFactory = null;
    if (this.requestFactory != null) {
      requestFactory = this.requestFactory.get();
    }
    else if (this.detectRequestFactory) {
      requestFactory = new ClientHttpRequestFactorySupplier().get();
    }
    if (requestFactory != null) {
      if (this.requestFactoryCustomizer != null) {
        this.requestFactoryCustomizer.accept(requestFactory);
      }
    }
    return requestFactory;
  }
```

org.springframework.boot.web.client.ClientHttpRequestFactorySupplier#get

```java
  @Override
  public ClientHttpRequestFactory get() {
    for (Map.Entry<String, String> candidate : REQUEST_FACTORY_CANDIDATES.entrySet()) {
      ClassLoader classLoader = getClass().getClassLoader();
      if (ClassUtils.isPresent(candidate.getKey(), classLoader)) {
        Class<?> factoryClass = ClassUtils.resolveClassName(candidate.getValue(), classLoader);
        return (ClientHttpRequestFactory) BeanUtils.instantiateClass(factoryClass);
      }
    }
    return new SimpleClientHttpRequestFactory();
  }
```

## 优化底层请求策略

### 连接管理

#### PoolingHttpClientConnectionManager

PoolingHttpClientConnectionManager是apache.http提供的连接管理，常用属性：

* timeToLive  连接存活时间
* maxTotal 连接最大数
* defaultMaxPerRoute 每个路由连接最大数

#### KeepAlive 策略

HTTP的keep-alive一般都会带上中间的横杠，普通的http连接是客户端连接上服务端，然后结束请求后，由客户端或者服务端进行http连接的关闭。下次再发送请求的时候，客户端再发起一个连接，传送数据，关闭连接。这么个流程反复。但是一旦客户端发送connection:keep-alive头给服务端，且服务端也接受这个keep-alive的话，两边对上暗号，这个连接就可以复用了，一个http处理完之后，另外一个http数据直接从这个连接走了。减少新建和断开TCP连接的消耗。

* 实现由apache.http提供的接口ConnectionKeepAliveStrategy
  
  ```java
  public class CustomConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy {
    private final long DEFAULT_SECONDS = 30;

    @Override
    public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
        return Arrays.asList(response.getHeaders(HTTP.CONN_KEEP_ALIVE))
                .stream()
                .filter(h -> StringUtils.equalsIgnoreCase(h.getName(), "timeout")
                        && StringUtils.isNumeric(h.getValue()))
                .findFirst()
                .map(h -> NumberUtils.toLong(h.getValue(), DEFAULT_SECONDS))
                .orElse(DEFAULT_SECONDS) * 1000;
    }
  }
  ```

* HttpClient指定KeepAlive策略

  ```java
  CloseableHttpClient httpClient = HttpClients.custom()
        .setConnectionManager(connectionManager)
        .evictIdleConnections(30, TimeUnit.SECONDS)
        .disableAutomaticRetries()
        // 有 Keep-Alive 认里面的值，没有的话永久有效
        //.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
        // 换成自定义的
        .setKeepAliveStrategy(new CustomConnectionKeepAliveStrategy())
        .build();
  ```

### 连接超时设置

* connectTimeout / readTimeout

```java
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
  //return new RestTemplate();
    return builder
        .setConnectTimeout(Duration.ofMillis(100))
        .setReadTimeout(Duration.ofMillis(500))
        .requestFactory(this::requestFactory)
        .build();
  }
```

### SSL校验

  证书检查策略

## 通过WebClient访问Web资源

* 代码位置：webresources/webclient-demo

WebClient是一个一Reactive方式处理HTTP请求的非阻塞式的客户端。

支持的底层HTTP库：

* Reactor Netty - ReactorClientHttpConnector
* Jetty ReactiveStream HttpClient - JettyClientHttpConnector

### WebClient 的基本⽤法

* 创建WebClient
  * WebClient.create();
  * WebClient.builder();
* 发起请求
  * get() / post() / put() / delete() / patch()
* 获取结果
  * retrieve() / exchange()
* 处理HTTP Status
  * onStatus()
* 应答正⽂
  * bodyToMono() / bodyToFlux()