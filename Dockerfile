# 基础镜像
FROM openjdk:17-jdk-alpine

# 维护者信息
MAINTAINER だれでもいいでしょう

# 设置工作目录为当前目录
WORKDIR .

# 复制项目的 pom.xml 文件到容器中
COPY pom.xml .

# 下载并安装项目的依赖
RUN mvn dependency:go-offline

# 复制整个项目到容器中
COPY . .

# 构建项目
RUN mvn package

# 暴露应用程序的端口 (除非你用 webhook）
# EXPOSE ?

# 运行应用程序
CMD ["java", "-jar", "target/suihan-bots.jar"]
