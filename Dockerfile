# ---- Build Stage ----
FROM --platform=$BUILDPLATFORM gradle:8-jdk23-alpine AS builder
WORKDIR /app

# Gradle 캐시 최적화
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon || return 0

# 소스 복사 후 빌드
COPY . .
RUN ./gradlew clean bootJar --no-daemon

# ---- Runtime Stage ----
FROM openjdk:23-slim
WORKDIR /app

# 빌더에서 JAR 복사
COPY --from=builder /app/build/libs/*.jar /app/jp2c.jar

EXPOSE 8080
EXPOSE 3000

ENTRYPOINT ["java", "-jar", "/app/jp2c.jar"]