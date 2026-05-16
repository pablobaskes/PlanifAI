# Wave 1.5 Orchestrator Scope

PlanifAI is the Wave 1.5 orchestrator and shared infrastructure repository.

## In Scope

- config-server
- eureka-server
- api-gateway
- auth-service
- docker compose orchestration
- shared infrastructure and configuration documentation

## Removed From This Repository

The following legacy placeholder modules are no longer part of the orchestrator repo:

- diet-service
- task-service
- finance-service
- ai-service

Future business-domain services should live in independent repositories. During Wave 1.5, the real diet-service is maintained and run from its separate repository/path, not from this repo.

## Runtime Dependencies Kept

- postgres-auth for auth-service persistence

MongoDB, Redis, and domain-service Postgres instances are not part of the orchestrator compose stack unless a future independent service explicitly owns them.
