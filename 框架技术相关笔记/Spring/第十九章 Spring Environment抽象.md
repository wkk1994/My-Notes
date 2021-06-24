# Spring Environment抽象

## 理解Spring Environment抽象

Spring3.1开始引入的Environment抽象，它主要有两个作用：统一的Spring配置属性管理和条件化SpringBean装配管理。

* 统一的Spring配置属性管理

  Environment相关的API统一了Spring配置属性的管理，包括占位符处理和类型转换，不仅完整地替换了PropertyPlaceholderConfigurer，而且还支持更丰富的配置属性源（PropertySource）。

* 条件化SpringBean装配管理

  通过Environment Profiles信息，帮助Spring容器提供条件化地装配Bean。

## Spring Environment接口使用场景

* 用于属性占位符处理：PropertyResolver#resolvePlaceholders方法可以将${xxx}中的xxx转换为对应的属性值。

* 用于转换Spring配置属性类型：PropertyResolver#getProperty的多个重载方法可以根据key获取属性值，并转换为指定的Class。

* 用户存储Spring属性配置源（PropertySource）：方法getActiveProfiles和getDefaultProfiles可以获取Profiles。

* 用于Profiles状态的维护：Profiles状态的维护由Environment的实现AbstractEnvironment实现，AbstractEnvironment的方法setActiveProfiles和setDefaultProfiles可以对Profiles进行维护。
