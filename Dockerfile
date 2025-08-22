FROM openjdk:23-slim
WORKDIR /app

COPY build/libs/project-mj.jar /app

EXPOSE 8080
EXPOSE 3000

ENTRYPOINT ["java", "-jar", "/app/project-mj.jar"]