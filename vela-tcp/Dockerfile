# =============================================================================
# Vela IM — vela-tcp (TCP/WebSocket 网关) Dockerfile
# 轻量版：本地 mvn package 打好 jar 后，直接 COPY 进 JRE 镜像
# =============================================================================

FROM eclipse-temurin:8-jre

WORKDIR /app

# 从本地编译产物中复制 jar
COPY vela-tcp/target/vela-tcp-*.jar app.jar

# 时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

EXPOSE 9000 19000

ENTRYPOINT ["java", "-jar", "app.jar"]
