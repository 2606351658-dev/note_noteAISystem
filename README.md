# 📝 NoteAI 智能笔记管理系统

> 基于 **Spring Boot 3 + Vue 3 + MySQL** 的智能笔记管理平台，集成 **DeepSeek 大语言模型** 实现 AI 辅助学习功能。

---

## 📋 项目概述

NoteAI 是一个面向教育场景的智能笔记管理平台，支持三种角色：**学生**、**教师**、**管理员**，分别拥有不同的功能权限。平台集成了 DeepSeek AI 大模型，可实现基于笔记内容的智能问答、笔记润色、分段总结等 AI 辅助功能。

### 核心特性

- ✅ **多角色支持**：学生、教师、管理员三种角色，权限分离
- ✅ **AI 智能助手**：基于 DeepSeek API 实现笔记问答、内容润色、自动总结
- ✅ **笔记版本管理**：支持笔记多版本管理，可回溯历史版本
- ✅ **课程与作业**：教师创建课程和作业，学生提交笔记作业
- ✅ **互动系统**：笔记浏览、点赞、评论互动
- ✅ **举报机制**：用户可举报违规内容，管理员审核处理
- ✅ **密码安全**：BCrypt 加密存储，保障用户信息安全

---

## 🏗️ 项目结构

```
noteSystem/
├── backend/                          # 🚀 Spring Boot 后端
│   ├── src/main/java/com/noteaiBackend/
│   │   ├── controller/               # 控制层 - 接收HTTP请求
│   │   ├── service/                  # 服务层 - 业务逻辑
│   │   ├── repository/               # 数据访问层 - JPA接口
│   │   ├── entity/                   # 实体类 - 数据库映射
│   │   ├── dto/                      # 数据传输对象
│   │   ├── config/                   # 配置类（BCrypt、跨域等）
│   │   └── utils/                    # 工具类
│   ├── src/main/resources/
│   │   └── application.properties    # 应用配置
│   └── pom.xml                       # Maven 依赖配置
│
├── frontend/                         # 🎨 Vue 3 前端
│   ├── src/
│   │   ├── api/                      # API 请求封装
│   │   ├── views/                    # 页面组件
│   │   │   ├── student/              # 学生端页面
│   │   │   ├── teacher/              # 教师端页面
│   │   │   └── admin/                # 管理员端页面
│   │   ├── layouts/                  # 布局组件
│   │   ├── router/                   # 路由配置
│   │   ├── store/                    # Pinia 状态管理
│   │   ├── utils/                    # 工具函数
│   │   └── styles/                   # 样式文件
│   ├── package.json                  # 前端依赖
│   └── vite.config.js                # Vite 配置
│
└── README.md                         # 📄 本文件
```

---

## 🛠️ 技术栈

### 后端技术

| 技术 | 说明 |
|------|------|
| **Spring Boot 3** (4.0.3) | 微服务框架 |
| **Spring Data JPA** | ORM 框架，操作数据库 |
| **MySQL** | 关系型数据库 |
| **BCrypt** | 密码加密（`spring-security-crypto`） |
| **OkHttp** | HTTP 客户端，调用 DeepSeek API |
| **Lombok** | 简化 Java 代码 |
| **Maven** | 项目构建管理 |

### 前端技术

| 技术 | 说明 |
|------|------|
| **Vue 3** (Composition API) | 前端框架 |
| **Vite** | 构建工具 |
| **Element Plus** | UI 组件库 |
| **Pinia** | 状态管理 |
| **Vue Router** | 路由管理 |
| **Axios** | HTTP 请求库 |
| **ECharts** | 数据可视化 |

### AI 能力

| 功能 | 说明 |
|------|------|
| **DeepSeek Chat** | 大语言模型 |
| **RAG** (检索增强生成) | 基于笔记内容的智能问答 |
| **笔记润色** | AI 自动优化笔记语言 |
| **分段总结** | 超长文本自动分段并总结 |

---

## 🚀 快速开始

### 环境要求

- **JDK 17+**（后端运行）
- **Node.js 18+**（前端构建）
- **MySQL 8.0+**（数据库）
- **Maven 3.6+**（后端构建）

### 1️⃣ 克隆项目

```bash
git clone <项目地址>
cd noteSystem
```

### 2️⃣ 数据库配置

1. 创建 MySQL 数据库：
```sql
CREATE DATABASE aiclassnotetable CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改 `backend/src/main/resources/application.properties` 中的数据库连接信息：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/aiclassnotetable?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3️⃣ 启动后端

```bash
# Windows
cd backend
mvnw spring-boot:run

