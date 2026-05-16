# Wave 1.5 Local Dev Runbook

## Active Architecture

Docker runs the active PlanifAI runtime services:

- api-gateway
- auth-service
- planifai-core
- postgres-auth
- postgres-core

IntelliJ runs the external `diet-service` from `C:\PlanifAI-Project\diet-service`.

Spring Cloud Config Server and Eureka are retained only as lab artifacts under `lab/spring-cloud`. They are not part of the active local runtime.

## Run Order

1. Build the active PlanifAI runtime jars:

   ```powershell
   mvn package "-Dmaven.test.skip=true"
   ```

2. Start the active runtime from `C:\PlanifAI-Project\PlanifAI`:

   ```powershell
   docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build api-gateway postgres-auth auth-service postgres-core planifai-core
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
curl http://localhost:8082/actuator/health
curl http://localhost:8080/api/tasks/module
curl http://localhost:8080/api/finance/module
curl http://localhost:8080/api/v1/foods
```

## Gateway Routes

The API Gateway routes:

- `/api/auth/**` to `http://auth-service:8081`
- `/api/tasks/**` to `http://planifai-core:8082`
- `/api/finance/**` to `http://planifai-core:8082`
- `/api/v1/diets/**`, `/api/v1/foods/**`, `/api/v1/recipes/**`, `/api/v1/inventory/**`, `/api/v1/shopping-lists/**`, and `/api/v1/meal-slots/**` to `http://host.docker.internal:8083`

External callers should use the gateway base URL:

```text
http://localhost:8080
```

## Full Stack Demo

To run frontend, gateway, auth, core, diet-service, and databases in Docker:

```powershell
docker compose -f docker-compose.yml -f docker-compose.dev.yml -f docker-compose.full.yml up --build
```

The full stack reuses the populated diet database volume from the standalone `diet-service` compose: `diet-service_postgres_data`. Stop the standalone diet database container before running the full stack so the same Postgres data directory is not mounted by two containers:

```powershell
cd C:\PlanifAI-Project\diet-service
docker compose stop postgres-db
cd C:\PlanifAI-Project\PlanifAI
docker compose -f docker-compose.yml -f docker-compose.dev.yml -f docker-compose.full.yml up --build
```

The full stack exposes:

- frontend: `http://localhost:4200`
- api-gateway: `http://localhost:8080`
- auth-service: `http://localhost:8081`
- planifai-core: `http://localhost:8082`
- diet-service: `http://localhost:8083`
- postgres-diet: `localhost:5432`, database `diet_db`

If `diet-service` fails with `password authentication failed for user "postgres"` or `database "..." does not exist`, check that `.env` still matches the original diet compose credentials: database `diet_db`, user `postgres`, password `1234`. Do not delete the `diet-service_postgres_data` volume unless you intentionally want to discard the populated diet data.
