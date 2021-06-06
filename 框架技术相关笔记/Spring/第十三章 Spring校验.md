# Spring校验

## Spring校验的使用场景

* Spring常规校验（Validator）：使用频率比较小。
* Spring数据绑定（DataBinder）
* Spring Web参数绑定（WebDataBinder）
* Spring Web MVC / Spring WebFlux 处理方法参数校验：通过会通过二次注解的形式，对方法参数进行校验。

## Validator接口设计

Validator接口是Spring内部校验器接口，通过编程的方式校验目标对象。Validator接口的设计有一些局限性，只能通过类来判断是否能被校验，目前主流已经不使用这个方式进行校验了，使用更多的是通过注解的方式，在不同的业务方法上进行拦截校验。

核心方法：

* Validator#supports(Class): 校验目标类是否能被校验。
* Validator#validate(Object,Errors): 对目标对象进行校验操作，并且将校验的错误信息输出到Errors对象中。

配套组件：

* 错误收集器：org.springframework.validation.Errors
* Validator 工具类：org.springframework.validation.ValidationUtils

使用实例：

```java
public class UserLoginValidator implements Validator {
  
   private static final int MINIMUM_PASSWORD_LENGTH = 6;
  
   public boolean supports(Class clazz) {
      return UserLogin.class.isAssignableFrom(clazz);
   }
  
   public void validate(Object target, Errors errors) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName", "field.required");
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "field.required");
      UserLogin login = (UserLogin) target;
      if (login.getPassword() != null
            && login.getPassword().trim().length() < MINIMUM_PASSWORD_LENGTH) {
         errors.rejectValue("password", "field.min.length",
               new Object[]{Integer.valueOf(MINIMUM_PASSWORD_LENGTH)},
               "The password must be at least [" + MINIMUM_PASSWORD_LENGTH + "] characters in length.");
      }
   }
}
```

## Errors接口设计

* 接口职责
  * 数据绑定和校验错误收集接口，与 Java Bean 和其属性有强关联性
* 核心方法
  * reject 方法（重载）：收集错误文案
  * rejectValue 方法（重载）：收集对象字段中的错误文案，比如上面登陆的密码长度不符合时。
* 配套组件
  * Java Bean 错误描述：org.springframework.validation.ObjectError
  * Java Bean 属性错误描述：org.springframework.validation.FieldError

## Errors文案来源

Errors文案生成步骤：

* 选择Errors实现（如：org.springframework.validation.BeanPropertyBindingResult）
* 调用 reject 或 rejectValue 方法
* 获取 Errors 对象中 ObjectError 或 FieldError
* 将ObjectError 或 FieldError 中的 code 和 args，关联 MessageSource 实现（如：
ResourceBundleMessageSource）

Errors错误文案示例代码：[ErrorsMessageDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/validation/src/main/java/com/wkk/learn/spring/ioc/validation/ErrorsMessageDemo.java)

## 自定义Validator

自定义Validator需要实现org.springframework.validation.Validator 接口

* 实现supports 方法
* 实现 validate 方法
  * 通过 Errors 对象收集错误
    * ObjectError：对象（Bean）错误：
    * FieldError：对象（Bean）属性（Property）错误
  * 通过 ObjectError 和 FieldError 关联 MessageSource 实现获取最终文案

自定义Validator示例代码：[ValidatorDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/validation/src/main/java/com/wkk/learn/spring/ioc/validation/ValidatorDemo.java)

## Validator的救赎

Spring校验中Validator接口整体比较难用，不仅需要传入校验的对象，还需要一个Errors实例，Errors又有多个实现。为了实现易用性，Validator和Bean Validation进行了适配。Bean Validation是标准的Java EE技术。

Bean Validation 与 Validator 适配：

* 核心组件 - org.springframework.validation.beanvalidation.LocalValidatorFactoryBean：对Bean Validation进行的适配实现。
* 依赖 Bean Validation - JSR-303 or JSR-349 provider
* Bean 方法参数校验 - org.springframework.validation.beanvalidation.MethodValidationPostProcessor：通过后置AOP拦截的方式，将@Validated注解的类进行拦截，在调用该类的接口时加上对应的校验。

Spring Bean Validation整合示例代码：[SpringBeanValidationDemo.java](https://github.com/wkk1994/spring-ioc-learn/blob/master/validation/src/main/java/com/wkk/learn/spring/ioc/validation/SpringBeanValidationDemo.java)

> Bean Validation和Hibernate Validation：Bean Validation的能力主要是由Hibernate Validation实现的。

## 面试题

* Spring 校验接口是哪个？

  org.springframework.validation.Validator

* Spring 有哪些校验核心组件？

  * 检验器：org.springframework.validation.Validator
  * 错误收集器：org.springframework.validation.Errors
  * Java Bean 错误描述：org.springframework.validation.ObjectError
  * Java Bean 属性错误描述：org.springframework.validation.FieldError
  * Bean Validation 适配：org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

* 请通过示例演示 Spring Bean 的校验？

  下章数据绑定。

> Spring的校验和国际化都属于中间环节，这些能力都是为了后面的数据转换和数据绑定等来服务的。但是这些能力都是Spring内部的组件，属于比较细节的部分，在日常开发中基本使用不到。但是这有助于对框架的扩展进行帮助，甚至对未来设计一些细节、抽象思想等更有帮助。
