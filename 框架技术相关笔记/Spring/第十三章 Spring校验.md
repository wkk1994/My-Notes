# Spring校验

## Spring校验的使用场景

* Spring常规校验（Validator）：使用频率比较小。
* Spring数据绑定（DataBinder）
* Spring Web参数绑定（WebDataBinder）
* Spring Web MVC / Spring WebFlux 处理方法参数校验：通过会通过二次注解的形式，对方法参数进行校验。

## Validator接口设计

Validator接口是Spring内部校验器接口，通过编程的方式校验目标对象。

核心方法：

* Validator#supports(Class): 校验目标类是否能被校验。
* Validator#validate(Object,Errors): 对目标对象进行校验操作，并且将校验的错误信息输出到Errors对象中。

配套组件：

* 错误收集器：org.springframework.validation.Errors
* Validator 工具类：org.springframework.validation.ValidationUtils
