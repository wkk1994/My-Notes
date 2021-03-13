[toc]

# javaScript基础知识

## 概述
 一个完整的 JavaScript 实现是由以下 3 个不同部分组成的：
*   核心（ECMAScript）
* 文档对象模型（DOM）
* 浏览器对象模型（BOM）

## 基础

### 1.变量命名

    类型	前缀	示例
    数组	a	aValues
    布尔型	b	bFound
    浮点型（数字）	f	fValue
    函数	fn	fnMethod
    整型（数字）	i	iValue
    对象	o	oType
    正则表达式	re	rePattern
    字符串	s	sValue
    变型（可以是任何类型）	v	vValue

### 2.引用值和原始值

* 原始值代表原始数据类型的值，也叫基本数据类型，存放在栈内存，包括 Number、Stirng、Boolean、Null、Underfined属于固定大小存储。
* 引用值指的是复合数据类型的值，存放在堆内存当中，包括 Object(Array也是Object)、Function、Date、RegExp。

### 3.值传递

js的操作通过值传递
* 原始类型的值都是存放在栈内存当中,所以他们的赋值操作,其实相当于在栈内存开辟新的空间,然后将值的副本赋给新的内存,所以他们互不干扰。
* 引用类型的值是存放在堆内存当中,栈内存中变量保存的只是一个堆内存的地址,所以赋值操作,也是开辟一个新的栈内存,然后将地址赋值给新的内存,由于两个变量对应的地址指向同一个地方,所以他们会互相影响


###  4.typeof 运算符

    检查变量或值的类型 (typeof 123) 当函数无明确返回值时，返回的也是值 "undefined"(测试不是？) 值 undefined 实际上是从值 null 派生来的 null == undefined
    NaN (Not a Number) NaN!=NaN

### 5.主要方法
 
    1. toString()
    var iNum = 10;
    iNum.toString() == iNum.toString(10);
    alert(iNum.toString(2));	//输出 "1010"
    alert(iNum.toString(8));	//输出 "12"
    alert(iNum.toString(16));	//输出 "A"
    1. parseInt()
    var iNum1 = parseInt("12345red");	//返回 12345
    var iNum1 = parseInt("0xA");	//返回 10
    var iNum1 = parseInt("56.9");	//返回 56
    var iNum1 = parseInt("red");	//返回 NaN
    指定进制解析字符串 基模式
    var iNum1 = parseInt("10", 2);	//返回 2
    var iNum2 = parseInt("10", 8);	//返回 8
    var iNum3 = parseInt("10", 10);	//返回 10
    3.parseFloat()
    var fNum1 = parseFloat("12345red");	//返回 12345
    var fNum2 = parseFloat("0xA");	//返回 NaN
    var fNum3 = parseFloat("11.2");	//返回 11.2
    var fNum4 = parseFloat("11.22.33");	//返回 11.22
    var fNum5 = parseFloat("0102");	//返回 102
    var fNum1 = parseFloat("red");	//返回 NaN
    4.强制类型转换
    Boolean(value) 对于 0, "", null, undefined, NaN 返回false
    Number(value) 转换整个字符串， 不能完整转换返回NaN
    String(value) 可把任何值转换成字符串。 强制转换成字符串和调用 toString() 方法的唯一不同之处在于，  
    对 null 和 undefined 值强制类型转换可以生成字符串而不引发错误  
    5.对象
    Number对象 
        toFixed(number) 方法返回的是具有指定位数小数的数字的字符串表示
        toExponential(number)，(number 指定要输出的小数的位数) 它返回的是用科学计数法表示的数字的字符串形式。
    1. arguments 存放当前函数传进来的参数

## 函数
    
声明一个函数
* var doAdd = new Function("num1","num2", "alert(num1 + num2)");
* function doAdd(iNum) { alert(iNum + 10) };

**注意：** 尽管可以使用 Function 构造函数创建函数，但最好不要使用它，因为用它定义函数比用传统方式要慢得多。不过，所有函数都应看作 Function 类的实例。

doAdd.length 声明了函数期望的参数个数(ECMAScript 可以接受任意多个参数（最多 25 个）)

