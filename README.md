# 学习搭子 StudyBuddy

> 《团队规范项目实训》课程编号 00931 · 个人技能实训（实验一 ~ 实验五）
> 学号：20243190415　姓名：方野

一个贯穿五个实验的全栈项目：从 Web 基础工程 → 前后端分离 → Git 协作与 CI →
登录鉴权与 AI 安全 → LLM 智能问答原型。

---

## 一、技术栈

| 层次 | 技术 | 版本 | 对应实验 |
| --- | --- | --- | --- |
| 构建 | Maven | 3.9.16 | 实验一 |
| 运行环境 | JDK | 21 | 实验一 |
| 后端框架 | Spring Boot | 3.3.5 | 实验一 |
| 持久层 | MyBatis | 3.5.14 | 实验一 |
| 数据库 | MySQL | 8.x | 实验一 |
| 前端 | Vue 3 + Vite + Element Plus | Vue 3.4 / Vite 5 | 实验二 |
| 网络请求 | Axios | 1.7 | 实验二 |
| 版本控制 | Git + GitHub Actions | — | 实验三 |
| 安全 | Spring Security + JWT(jjwt) + BCrypt | Security 6.3 / jjwt 0.12.6 | 实验四 |
| 大模型 | 兼容 OpenAI 协议的任意服务 | — | 实验五 |
| 切面 | Spring AOP | — | 功能增强（操作审计） |
| 接口文档 | springdoc-openapi + Swagger UI | 2.6.0 | 功能增强 |

## 二、目录结构

```
studybuddy/
├── studybuddy-backend/              # Spring Boot 后端（实验一/二/四/五）
│   ├── src/main/java/com/zjnu/studybuddy/
│   │   ├── controller/              # 接口层（RESTful）
│   │   ├── service/ + impl/         # 业务层（接口 + 实现）
│   │   ├── mapper/                  # MyBatis 数据访问接口
│   │   ├── entity/                  # 实体（User / Role / Permission）
│   │   ├── dto/ vo/                 # 入参 / 出参对象
│   │   ├── config/                  # 数据源、CORS、Security、属性绑定
│   │   ├── security/                # JWT 工具、过滤器、UserDetails
│   │   ├── ai/                      # LLM 客户端、提示模板、RAG、注入防护、多轮上下文装配
│   │   ├── annotation/ aspect/      # 自定义注解 @OpLog 与审计切面（功能增强）
│   │   └── common/                  # 统一返回、异常、分页、脱敏
│   ├── src/main/resources/
│   │   ├── application.yml          # 数据源 / MyBatis / JWT / AI 配置
│   │   ├── mapper/*.xml             # SQL 映射文件
│   │   ├── db/studybuddy.sql        # 建库建表脚本（8 张表）
│   │   ├── db/upgrade-v2.sql        # 功能增强的增量迁移脚本
│   │   └── kb/course-faq.txt        # RAG 知识库
│   └── src/test/java/...            # 单元测试（CI 中执行）
├── studybuddy-frontend/             # Vue3 前端（实验二）
│   └── src/{api,router,utils,views}/ # Axios 封装 / 路由 / 登录态与权限 / 页面
├── .github/workflows/ci.yml         # CI 流水线（实验三）
├── .github/pull_request_template.md # PR 模板（实验三）
├── docs/                            # 协作规范、AI 使用公约、接口文档、知识点详解
└── ai-demo/                         # 实验五 Python 演示脚本
```

> **功能增强（第二轮补充）**：在五个实验之外，另做了七项增强——RBAC 闭环（角色权限管理与用户角色分配）、本人自助改密、多轮对话会话持久化、知识库在线查看与热更新、基于 AOP 的操作审计日志、首页数据看板、OpenAPI 接口文档。详见实验报告的相应章节与 `docs/知识点详解.md` 第 9 章。

## 三、本地运行（复现步骤）

### 1. 准备数据库

```bash
# 方式 A：命令行
mysql -uroot -p < studybuddy-backend/src/main/resources/db/studybuddy.sql

# 方式 B：Navicat 中打开 studybuddy.sql 直接执行
```

### 2. 配置数据库密码

`application.yml` 中的密码取自环境变量 `DB_PASSWORD`，未设置时默认 `123456`：

```yaml
spring:
  datasource:
    username: root
    password: ${DB_PASSWORD:123456}
```

将其改为你本机 MySQL 的密码，或设置环境变量后启动：

```bash
# Windows PowerShell
$env:DB_PASSWORD="你的密码"
```

### 3. 启动后端

```bash
cd studybuddy-backend
mvn spring-boot:run
# 启动后访问 http://localhost:8080/api/hello 验证
```

打包部署（内嵌 Tomcat 打成可执行 jar）：

```bash
mvn -B clean package -DskipTests
java -jar target/studybuddy-backend-1.0.0.jar
```

### 4. 启动前端

```bash
cd studybuddy-frontend
npm install
npm run dev
# 浏览器打开 http://localhost:5173
```

