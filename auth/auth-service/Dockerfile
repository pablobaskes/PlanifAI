FROM eclipse-temurin:17-jre
LABEL authors="pablo"
VOLUME /tmp

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
