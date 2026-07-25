# Vela IM — Docker 操作指南

> 适用环境：Windows + Docker Desktop (WSL 2 backend)
> 项目路径：`F:\PersonalProject-Demetrius\Vela`

---

## 目录

- [一、基础概念](#一基础概念)
- [二、Docker Compose 全量启动（完整版）](#二docker-compose-全量启动完整版)
- [三、仅启动中间件（开发模式）](#三仅启动中间件开发模式)
- [四、单独启动某个服务](#四单独启动某个服务)
- [五、查看状态与日志](#五查看状态与日志)
- [六、停止与清理](#六停止与清理)
- [七、镜像操作](#七镜像操作)
- [八、容器操作](#八容器操作)
- [九、实用组合命令速查](#九实用组合命令速查)

---

## 一、基础概念

```
┌──────────────┐    docker build     ┌──────────────┐    docker run     ┌──────────────┐
│  Dockerfile   │ ──────────────────→ │   镜像 Image   │ ───────────────→ │  容器 Container │
│  (构建配方)    │                    │  (编译好的jar   │                 │  (正在运行的    │
│               │                    │   + JDK环境)   │                 │   Java进程)    │
└──────────────┘                    └──────────────┘                 └──────────────┘
                                           │                               │
                                      docker push                       docker stop
                                           ▼                               ▼
                                    Docker Hub (仓库)                 停止的容器
                                                                   (可重启，也可删除)

    docker-compose = 批量管理多个容器的工具（一次性启动/停止一组服务）
```

| 术语 | 类比 | 特点 |
|------|------|------|
| **镜像 (Image)** | 类的定义 / ISO 安装包 | 只读，可分享，可复用 |
| **容器 (Container)** | 类的实例 / 正在运行的系统 | 可读写，可启停，可删除 |
| **Dockerfile** | 构建镜像的配方 | 告诉 Docker 怎么打包 |
| **docker-compose.yml** | 服务编排清单 | 告诉 Docker 哪些容器一起跑 |

---

## 二、Docker Compose 全量启动（完整版）

### 2.1 启动全部（中间件 + 业务服务）

```powershell
# 切换到项目目录
cd F:\PersonalProject-Demetrius\Vela

# 构建镜像 + 后台启动全部容器
docker-compose up -d
```

启动顺序由 `depends_on` 控制：
```
MySQL ──→ vela-service ──→ vela-tcp
Redis ───→ vela-message-store
RabbitMQ ─→ all services
ZooKeeper ─→ all services
```

首次执行会：
1. 拉取中间件镜像（MySQL/Redis/RabbitMQ/ZooKeeper，~1GB，仅一次）
2. 构建三个业务服务镜像（先 mvn package 打包 jar，耗时 ~3-5 分钟，仅一次或代码变更后重构建）
3. 按依赖顺序启动所有容器

### 2.2 仅重新构建某个服务（代码变更后更新镜像）

```powershell
# 只重新构建 vela-service 的镜像并重启
docker-compose build vela-service
docker-compose up -d vela-service
```

---

## 三、仅启动中间件（开发模式）

日常在 IDEA 开发时，只需要中间件在 Docker 里跑着，业务服务在 IDEA 里启动（热更新方便）。

### 方式一：只启动中间件容器

```powershell
# 只启动 mysql/redis/rabbitmq/zk
docker-compose up -d mysql redis rabbitmq zookeeper
```

### 方式二：使用独立的中间件配置文件

推荐新建 `docker-compose.middleware.yml`：

```yaml
# docker-compose.middleware.yml
services:
  mysql:
    image: mysql:5.7
    container_name: vela-mysql
    restart: always
    ports:
      - "3306:3306"
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

  redis:
    image: redis:6.2
    container_name: vela-redis
    restart: always
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5

  rabbitmq:
    image: rabbitmq:3.8-management
    container_name: vela-rabbitmq
    restart: always
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    volumes:
      - rabbitmq-data:/var/lib/rabbitmq
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  zookeeper:
    image: zookeeper:3.6
    container_name: vela-zk
    restart: always
    ports:
      - "2181:2181"
    environment:
      ZOO_MY_ID: 1
    volumes:
      - zk-data:/data
      - zk-datalog:/datalog
    healthcheck:
      test: ["CMD", "echo", "ruok", "|", "nc", "localhost", "2181"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  mysql-data:
  redis-data:
  rabbitmq-data:
  zk-data:
  zk-datalog:
```

使用方式：

```powershell
# 启动中间件（不论 docker-compose.yml 里有什么，只跑这个文件定义的）
docker-compose -f docker-compose.middleware.yml up -d

# 停止中间件
docker-compose -f docker-compose.middleware.yml down
```

---

## 四、单独启动某个服务

```powershell
# 只启动某个服务（会自动启动它依赖的服务）
docker-compose up -d vela-service
docker-compose up -d mysql
docker-compose up -d redis

# 启动多个
docker-compose up -d mysql redis rabbitmq
```

---

## 五、查看状态与日志

### 5.1 查看所有容器状态

```powershell
docker-compose ps
```

输出示例：
```
NAME                IMAGE               STATUS                    PORTS
vela-mysql          mysql:5.7           Up (healthy)              0.0.0.0:3306->3306/tcp
vela-redis          redis:6.2           Up (healthy)              0.0.0.0:6379->6379/tcp
vela-rabbitmq       rabbitmq:3.8        Up (healthy)              0.0.0.0:5672->5672/tcp
vela-zk             zookeeper:3.6       Up (healthy)              0.0.0.0:2181->2181/tcp
vela-service        vela-service        Up                        0.0.0.0:8000->8000/tcp
vela-message-store  vela-message-store  Up                        0.0.0.0:0->0/tcp
vela-tcp            vela-tcp            Up                        0.0.0.0:9000->9000/tcp
```

- `Up` = 运行中
- `Up (healthy)` = 运行中且健康检查通过
- `Exit` = 已停止

### 5.2 查看日志

```powershell
# 实时查看某个服务的日志
docker-compose logs -f vela-service

# 查看最后 100 行
docker-compose logs --tail=100 vela-service

# 查看所有服务的日志
docker-compose logs -f

# 只查某段时间
docker-compose logs --since=5m vela-service
```

### 5.3 Docker Desktop 图形化查看

打开 Docker Desktop → Containers 选项卡，可以看到所有容器：
- 点容器名看日志
- 点 `>` 进入容器命令行
- 点 ⏹ / ▶ 启停

---

## 六、停止与清理

```powershell
# 停止所有容器（不会删除容器和镜像）
docker-compose stop

# 停止并删除容器（下次 up 需要重建）
docker-compose down

# 停止并删除容器 + 网络（推荐日常用）
docker-compose down --remove-orphans

# 停止并删除容器 + 网络 + 数据卷（！！！清空数据库）
docker-compose down -v

# 停止某个服务
docker-compose stop vela-service

# 重启某个服务
docker-compose restart vela-service
```

**建议的日常流程：**
```
下班： docker-compose stop         # 暂停，明天继续
换项目：docker-compose down         # 清理干净
清数据：docker-compose down -v     # 慎用，数据库全清
```

---

## 七、镜像操作

### 7.1 查看本地镜像

```powershell
docker images
```

输出：
```
REPOSITORY               TAG       IMAGE ID       SIZE
vela-vela-service        latest    abc123...      450MB    ← 你构建的
vela-vela-tcp            latest    def456...      450MB    ← 你构建的
vela-vela-message-store  latest    ghi789...      450MB    ← 你构建的
mysql                    5.7       4bc6bc9...     450MB    ← 拉取的
redis                    6.2       28df45e...     115MB    ← 拉取的
rabbitmq                 3.8       4206c16...     180MB    ← 拉取的
zookeeper                3.6       5622117...     280MB    ← 拉取的
```

### 7.2 删除镜像

```powershell
# 删除单个镜像（需要先停止并删除使用该镜像的容器）
docker rmi vela-vela-service:latest

# 删除所有未使用的镜像（谨慎）
docker image prune

# 删除所有未被任何容器引用的镜像
docker image prune -a
```

### 7.3 构建单个服务的镜像（不用 compose）

```powershell
# 构建 vela-service 镜像
docker build -t vela-service:latest -f vela-service/Dockerfile .

# 运行
docker run -d --name vela-service -p 8000:8000 vela-service:latest
```

---

## 八、容器操作

### 8.1 进入容器内部

```powershell
# 进入容器的命令行
docker exec -it vela-mysql bash

# 在 MySQL 容器里执行 SQL
docker exec -it vela-mysql mysql -uroot -proot -e "SHOW DATABASES;"

# 在 Redis 容器里执行命令
docker exec -it vela-redis redis-cli ping
```

### 8.2 查看容器资源占用

```powershell
# 所有容器的 CPU/内存/网络
docker stats
```

### 8.3 查看容器详情

```powershell
# 查看容器配置
docker inspect vela-mysql

# 查看容器 IP 地址
docker inspect vela-mysql | grep IPAddress
```

---

## 九、实用组合命令速查

| 场景 | 命令 |
|------|------|
| **第一次部署** | `docker-compose up -d` |
| **日常开发（只跑中间件）** | `docker-compose up -d mysql redis rabbitmq zookeeper` |
| **改代码后更新服务镜像** | `docker-compose build vela-service && docker-compose up -d vela-service` |
| **看所有服务状态** | `docker-compose ps` |
| **看服务日志** | `docker-compose logs -f vela-service` |
| **重启某个服务** | `docker-compose restart vela-service` |
| **下班关闭** | `docker-compose stop` |
| **完全清理** | `docker-compose down --remove-orphans` |
| **连 MySQL** | `docker exec -it vela-mysql mysql -uroot -proot` |
| **连 Redis** | `docker exec -it vela-redis redis-cli` |
| **打开 RabbitMQ 管理后台** | 浏览器访问 `http://localhost:15672` |
| **查看本地镜像列表** | `docker images` |
| **清理未使用的镜像** | `docker image prune` |

---

> 文档版本: v1.0 | 更新时间: 2026-07-25
