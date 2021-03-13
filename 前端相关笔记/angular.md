# angluarJS笔记

## 1. 指令

    myModule.directive("name".function(){
        return {}
    })
    > 指令内容
    1.restrict 指明指令在DOM里面以什么形式被声明；取值有：E(元素),A(属性,默认值),C(类),M(注释)
    2.priority 指明指令的优先级
    3.terminal 若设置为true，则优先级低于此指令的其他指令则无效，不会被调用(优先级相同的还是会执行)
    4.template（字符串或者函数）可选参数 可以是
    （1）一段html 
    （2）一个函数 函数接收两个参数tElement和tAttrs 其中tElement是指使用此指令的元素，而tAttrs则实例的属性
    5.templateUrl（字符串或者函数），可选参数，可以是
    （1）一个代表HTML文件路径的字符串
    （2）一个函数，可接受两个参数tElement和tAttrs（大致同上）
    6.replace 默认值为false 为true 指令标签会被替换不存在
    7.scope 
    （1）默认值false 表示继承父作用域; 
    （2）为true 表示会继承父类的作用域并创建子作用域;初始时父作用域值改变子作用域值也会改变，当子作用域值改变时就创建
        自己的作用域
    （3）{} 表示创建一个全新的隔离作用域;
    * 隔离作用域可以通过绑定策略来访问父作用域的属性。
    （1）@ 方式局部属性用来访问 directive 外部环境定义的字符串值，主要是通过 directive 所在的标签属性绑定外部字符串值。
        这种绑定是单向的，即父 scope 的绑定变化，directive 中的 scope 的属性会同步变化，而隔离 scope 中的绑定变化，父 
        scope 是不知道的。
    （2）= 通过 directive 的 attr 属性的值在局部 scope 的属性和父 scope 属性名之间建立双向绑定。意思是，当你想要一个双
        向绑定的属性的时候，你可以使用=来引入外部属性。无论是改变父 scope 还是隔离 scope 里的属性，父 scope 和隔离 scope 
        都会同时更新属性值，因为它们是双向绑定的关系。
    （3）& 方式提供一种途经是 directive 能在父 scope 的上下文中执行一个表达式。此表达式可以是一个 function
    8.transclude  如果不想让指令内部的内容被模板替换，可以设置这个值为true。一般情况下需要和ngTransclude指令一起使用。 比
      如：template:"<div>hello every <div ng-transclude></div></div>"
    9.controller 可以是一个字符串或者函数。
        若是为字符串，则将字符串当做是控制器的名字，来查找注册在应用中的控制器的构造函数
    10.controllerAs 作用是可以设置你的控制器的别名
    11.require(字符串或者数组)
    （1）字符串代表另一个指令的名字，它将会作为link函数的第四个参数。
        require的参数值加上下面的某个前缀，这会改变查找控制器的行为：
        （1）没有前缀，指令会在自身提供的控制器中进行查找，如果找不到任何控制器，则会抛出一个error
        （2）？如果在当前的指令没有找到所需的控制器，则会将null传给link连接函数的第四个参数
        （3）^如果在当前的指令没有找到所需的控制器，则会查找父元素的控制器
        （4）?^组合

# Angular 学习笔记

## angular程序架构

        1.组件Component： 是angular的基本构建块
        2.服务service： 业务逻辑
        3.指令： 允许你像html代码添加自定义行为
        4.模块Ngmodule： 组件 服务 指令 组成一个单元 实现一个业务(大概吧)

### 组件Component

        通过@Component({...})定义 组件下定义的class相当于Controller用于处理组件的业务逻辑 组件相当于是指令 组件应保持精简
        指令分类
          1.结构型指令通过在 DOM 中添加、移除和替换元素来修改布局 *ngFor
          2.属性型 指令修改一个现有元素的外观或行为 ngModel
    
### 路由

> Routes

        路由配置，保存Url对应展示的组件，以及在哪个RouterOutlet 中展示
> RouterOutlet

        在html中记录路由呈现位置的占位符指令。
> Router
        
        负责在运行时执行路由的对象，可以通过调用其navigate()和navigateByUrl()方法来导航到一个指定路由
> RouterLink

        在html中声明路由导航用的指令
> ActivatedRoute

        当前激活的路由对象，保存着当前的路由信息 地址参数等
> 提供器

        1.提供器
        @Injectable 定义的当前类的constructor可不可以注入其他类
        在@NgModule的 providers里注册提供器 是全局可以使用的 在组件里@Component也可以providers自定义提供器 只对当前组件有用
        {provider: 提供器名, useClass: 指定使用的类, useFactory: (Logger: LoggerService)=> { 实例化方法  },deps: [LoggerService]}
        可以注入一个变量值{provider: "FLAG",useValue: {isDev: false}}

> 属性绑定

        1. Dom属性绑定