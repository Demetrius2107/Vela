# Vela 项目长期记忆

## 用户偏好
- **图片产物保存位置**：所有可视化产物（HTML + SVG/图片）保存到 `docs/visuals/` 目录下，不要放在项目根目录
- **分支策略**：连通性测试工作在 `feat/connectivity-test` 分支上进行（2026-08-08 创建）

## 关键发现
- vela-tcp config.yml 的 logicUrl 原指向已清空的 8000 端口，已修复为 8889（网关，因8888被宝塔占用）
- 所有前端聊天页使用 Mock 数据，未对接真实后端 API
- 零单元测试覆盖
- **Spring Boot 多模块打包冲突**：12 个微服务模块的 spring-boot-maven-plugin 需加 `<classifier>exec</classifier>`，否则被依赖模块找不到类
- 运行服务用 `-exec.jar`，如 `java -jar vela-service-user-1.0-SNAPSHOT-exec.jar`
- **SnakeYAML 版本**：父 pom 原 snakeyaml.version=2.0 与 Spring Boot 2.3.2 不兼容，已改为 1.24
- **Gateway 不需要数据库**：父 pom 强制引入 MyBatis/MySQL 驱动，GatewayApplication 需排除 DataSourceAutoConfiguration
- **MySQL 配置**：Docker 映射 3307:3306，密码 root；10 个业务服务 application.yml 已全部修正
- **版本混搭风险**：parent=2.3.2 但 springboot.version=2.3.12，netty=4.1.100.Final（默认4.1.51），可能引发其他 NoSuchMethodError
- **Gateway 不需要 Nacos**：使用静态路由配置（application.yml 中写死 localhost:8010 等）

## 已知设计债（待后续重构）
- **@EnableFeignClients 缺失**：所有有 Feign 客户端的服务 Application 类都没加该注解（friendship/group/user/message），已手动补上
- **scanBasePackages 漏扫 shared 包**：所有服务都没扫 `com.vela.im.shared`，`ImServerProperties` 等 @Component 不可见，已手动补上
- **GlobalHttpClientConfig 缺少默认值**：`httpclient` 配置项缺失时 Integer 字段为 null 导致 NPE，已将 Integer 改为 int 并加默认值
- **ImFriendServiceImpl ↔ ImFriendShipRequestServiceImpl 循环依赖**：两个 domain service 互相注入，用 @Lazy 临时解决，应抽 application service 编排或用事件驱动解耦
- **ImFriendShipEntity 缺 @TableId**：MyBatis-Plus 警告找不到主键，friendship 表使用联合主键需确认 mybatisplus-plus 配置
- **项目零测试覆盖**：无任何单元测试或 Spring Context Load 测试，所有运行时问题都在首次启动时集中爆发

## Phase 1 联调方案
- 三层验证策略：Phase 1 REST API 脚本 → Phase 2 TCP 协议 → Phase 3 浏览器端到端
- 测试脚本：`deploy/scripts/phase1_api_test.py`
- 中间件启动：`docker-compose -f docker-compose.middleware.yml up -d`（MySQL/Redis/RabbitMQ/ZK）
- 需启动：MySQL/Redis/RabbitMQ/ZK + gateway(8888)/user(8010)/friendship(8011)/message(8014)/conversation(8013)
