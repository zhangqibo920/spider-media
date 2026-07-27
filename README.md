# Spider Media - 蜘蛛自媒体运营中台

全链路自媒体运营中台，覆盖「数据采集 → 热点抓取 → AI创作 → 任务调度 → 多平台发布」完整闭环。支持 **RBAC 动态路由**，侧边栏和菜单权限基于角色自动生成。

## 功能模块

| 模块 | 功能 | 说明 |
|------|------|------|
| **数据采集** | 对标账号管理、竞品数据抓取 | 支持微信公众号、百家号、头条号、小红书、抖音、知乎 |
| **AI创作** | 全网热点抓取、AI文章生成 | 支持多模型切换，模型可在后台配置和测试 |
| **内容发布** | 多平台账号管理、定时/即时发布 | 图文内容统一发布，支持定时排期 |
| **任务调度** | Cron 定时任务、执行监控 | 支持热点抓取、数据采集、内容发布等定时任务 |
| **系统管理** | 用户/配置/字典/模型/日志管理 | 完整的后台管理面板 |

## 技术栈

**后端**

- Java 17 + Spring Boot 3.3
- MyBatis + PageHelper
- Spring Security + JWT（RBAC 权限控制）
- Spring AOP（操作日志）
- Druid（连接池）
- Resilience4j（熔断、限流、重试）

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
├── spider-server/            # 后端服务（Java Spring Boot）
│   ├── spider-admin/         # 启动模块（入口）
│   ├── spider-common/        # 公共工具（分页、异常、结果封装、字典常量）
│   ├── spider-framework/     # 框架配置（安全、JWT、跨域、全局异常处理）
│   ├── spider-system/        # 系统管理（用户、角色、菜单、配置、字典、操作日志）
│   ├── spider-datacollection/# 数据采集（对标账号、采集文章）
│   ├── spider-aicreation/    # AI创作（热点话题、文章生成、模型管理）
│   ├── spider-contentpublish/# 内容发布（平台账号、发布任务）
│   └── spider-taskscheduler/ # 任务调度（定时任务管理）
├── spider-web/               # 前端应用（Vue 3 + TypeScript）
│   └── src/
│       ├── api/              # API 接口定义
│       ├── components/       # 公共组件（DictTag、layout 布局）
│       ├── composables/      # 组合式函数（useDict、usePolling）
│       ├── constants/        # 字典 fallback 数据
│       ├── views/            # 页面组件
│       ├── stores/           # Pinia 状态管理（user、app）
│       ├── router/           # 路由配置（含动态路由生成）
│       ├── types/            # TypeScript 类型定义
│       └── utils/            # 工具函数（请求封装、防抖）
└── README.md
```

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+

### 数据库初始化

```sql
CREATE DATABASE spider_media DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

执行以下 SQL 脚本（位于 `spider-server/spider-admin/src/main/resources/db/`，**按顺序执行**）：

```bash
# 1. 完整建表（sys_user、sys_config、sys_menu、sys_role、sys_role_menu、
#    sys_user_role、sys_oper_log、sys_dict、AI 模型等）并初始化菜单与角色
mysql -u root -p spider_media < schema.sql

# 2. 字典数据（状态、角色、平台等）
mysql -u root -p spider_media < dict-init.sql

# 3. AI 模型管理表（schema.sql 已包含此表，仅需首次建表时执行）
mysql -u root -p spider_media < ai-model-init.sql
```

### 后端启动

```bash
cd spider-server

# 复制配置文件并修改数据库密码、JWT密钥等
cp spider-admin/src/main/resources/application.yml.example \
   spider-admin/src/main/resources/application.yml

# 编译
mvn clean install -DskipTests

# 启动
java -jar spider-admin/target/spider-admin-1.0.0-SNAPSHOT.jar
```

后端默认端口：`8080`

### 前端启动

```bash
cd spider-web
npm install
npm run dev
```

前端默认端口：`3000`，自动代理 `/api` 到后端 `8080`。

### 访问系统

打开浏览器访问 http://localhost:3000

## 配置说明

### 应用配置

