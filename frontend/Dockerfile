# ============================================================
# NoteAI 前端 - Docker 构建文件
# 使用多阶段构建：
#   阶段1: Node.js 环境构建 Vue 项目
#   阶段2: Nginx 托管静态文件
# ============================================================

# ---------- 第一阶段：构建阶段 ----------
# 使用 Node.js 20 镜像来编译前端项目
FROM node:20-alpine AS builder

# 设置工作目录
WORKDIR /app

# 1. 先复制 package.json 和 package-lock.json（如果有）
#    利用 Docker 缓存：依赖没变就不重新 npm install
COPY package*.json ./

# 安装项目依赖
RUN npm install

# 2. 复制源代码
COPY . .

# 3. 构建生产版本
#    vite build 会生成 dist/ 目录，包含打包后的静态文件
RUN npm run build

# ---------- 第二阶段：运行阶段 ----------
# 使用更小的 Nginx Alpine 镜像
FROM nginx:alpine

# 设置工作目录（Nginx 默认的静态文件目录）
WORKDIR /usr/share/nginx/html

# 删除 Nginx 默认的静态文件
RUN rm -rf ./*

# 从 builder 阶段复制构建好的 dist 目录
COPY --from=builder /app/dist .

# 复制自定义 Nginx 配置文件
# 注意：需要先在同级目录下创建 nginx.conf 文件
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口（Nginx 默认端口 80）
EXPOSE 80

# 启动 Nginx（Nginx 基础镜像默认 CMD 就是启动 nginx，所以可以不写）
# 这里显式写出以便理解：
CMD ["nginx", "-g", "daemon off;"]
