# Spider Media - 蜘蛛自媒体运营中台

全链路自媒体运营中台，覆盖「数据采集 → 热点抓取 → AI创作 → 任务调度 → 多平台发布」完整闭环。

## 功能模块

| 模块 | 功能 | 说明 |
|------|------|------|
| **数据采集** | 对标账号管理、竞品数据抓取 | 支持微信公众号、百家号、头条号、小红书、抖音、知乎 |
| **AI创作** | 全网热点抓取、AI文章生成 | 接入 DeepSeek、智谱、通义千问 |
| **内容发布** | 多平台账号管理、定时/即时发布 | 图文内容统一发布，支持定时排期 |
| **任务调度** | Cron 定时任务、执行监控 | 支持热点抓取、数据采集、内容发布等定时任务 |
| **系统管理** | 用户管理、角色权限、系统配置 | JWT 认证、数据隔离 |

## 技术栈

**后端**

- Java 17 + Spring Boot 3.3
- MyBatis + PageHelper
- Spring Security + JWT
- Redis（缓存/会话）
- Quartz（任务调度）
- Druid（连接池）

**前端**

- Vue 3 + TypeScript
- Vite 5
- Element Plus
- Pinia（状态管理）
- Axios

**数据库**

- MySQL 8.0+

## 项目结构

```
spider-media/
├── backend/
│   ├── spider-admin/            # 启动模块（入口）
│   ├── spider-common/           # 公共工具（分页、异常、结果封装）
│   ├── spider-framework/        # 框架配置（安全、JWT、跨域、拦截器）
│   ├── spider-system/           # 系统管理（用户、登录、注册）
│   ├── spider-datacollection/   # 数据采集（对标账号、采集文章）
│   ├── spider-aicreation/       # AI创作（热点话题、文章生成）
│   ├── spider-contentpublish/   # 内容发布（平台账号、发布任务）
│   └── spider-taskscheduler/    # 任务调度（定时任务管理）
├── frontend/
│   └── src/
│       ├── api/                 # API 接口定义
│       ├── views/               # 页面组件
│       ├── stores/              # Pinia 状态管理
│       ├── router/              # 路由配置
│       ├── types/               # TypeScript 类型
│       └── utils/               # 工具函数（请求封装）
└── README.md
```

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis

### 数据库初始化

```sql
CREATE DATABASE spider_media DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

执行 `backend/spider-admin/src/main/resources/db/schema.sql` 创建表结构。

### 后端启动

```bash
cd backend

# 复制配置文件并修改数据库密码、JWT密钥等
cp spider-admin/src/main/resources/application.yml.example \
   spider-admin/src/main/resources/application.yml

# 编译
mvn clean install -DskipTests

# 启动（IDEA 中运行 SpiderMediaApplication，或）
java -jar spider-admin/target/spider-admin-1.0.0-SNAPSHOT.jar
```

后端默认端口：`8080`

### 前端启动

```bash
cd frontend

npm install
npm run dev
```

前端默认端口：`3000`，自动代理 `/api` 到后端 `8080`。

### 访问系统

打开浏览器访问 http://localhost:3000

## 配置说明

复制 `application.yml.example` 为 `application.yml`，修改以下配置：

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `spring.datasource.username` | 数据库用户名 | root |
| `spring.datasource.password` | 数据库密码 | - |
| `token.secret` | JWT 密钥（至少32字符） | - |
| `token.expireTime` | Token 过期时间（毫秒） | 86400000（24小时） |
| `ai.models.deepseek.api-key` | DeepSeek API Key | - |
| `ai.models.zhipu.api-key` | 智谱 API Key | - |

## API 接口

| 模块 | 路径前缀 | 说明 |
|------|----------|------|
| 认证 | `/api/auth` | 登录、注册、获取用户信息 |
| 数据采集 | `/collection` | 对标账号 CRUD、触发采集、查询文章 |
| AI创作 | `/ai` | 抓取热点、生成文章、查询文章列表 |
| 内容发布 | `/publish` | 平台账号管理、创建/发布任务 |
| 任务调度 | `/scheduler` | 创建/启用/停用定时任务 |

## License

MIT License
