# Spring Cloud Lab Lessons

## What Was Built

PlanifAI previously included:

- Spring Cloud Config Server
- Eureka Service Discovery
- API Gateway routes using `lb://service-name`
- auth-service registration through Eureka
- external diet-service registration through Eureka
- local Config Server native mode backed by `../PlanifAI-config-repo`

The lab code is preserved under:

```text
lab/spring-cloud/config-server
lab/spring-cloud/eureka-server
```

## What It Taught

- How Spring Cloud clients bootstrap configuration.
- How Config Server changes startup ordering and failure modes.
- How Eureka registration metadata affects routing from Docker to host services.
- Why service discovery needs careful hostname/IP decisions in hybrid Docker + host development.
- How Gateway can route through discovery or directly to known upstreams.
- Why distributed architecture multiplies version, networking, healthcheck, and local-dev complexity.

## Why It Was Removed From Active Runtime

For PlanifAI's current product shape, Eureka and Config Server solve problems the project does not really have:

- service addresses are known
- there are few services
- there is no autoscaling or dynamic instance churn
- one developer owns all modules
- environment configuration is small
- Docker Compose already defines the runtime topology

Keeping them active would optimize for architectural appearance instead of maintainability.

## What Remains Valuable

The concepts are still valuable career-wise:

- externalized configuration
- service discovery
- gateway routing
- service registration metadata
- distributed startup dependencies
- operational failure analysis

The practical lesson is not "never use these tools"; it is "introduce them when the system has the problem they solve."

## Future Trigger Points

Spring Cloud infrastructure would become more justified if PlanifAI evolved into:

- a multi-user hosted SaaS with several independently deployed services
- multiple runtime environments with meaningful config drift
- independently scaled service replicas
- team-owned services with separate deploy cycles
- dynamic infrastructure where service locations are not known ahead of time

Until then, explicit routes and local environment configuration are the honest default.
