FROM eclipse-temurin:17-jre
VOLUME /tmp
ARG JAR_FILE=target/*.jar

# Instalar curl para el healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
