##  nginx [安装](https://www.nginx.com/resources/admin-guide/)<https://blog.csdn.net/wxyjuly/article/details/79443432>

*  先安装编译套件

        sudo apt-get install gcc g++;(ubuntu)
        yum install gcc g++;(centos)
        

* 安装pcre zlib openssl

        
        $ wget ftp://ftp.csx.cam.ac.uk/pub/software/programming/pcre/pcre-8.41.tar.gz
        $ tar -zxf pcre-8.41.tar.gz
        $ cd pcre-8.41
        $ ./configure
        #conteos安装可能出现configure: error: You need a C++ compiler for C++ support错误要安装yum install -y gcc gcc-c++
        $ make
        $ sudo make install

        $ wget http://zlib.net/zlib-1.2.11.tar.gz
        $ tar -zxf zlib-1.2.11.tar.gz
        $ cd zlib-1.2.11
        $ ./configure
        $ make
        $ sudo make install

        $ wget http://www.openssl.org/source/openssl-1.0.2k.tar.gz
        $ tar -zxf openssl-1.0.2k.tar.gz
        $ cd openssl-1.0.2k
        $ ./configure darwin64-x86_64-cc --prefix=/usr;(ubuntu)
        ./config --prefix=/usr/local/ssl --openssldir=/usr/local/ssl;(centos)
        # 可能需要安装Perl5 https://blog.csdn.net/qq_20678155/article/details/68926562

        $ make
        $ sudo make install

        $ wget http://nginx.org/download/nginx-1.12.2.tar.gz
        $ tar zxf nginx-1.12.2.tar.gz
        $ cd nginx-1.12.2
        指定参数
        $ ./configure \
        --sbin-path=/usr/local/nginx/nginx  \
        --conf-path=/usr/local/nginx/nginx.conf  \
        --pid-path=/usr/local/nginx/nginx.pid  \
        --with-pcre=../pcre-8.41  \
        --with-zlib=../zlib-1.2.11  \
        --with-openssl=../openssl-1.0.2k \
        --with-http_ssl_module  \
        --with-stream  \
        --with-mail=dynamic  
        
        --add-module=/usr/build/nginx-rtmp-module  \--添加module
        --add-dynamic-module=/usr/build/3party_module
        $ make
        $ make install

## docker[安装](https://docs.docker.com/engine/installation/#supported-platforms)

    适用于测试环境和开发环境 
    脚本安装
    1. 国内安装镜像 curl -sSL https://get.daocloud.io/docker | sh
        正常地址： curl -fsSL https://get.docker.com/ | sh
        
    2. 添加用户到docker组内 sudo usermod -aG docker username
    3. 通过daocloud获取docker加速器
        curl -sSL https://get.daocloud.io/daotools/set_mirror.sh | sh -s http://a33ce760.m.daocloud.io
    4. 重启docker sudo service docker restart
    生产环境可以参考官方安装教程
    docker必要条件linux内核3.10以上，最好是ubunut

## docker-compose安装

    1. sudo curl -L https://github.com/docker/compose/releases/download/1.17.0/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose

    2. sudo chmod +x /usr/local/bin/docker-compose --设置为可执行文件
    3.sudo curl -L https://raw.githubusercontent.com/docker/compose/1.17.0/contrib/completion/bash/docker-compose -o /etc/bash_completion.d/docker-compose --不知道干嘛的
    * docker-compose和docker有版本对照(https://github.com/docker/compose/tags)
    4. docker-compose version 测试安装是否成功

## nginx 常用命令

 进入nginx安装目录(一般路径/usr/local/nginx)执行
 
 1. 启动nginx  ./nginx
 2. 停止nginx需要手动停止 杀进程
        
        ps -ef|grep nginx
        找到主进程 杀掉
3. 验证nginx配置文件是否正确 ./nginx -t  

4. 重启Nginx服务 ./nginx -s reload

```
ubuntu通过apt-get安装Nginx,安装好的文件位置：

    /usr/sbin/nginx：主程序

    /etc/nginx：存放配置文件

    /usr/share/nginx：存放静态文件

    /var/log/nginx：存放日志
```