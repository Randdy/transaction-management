# 使用 JDK 21 的官方镜像
FROM eclipse-temurin:21-jdk-alpine

# 添加元数据标签
LABEL maintainer="HSBC WPB"
LABEL version="1.0"
LABEL description="HSBC WPB Transaction Management System"

# 设置工作目录
WORKDIR /app

# 安装必要的工具
RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 添加非 root 用户
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 复制构建好的 jar 文件
COPY target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 设置 JVM 参数
ENV JAVA_OPTS="-XX:+UseZGC -Xms512m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp -XX:+ExitOnOutOfMemoryError"

# 设置时区
ENV TZ=Asia/Shanghai

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
