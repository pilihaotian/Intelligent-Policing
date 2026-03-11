# 智慧公安管理系统

一个基于Spring Boot和Vue的智能公安管理平台，集成了大模型能力，提供执法办案、刑侦研判、治安管理等核心业务功能。

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                      前端 (Vue 3 + Ant Design Vue)           │
├─────────────────────────────────────────────────────────────┤
│                      后端 (Spring Boot 3.x)                  │
├─────────────────┬─────────────────┬─────────────────────────┤
│   系统管理模块   │   执法办案模块   │      智能助手模块       │
│   智能导航模块   │   刑侦研判模块   │      治安管理模块       │
├─────────────────┴─────────────────┴─────────────────────────┤
│                    PostgreSQL 数据库                         │
├─────────────────────────────────────────────────────────────┤
│                    大模型服务 (OpenAI 兼容)                   │
└─────────────────────────────────────────────────────────────┘
```

## 功能模块

### 1. 系统管理
- 用户管理：用户增删改查、角色分配
- 角色管理：角色定义、权限配置
- 机构管理：机构树形结构管理
- 权限管理：前端路由权限和后端API权限

### 2. 执法办案
- 案件信息填报：上传PDF文书，AI智能提取关键信息，自动填充表单

### 3. 智能助手
- 知识库管理：上传业务领域文档构建知识库
- RAG智能问答：基于知识库的领域知识问答

### 4. 智能导航
- 意图识别：理解用户自然语言输入
- 智能路由：根据用户权限导航到目标页面

### 5. 刑侦研判
- 重点人员管理：人员画像展示
- 资金流水分析：可疑交易标记
- 案件智能分析：AI分析疑点、生成报告

### 6. 治安管理
- 人员核查：背景核查流程管理、AI辅助分析
- 线索管理：线索情报处理、AI辅助判断

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.x
- Spring Security + JWT
- MyBatis-Plus
- PostgreSQL
- Redis (可选)

### 前端
- Vue 3
- Vite
- Pinia
- Ant Design Vue
- Axios
- Marked (Markdown渲染)

### AI集成
- OpenAI兼容API
- RAG知识库

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- PostgreSQL 14+
- Maven 3.8+

### 数据库初始化

```bash
# 创建数据库
psql -U {数据库用户名} -d postgres -c "CREATE DATABASE {数据库名};"

# 执行初始化脚本
psql -U {数据库用户名} -d {数据库名} -f database/init.sql
psql -U {数据库用户名} -d {数据库名} -f database/data.sql
```

### 后端启动

```bash
cd gongan-backend

# 编译项目
mvn clean package -DskipTests

# 启动服务
java -jar target/risk-backend-1.0.0.jar

# 或使用Maven运行
mvn spring-boot:run
```

### 前端启动

```bash
cd gongan-frontend

# 安装依赖
npm install

# 开发模式启动
npm run dev

# 生产构建
npm run build
```

### 访问系统
- 前端地址：http://localhost:5173
- 后端API：http://localhost:8080/api
- 默认账号：admin / 123456

## 配置说明

### 后端配置 (application.yml)

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/{数据名}
    username: {数据库用户名}
    password: your_password

# JWT配置
jwt:
  secret: your-jwt-secret-key-at-least-256-bits
  expiration: 86400000

# 大模型配置
llm:
  api-url: http://localhost:11434/v1
  api-key: ollama
  model: qwen3:1.7b
```

### 前端配置 (.env)

```env
VITE_API_BASE_URL=/api
```

## 项目结构

```
Risk/
├── database/                    # 数据库脚本
│   ├── init.sql                # DDL建表语句
│   └── data.sql                # DML测试数据
├── risk-backend/               # 后端项目
│   ├── src/main/java/com/risk/
│   │   ├── config/             # 配置类
│   │   ├── controller/         # 控制器
│   │   ├── service/            # 服务层
│   │   ├── mapper/             # MyBatis Mapper
│   │   ├── entity/             # 实体类
│   │   ├── dto/                # 数据传输对象
│   │   ├── util/               # 工具类
│   │   ├── exception/          # 异常处理
│   │   └── filter/             # 过滤器
│   └── src/main/resources/
│       ├── mapper/             # Mapper XML
│       └── application.yml     # 配置文件
├── risk-frontend/              # 前端项目
│   ├── src/
│   │   ├── api/                # API接口
│   │   ├── views/              # 页面组件
│   │   ├── components/         # 公共组件
│   │   ├── router/             # 路由配置
│   │   ├── store/              # 状态管理
│   │   ├── utils/              # 工具函数
│   │   └── assets/             # 静态资源
│   └── package.json
└── README.md
```

## API文档

✦ 启动后端服务后，可访问：
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/v3/api-docs


## 安全说明

- 使用JWT进行身份认证
- 基于角色的权限控制(RBAC)
- API权限校验
- 敏感配置外部化

## 许可证

MIT License