# ADR 001: Simplify Spring Cloud Runtime

## Status

Accepted.

## Context

PlanifAI started as a personal learning project for enterprise backend patterns: Config Server, Eureka, API Gateway, auth service, Docker orchestration, and multiple microservices.

After building a more realistic Wave 1 `diet-service` and gaining real enterprise backend experience, the active architecture no longer matched the product reality. PlanifAI is a personal productivity app, not a high-scale SaaS platform.

The active product domains are:

- diets
- tasks/kanban
- personal finance

Tasks and finance do not currently justify separate deployable services. The real `diet-service` is mature enough to remain separate.

## Decision

Remove Spring Cloud Config Server and Eureka from the active runtime.

Keep:

- API Gateway as the single frontend entrypoint
- auth-service for JWT/auth learning and current implementation continuity
- external real diet-service
- Docker Compose for local orchestration
- PostgreSQL for service persistence

Move Config Server and Eureka to `lab/spring-cloud` as preserved learning artifacts.

## Rationale

Config Server and Eureka add operational and cognitive cost that is not justified by this project's current scale:

- services are statically known
- there is no elastic service fleet
- there are no independently scaled replicas
- local development becomes harder when every service depends on config/discovery startup order
- configuration can be handled with Spring profiles, environment variables, and Compose

The API Gateway remains useful because it provides one stable API entrypoint for the frontend and lets the external `diet-service` stay separate while frontend routing remains simple.

## Consequences

Positive:

- fewer moving parts
- faster local startup
- simpler debugging
- fewer dependency/version traps
- architecture better matches product reality

Negative:

- no active service discovery practice in the normal runtime
- config is less centralized
- gateway routes must be explicit

This is acceptable because the removed pieces are preserved as lab artifacts and are not needed for the active product.

## When To Reconsider

Reintroduce service discovery or centralized config only if at least several of these become true:

- multiple independently deployed backend services are active
- services have multiple replicas or dynamic addresses
- deployments happen outside a single Compose/runtime boundary
- configuration must be changed consistently across environments without rebuilding
- runtime secrets/config rotation becomes a real operational need
- a team, not one developer, is coordinating service ownership
- the system has enough traffic or uptime requirements that service replacement/rolling deploys matter
