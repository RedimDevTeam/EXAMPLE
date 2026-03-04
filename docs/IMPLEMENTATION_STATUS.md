# Implementation Status - B2B Platform

This document reflects the current implementation as of the last update. Use it as the single source of truth for what is built and how it works.

---

## Infrastructure & Discovery

| Component | Status | Notes |
|-----------|--------|--------|
| **Eureka Server** | ✅ Implemented | Port 10000. Module: `eureka-server/`. All services register as Eureka clients. Start Eureka before other services. |
| **Service Discovery** | ✅ Eureka | Services use `eureka.client.serviceUrl.defaultZone=http://localhost:10000/eureka/`. |
| **Load Balancing** | ✅ Implemented (part of API Gateway) | **Not a separate service.** The API Gateway uses Spring Cloud LoadBalancer when routing to `lb://service-name`; it gets the list of instances from Eureka and balances across them (e.g. round-robin). Do not add a separate load balancer—it is already built into the gateway. |
| **API Gateway** | ✅ Eureka-integrated | Routes use `lb://operator-service`, `lb://authentication-service`, etc. Discovery and load balancing are both handled by the gateway. |

---

## Configuration

| Item | Status | Notes |
|------|--------|--------|
| **Service config** | ✅ application.properties | All services (except API Gateway) use `application.properties`. API Gateway uses `application.yml`. |
| **Dev profile** | ✅ application-dev.properties | DB (PostgreSQL, schema b2b), Redis (0.0.0.0), and other overrides. Activate with `--spring.profiles.active=dev`. |
| **Spring Cloud Config** | ❌ Not implemented | Each service has local config only. |

---

## Shared Libraries

| Module | Status | Notes |
|--------|--------|--------|
| **common-api** | ✅ Implemented | Shared `APIResponse`, `StatusCode`. All APIs return `APIResponse`; no exceptions to client. Build with `mvn install` first. |

---

## Messaging

| Item | Status | Notes |
|------|--------|--------|
| **Broker** | ✅ Kafka | Docker Compose: `infrastructure/docker-compose/docker-compose.yml` (PostgreSQL, Redis, Kafka). Kafka port 9092, bootstrap `localhost:9092`. |
| **RabbitMQ** | ❌ Replaced | Replaced by Kafka. Docs and compose updated. |
| **Spring Kafka in services** | ⏳ Not yet | Kafka is in infra; no service currently publishes/consumes. Use `spring.kafka.bootstrap-servers` when adding. |

---

## Services (Runnable)

| Service | Port | Config | Notes |
|---------|------|--------|--------|
| **eureka-server** | 10000 | application.properties | Start first. No DB/Redis. |
| **api-gateway** | 8080 | application.yml | Eureka client, LoadBalancer, lb:// routes. API Key + JWT filters. No Redis (in-memory API key cache). |
| **operator-service** | 8081 | application.properties | Operator CRUD, API keys. Eureka client. |
| **authentication-service** | 8087 | application.properties | Two flows: (1) Internal login: operatorCode + username + password. (2) Casino launch: X-Api-Key + player info, create session, return launch URL. |
| **session-service** | 8085 | application.properties | Session create/validate/refresh; DB + Redis. Eureka client. |
| **wallet-service** | 8083 | application.properties | Debit/credit, Feign via gateway. Eureka client. |
| **bet-service** | 8084 | application.properties | Bet place/settle/cancel; Feign to gateway for wallet. Eureka client. |
| **b2c-integration-service** | 8086 | application.properties | B2C provider wallet integration. Eureka client. |

---

## Authentication

| Flow | Endpoint | Notes |
|------|----------|--------|
| **Internal / testing** | `POST /api/v1/auth/login` | Body: operatorCode, username, password. Returns JWT. |
| **Casino launch** | `POST /api/v1/auth/launch` | Header: `X-Api-Key`. Body: CasinoLoginRequest (player info, optional password). Validates operator, find/create player, create session (DB + Redis), return launch URL + token. |

---

## Build & Deploy

| Item | Status | Notes |
|------|--------|--------|
| **Build script (Windows)** | ✅ build.ps1 | From project root: `.\build.ps1`. Builds common-api (install), then eureka, gateway, all services. Copies runnable JARs to `./deploy/`. |
| **Build script (Linux/Mac)** | ✅ build.sh | `./build.sh`. Same behavior. `chmod +x build.sh` if needed. |
| **Build order** | common-api → eureka-server → api-gateway → authentication-service → b2c-integration-service → bet-service → operator-service → session-service → wallet-service | |
| **CI/CD, Kubernetes, blue-green** | ❌ Not in repo | Manual / script-based deploy only. |

---

## Observability & Operations

| Item | Status | Notes |
|------|--------|--------|
| **Actuator** | ✅ | health, info, metrics, prometheus (where enabled). |
| **Prometheus metrics** | ✅ Exposed | Gateway and services expose `/actuator/prometheus`. Scraping/Grafana not in repo. |
| **Distributed tracing** | ❌ Not implemented | No Zipkin/Jaeger/Micrometer tracing. |
| **Structured logging** | ❌ Not implemented | Standard log format. |

---

## Start Order (Local)

1. **Infrastructure:** `cd infrastructure/docker-compose && docker-compose up -d` (PostgreSQL, Redis, Kafka).
2. **Eureka:** `cd eureka-server && mvn spring-boot:run`.
3. **Other services** (any order after Eureka): operator-service, authentication-service, session-service, wallet-service, bet-service, b2c-integration-service.
4. **API Gateway:** `cd api-gateway && mvn spring-boot:run`.

Or use the deploy folder: build with `.\build.ps1` (or `./build.sh`), then run each JAR with `java -jar deploy/<service>.jar` (Eureka first, then others, then gateway).

---

**Last Updated:** February 2026
