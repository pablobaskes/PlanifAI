# Wave 1.5 Runtime Scope

PlanifAI is now a small local runtime/integration repository, not a full Spring Cloud orchestrator.

## Active Runtime

- api-gateway
- auth-service
- planifai-core
- postgres-auth
- postgres-core
- docker compose orchestration
- shared architecture and run documentation

## External Runtime

- The real `diet-service` lives in `C:\PlanifAI-Project\diet-service`.
- During local development it is run from IntelliJ or Maven on port `8083`.
- The gateway reaches it through `host.docker.internal:8083`.
- In full-stack Docker mode, Compose builds it from `../diet-service` and the gateway reaches it through `http://diet-service:8083`.

## Lab Artifacts

The previous Spring Cloud infrastructure is preserved under `lab/spring-cloud`:

- config-server
- eureka-server

Those services are no longer Maven modules and are no longer included in Docker Compose. They remain as learning material and as a record of the architectural experiment.

## Removed From Active Scope

The following are not part of the active runtime:

- Spring Cloud Config Server
- Eureka Service Discovery
- legacy in-repo diet-service placeholder
- task-service placeholder
- finance-service placeholder
- ai-service placeholder
- MongoDB, Redis, and domain-service Postgres instances not owned by the active runtime

Tasks and finance capabilities live as modules inside `planifai-core`, not as separate microservices by default.
