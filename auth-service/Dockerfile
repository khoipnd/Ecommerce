# syntax=docker/dockerfile:1
# ===== Giai đoạn 1: build file JAR bằng Maven (JDK 17) =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copy pom.xml trước và tải thư viện riêng -> các lần build sau tận dụng cache của Docker
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ===== Giai đoạn 2: image chạy nhỏ gọn (chỉ cần JRE) =====
FROM eclipse-temurin:17-jre
WORKDIR /app

# Chạy bằng user thường, không dùng root
RUN groupadd --system app && useradd --system --gid app --no-create-home app

COPY --from=build /build/target/auth-service.jar /app/auth-service.jar
USER app

EXPOSE 8081
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/auth-service.jar"]
