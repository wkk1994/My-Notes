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