**块级作用域**
```javascript
function foo() {
    for (var i=0; i<100; i++) {
        // ...
    }
    i += 100; // 仍然可以引用变量i
}
```
解决块级作用域，ES6引入了新的关键字let，用let替代var可以申明一个块级作用域的变量
```javascript
function foo() {
    var sum = 0;
    for (let i=0; i<100; i++) {
        sum += i;
    }
    // SyntaxError:
    i += 1;
}
```        
* ES6标准引入了新的关键字const来定义常量，const与let都具有块级作用域
    
* ES6中，可以使用解构赋值，直接对多个变量同时赋值var [x, y, z] = ['hello', 'JavaScript', 'ES6'];//支持更多姿势包括变量

### call() 和 apply() 

要指定函数的this指向哪个对象，可以用函数本身的apply，call方法。
* call() 方法 它的第一个参数用作 this 的对象。其他参数都直接传递给函数自身。
* apply() 方法 两个参数，用作 this 的对象和要传递给函数的参数的数组 oldParseInt.apply(null, arguments)。

apply,call可以用做装饰器，装饰方法

### 高阶函数

函数的参数能接收变量，这个变量为函数。称为高阶函数。

* 1.map() 
map()方法定义在JavaScript的Array中 调用Array的map()方法，传入我们自己的函数，就得到了一个新的Array作为结果
    ```javascript
    var arr = [1,2,3]; 
    arr.map(String);//Array数据转字符串  ['1', '2', '3']
    arr.map(function(x){return x * x;});
    ```
* 2.reduce()  
Array的reduce()把一个函数作用在这个Array的[x1, x2, x3...]上，这个函数必须接收两个参数，reduce()把结果继续和序列的下一个元素做累积计算

* 3.filter()  
Array的filter()也接收一个函数。和map()不同的是，filter()把传入的函数依次作用于每个元素，然后根据返回值是true还是false决定保留还是丢弃该元素。
    ```javascript
    var arr = ['A', 'B', 'C'];
    var r = arr.filter(function (element, index, self) {
        console.log(element); // 依次打印'A', 'B', 'C'
        console.log(index); // 依次打印0, 1, 2
        console.log(self); // self就是变量arr
        return true;
    });
    ```

* 4.sort() 
Array的sort()方法就是用于排序的 默认根据字符串 ASCII码排序 返回-1不交换位置  返回1 交换位置  0不变

**数组的其他方法**

* every()方法可以判断数组的所有元素是否满足测试条件。
    ```javascript
    var arr = ['Apple', 'pear', 'orange'];
    console.log(arr.every(function (s) {
        return s.length > 0;
    })); // true, 因为每个元素都满足s.length>0

    console.log(arr.every(function (s) {
        return s.toLowerCase() === s;
    })); // false, 因为不是每个元素都全部是小写
    ```
* find()方法用于查找符合条件的第一个元素，如果找到了，返回这个元素，否则，返回undefined。

    ```javascript
    var arr = ['Apple', 'pear', 'orange'];
    console.log(arr.find(function (s) {
        return s.toLowerCase() === s;
    })); // 'pear', 因为pear全部是小写

    console.log(arr.find(function (s) {
        return s.toUpperCase() === s;
    })); // undefined, 因为没有全部是大写的元素
    ```
* findIndex()和find()类似，返回索引，不存在返回-1。

    ```javascript
    var arr = ['Apple', 'pear', 'orange'];
    console.log(arr.findIndex(function (s) {
        return s.toLowerCase() === s;
    })); // 1, 因为'pear'的索引是1

    console.log(arr.findIndex(function (s) {
        return s.toUpperCase() === s;
    })); // -1
    ```
* forEach()和map()类似，它也把每个元素依次作用于传入的函数，但不会返回新的数组。forEach()常用于遍历数组，因此，传入的函数不需要返回值

    ```javascript
    var arr = ['Apple', 'pear', 'orange'];
    arr.forEach(console.log); // 依次打印每个元素
    ```
### 闭包
    
方法返回值为一个函数，返回的函数并没有立刻执行，而是直到调用了才执行 。
返回闭包时牢记的一点就是：返回函数不要引用任何循环变量，或者后续会发生变化的变量。

### 匿名函数

