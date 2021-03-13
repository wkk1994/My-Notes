# 第六章 Spring MVC实践

## Spring的应用上下文

**Spring应用上下文常用的接口及其实现**

* BeanFactory
  * DefaultListableBeanFactory
* ApplicationContext 内部通过BeanFactory实现功能，最常用的上下文类，实现类BeanFactory的全部功能
  * ClassPathXmlApplicationContext
  * FileSystemXmlXmlApplicationContext
  * AnnotationConfigApplicationContext
* WebApplicationContext

**Web应用上下文**

![Web应用上下文](https://note.youdao.com/yws/api/personal/file/WEB3288b684f145c4e25fd956cab3cbe72e?method=download&shareKey=a8a961ac8d4bcf0762efa8525a5d0544)

**Spring上下文继承的问题**

Spiring的上下文相互独立，当子上下文继承父上下文时，定义在父上下文的AOP拦截可能在子上下文上不能起作用，如果想将切面配置成通用的，对父和子上下文的 bean 均支持增强，则：
1. 切面 fooAspect 定义在父上下文。
2. 父上下文和子上下文，均要开启 aop 的增加，即 @EnableAspectJAutoProxy 或<aop: aspectj-autoproxy /> 的支持。

## Spring MVC的请求处理流程

![SpringMVC请求处理流程](https://note.youdao.com/yws/api/personal/file/WEB3b6f4f7b5b7ed362cadb2bfb6e505a74?method=download&shareKey=d84ed811fa0b79d97c5dfb048ebd950d)

**大致流程**

* 绑定一些Attribute
  * WebApplicationContext / LocaleResolver / ThemeResolver
* 处理Multipart
  * 如果是，将请求转为 MultipartHttpServletRequest
* Handler处理
  * 如果找到对应 Handler，执⾏ Controller 及前后置处理器逻辑
* 处理返回的 Model ，呈现视图

**[SpringMVC方法注解](https://docs.spring.io/spring/docs/5.1.5.RELEASE/spring-framework-reference/web.html#mvc-ann-arguments)**

**[SpringMVC返回参数注解](https://docs.spring.io/spring/docs/5.1.5.RELEASE/spring-frameworkreference/web.html#mvc-ann-return-types )**

## 自定义转换类型

SpringMVC提供了WebMvcConfigurer实现类型转换等定义，SpringBoot提供了WebMvcAutoConfiguration，不需要实现WebMvcConfiguration，只需要添加自定义的Converter和Formatter（org.springframework.web.servlet.config.annotation.WebMvcConfigurer#addFormatters）就可以实现。

## 定义校验

通过在参数前添加@Valid 注解可以实现对传入参数的校验，校验结果通过BindingResult返回。当系统中存在Hibernate Validator，就会使用Hibernate Validator进行校验。

## Multipart上传

* 配置 MultipartResolver
* Spring Boot ⾃动配置 MultipartAutoConfiguration
* ⽀持类型 multipart/form-data
* MultipartFile 类型

## SpringMVC中的视图解析

视图解析的父类：ViewResolver

* AbstractCachingViewResolver 抽象的基类实现类ViewResolver
  * UrlBasedViewResolver 经常用到的视图解析，将返回的url解析成一个view Example: prefix="/WEB-INF/jsp/", suffix=".jsp", viewname="test" ->  "/WEB-INF/jsp/test.jsp"
  * FreeMarkerViewResolver  FreeMarker模板解析器
  * InternalResourceViewResolver 默认放在解析链最后的解析器，可以解析jsp和jstl
  * ContentNegotiatingViewResolver 根据请求文件名或者请求头的Accept找到最合适的视图。

**DispatcherServlet中的视图解析逻辑**

* initStrategies()
  * initViewResolvers() 初始化ViewResolver，默认初始化全部。
* doDispatch()
  * processDispatchResult() 从视图名到具体视图的解析
    * 没有返回视图时，尝试RequestToViewNameTranslator（尝试解析出一个视图名称）
    * resolverViewName()解析View对象

**使⽤ @ResponseBody 的情况**

* 在 HandlerAdapter.handle() 的中完成了 Response 输出
• RequestMappingHandlerAdapter.invokeHandlerMethod()
• HandlerMethodReturnValueHandlerComposite.handleReturnValue()
• RequestResponseBodyMethodProcessor.handleReturnValue()

关键性代码：
org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#invokeHandlerMethod

```java
protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
      HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
    // ...
    // 设置returnValueHandler 后续根据返回类型选择对应的handler处理
    if (this.returnValueHandlers != null) {
      invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
    }
    // 处理请求
    invocableMethod.invokeAndHandle(webRequest, mavContainer);
    // ...
}
```

org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod#invokeAndHandle

```java
public void invokeAndHandle(ServletWebRequest webRequest, ModelAndViewContainer mavContainer,
      Object... providedArgs) throws Exception {
    // 处理请求调用对应的controller方法
  Object returnValue = invokeForRequest(webRequest, mavContainer, providedArgs);

    // 选择对应的handler处理请求 @RequestBody对应RequestResponseBodyMethodProcessor
    this.returnValueHandlers.handleReturnValue(
        returnValue, getReturnValueType(returnValue), mavContainer, webRequest);
```

**两种不同的重定向前缀**

* redirect: 重定向
* forward: 传发

## SpringMVC支持的视图

**[支持的视图列表](https://docs.spring.io/spring/docs/5.1.5.RELEASE/spring-frameworkreference/web.html#mvc-view)**

比较常用的视图：

* Jackson-based JSON / XML
* Thymeleaf & FreeMarker

## 自定义配置 MessageConverter

Spring MVC通过 WebMvcConfigurer 的 configureMessageConverters()方法添加MessageConverter。

SpringBoot通过WebMvcAutoConfigurationAdapter自动查找容器中的HttpMessageConverters，实现自动添加MessageConverter。

## Spring Boot 对 Jackson 的⽀持

* JacksonAutoConfiguration
  * Spring Boot 可以通过 @JsonComponent注解自动注册JSON 序列化组件
  
  ```java
  @JsonComponent
  public class MoneyDeserializer extends StdDeserializer<Money> {

    protected MoneyDeserializer() {
        super(Money.class);
    }

    @Override
    public Money deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        return Money.of(CurrencyUnit.of("CNY"), jsonParser.getDecimalValue());
    }
  }

  @JsonComponent
  public class MoneySerializer extends StdSerializer<Money> {

    protected MoneySerializer() {
        super(Money.class);
    }

    @Override
    public void serialize(Money money, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeNumber(money.getAmount());
    }
  }
  ```

  * Jackson2ObjectMapperBuilderCustomizer，可以自己在Spring上下文中构建Jackson2ObjectMapperBuilderCustomizer，对json转换做定制化处理

  ```java
      @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder.indentOutput(true);
    }
  ```

* JacksonHttpMessageConvertersConfiguration
  * 增加 jackson-dataformat-xml 以⽀持 XML 序列化

## 使⽤ Thymeleaf

SpringBoot的默认模板引擎

* 添加 Thymeleaf 依赖
  * org.springframework.boot:spring-boot-starter-thymeleaf
* Spring Boot 的⾃动配置
  * ThymeleafAutoConfiguration
  * ThymeleafViewResolver

**Thymeleaf 的⼀些默认配置**

* spring.thymeleaf.cache=true  # 是否开启缓存，开启缓存后开发环境修改模板不会更新
* spring.thymeleaf.check-template=true # 是否检查模板
* spring.thymeleaf.check-template-location=true # 是否检查模板位置
* spring.thymeleaf.enabled=true # 启用模板
* spring.thymeleaf.encoding=UTF-8 # 模板编码
* spring.thymeleaf.mode=HTML # 模板模式，html/xml
* spring.thymeleaf.servlet.content-type=text/html # 模板的类型
* spring.thymeleaf.prefix=classpath:/templates/ # 模板地址的前缀
* spring.thymeleaf.suffix=.html # 模板地址的后缀

## Spring Boot 中的静态资源配置

* 核⼼逻辑
  * WebMvcConfigurer.addResourceHandlers()
* 常⽤配置
  * spring.mvc.static-path-pattern=/**
  * spring.resources.static-locations=classpath:/META-INF/
resources/,classpath:/resources/,classpath:/static/,classpath:/public/

## Spring Boot 中的缓存配置

ResourceProperties.Cache是springboot缓存相关的配置类

* 常⽤配置（默认时间单位都是秒）
  * spring.resources.cache.cachecontrol.max-age=时间 #缓存时间
  * spring.resources.cache.cachecontrol.no-cache=true/false #是否启用缓存
  * spring.resources.cache.cachecontrol.s-max-age=时间 #缓存

* 在Controller方法中手工设置缓存

```java
    @ResponseBody
    public ResponseEntity<Coffee> getById(@PathVariable Long id) {
        Coffee coffee = coffeeService.getCoffee(id);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(10, TimeUnit.SECONDS))
                .body(coffee);
    }
```

## SpringMVC的异常解析

### 异常相关接口

* 核心接口
  * HandlerExceptionResolver 是所有异常类的基类
* 实现接口
  * SimpleMappingExceptionResolver 将异常转换成jsp页面处理
  * HandlerExceptionResolver的默认实现，将错误转换成对应的reponse状态码
  * ResponseStatusExceptionResolver 根据@ResponseStatus注解响应对应的reponse状态码
  * ExceptionHandlerExceptionResolver
* DispatchServlet异常处理的核心代码：

org.springframework.web.servlet.DispatcherServlet#initHandlerExceptionResolvers 初始化异常处理类

```java
    private void initHandlerExceptionResolvers(ApplicationContext context) {
        this.handlerExceptionResolvers = null;

        if (this.detectAllHandlerExceptionResolvers) {
            // Find all HandlerExceptionResolvers in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerExceptionResolver> matchingBeans = BeanFactoryUtils
                    .beansOfTypeIncludingAncestors(context, HandlerExceptionResolver.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerExceptionResolvers = new ArrayList<>(matchingBeans.values());
                // We keep HandlerExceptionResolvers in sorted order.
                AnnotationAwareOrderComparator.sort(this.handlerExceptionResolvers);
            }
        } else {
            try {
                HandlerExceptionResolver her =
                        context.getBean(HANDLER_EXCEPTION_RESOLVER_BEAN_NAME, HandlerExceptionResolver.class);
                this.handlerExceptionResolvers = Collections.singletonList(her);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, no HandlerExceptionResolver is fine too.
            }
        }

        // Ensure we have at least some HandlerExceptionResolvers, by registering
        // default HandlerExceptionResolvers if no other resolvers are found.
        if (this.handlerExceptionResolvers == null) {
            this.handlerExceptionResolvers = getDefaultStrategies(context, HandlerExceptionResolver.class);
            if (logger.isTraceEnabled()) {
                logger.trace("No HandlerExceptionResolvers declared in servlet '" + getServletName() +
                        "': using default strategies from DispatcherServlet.properties");
            }
        }
    }
```

org.springframework.web.servlet.DispatcherServlet#processHandlerException 执行异常处理

```java
    @Nullable
    protected ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response,
                                                   @Nullable Object handler, Exception ex) throws Exception {

        // Success and error responses may use different content types
        request.removeAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);

        // Check registered HandlerExceptionResolvers...
        ModelAndView exMv = null;
        if (this.handlerExceptionResolvers != null) {
            for (HandlerExceptionResolver resolver : this.handlerExceptionResolvers) {
                exMv = resolver.resolveException(request, response, handler, ex);
                if (exMv != null) {
                    break;
                }
            }
        }
        if (exMv != null) {
            if (exMv.isEmpty()) {
                request.setAttribute(EXCEPTION_ATTRIBUTE, ex);
                return null;
            }
            // We might still need view name translation for a plain error model...
            if (!exMv.hasView()) {
                String defaultViewName = getDefaultViewName(request);
                if (defaultViewName != null) {
                    exMv.setViewName(defaultViewName);
                }
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Using resolved error view: " + exMv, ex);
            } else if (logger.isDebugEnabled()) {
                logger.debug("Using resolved error view: " + exMv);
            }
            WebUtils.exposeErrorRequestAttributes(request, ex, getServletName());
            return exMv;
        }

        throw ex;
    }
```

### 异常处理的方法

* 在方法上添加@ExceptionHandler注解指定处理的异常
* 方法添加的地方
  * 在@Controller / @RestController 注解类里添加方法
  * 在@ControllerAdvice / @RestControllerAdvice注解类里添加方法

**注意：** 在@Controller添加的异常处理优先于@ControllerAdvice的先处理，@Controller内添加的异常只能处理本@Controller内的异常，其他@Controller内的不会处理。

## Spring MVC的拦截器

**核心接口**

* HandlerInteceptor
  * boolean preHandle() 执行的前置处理，返回true会继续处理，返回false停止处理
  * void postHandle()  在HandlerAdapter实际调用处理程序后，呈现视图前调用
  * void afterCompletion() 请求完成后处理
* 针对 @ResponseBody 和 ResponseEntity 的情况
  * ResponseBodyAdvice 在写入ResponseBody或ResponseEntity之前调用
* 针对异步请求的接⼝
  * AsyncHandlerInterceptor
    * void afterConcurrentHandlingStarted() 在异步线程启动开始之后调用，可以清除线程绑定属性

### 拦截器的配置⽅式

* 在SpringMVC中通过WebMvcConfigurer.addInterceptors()添加拦截器
* Spring Boot 中的配置
  * 拦截器类实现HandlerInterceptor
  * 创建⼀个带 @Configuration 的 WebMvcConfigurer 配置类，将拦截器添加
  * 不能带 @EnableWebMvc（想彻底⾃⼰控制 MVC 配置除外）

* 示例代码

```java
public class PerformanInterceptor implements HandlerInterceptor {

    private ThreadLocal<StopWatch> stopWatch = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        StopWatch sw = new StopWatch();
        stopWatch.set(sw);
        sw.start();
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        stopWatch.get().stop();
        stopWatch.get().start();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        StopWatch sw = stopWatch.get();
        sw.stop();
        String method = handler.getClass().getSimpleName();
        if(handler instanceof HandlerMethod) {
            String beanName = ((HandlerMethod) handler).getBeanType().getName();
            String methodName = ((HandlerMethod) handler).getMethod().getName();
            method = beanName + "." + methodName;
        }
        log.info("{};{};{};{};{}ms;{}ms;{}ms", request.getRequestURI(), method, response.getStatus(), ex == null ? "-" : ex.getClass().getSimpleName(),
                sw.getTotalTimeMillis(), sw.getTotalTimeMillis() - sw.getLastTaskTimeMillis(), sw.getLastTaskTimeMillis());
        stopWatch.remove();
    }
}

@SpringBootApplication
@EnableJpaRepositories
@Slf4j
@EnableCaching
public class  InterceptorDemo implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(InterceptorDemo.class,args);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PerformanInterceptor())
                .addPathPatterns("/coffee/**").addPathPatterns("/order/**");
    }

    @Bean
    public Hibernate5Module hibernate5Module() {
        return new Hibernate5Module();
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonBuilderCustomizer() {
        return builder -> {
            builder.indentOutput(true);
            builder.timeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        };
    }
}
```
