# spring-java-config-shiro

##  从 Docker 启动 MySQL 8

```bash
docker run -d -p 3306:3306 --privileged=true --name mysql1 -v E:\workspace-docker\mysql\my.cnf:/etc/my.cnf -v E:\workspace-docker\mysql\data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=123456 mysql
```
命令说明：

-p 3306:3306：将容器的3306端口映射到主机的3306端口

-v E:\workspace-docker\mysql\my.cnf:/etc/my.cnf：将主机E:\workspace-docker\mysql\my.cnf挂载到容器的/etc/my.cnf

```text
[mysqld]
character-set-server=utf8

collation-server=utf8_general_ci

init_connect=SET collation_connection=utf8_general_ci

init_connect=SET NAMES utf8

sql_mode=NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES

innodb_data_file_path = ibdata1:10M:autoextend

default_authentication_plugin=mysql_native_password

[client]
default-character-set=utf8

[mysql]
default-character-set=utf8
```

-v E:\workspace-docker\mysql\data:/var/lib/mysql：将主机E:\workspace-docker\mysql\data目录挂载到容器的/var/lib/mysql

-e MYSQL_ROOT_PASSWORD=123456：初始化root用户的密码

## 从 Docker 启动 Redis

```bash
docker run --name redis1 -d -p 6379:6379 redis
```

## 运行

导入IDEA，从 `com.joizhang.imooc.Application` 启动