# =============================================================================
# Vela IM — vela-service (核心业务服务) Dockerfile
# 轻量版：本地 mvn package 打好 jar 后，直接 COPY 进 JRE 镜像
# 使用方式：
#   1. 在项目根目录执行 mvn clean package -DskipTests
#   2. docker build -t vela-service:latest -f vela-service/Dockerfile .
# =============================================================================

FROM eclipse-temurin:8-jre

WORKDIR /app

# 从本地编译产物中复制 jar
COPY vela-service/target/vela-service-*.jar app.jar

# 时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]
