# Vela IM — Docker 踩坑记录与排查方案

> 文档记录项目 Docker 化过程中遇到的实际问题、根因分析和解决步骤。
> 时间：2026-07-25

---

## 目录

- [问题 1：docker-compose pull/build 提示镜像不存在](#问题-1docker-compose-pullbuild-提示镜像不存在)
- [问题 2：业务服务构建失败，eclipse-temurin/maven 镜像拉不下来](#问题-2业务服务构建失败eclipse-temurinmaven-镜像拉不下来)
- [问题 3：build context 只传了 2B，COPY 找不到 jar 文件](#问题-3build-context-只传了-2bcopy-找不到-jar-文件)
- [问题 4：docker-compose 构建时报 "pull access denied"](#问题-4docker-compose-构建时报-pull-access-denied)
- [问题 5：端口占用 3306，MySQL 容器启动不了](#问题-5端口占用-3306mysql-容器启动不了)
- [问题 6：修改端口后容器仍报 unhealthy](#问题-6修改端口后容器仍报-unhealthy)
- [附录：Docker Desktop File Sharing 配置说明](#附录docker-desktop-file-sharing-配置说明)

---

## 问题 1：docker-compose pull/build 提示镜像不存在

### 现象

```text
pull access denied, repository does not exist or may require authorization:
insufficient_scope: authorization failed
```

### 根因

Docker Hub 自 2024 年起对匿名拉取实施限制，需要先登录才能拉镜像。

### 解决

```powershell
docker login
```

输入 Docker Hub 用户名和密码。一次登录永久有效，除非手动登出。

### 验证

```powershell
docker pull eclipse-temurin:8-jre
```

如果能正常拉取，说明登录生效。

---

## 问题 2：业务服务构建失败，eclipse-temurin/maven 镜像拉不下来

### 现象

```text
FROM eclipse-temurin:8-jre
ERROR: pull access denied
```

### 根因

使用了错误的镜像标签：

| 错误标签 | 问题 |
|---------|------|
| `maven:3.8.8-eclipse-temurin-8` | Docker Hub 上不存在此标签 |
| `eclipse-temurin:8-jre-focal` | `-focal` 后缀不存在，正确标签是 `eclipse-temurin:8-jre` |

### 解决

修正为正确的标签：

```dockerfile
# 构建阶段（多阶段构建需要）
FROM maven:3.6.3-jdk-8-slim AS build

# 运行阶段
FROM eclipse-temurin:8-jre
```

---

## 问题 3：build context 只传了 2B，COPY 找不到 jar 文件

### 现象

```text
#7 [vela-service internal] load build context
#7 transferring context: 2B 0.0s done    ← 只有 2 字节！

COPY vela-service/target/vela-service-*.jar app.jar
ERROR: lstat /vela-service/target: no such file or directory
```

### 根因

`.dockerignore` 文件中配置了：

```gitignore
**/target/
**/*.jar
**/*.war
```

这三行把编译产物 `target/` 目录和所有 `.jar` 文件从构建上下文中排除了，导致 Docker 引擎打包构建上下文时没有包含它们。

`docker run -v`（绑定挂载）跟 `docker build`（打包 tar）是两套机制，前者受 File Sharing 控制，后者受 `.dockerignore` 控制。

### 解决

打开项目根目录的 `.dockerignore`，删除或注释掉这三行：

```diff
-# Maven 构建产物
-**/target/
-**/*.jar
-**/*.war
+# Maven 构建产物（保留 target 以便 COPY jar 到镜像）
```

### 原理

```
docker build 流程：
项目文件 → 读取 .dockerignore 过滤 → 打包成 tar → 发送给 Docker 引擎

.dockerignore 里写了 **/target/ → target 目录被过滤 → jar 不在 tar 里 → COPY 找不到
```

---

## 问题 4：docker-compose 构建时报 "pull access denied"

### 现象

即使 `docker login` 成功，`docker-compose up -d` 仍然报权限错误。

### 根因

Docker Desktop for Windows 的 WSL2 后端默认只共享了 `C:\` 盘。如果项目在 `F:\` 盘，Docker 引擎读取文件时会因为没有添加 File Sharing 而失败。

### 解决

在 Docker Desktop 的 Settings → Resources → File Sharing 中添加 `F:\` 或具体路径 `F:\PersonalProject-Demetrius\Vela`，然后 Apply & Restart。

### 验证

```powershell
docker run --rm -v .:/workspace alpine ls /workspace
```

如果能看到项目文件列表，说明 File Sharing 配置成功。

---

## 问题 5：端口占用 3306，MySQL 容器启动不了

### 现象

```text
Error response from daemon: ports are not available:
exposing port TCP 0.0.0.0:3306 -> 127.0.0.1:0:
listen tcp 0.0.0.0:3306: bind: Only one usage of each socket address...
```

### 根因

宿主机上已有一个 MySQL 服务占用了 3306 端口（Windows 本地安装的 MySQL 或之前运行的容器）。

### 解决

有两种方案：

**方案 A：停掉本机 MySQL（推荐）**

```powershell
net stop mysql
# 或
net stop MySQL80
docker-compose restart mysql
```

**方案 B：修改 Docker MySQL 的映射端口**

```yaml
# docker-compose.yml
ports:
  - "3307:3306"   # 宿主机 3307 → 容器内 3306
```

注意：业务服务通过 Docker 内部网络访问 MySQL，使用 `mysql:3306`（容器名:内部端口），不依赖宿主机端口映射，所以无需修改应用配置。

---

## 问题 6：修改端口后容器仍报 unhealthy

### 现象

修改端口后重新 `docker-compose up -d`，MySQL 仍然是 unhealthy。

### 根因

旧容器（使用 3306 端口）还残留在系统中，与新的容器冲突。

### 解决

```powershell
# 清理所有已停止的容器
docker container prune -f

# 彻底清理当前 compose 的所有资源
docker-compose down

# 重新启动
docker-compose up -d
```

---

## 附录：Docker Desktop File Sharing 配置说明

### 为什么要配置

Docker Desktop for Windows 的 WSL2 后端在 `docker build` 时需要读取宿主机文件并打包成 tar 发送给 Docker 引擎。默认只有 `C:\` 盘被允许读取，其他盘需要手动添加。

### 不配置会怎样

- `docker run -v` 绑定挂载可以正常工作（由 Docker CLI 直接映射路径）
- `docker build` / `docker-compose build` 会失败（Docker 引擎无权限读取文件）
- 现象为 build context 只有 2B

### 配置路径

Docker Desktop → ⚙️ Settings → Resources → File Sharing → 添加 `F:\` 或具体项目路径 → Apply & Restart

### 影响范围

只影响 Docker 引擎读取文件的权限，不影响 Windows 文件系统安全。不是网络共享，不暴露端口，不改变文件属性。

---

> 文档版本: v1.0 | 更新时间: 2026-07-25
