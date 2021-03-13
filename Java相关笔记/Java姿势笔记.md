## 随便记记

> 序列化及反序列化相关知识 存在一定安全问题，将废弃？？

    1. 序列化的类需要实现 java.io.Serializable
    2. 通过ObjectInputStream和ObjectOutputStream对对象进行序列化和反序列化
    3. 虚拟机是否允许反序列化，不仅取决于类路径和功能代码是否一致，一个非常重要的一点是两个类的序列化 ID 是否一致（就是 private static final long serialVersionUID）
    4. 序列化不保存静态变量
    5. 要想将父类对象也序列化，需要父类对象也实现Serializable
    6. 被transient 修饰的变量不会被序列化到文件中，反序列化时，transient 变量的值被设为初始值不会从文件获取
    7. 服务器端给客户端发送序列化对象数据，对象中有一些数据是敏感的，比如密码字符串等，
    希望对该密码字段在序列化时，进行加密，而客户端如果拥有解密的密钥，只有在客户端进行反序列化时，才可以对密码进行读取，这样可以一定程度保证序列化对象的数据安全。
    8. 在序列化过程中，如果被序列化的类中定义了writeObject 和 readObject 方法，虚拟机会试图调用对象类里的 writeObject 和 readObject 方法，进行用户自定义的序列化和反序列化。
    如果没有这样的方法，则默认调用是 ObjectOutputStream 的 defaultWriteObject 方法以及 ObjectInputStream 的 defaultReadObject 方法。
    这个可以实现对对敏感字段加密
    
    ps: ArrayList为了防止序列化出现null值 内部有writeObject和readObject方法
