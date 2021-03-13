## Web Services简介
>1.简介

    web services是指用于架构Web service的整体技术框架，而Web Servier则是使用Web Services技术而创建的应用实例    
    WebServices 提供一个建立分布式应用的平台，使得运行在不同操作系统和不同设备上的软件，或者是用不同的程序语言和不同厂商的软件开发工具开发的软件，所有可能的已开发和部署的软件，能够利用这一平台实现分布式计算的目的。

> WebServices体系结构

*  Web 服务提供者: Web 服务的拥有者，它会等待其他的服务或者是应用程序访问自己。
*  Web 服务中介者: 它通过服务注册中心也就是 Web 服务中介者查找到所需要的服务，再利用 SOAP 消息向 Web 服务提供者发送请求以获得服务
*  Web 服务请求者: Web 服务中介者的作用就是把一个 Web 服务请求者和合适的 Web 服务提供者联系在一起，充当一个管理者的角色，一般是通过 UDDI来实现
     
        Web Service大体上分为5个层次:
        1. Http传输信道
        2. XML的数据格式
        3. SOAP封装格式
        4. WSDL的描述方式
        5. UDDI  UDDI是一种目录服务，企业可以使用它对Webservices进行注册和搜索

### SOAP Simple Object AccessProtocol(简单对象访问协议)

    基于xml和http，通过xml来实现消息描述，然后再通过http实现消息传输。
    SOAP是用于应用程序之间进行通信的一种通信协议
### WSDL Web Services Description Language(Web 服务描述语言)

    基于 XML的用于描述 Web 服务以及如何访问 Web 服务的语言,服务提供者通过服务描述将所有用于访问 Web服务的规范传送给服务请求者
    WSDL 描述了 Web服务的三个基本属性:1.服务所提供的操作 2.如何访问服务 3.服务位于何处
### UDDI Universal Description，Discovery and Integration(通用的描述，发现以及整合)

    UDDI 是一种目录服务，企业可以通过 UDDI 来注册和搜索 Web 服务, UDDI 通过SOAP 进行通讯
 