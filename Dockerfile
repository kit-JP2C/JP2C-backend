FROM openjdk:23-slim
WORKDIR /app

COPY build/libs/jp2c.jar /app

EXPOSE 8080
EXPOSE 3000

ENTRYPOINT ["java", "-jar", "/app/jp2c.jar"]