创建一个匿名函数并立刻执行的语法[参考](https://blog.csdn.net/stpice/article/details/80586444)
    
**写法**

* 1.`(function (x) {return x * x;})(3);`  
* 2.`function (x) { return x * x }(3);` JavaScript语法解析的问题，会报SyntaxError错误  
* 3.`(function (x) { return x * x })(3)`;  
* 4.!function foo() {/*...*/}();  
    +function foo() {/*...*/}();  
    -function foo() {/*...*/}();  
    ~function foo() {/*...*/}();  
* 5.$(function(){});是在DOM加载完成后执行的回调函数，并且只会执行一次  
* 6.(function($){...})(jQuery);即是将实参jQuery传入函数function($){}，通过形参$接收

### 箭头函数 
Arrow Function ES6标准新增    

    x => x * x
    相当于
    function (x) {
        return x * x;
    }
    x => ({args: x}) //返回的是json对象 使用括号括起来
    (x,y) => {
        if (x > 0) { return x * y;}
        else {return - x * y;}
    }
    箭头函数this总是指向词法作用域，也就是外层调用者obj 
    由于this在箭头函数中已经按照词法作用域绑定了，所以，用call()或者apply()调用箭头函数时，无法对this进行绑定

```javascript
var obj = {
    birth: 1990,
    getAge: function (year) {
        var b = this.birth; // 1990
        var fn = (y) => y - this.birth; // this.birth仍是1990
        return fn.call({birth:2000}, year);
    }
};
obj.getAge(2015); // 25
```

### generator（生成器）

generator（生成器）是ES6标准引入的新的数据类型。一个generator看上去像一个函数，但可以返回多次。

定义一个generator
```javascript
function* fib(max) {
    var
        t,
        a = 0,
        b = 1,
        n = 0;
    while (n < max) {
        yield a;
        [a, b] = [b, a + b];
        n ++;
    }
    return;
}
var f = fib(5);
f.next(); // {value: 0, done: false}
f.next(); // {value: 1, done: false}
f.next(); // {value: 1, done: false}
f.next(); // {value: 2, done: false}
f.next(); // {value: 3, done: false}
f.next(); // {value: undefined, done: true}
```
## 对象

### 创建对象

`obj.xxx`访问一个对象的属性时，JavaScript引擎先在当前对象上查找该属性，如果没有找到，就到其原型对象上找，如果还没有找到，就一直上溯到Object.prototype对象，最后，如果还没有找到，就只能返回undefined

    原型链： xiaoming ----> Student.prototype ----> Object.prototype ----> null
    new Student()创建的对象还从原型上获得了一个constructor属性，它指向函数Student本身
    xiaoming.constructor === Student.prototype.constructor; // true
    Student.prototype.constructor === Student; // true  
    Object.getPrototypeOf(xiaoming) === Student.prototype; // true  
    xiaoming instanceof Student; // true

### 原型继承实现方式

```javascript
// PrimaryStudent构造函数:
function PrimaryStudent(props) {
    Student.call(this, props);
    this.grade = props.grade || 1;
}

// 空函数F:
function F() {
}

// 把F的原型指向Student.prototype:
F.prototype = Student.prototype;

// 把PrimaryStudent的原型指向一个新的F对象，F对象的原型正好指向Student.prototype:
PrimaryStudent.prototype = new F();

// 把PrimaryStudent原型的构造函数修复为PrimaryStudent:
PrimaryStudent.prototype.constructor = PrimaryStudent;

// 继续在PrimaryStudent原型（就是new F()对象）上定义方法：
PrimaryStudent.prototype.getGrade = function () {
    return this.grade;
};

// 创建xiaoming:
var xiaoming = new PrimaryStudent({
    name: '小明',
    grade: 2
});
xiaoming.name; // '小明'
xiaoming.grade; // 2

// 验证原型:
xiaoming.__proto__ === PrimaryStudent.prototype; // true
xiaoming.__proto__.__proto__ === Student.prototype; // true

// 验证继承关系:
xiaoming instanceof PrimaryStudent; // true
xiaoming instanceof Student; // true
```
**总结：**
* 1.定义新的构造函数，并在内部用call()调用希望“继承”的构造函数，并绑定this；

* 2.借助中间函数F实现原型链继承，最好通过封装的inherits函数完成；

* 3.继续在新的构造函数的原型上定义新方法。

### 创建对象的方式

* 1.工厂方式 function createCar() {var car = Object;... return car} 在方法内部定义对象的方法属性 有一个问题 每次new一个对象就会产生一个方法对象，解决可以先在外部定义方法，内部指向方法名

* 2.构造函数方式 function Car(...){...} var car = new Car(...); 也有上述问题

* 3.原型方式 function Car() {} Car.prototype.showColor = function() {alert(this.color);}; 解决上述问题 但是原型方式会共享引用

* 4.混合的构造函数/原型方式 最广泛

* 5.动态原型方法 当对象方法不存在时才初始化方法

* 6.ES6 引入关键字 class extends constructor
    
> document 对象表示当前页面

    document.cookie 可以获取当前页面的Cookie。
    不安全的，为了确保安全，服务器端在设置Cookie时，应该始终坚持使用httpOnly。

> 跨域访问

* 通过Flash插件发送HTTP请求 可以绕过浏览器的安全限制，但必须安装Flash，并且跟Flash交互
* 通过在同源域名下架设一个代理服务器来转发，JavaScript负责把请求发送到代理服务器 '/proxy?url=http://www.sina.com.cn'
* JSONP，它有个限制，只能用GET请求，并且要求返回JavaScript。
* CORS HTML5 新的跨域策略：CORS 

> 这种“承诺将来会执行”的对象在JavaScript中称为Promise对象。[资料](https://www.liaoxuefeng.com/wiki/001434446689867b27157e896e74d51a89c25cc8b43bdb3000/0014345008539155e93fc16046d4bb7854943814c4f9dc2000)

> 编写一个jQuery插件的原则：

    给$.fn绑定函数，实现插件的代码逻辑；
    插件函数最后要return this;以支持链式调用；
    插件函数要有默认值，绑定在$.fn.<pluginName>.defaults上；
    用户在调用时可传入设定值以便覆盖默认值。

## node.js

    一种javascript的运行环境，能够使得javascript脱离浏览器运行
    服务器端JavaScript处理：server-side JavaScript execution 
    非阻断/异步I/O：non-blocking or asynchronous I/O
    事件驱动：Event-driven
    
    常见的Web框架包括：Express，Sails.js，koa，Meteor，DerbyJS，Total.js，restify……

    ORM框架比Web框架要少一些：Sequelize，ORM2，Bookshelf.js，Objection.js……

    模版引擎PK：Jade，EJS，Swig，Nunjucks，doT.js……

    测试框架包括：Mocha，Expresso，Unit.js，Karma……

    构建工具有：Grunt，Gulp，Webpack……


> CommonJS规范
    
    每个.js文件都是一个模块
    一个模块想要对外暴露变量（函数也是变量），可以用module.exports = variable;，一个模块要引用其他模块暴露的变量，用var ref = require('module_name');就拿到了引用模块的变量。
    node实现模块隔离的原理
    Node利用JavaScript的函数式编程的特性，轻而易举地实现了模块的隔离
    // 准备module对象:
    var module = {
         id: 'hello',
          exports: {}
    };
    var load = function (module) {
         // 读取的hello.js代码:
        function greet(name) {
             console.log('Hello, ' + name + '!');
        }
        module.exports = greet;
        // hello.js代码结束
        return module.exports;
    };
    var exported = load(module);
    // 保存module:
    save(module, exported);
    如果要输出一个键值对象{}，可以利用exports这个已存在的空对象{}，并继续在上面添加新的键值；
    如果要输出一个函数或数组，必须直接对module.exports对象赋值

    
  * module.exports vs exports
    
        不能直接对  exports赋值
        // 代码可以执行，但是模块并没有输出任何变量:
        exports = {
            hello: hello,
            greet: greet
        };
        默认情况下，Node准备的exports变量和module.exports变量实际上是同一个变量，并且初始化为空对象{}
        exports.foo = function () { return 'foo'; };//这是允许的
        module.exports.foo = function () { return 'foo'; };//OK的
        
        结论：如果要输出一个键值对象{}，可以利用exports这个已存在的空对象    {}，并继续在上面添加新的键值；
            如果要输出一个函数或数组，必须直接对module.exports对象赋值。

    
> 基本模块 node的内置模块

* `global` 唯一的全局对象
* `process` 代表当前Node.js进程
    
    在下一次事件响应中执行代码，可以调用process.nextTick(function(){...})  
    //退出事件监听 
    process.on('exit', function (code) {
            console.log('about to exit with code: ' + code);
        });

* `fs`模块就是文件系统模块，负责读写文件
    
    读取文件  异步 readFile(文件名，编码格式，function(err){})  同步 data = readFileSync(文件名，编码)  读取的二进制文件获取的Buffer对象
    写文件 异步 writeFile(文件名，数据，function(errs){}) 同步  
    stat(文件名，function(err,stat){}) 获取文件大小，创建时间等信息

* `stream`模块 一个仅在服务区端可用的模块，目的是支持“流”这种数据结构。

   ```
   data事件表示流的数据已经可以读取了，end事件表示这个流已经到末尾了，
   没有数据可以读取了，error事件表示出错了
    pipe 管道 用pipe()把一个文件流和另一个文件流串起来，这样源文件的所有数据就自动写入到目标文件里了```
    
* `http` 模块

* `crypto`模块的目的是为了提供通用的加密和哈希算法
    
    Nodejs用C/C++实现这些算法后，通过cypto这个模块暴露为JavaScript接口，这样用起来方便，运行速度也快

> MVVM 设计思想
    关注Model的变化，让MVVM框架去自动更新DOM的状态

## javaScript 事件机制

### 事件冒泡和事件捕获
 
 描述事件触发时序问题  
    
* 1.事件捕获指的是从document到触发事件的那个节点，即自上而下的去触发事件。
* 2.事件冒泡是自下而上的去触发事件。绑定事件方法的第三个参数，就是控制事件触发顺序是否为事件捕获。true,事件捕获；false,事件冒泡。默认false,即事件冒泡。Jquery的e.stopPropagation会阻止冒泡(实际测试也会阻止事件捕获)

**注意：**
火狐Firefox、opera、IE下阻止冒泡事件是不同的代码的，火狐下使用的是event.stopPropagation()，而IE下使用的是event.cancelBubble=true，google下两者皆可，jQuery 可以使用e.stopPropagation()兼容。

#### Event对象的获取方法：
例如：
```javascript function demo1(e){
 var e = e || window.event;
 //此种方法在ie中和google中可以不传参数e也可以获取到event，
 //但是在火狐中必须在事件方法中传递event参数才可以获取到event对象。
 }
 ```
<input onclick = "demo1(event)" value="demo1"/>

### === 和==的区别

    ==  用于比较判断两者相等  == 在比较的时候可以转自动换数据类型
    === 用于严格比较判断两者严格相等   === 严格比较，不会进行自动转换，要求进行比较的操作数必须类型一致，不一致时返回flase

> 匿名函数 匿名自执行函数

    匿名函数 function(args){};
    匿名自执行函数 匿名函数可以自己执行不需要借助其他元素
    1.第一种实现方式  
        (function(args){})(args);
        !function(args){}(args);
        +function(args){}(args);
        -function(args){}(args);
        ~function(args){}(args);
        ...
        都是跟(function(){})();这个函数是一个意思，都是告诉浏览器自动运行这个匿名函数的，因为!+()这些符号的运算符是最高的，所以会先运行它们后面的函数
    2.第二种实现方式  
        (function(args){  }(args));
    3.第三种实现方式
        var fun=function(data){  alert(data);  }("iii");
> 词法作用域 和 动态作用域
    
    词法作用域的函数中遇到既不是形参也不是函数内部定义的局部变量的变量时，去函数定义时的环境中查询。
    动态域的函数中遇到既不是形参也不是函数内部定义的局部变量的变量时，到函数调用时的环境中查。
    词法作用域（静态作用域）是在书写代码或者说定义时确定的，而动态作用域是在运行时确定的。
    词法作用域关注函数在何处声明，而动态作用域关注函数从何处调用，其作用域链是基于运行时的调用栈的。

> 函数声明式和函数表达式

    区别如下:
    1. 以函数声明式定义的函数，函数名是必须的,而函数表达式的函数名是可选的；
    2. 以函数声明式定义的函数，可以在函数声明之前调用,而函数表达式定义的函数只能在定义之后调用
    3. 以函数声明的方法定义的函数并不是真正的声明,它们仅仅可以出现在全局中,或者嵌套在其他的函数中,但是它们不能出现在循环,条件或者try/catch/finally中,而函数表达式可以在任何地方声明.
    //函数声明式
    function greeting(){
        console.log("hello world"); 
    }
     //函数表达式
    var greeting = function(){
        console.log("hello world"); 
    }
    函数表达式的作用域:
    如果函数表达式声明的函数有函数名,那么这个函数名就相当于这个函数的一个局部变量,只能在函数内部调用

