# Vela — 即时通讯系统

> **企业级 IM + 办公生态一体化系统** | SpringBoot + Netty + Vue 3

---

## 目录

- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [功能矩阵](#功能矩阵)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [模块说明](#模块说明)
- [开发规范](#开发规范)
- [更新日志](#更新日志)

---

## 项目概述

Vela 是一个涵盖 IM 即时通讯、管理后台、办公生态、音视频通话的全栈项目。后端基于 DDD 六边形架构，前端支持 Web / Android / Electron 多端。

### 核心架构

```
Client ──TCP/WS──→ vela-tcp(网关) ──MQ──→ vela-service(业务) ──MQ──→ vela-message-store(存储)
                       │                         │
                       ├── Redis (缓存/会话)      ├── MySQL (持久化)
                       ├── RabbitMQ (事件)        ├── Elasticsearch (全文检索)
                       └── ZooKeeper (注册)        └── Logstash (日志采集)
                                                       └── Kibana (可视化)
```

### 统计

| 指标 | 数据 |
|:----|:----:|
| Java 源码 | ~400+ 文件 |
| 单元测试 | 123 个 |
| REST 端点 | 60+ 个 |
| Docker 容器 | 16 个 |
| Git 提交 | 63+ 个 |

---

## 技术栈

| 类别 | 技术 | 用途 |
|------|------|------|
| 开发语言 | Java 17 + Kotlin | 后端 + Android |
| 框架 | SpringBoot 2.3.2 | 业务服务容器 |
| 网络框架 | Netty 4.1 | TCP/WebSocket 长连接 |
| ORM | MyBatis-Plus 3.4.2 | 数据库访问 |
| 缓存 | Redis 6.2 | Session/离线消息/序列号 |
| 消息队列 | RabbitMQ 3.8 | 异步解耦/事件驱动 |
| 注册中心 | ZooKeeper 3.6 | 网关节点发现 |
| 全文检索 | Elasticsearch 7.17 | 消息搜索 + 日志存储 |
| 日志采集 | Logstash 7.17 + Kibana 7.17 | ELK 日志体系 |
| 序列化 | Protostuff | TCP 协议编解码 |
| 前端 | Vue 3 + Naive UI | Web 端 IM |
| 桌面端 | Electron 28 | 桌面 IM 客户端 |
| 移动端 | Kotlin + Jetpack Compose | Android 客户端 |
| 监控 | Prometheus + Grafana + SkyWalking | 指标/APM |
| 构建 | Maven + Gradle | 后端 + Android |

---

## 功能矩阵

### Phase 0-4 — IM 核心 ✅

| 模块 | 功能 |
|:----|:------|
| 文字消息 | P2P + 群聊消息收发、ACK、去重、多端同步 |
| 消息撤回 | 可配置撤回窗口 + 时钟偏差容错 |
| 已读回执 | 单聊 + 群聊已读通知 |
| 离线消息 | Redis ZSet 增量拉取 + 超限降级 DB |
| 会话管理 | 置顶/免打扰/删除/标记已读 |
| 好友关系 | 增删改查/分组/黑名单/请求审批 |
| 群组管理 | 创建/解散/禁言/转让/角色管理/群公告/群投票 |
| 多端登录 | 4 种策略（单端~不限制）|
| TCP/WS 网关 | Netty 双协议 + 心跳 + 注册发现 |
| 链路追踪 | MDC TraceId 全链路透传 |

### Phase 0.5 — L2 异常边界 ✅

| 功能 | 说明 |
|:----|:------|
| 消息重试 | 指数退避重试（可配 3 次）|
| ACK 重推 | PendingAckTracker + 定时扫描 |
| 降级框架 | ServiceDegradationManager（Redis/MQ 熔断）|
| DB 补偿 | MessageCompensationStore + 定时重试 |
| 并发锁 | MessageLockManager（ReadWriteLock 协调撤回↔推送）|
| 时间容错 | 可配时钟偏差 + 反向偏差检查 |

### Phase 5 — 管理后台 ✅

| 模块 | 功能 |
|:----|:------|
| 数据看板 | 统计卡片 + 消息趋势 + Top 10 群组 |
| 用户管理 | 搜索/分页/详情/批量禁用/登录日志 |
| 群组管理 | 列表/状态筛选/详情/解散/导出 |
| 消息审计 | ES 全文搜索 + SQL LIKE 降级 |
| 操作日志 | 自动记录全部管理操作 |
| 管理员 | 超管/运营/审计三级权限 |
| 系统配置 | 动态参数调整 |

### Phase 6 — 办公生态 ✅

| 模块 | 功能 |
|:----|:------|
| 日程管理 | 创建/列表/状态/删除 |
| 待办管理 | 创建/列表/优先级/完成 |
| 审批流程 | 提交/审批通过/拒绝 |

### 前端客户端 ✅

| 平台 | 状态 | 说明 |
|:----|:----:|:------|
| Web (Vue 3) | ✅ | 完整 IM + 管理后台 + 办公生态 |
| Android (Compose) | ✅ | 登陆/注册/会话列表/聊天/通讯录 |
| Electron 桌面端 | ✅ | Web 套壳 + 系统托盘 + 窗口管理 |

### 基础设施 ✅

| 组件 | 用途 |
|:----|:------|
| Docker Compose | 16 个容器一键启动 |
| Elasticsearch | 消息全文检索 |
| Kibana | 日志可视化 |
| Logstash | 日志采集管道 |
| Prometheus + Grafana | 指标监控 |
| SkyWalking | APM 链路追踪 |

---

## 快速开始

### Docker 一键启动（推荐）

```bash
# 1. 构建后端
mvn clean package -DskipTests -q

# 2. 启动全部服务
docker-compose up -d
```

### 手动启动

```bash
# 1. 启动中间件：MySQL / Redis / RabbitMQ / ZooKeeper
# 2. 启动业务服务（端口 8000）
cd vela-service && mvn spring-boot:run

# 3. 启动消息存储
cd vela-message-store && mvn spring-boot:run

# 4. 启动 TCP/WS 网关（端口 9000 / 19000）
cd vela-tcp && mvn spring-boot:run

# 5. 启动前端
cd web && npm install && npm run dev
```

详细部署指南参见 [`docs/guide/deployment-guide.md`](docs/guide/deployment-guide.md)。

### 访问入口

| 入口 | 地址 |
|:----|:-----|
| IM Web 端 | http://localhost:3000 |
| 管理后台 | http://localhost:3000/#/admin |
| 办公生态 | http://localhost:3000/#/office |
| Kibana | http://localhost:5601 |
| Grafana | http://localhost:3000 (admin/admin) |

---

## 项目结构

```
Vela/
├── vela-common/           # 共享内核层（枚举/常量/消息类型/配置）
├── vela-codec/            # 基础设施：TCP/WS 协议编解码
├── vela-tcp/              # 接口适配层：Netty TCP/WS 网关
├── vela-service/          # 核心业务层（DDD 五域）
│   └── src/main/java/com/vela/im/service/
│       ├── user/          # 用户域
│       ├── friendship/    # 好友关系域
│       ├── group/         # 群组域（含群公告/投票/标签/文件）
│       ├── message/       # 消息域（含 ES 搜索/已读跟踪）
│       ├── conversation/  # 会话域
│       ├── admin/         # 管理后台
│       ├── bot/           # Bot 机器人
│       ├── office/        # 办公生态（日程/待办/审批）
│       └── application/   # 应用服务（管道/降级/WebRTC)
├── vela-message-store/    # 基础设施：消息持久化服务
├── vela-gateway/          # API 网关
├── web/                   # Vue 3 前端（IM/管理后台/办公）
├── android/               # Android 客户端（Kotlin + Compose）
├── electron/              # Electron 桌面端
├── deploy/                # 部署配置（Logstash/Prometheus）
├── docs/                  # 文档
│   ├── guide/             # 部署指南/Docker 指南
│   ├── analysis/          # 差距分析/功能对比
│   ├── roadmap/           # 迭代计划/TODO 清单
│   ├── architecture/      # 架构设计文档
│   └── 会议记录/           # 会话工作记录
└── docker-compose.yml     # 16 容器编排
```

---

## 开发规范

参见 [`AGENTS.md`](AGENTS.md) 和 `.atomcode/skills/`。

核心规则：

```
1. DDD 分层依赖：interfaces → application → domain ← infrastructure
2. 构造器注入，非 @Autowired
3. 函数不超过 50 行，硬编码常量抽到配置
4. 新建实体同步建表 SQL / 修改消息模型同步更新 OfflineMessageContent
5. 注释写"为什么这么做"而非"做了什么"
6. Git 提交格式：<type>(<scope>): <subject>
```

---

## 更新日志

### 2026-07-28 — 单元测试覆盖 + 部署文档 + Electron

- 新增 40 个单元测试（办公生态/Admin/Bot/通话），总计 123 个
- Docker 部署文档（从零到一键启动完整指南）
- Electron 桌面端（系统托盘 + 窗口管理）
- 办公生态（日程/待办/审批）+ 消息推送（Web Notification）

### 2026-07-27 — ES + 管理后台 + Bot + Android

- Elasticsearch 全文搜索 + ELK 日志体系
- 管理后台补全（操作日志/角色权限/系统配置/趋势导出）
- Bot 深挖（富文本回复/群聊 @mentions/速率限制）
- Android 客户端对接真实 API
- WebRTC 音视频通话信令

### 2026-07-25 — 前端开发与 Docker 集成

- Vue 3 + Naive UI 前端客户端（11 个页面）
- Docker Compose 一键启动（含网关 + 监控 + 前端）
- API 网关、Prometheus、SkyWalking 集成

### 2026-07-19 — DDD 六边形架构重构

- 全项目 DDD 四层架构重构
- 包名更新为分层结构
- GitHub Actions CI + `.atomcode` 编码规范

---

> Copyright © 2026. All rights reserved.
