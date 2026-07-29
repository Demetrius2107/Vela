# 代码审查技能

## 审查检查清单
1. 遵循 DDD 分层架构，职责清晰
2. 包名符合规范
3. 类级注释包含 @description、@author、@version、@date
4. 方法注释包含 @description、@param、@return、@throws
5. 无硬编码常量，使用枚举或常量类
6. 异常处理恰当，使用统一 ResponseVO 返回
7. 事务注解 @Transactional 使用正确
8. 依赖注入使用构造器注入（推荐）或 @Autowired
9. 跨模块引用只允许：interfaces → application → domain ← infrastructure

## 编码前自检（每次请求默认思考）

### 代码结构
- 函数是否超过 50 行？（超过则考虑拆分）
- 是否有硬编码常量？（抽到常量类或配置文件中）
- 异常被 catch 后是吞掉了还是抛出了/补偿了？
- 注释写了"为什么这么做"而非"做了什么"？

### 增量改动
- 新建实体？是否有对应的建表 SQL？
- 新增接口？是否有对应的 REST 端点文档？
- 修改了消息模型？是否同步更新了 `OfflineMessageContent`？