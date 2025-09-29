FROM openjdk:17-jdk-slim
LABEL authors="pablo"
VOLUME /tmp

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]