### 5. 默认账号（首次启动自动创建）

| 用户名 | 密码 | 角色 | 说明 |
| --- | --- | --- | --- |
| admin | admin123 | ROLE_ADMIN | 全部 10 个权限点 |
| teacher | teacher123 | ROLE_TEACHER | 用户查询 + 智能问答 + 角色查询 + 知识库管理 + 日志 + 看板 |
| student | student123 | ROLE_STUDENT | 智能问答 + 看板 |

### 6. 启用实验五的真实大模型（可选）

未配置 Key 时智能问答会自动降级为"知识库直答"，仍可演示 RAG；配置后走真实模型。
本项目默认对接 **DeepSeek 官方**（`deepseek-flash`）。

推荐把密钥写进本地私有文件 `studybuddy/.env.local`（已被 `.gitignore` 忽略，
`start-backend.sh` 会自动加载），**不要写进 `application.yml`**：

```bash
# studybuddy/.env.local
DB_PASSWORD=你的MySQL密码
LLM_API_KEY=sk-你的DeepSeek密钥
```

临时传参也可以：

```bash
DB_PASSWORD=xxx LLM_API_KEY=sk-xxx bash start-backend.sh
```

切换服务商时改 `base-url` 与 `model` 两个值即可（均为 OpenAI 兼容协议）：

| 服务商 | base-url | model 示例 |
| --- | --- | --- |
| DeepSeek 官方（默认） | `https://api.deepseek.com` | `deepseek-flash` |
| 阿里云百炼 | `https://dashscope.aliyuncs.com/compatible-mode/v1` | `qwen-turbo` |
| 本地 Ollama | `http://localhost:11434/v1` | `qwen2.5:7b` |

> 注意：Key 与 base-url 必须属于同一服务商。指向 DashScope 却用 DeepSeek 的 Key，
> 会收到 `invalid_api_key`，此时智能问答会静默降级为知识库直答。

修改 `application.yml` 后**必须重新打包**（配置是打进 jar 的），再启动才会生效。
`start-backend.sh` 已能自动识别"src 比 jar 新"并重新打包。

## 四、复现实验一~三的接口截图

实验四引入了 Spring Security。若要按下述"无鉴权"的方式直接调用用户 CRUD 接口
（对应实验一、实验二的原始形态），可关闭安全开关：

```bash
# application.yml 中
studybuddy:
  security:
    enabled: false
```

此时所有接口直接放行；实验四开始再改回 `true`，即可看到 401/403 的效果对比。

## 五、接口速览

> 完整、最新的接口文档由后端自动生成，见 Swagger UI：<http://localhost:8080/swagger-ui/index.html>（JSON：`/v3/api-docs`）。下表为速览。

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| GET | /api/hello | 连通性测试 | 公开 |
| POST | /api/auth/login | 登录，返回 JWT、角色与权限 | 公开 |
| POST | /api/auth/register | 注册 | 公开 |
| GET | /api/auth/me | 当前登录用户及其权限 | 登录 |
| PUT | /api/auth/password | 修改本人密码（校验旧密码） | 登录 |
| GET | /api/users | 用户分页查询 | user:list |
| GET | /api/users/{id} | 用户详情 | user:list |
| POST | /api/users | 新增用户 | user:create |
| PUT | /api/users/{id} | 更新用户 | user:update |
| DELETE | /api/users/{id} | 删除用户 | user:delete |
| PUT | /api/users/{id}/password | 重置密码（管理员） | user:update |
| PUT | /api/users/{id}/roles | 分配角色（增强） | role:assign |
| GET | /api/roles | 角色列表（含权限与人数）（增强） | role:list |
| GET | /api/roles/permissions | 权限点全集（增强） | role:list |
| PUT | /api/roles/{id}/permissions | 调整角色权限（增强） | role:assign |
| GET | /api/kb/chunks · /api/kb/stats | 知识库查看（增强） | kb:manage |
| POST | /api/kb/reload · /api/kb/chunks | 知识库热更新（增强） | kb:manage |
| GET | /api/logs | 操作审计日志（增强） | log:list |
| GET | /api/stats/overview | 数据看板（增强） | stats:view |
| POST | /api/ai/chat | 智能问答（支持多轮） | ai:chat |
| GET | /api/ai/sessions | 我的会话列表（增强） | ai:chat |
| GET | /api/ai/sessions/{id}/messages | 会话消息历史（增强） | ai:chat |
| DELETE | /api/ai/sessions/{id} | 删除会话（增强） | ai:chat |

## 六、AI 使用说明

本项目在开发过程中使用了 AI 编程助手辅助。所有 AI 生成的代码均经过人工逐行理解，
关键安全逻辑（密码加密、鉴权、注入防护）由人工确认。详细记录见
[`docs/AI使用公约与记录.md`](docs/AI使用公约与记录.md)。

> 声明：仓库中不含任何真实密钥、密码明文。数据库密码与大模型 Key 一律通过环境变量注入。
