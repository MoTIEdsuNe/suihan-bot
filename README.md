# 岁寒 Bot

## 简述

1. 基于 Spring boot 框架
2. 进行了基础封装
3. 主要用于岁寒 bot 使用

## 运行

### 环境需求

1. java 17
2. MySQL 8
3. 好像没了

### 系统运行

1. Mysql SQL：见 `init.sql`
2. 参数配置(/src/main/resources/application-prod.yml)
    - 数据库参数
        - `spring.datasource.url`
        - `spring.datasource.username`
        - `spring.datasource.password`
    - bot 参数
        - `bot.telegram.proxy`, 代理配置
        - `bot.telegram.proxy.enable`, 是否启用; 参数选项：`true`,`false`
        - `bot.telegram.proxy.host`, host
        - `bot.telegram.proxy.port`, 端口号
        - `bot.telegram.proxy.type`, 类型；参数选项：`SOCKS4`,`SOCKS5`,`HTTP`
        - `bot.telegram.webhook`, webhook 配置
        - `bot.telegram.webhook.enable`, 是否启用; 参数选项：`true`,`false`
        - `bot.telegram.webhook.url`, webhook 的请求 URL
        - `bot.telegram.base`, 基本 bot 配置
        - `bot.telegram.base.name`, bot 名称(暂未用到)
        - `bot.telegram.base.token`, bot token(见 @botFather)
3. 系统构建：`mvn clean package -Dmaven.test.skip=true -U -e -X -B`
4. 系统运行：`nohup java -jar suihan-bots.jar > ./nohup.log 2>&1 &`

### 其他
有没有可能我提供了 docker file

## 其他
1. 新功能请提 issue ，做不做看狸想不想要；
2. 联系狸：略
