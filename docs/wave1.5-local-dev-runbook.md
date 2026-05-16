# Wave 1.5 Local Dev Runbook

## Active Architecture

Docker runs the active PlanifAI runtime services:

- api-gateway
- auth-service
- postgres-auth

IntelliJ runs the external `diet-service` from `C:\PlanifAI-Project\diet-service`.

Spring Cloud Config Server and Eureka are retained only as lab artifacts under `lab/spring-cloud`. They are not part of the active local runtime.

## Run Order

1. Build the active PlanifAI runtime jars:

   ```powershell
   mvn package "-Dmaven.test.skip=true"
   ```

2. Start the active runtime from `C:\PlanifAI-Project\PlanifAI`:

   ```powershell
   docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build api-gateway postgres-auth auth-service
   ```

3. Start the diet database from `C:\PlanifAI-Project\diet-service` if it is not already running:

   ```powershell
   docker compose up -d postgres-db
   ```

4. Start `diet-service` from IntelliJ with the default local settings.

## Validation

```powershell
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8080/api/v1/foods
```

## Gateway Routes

The API Gateway routes:

- `/api/auth/**` to `http://auth-service:8081`
- `/api/v1/diets/**`, `/api/v1/foods/**`, `/api/v1/recipes/**`, `/api/v1/inventory/**`, `/api/v1/shopping-lists/**`, and `/api/v1/meal-slots/**` to `http://host.docker.internal:8083`

External callers should use the gateway base URL:

```text
http://localhost:8080
```
