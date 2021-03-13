# spring-boot 笔记

> spring-boot

 @SpringBootApplication = (默认属性)@Configuration +       @EnableAutoConfiguration + @ComponentScan

* @Configuration

  提到@Configuration就要提到他的搭档@Bean。使用这两个注解就可以创建一个简单的spring配置类，可以用来替代相应的xml配置文件

* @EnableAutoConfiguration:

  能够自动配置spring的上下文，试图猜测和配置你想要的bean类，通常会自动根据你的类路径和你的bean定义自动配置

* @ComponentScan: 

  会自动扫描指定包下的全部标有@Component的类，并注册成bean，当然包括@Component下的子注解@Service,@Repository,@Controller

> @RestController = @ResponseBody + @Controller


