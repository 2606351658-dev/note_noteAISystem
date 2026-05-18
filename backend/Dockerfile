# ============================================================
# NoteAI 后端 - Docker 构建文件
# 使用多阶段构建（Multi-stage Build）来减小最终镜像体积
# ============================================================

# ---------- 第一阶段：构建阶段 ----------
# 使用 Maven + JDK 17 的基础镜像来编译项目
# 镜像名称为 "builder"，后续阶段可以引用它
FROM maven:3.9-eclipse-temurin-17 AS builder

# 设置工作目录（容器内的目录）
WORKDIR /app

# 1. 先复制 pom.xml 和 maven wrapper 文件
#    这样做可以利用 Docker 的缓存机制：
#    只要 pom.xml 没变，就不会重新下载依赖，加快构建速度
COPY pom.xml ./
COPY mvnw ./
COPY .mvn ./.mvn

# 下载项目依赖（只下载，不编译）
# -DskipTests 跳过测试
# --no-transfer-progress 减少日志输出
RUN mvn dependency:go-offline -DskipTests --no-transfer-progress

# 2. 复制源代码
COPY src ./src

# 3. 编译打包，生成可执行的 jar 文件
#    package 命令会执行 compile、test、package 等步骤
RUN mvn package -DskipTests --no-transfer-progress

# ---------- 第二阶段：运行阶段 ----------
# 使用更小的 JDK 17 运行时镜像（没有 Maven 等构建工具）
FROM eclipse-temurin:17-jre

# 设置工作目录
WORKDIR /app

# 从 builder 阶段复制构建好的 jar 包
# 注意：Spring Boot 的 Maven 插件打包后，jar 文件名通常为
#       <artifactId>-<version>.jar，即 noteaiBackend-0.0.1-SNAPSHOT.jar
COPY --from=builder /app/target/*.jar ./app.jar

# 暴露端口（与 application.properties 中的 server.port 一致）
EXPOSE 8081

# 设置容器启动命令
# 使用 exec 格式的 CMD（推荐方式）
# 通过环境变量来配置数据库连接、API 密钥等
# 这些环境变量在 Coolify 的部署设置中配置
ENTRYPOINT ["java", \
    "-jar", "app.jar", \
    "--spring.datasource.url=${DB_URL}", \
    "--spring.datasource.username=${DB_USERNAME}", \
    "--spring.datasource.password=${DB_PASSWORD}", \
    "--deepseek.api.key=${DEEPSEEK_API_KEY}" \
]
