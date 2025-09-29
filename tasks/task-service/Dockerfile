FROM openjdk:17-jdk-slim
LABEL authors="pablo"
VOLUME /tmp

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]