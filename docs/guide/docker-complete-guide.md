# Vela IM — Docker 全面指南

> 适用环境：Windows + Docker Desktop (WSL 2 backend)
> 项目路径：`F:\PersonalProject-Demetrius\Vela`

---

## 目录

- [一、Docker 核心概念](#一docker-核心概念)
- [二、镜像 vs 容器 vs Dockerfile](#二镜像-vs-容器-vs-dockerfile)
- [三、Dockerfile 详解](#三dockerfile-详解)
- [四、Docker Compose 详解](#四docker-compose-详解)
- [五、Docker 常用命令速查](#五docker-常用命令速查)
- [六、Docker Compose 常用命令速查](#六docker-compose-常用命令速查)
- [七、Vela 项目 Docker 操作手册](#七vela-项目-docker-操作手册)
- [八、如何修改 Docker 配置](#八如何修改-docker-配置)

---

## 一、Docker 核心概念

### 1.1 三个核心对象

```
┌──────────────┐    docker build     ┌──────────────┐    docker run     ┌──────────────┐
│  Dockerfile   │ ──────────────────→ │   镜像 Image   │ ───────────────→ │  容器 Container │
│  (构建配方)    │                    │  (编译好的产物   │                 │  (正在运行的    │
│               │                    │   + 运行环境)   │                 │   进程实例)     │
└──────────────┘                    └──────────────┘                 └──────────────┘
```

| 对象 | 类比 | 特点 |
|------|------|------|
| **Dockerfile** | 菜的食谱 | 告诉 Docker 怎么做 |
| **镜像 (Image)** | 冷冻披萨 | 只读模板，可分享，可复用 |
| **容器 (Container)** | 正在烤的披萨 | 可读写，可启停，可删除 |

### 1.2 一次构建，到处运行

```
开发机 (Windows)             服务器 (Linux)
┌──────────────┐             ┌──────────────┐
│  Dockerfile   │             │               │
│  mvn package  │             │               │
│      ↓        │   docker    │               │
│  镜像 v1.0    │ ── push ──→ │  docker pull  │
│              │             │  镜像 v1.0    │
│              │             │      ↓        │
│              │             │  docker run   │
└──────────────┘             │  (生产环境)    │
                              └──────────────┘
```

---

## 二、镜像 vs 容器 vs Dockerfile

### 2.1 镜像 (Image)

镜像是**只读的模板**，包含运行应用所需的一切：

- 操作系统基础层（如 Ubuntu、Alpine）
- 运行时环境（如 JDK 8、Python）
- 应用代码和依赖（如 jar 包）
- 环境变量、默认命令

```powershell
# 查看本地所有镜像
docker images

# 从远程仓库拉取镜像
docker pull eclipse-temurin:8-jre

# 删除镜像
docker rmi vela-vela-service:latest

# 查看镜像详细信息
docker inspect eclipse-temurin:8-jre
```

### 2.2 容器 (Container)

容器是镜像的**运行实例**，有自己独立的文件系统、网络、进程空间。

```powershell
# 查看运行中的容器
docker ps

# 查看所有容器（含已停止）
docker ps -a

# 进入容器内部
docker exec -it vela-mysql bash

# 在容器内执行命令
docker exec vela-mysql mysql -uroot -proot -e "SHOW DATABASES;"

# 查看容器日志
docker logs vela-service

# 实时查看日志
docker logs -f vela-service

# 查看容器资源占用
docker stats

# 停止容器
docker stop vela-service

# 启动已停止的容器
docker start vela-service

# 重启容器
docker restart vela-service

# 删除容器（需先停止）
docker rm vela-service
```

### 2.3 镜像 vs 容器的关键区别

| | 镜像 (Image) | 容器 (Container) |
|------|-------------|-----------------|
| 读写 | 只读 | 可读写 |
| 生命周期 | 永久（除非删除） | 临时（可启停删） |
| 大小 | 几百 MB ~ 几 GB | 镜像大小 + 运行时数据 |
| 共享 | 可推送仓库分享 | 不可直接分享 |
| 类比 | 类 (Class) | 实例 (Instance) |

---

## 三、Dockerfile 详解

### 3.1 什么是 Dockerfile

Dockerfile 是一个文本文件，包含一系列**指令**，告诉 Docker 如何构建镜像。

### 3.2 常用指令

| 指令 | 作用 | 示例 |
|------|------|------|
| `FROM` | 指定基础镜像 | `FROM eclipse-temurin:8-jre` |
| `WORKDIR` | 设置工作目录 | `WORKDIR /app` |
| `COPY` | 从宿主机复制文件到镜像 | `COPY app.jar app.jar` |
| `RUN` | 在构建时执行命令 | `RUN apt-get update` |
| `EXPOSE` | 声明容器监听的端口 | `EXPOSE 8000` |
| `ENV` | 设置环境变量 | `ENV JAVA_OPTS="-Xmx512m"` |
| `ENTRYPOINT` | 容器启动时的默认命令 | `ENTRYPOINT ["java", "-jar", "app.jar"]` |
| `CMD` | 默认参数（可被覆盖） | `CMD ["--server.port=8000"]` |

### 3.3 两种构建方式

#### 方式一：单阶段构建（简单、适合已有 jar）

```dockerfile
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY target/app.jar app.jar
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
```

使用场景：**本地已打好 jar**，Docker 只负责打包成镜像运行。

构建方式：
```powershell
# 先本地编译
mvn clean package -DskipTests

# 再构建镜像
docker build -t my-app:latest -f Dockerfile .
```

#### 方式二：多阶段构建（复杂、适合没有 JDK/Maven 的环境）

```dockerfile
# 第一阶段：编译
FROM maven:3.6.3-jdk-8-slim AS build
WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests

# 第二阶段：运行
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

使用场景：**没有本地 Maven**，所有工作在 Docker 内完成。
缺点：构建慢（每次都要下载 Maven 依赖）。
优点：不依赖宿主机环境。

### 3.4 .dockerignore

类似于 `.gitignore`，告诉 Docker 在构建时**排除不需要的文件**，让构建上下文更小、构建更快。

```gitignore
# 排除 IDE 配置
.idea/
.vscode/

# 排除版本控制
.git/

# 排除本地日志
logs/
*.log

# 排除文档
docs/
README.md
```

**注意**：不要排除 `target/` 和 `*.jar`，否则单阶段构建时找不到 jar 文件。

### 3.5 构建上下文 (Build Context)

`docker build` 命令指定的最后一个参数 `.` 就是构建上下文。

```powershell
# . 表示当前目录是构建上下文
docker build -t my-app -f Dockerfile .
```

Docker 会：
1. 读取 `.dockerignore`，过滤排除的文件
2. 将剩余文件**打包成 tar**
3. 发送给 Docker 引擎
4. 引擎解压后执行 Dockerfile 指令

因此 `COPY target/app.jar app.jar` 中的 `target/app.jar` 是相对于构建上下文的路径。

---

## 四、Docker Compose 详解

### 4.1 什么是 Docker Compose

Docker Compose 是一个**批量管理多个容器**的工具。通过一个 `docker-compose.yml` 文件定义一组服务（中间件 + 业务应用），然后用一条命令启动/停止全部。

### 4.2 docker-compose.yml 结构

```yaml
# docker-compose.yml 顶级字段
services:     # 定义所有服务
  服务名1:     # 服务名称（也是容器间通信的 DNS 名称）
    image:     # 使用的镜像
    build:     # 或者用 Dockerfile 构建
    ports:     # 端口映射
    environment:  # 环境变量
    volumes:   # 数据卷
    depends_on:   # 依赖关系
    restart:      # 重启策略

  服务名2:
    ...

volumes:       # 定义数据卷
networks:      # 定义网络（通常用默认）
```

### 4.3 核心配置项详解

#### image / build

```yaml
# 方式一：直接使用现成镜像
services:
  mysql:
    image: mysql:5.7          # 从 Docker Hub 拉取

# 方式二：使用 Dockerfile 构建
services:
  vela-service:
    build:
      context: .              # 构建上下文
      dockerfile: vela-service/Dockerfile   # Dockerfile 路径
```

#### ports（端口映射）

```
"宿主机端口:容器内部端口"
```

```yaml
ports:
  - "3307:3306"    # 宿主机 3307 → MySQL 3306
  - "6379:6379"    # 宿主机 6379 → Redis 6379
  - "8000:8000"    # 宿主机 8000 → 业务服务 8000
```

- 宿主机端口：外部访问的端口（如浏览器、IDEA 连接）
- 容器内部端口：服务本身监听的端口
- 多个容器之间通过**容器名:内部端口**通信，不经过宿主机

#### environment（环境变量）

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/vela?...
  SPRING_DATASOURCE_USERNAME: root
  SPRING_DATASOURCE_PASSWORD: root
```

注意：`mysql:3306` 中的 `mysql` 是**服务名**，Docker Compose 会自动做 DNS 解析，指向 MySQL 容器的 IP。

#### depends_on（依赖关系）

```yaml
depends_on:
  mysql:
    condition: service_healthy   # 等待 MySQL 健康检查通过后才启动
  redis:
    condition: service_healthy
```

#### volumes（数据持久化）

```yaml
volumes:
  - mysql-data:/var/lib/mysql   # 命名卷，Docker 管理存储位置
  - ./config.yml:/app/config.yml  # 绑定挂载，宿主机文件映射到容器
```

### 4.4 docker-compose.yml 完整示例

```yaml
services:
  mysql:
    image: mysql:5.7
    container_name: vela-mysql
    restart: always
    ports:
      - "3307:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: vela
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  vela-service:
    build:
      context: .
      dockerfile: vela-service/Dockerfile
    container_name: vela-service
    restart: always
    ports:
      - "8000:8000"
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/vela?...

volumes:
  mysql-data:
```

---

## 五、Docker 常用命令速查

### 镜像管理

```powershell
# 拉取镜像
docker pull eclipse-temurin:8-jre

# 查看本地镜像
docker images

# 删除镜像
docker rmi 镜像名:标签

# 构建镜像
docker build -t 镜像名:标签 -f Dockerfile路径 .

# 查看镜像详情
docker inspect 镜像名:标签
```

### 容器管理

```powershell
# 运行容器
docker run -d --name 容器名 -p 宿主机端口:容器端口 镜像名

# 查看运行中的容器
docker ps

# 查看所有容器（含已停止）
docker ps -a

# 停止容器
docker stop 容器名

# 启动已停止的容器
docker start 容器名

# 重启容器
docker restart 容器名

# 删除容器
docker rm 容器名

# 强制删除运行中的容器
docker rm -f 容器名

# 查看日志
docker logs 容器名
docker logs -f 容器名     # 实时跟踪

# 进入容器内部
docker exec -it 容器名 bash

# 在容器内执行命令
docker exec 容器名 mysql -uroot -proot -e "SHOW DATABASES;"

# 查看资源占用
docker stats
```

### 容器生命周期

```
docker create → Created（已创建）
     ↓
docker start → Running（运行中）
     ↓
docker stop  → Exited（已退出，可再启动）
     ↓
docker rm    → 删除（彻底消失）
```

### 清理命令

```powershell
# 清理所有已停止的容器
docker container prune

# 清理所有未使用的镜像
docker image prune

# 清理所有未使用的资源（容器、镜像、网络、卷）
docker system prune

# 清理全部（含数据卷，慎用！）
docker system prune -a --volumes
```

---

## 六、Docker Compose 常用命令速查

### 启动与停止

```powershell
# 后台启动所有服务（含构建）
docker-compose up -d

# 前台启动（看日志）
docker-compose up

# 停止所有服务（保留容器）
docker-compose stop

# 启动已停止的服务
docker-compose start

# 重启所有服务
docker-compose restart

# 停止并删除所有容器
docker-compose down

# 停止并删除容器 + 数据卷（清空数据库）
docker-compose down -v
```

### 构建

```powershell
# 构建或重新构建所有服务的镜像
docker-compose build

# 只重新构建某个服务
docker-compose build vela-service
```

### 查看状态

```powershell
# 查看所有容器状态
docker-compose ps

# 查看日志
docker-compose logs vela-service
docker-compose logs -f vela-service     # 实时跟踪

# 查看最后 N 行日志
docker-compose logs --tail=100 vela-service
```

### 单服务操作

```powershell
# 只启动某个服务（含依赖的服务）
docker-compose up -d mysql

# 只启动多个服务
docker-compose up -d mysql redis rabbitmq

# 重启某个服务
docker-compose restart vela-service
```

---

## 七、Vela 项目 Docker 操作手册

### 7.1 启动开发环境（只跑中间件）

```powershell
cd F:\PersonalProject-Demetrius\Vela
docker-compose up -d mysql redis rabbitmq zookeeper
```

然后 IDEA 里启动业务服务即可。

### 7.2 启动完整环境（中间件 + 业务服务）

```powershell
# 先本地编译（打 jar）
mvn clean package -DskipTests

# Docker 打包 + 启动
docker-compose up -d
```

### 7.3 更新业务代码后重新部署

```powershell
# 重新编译
mvn clean package -DskipTests

# 重新构建某个服务的镜像并启动
docker-compose build vela-service
docker-compose up -d vela-service
```

### 7.4 查看服务是否正常运行

```powershell
docker-compose ps
```

输出示例：

```
NAME                IMAGE                 STATUS              PORTS
vela-mysql          mysql:5.7             Up (healthy)        0.0.0.0:3307->3306/tcp
vela-redis          redis:6.2             Up (healthy)        0.0.0.0:6379->6379/tcp
vela-rabbitmq       rabbitmq:3.8          Up (healthy)        0.0.0.0:5672->5672/tcp
vela-zk             zookeeper:3.6         Up (healthy)        0.0.0.0:2181->2181/tcp
vela-service        vela-vela-service     Up                  0.0.0.0:8000->8000/tcp
vela-message-store  vela-vela-message     Up                  0.0.0.0:0->0/tcp
vela-tcp            vela-vela-tcp         Up                  0.0.0.0:9000->9000/tcp
```

- `Up (healthy)` = 运行中且健康检查通过
- `Up` = 运行中
- `Exit` = 已停止

### 7.5 下班关闭

```powershell
docker-compose stop
```

明天只需 `docker-compose start` 恢复。

### 7.6 彻底清理

```powershell
docker-compose down --remove-orphans
```

### 7.7 常用调试命令

```powershell
# 连 MySQL
docker exec -it vela-mysql mysql -uroot -proot

# 连 Redis
docker exec -it vela-redis redis-cli

# 查看 RabbitMQ 管理后台
# 浏览器打开 http://localhost:15672  (guest/guest)

# 查看某个容器的日志
docker-compose logs -f vela-service
```

---

## 八、如何修改 Docker 配置

### 8.1 修改端口映射

编辑 `docker-compose.yml`，修改对应服务的 `ports`：

```yaml
ports:
  - "新宿主机端口:容器内部端口"
```

示例：MySQL 从 3306 改为 3307

```yaml
ports:
  - "3307:3306"   # 宿主机 3307 → 容器 MySQL 3306
```

### 8.2 修改环境变量

编辑 `docker-compose.yml`，修改对应服务的 `environment`：

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/vela?...
  MYSQL_ROOT_PASSWORD: 新密码
```

### 8.3 修改 Dockerfile

编辑各服务的 `Dockerfile` 文件：

```dockerfile
# 换基础镜像
FROM eclipse-temurin:8-jre     # → FROM eclipse-temurin:11-jre

# 换启动参数
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=8080"]
```

### 8.4 添加新服务

在 `docker-compose.yml` 中添加新服务段：

```yaml
services:
  新服务名:
    image: 镜像名:标签
    container_name: vela-新服务名
    restart: always
    ports:
      - "端口:端口"
    environment:
      KEY: value
    depends_on:
      - mysql
```

### 8.5 修改后生效

每次修改完 `docker-compose.yml` 或 `Dockerfile`，需要重新构建并启动：

```powershell
docker-compose up -d
```

Compose 会自动检测哪些服务需要重建。

---

> 文档版本: v1.0 | 更新时间: 2026-07-25