# Mac / Linux
cd backend
./mvnw spring-boot:run
```

后端默认启动在 **http://localhost:8081**

### 4️⃣ 启动前端

```bash
# 新开一个终端
cd frontend
npm install
npm run dev
```

前端默认启动在 **http://localhost:5173**

### 5️⃣ 访问系统

打开浏览器访问 **http://localhost:5173**

---

## 🎮 角色功能

### 👨‍🎓 学生端

| 功能模块 | 说明 |
|---------|------|
| 📚 **我的课程** | 查看已加入课程、课程详情、课程作业 |
| 📝 **笔记管理** | 创建自由笔记、提交作业笔记、编辑笔记 |
| 🌐 **公开笔记** | 浏览所有人公开的笔记 |
| 💡 **AI 问答** | 基于笔记内容向 AI 提问 |
| ✨ **笔记润色** | AI 自动优化笔记语言表达 |
| 👍 **互动系统** | 点赞、浏览笔记 |
| 👤 **个人中心** | 修改个人信息、密码、头像 |

### 👨‍🏫 教师端

| 功能模块 | 说明 |
|---------|------|
| 📖 **课程管理** | 创建课程、查看课程详情（含学生名单+进度统计） |
| 📋 **作业管理** | 布置作业、设置截止时间 |
| ✅ **笔记批改** | 查看学生提交的作业笔记，打分写评语 |
| 👥 **学生管理** | 查看课程下的学生列表 |
| 👤 **个人中心** | 修改个人信息 |

### 👑 管理员端

| 功能模块 | 说明 |
|---------|------|
| 👥 **用户管理** | 查看/搜索用户、封禁/解封用户、编辑用户信息 |
| 📚 **班级管理** | 查看全平台班级、屏蔽违规班级 |
| 📝 **笔记管理** | 查看全平台笔记、屏蔽违规笔记 |
| 🚨 **举报管理** | 处理用户/笔记/课程举报、统计面板 |

---

## 🗺️ 前端路由

| 路径 | 页面 | 角色 |
|------|------|------|
| `/login` | 登录页 | 所有 |
| `/register` | 注册页 | 所有 |
| `/student/home` | 学生首页 | 学生 |
| `/student/courses` | 我的课程 | 学生 |
| `/student/courses/:id` | 课程详情 | 学生 |
| `/student/notes` | 我的笔记 | 学生 |
| `/student/notes/:id` | 笔记详情 | 学生 |
| `/student/notes/edit/:id` | 笔记编辑器 | 学生 |
| `/student/profile` | 个人中心 | 学生 |
| `/teacher/courses` | 课程管理 | 教师 |
| `/teacher/assignments` | 作业管理 | 教师 |
| `/teacher/grading/:id` | 批改页面 | 教师 |
| `/teacher/students` | 学生管理 | 教师 |
| `/teacher/profile` | 个人中心 | 教师 |
| `/admin/users` | 用户管理 | 管理员 |
| `/admin/classes` | 班级管理 | 管理员 |
| `/admin/notes` | 笔记管理 | 管理员 |
| `/admin/reports` | 举报管理 | 管理员 |
| `/blog/notes` | 公开笔记 | 所有 |
| `/blog/notes/:id` | 笔记详情 | 所有 |

---

## 📡 API 概览

### 核心接口

| 模块 | 基础路径 | 主要功能 |
|------|---------|---------|
| **用户** | `/api/users` | 登录、注册、CRUD、密码管理 |
| **笔记** | `/api/note` | 笔记CRUD、公开笔记、优秀笔记 |
| **课程** | `/api/class` | 课程CRUD、教师课程列表 |
| **作业** | `/api/task` | 作业CRUD、学生作业状态 |
| **举报** | `/api/reports` | 提交/处理举报、统计 |
| **AI 问答** | `/api/llm` | 笔记问答、分段总结、润色 |
| **评论** | `/api/note-comments` | 教师批改评语 |
| **互动** | `/api/note-interactions` | 点赞、浏览记录 |
| **版本** | `/api/note-version` | 笔记版本管理 |
| **文件** | `/api/files` | 文件上传/下载 |

> 完整 API 文档请参见：`backend/项目技术文档.md`

---

## ⚙️ IDEA 运行配置

由于前后端分属两个独立目录，IDEA 无法通过顶部按钮直接运行。需要手动配置运行配置：

### 配置后端启动

1. **Run → Edit Configurations...**
2. 点击 **+ → Spring Boot**
3. 配置：
   - **Name**: `noteSystem-backend`
   - **Module**: `noteaiBackend` 主模块
   - **Main class**: `com.noteaiBackend.NoteaiFrontApplication`
   - **Working directory**: `C:\Users\32989\IdeaProjects\noteSystem\backend`
   - **JRE**: Java 17

### 配置前端启动

1. **Run → Edit Configurations...**
2. 点击 **+ → npm**
3. 配置：
   - **Name**: `noteSystem-frontend`
   - **package.json**: 选择 `frontend/package.json`
   - **Command**: `run`
   - **Scripts**: `dev`
   - **Working directory**: `C:\Users\32989\IdeaProjects\noteSystem\frontend`

### 一键启动（可选）

1. **Run → Edit Configurations...**
2. 点击 **+ → Compound**
3. **Name**: `noteSystem-all`
4. 勾选上述两个配置

---

## 📊 数据模型

### 核心表结构

| 表名 | 说明 | 主要字段 |
|------|------|---------|
| `user` | 用户表 | id, username, password, role, phone, avatar |
| `note` | 笔记表 | id, title, content, userId, isPublic, isScore |
| `class` | 课程表 | id, className, teacherId, classCode, describe |
| `task` | 作业表 | id, title, classId, teacherId, deadline |
| `report` | 举报表 | id, type, typeId, userId, info, status |
| `note_comment` | 评语表 | id, noteId, teacherId, score, comment |
| `note_interaction` | 互动表 | id, noteId, userId, hasViewed, hasLiked |
| `note_version` | 版本表 | id, noteId, content, version, userId |
| `class_joined` | 选课表 | id, classId, studentId |

> 详细 ER 图请参见：`frontend/puml/`

---

## 🔐 密码安全

项目使用 **BCrypt** 密码哈希算法保障用户密码安全：

- **注册**：密码经 BCrypt 加盐哈希后存储
- **登录**：BCrypt 校验输入密码与存储哈希
- **改密码**：需验证旧密码，新密码再次哈希存储

BCrypt 的优势：
- ✅ 自动加盐，相同密码每次加密结果不同
- ✅ 可调节强度，随硬件发展可增加加密轮数
- ✅ 单向哈希，无法从哈希值还原原文

---

## 🤖 AI 功能详解

### RAG（检索增强生成）

当用户对笔记内容提问时，系统会：
1. 将笔记按段落分割
2. 提取问题关键词，计算各段落相关性
3. 选取最相关的 3 个段落作为上下文
4. 将上下文 + 问题发送给 DeepSeek AI

### 分段总结

针对超长笔记（超过 4000 字符）：
1. 自动分割为多个段落
2. 逐个段落调用 AI 生成摘要
3. 合并各段落摘要，再次调用 AI 整合为连贯的最终总结

---

## 📄 文件上传

- 上传路径：`file/document/`
- 文件命名：UUID（防止重名冲突）
- 访问方式：`/api/files/download/{uuid}.{ext}`
- 图片支持：浏览器内联预览

---

## 🧩 前端技术要点

### Vue 3 Composition API

项目使用 Vue 3 `<script setup>` 语法：

```javascript
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const count = ref(0)
const user = reactive({ name: '张三', age: 20 })
const fullName = computed(() => user.name + '同学')

