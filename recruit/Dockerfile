# 招聘管理后端 Dockerfile
# 构建阶段：Maven 打包
FROM maven:3.8.6-openjdk-8 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline || true
COPY src ./src
RUN mvn -q -DskipTests clean package

# 运行阶段：仅复制 jar，镜像体积更小
FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/recruit-*.jar app.jar

# 时区修正
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Xms256m", "-Xmx512m", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]
