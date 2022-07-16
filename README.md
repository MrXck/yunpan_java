# 环境准备
```
jdk8
MySQL >= 5.6
```

# 修改配置项
```
applicaion.yml中
datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.alibaba.druid.pool.DruidDataSource
    url: jdbc:mysql://数据库ip:数据库端口/file
    username: 用户名
    password: 密码
```

# 创建数据库
```
MySQL创建file数据库
将项目根目录下的file.sql导入数据库
```

# 默认url

```
http://127.0.0.1:8080
```

