# syntax=docker/dockerfile:1
# Stage 1: Build application with Maven and JDK 17
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline

COPY src ./src
RUN mvn --batch-mode --no-transfer-progress clean package -DskipTests

# Stage 2: Runtime image with JRE 17
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --no-create-home app
COPY --from=builder /app/target/*.jar app.jar

USER app
EXPOSE 8080

# Cố định giới hạn RAM tối đa 256MB để không vượt quá giới hạn 512MB của Render Free Tier
ENV JAVA_OPTS="-Xms64m -Xmx256m -XX:+UseG1GC -XX:MaxMetaspaceSize=128m"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