复制 `application.yml.example` 为 `application.yml`，修改以下配置：

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `spring.datasource.username` | 数据库用户名 | root |
| `spring.datasource.password` | 数据库密码 | - |
| `token.secret` | JWT 密钥（至少32字符） | - |
| `token.expireTime` | Token 过期时间（毫秒） | 86400000（24小时） |

### AI 模型配置（后台管理）

AI 模型的 API 密钥和地址通过 **系统管理 → 模型管理** 在线配置，无需修改配置文件：

1. 进入系统管理 → 模型管理
2. 编辑模型，填入 API 密钥
3. 点击"测试"验证连通性
4. 启用模型

支持的模型：DeepSeek、智谱 GLM-4、OpenAI 及其他兼容 OpenAI API 格式的模型。

## 字典系统

采用 RuoYi 风格的数据字典设计，字典数据存储在数据库中，通过管理后台在线维护。

### 字典类型

| 字典类型 | 说明 | 字典值 |
|----------|------|--------|
| `sys_user_status` | 用户状态 | 0=正常, 1=停用 |
| `sys_user_role` | 用户角色 | USER=普通用户, ADMIN=管理员 |
| `sys_menu_type` | 菜单类型 | M=目录, C=菜单, F=按钮 |
| `sys_visible` | 菜单可见性 | 0=显示, 1=隐藏 |
| `sys_normal_status` | 正常/停用 | 0=正常, 1=停用 |
| `pb_publish_status` | 发布任务状态 | 0=草稿, 1=发布中, 2=已发布, 3=失败 |
| `pb_account_status` | 发布账号状态 | 0=在线, 1=离线 |
| `dc_account_status` | 对标账号状态 | 0=监控中, 1=已暂停 |
| `ts_task_status` | 定时任务状态 | 0=已停止, 1=运行中 |
| `ac_article_status` | AI文章状态 | GENERATING/COMPLETED/FAILED |
| `hot_topic_platform` | 热点平台 | weibo/douyin/zhihu/toutiao |
| `publish_platform` | 发布平台 | wechat/toutiao/baijia/xiaohongshu/douyin |
| `collection_platform` | 采集平台 | wechat/baijia/toutiao/xiaohongshu/douyin/zhihu |

### 前端使用

```vue
<!-- DictTag 组件自动渲染标签 -->
<DictTag dict-type="sys_user_status" :value="row.status" />

<!-- useDict composable -->
const { dict } = useDict('pb_publish_status')
const label = getDictLabel(dict, '0')  // => '草稿'
```

### 后台管理

进入 **系统管理 → 字典管理**，可在线增删改查字典类型和字典数据。

## API 接口

| 模块 | 路径前缀 | 说明 |
|------|----------|------|
| 认证 | `/api/auth` | 登录、注册、获取用户信息 |
| 菜单路由 | `/api/menu` | 获取当前用户路由树（动态生成侧边栏） |
| 数据采集 | `/api/collection` | 对标账号 CRUD、触发采集、查询文章 |
| AI创作 | `/api/ai` | 抓取热点、生成文章、查询文章列表 |
| AI模型 | `/api/ai/model` | 模型 CRUD、测试连通性、启停控制 |
| 内容发布 | `/api/publish` | 平台账号管理、创建/发布任务 |
| 任务调度 | `/api/scheduler` | 创建/启用/停用定时任务 |
| 系统管理 | `/api/admin` | 用户/角色/菜单/配置/日志管理 |
| 字典管理 | `/api/dict` | 字典类型和字典数据 CRUD |
| 仪表盘 | `/api/dashboard` | 系统统计数据 |

## 系统管理功能

| 功能 | 说明 |
|------|------|
| **菜单管理** | 树形菜单 CRUD，支持多级嵌套、图标选择、路由配置 |
| **角色管理** | 角色 CRUD，菜单权限勾选分配（内置 ADMIN/USER 不可删除） |
| **用户管理** | 用户列表、角色分配、编辑信息、重置密码、启用/禁用 |
| **系统配置** | 键值对配置管理，支持分组查询 |
| **操作日志** | AOP 自动记录，支持分页和筛选 |
| **模型管理** | AI 模型配置、测试连通性、启停控制 |
| **字典管理** | 数据字典在线维护，前端实时生效 |

## License

MIT License