onMounted(() => {
  fetchData()
})
```

### Pinia 状态管理

```javascript
// 定义 store
export const useUserStore = defineStore('user', {
  state: () => ({
    username: '',
    role: '',
    token: '',
    id: null
  }),
  actions: {
    login(data) { /* 保存登录信息 */ },
    logout() { /* 清除登录信息 */ }
  }
})
```

---

## 📁 相关文档

| 文档 | 说明 |
|------|------|
| [`backend/项目技术文档.md`](backend/项目技术文档.md) | 完整 API 接口文档、测试用例、技术详解 |
| [`backend/ER.txt`](backend/ER.txt) | 数据库 ER 图文本描述 |
| [`frontend/puml/noteER.puml`](frontend/puml/noteER.puml) | PlantUML 格式的 ER 图 |
| [`frontend/puml/totalER.puml`](frontend/puml/totalER.puml) | 完整 ER 关系图 |

---

## 📜 许可证

本项目仅供学习交流使用。

---

## 🤝 贡献指南

欢迎提交 Issue 或 Pull Request 来改进项目。

---

> **项目框架**: Spring Boot 4.0.3 + Vue 3 + MySQL  
> **AI 模型**: DeepSeek Chat  
> **代码规范**: MVC 三层架构（Controller → Service → Repository）  
> **文档版本**: v1.0.0  
> **更新日期**: 2026 年 5 月
