# B2B Gaming Platform

A comprehensive Business-to-Business gaming platform that enables operators to offer betting games to their players without owning game infrastructure.

## Overview

This platform provides:
- Multi-tenant gaming engine
- Bet processing and management
- Result generation and payout calculation
- Financial settlement with operators
- Real-time game events via WebSocket
- Comprehensive reporting and analytics

## Architecture

- **Architecture Style:** Microservices
- **Service Discovery:** Eureka Server (port 10000) — implemented in `eureka-server/`
- **Load Balancing:** Built into the API Gateway (Spring Cloud LoadBalancer + Eureka). No separate LB service; the gateway does discovery and load balancing when using `lb://service-name` routes.
- **API Gateway:** Spring Cloud Gateway (Eureka client; routes to services via `lb://service-name`; discovery + LB in one)
- **Core Services:** Java Spring Boot
- **Reporting:** .NET
- **Database:** PostgreSQL (with read replicas)
- **Cache:** Redis
- **Message Queue:** Apache Kafka
- **Real-time:** WebSockets

### Architecture Standards

All services follow **standardized architecture patterns**:
- ✅ Type-safe DTOs for all request/response
- ✅ Thin controllers with service layer separation
- ✅ Centralized exception handling
- ✅ Jakarta Bean Validation
- ✅ Consistent error responses

**See:** [docs/architecture/ARCHITECTURE_GUIDELINES.md](docs/architecture/ARCHITECTURE_GUIDELINES.md) for complete guidelines.

## Project Structure

```
b2b-platform/
├── common-api/               # Shared APIResponse, StatusCode (build first: mvn install) ✅
├── eureka-server/            # Service discovery (port 10000) ✅
├── api-gateway/              # API Gateway (port 8080); Eureka + LoadBalancer, lb:// routes ✅
├── services/                 # Microservices
│   ├── operator-service/    # Operator management (port 8081) ✅
│   ├── authentication-service/ # Player auth, internal login + casino launch (port 8087) ✅
│   ├── session-service/     # Session management, DB + Redis (port 8085) ✅
│   ├── wallet-service/      # Wallet operations (port 8083) ✅
│   ├── bet-service/         # Bet processing (port 8084) ✅
│   ├── b2c-integration-service/ # B2C provider integration (port 8086) ✅
│   ├── settlement-service/  # Settlement & reconciliation (planned)
│   ├── exposure-service/    # Exposure tracking (planned)
│   └── game-services/       # Individual game services (planned)
├── infrastructure/docker-compose/ # PostgreSQL, Redis, Kafka ✅
├── docs/                     # Documentation ✅
├── build.ps1, build.sh       # Build all JARs → deploy/ ✅
└── scripts/                 # Utility scripts ✅
```

**Legend:** ✅ = Implemented | (planned) = Future

## Getting Started

### Prerequisites

- Java JDK 17+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL client (optional)
- Redis client (optional)
- IDE (IntelliJ IDEA / VS Code)

### Quick Start

1. **Start infrastructure** (PostgreSQL, Redis, Kafka)
   ```bash
   cd infrastructure/docker-compose
   docker-compose up -d
   ```

2. **Start Eureka first** (required for discovery)
   ```bash
   cd eureka-server
   mvn spring-boot:run
   ```

3. **Start services** (in any order; each registers with Eureka)
   ```bash
   cd services/operator-service && mvn spring-boot:run
   cd services/authentication-service && mvn spring-boot:run
   cd services/session-service && mvn spring-boot:run
   cd services/wallet-service && mvn spring-boot:run
   cd services/bet-service && mvn spring-boot:run
   cd services/b2c-integration-service && mvn spring-boot:run
   ```

4. **Start API Gateway** (routes to services via `lb://`)
   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```

5. **Build all JARs for deployment** (optional)
   ```bash
   .\build.ps1          # Windows
   ./build.sh           # Linux/Mac; JARs in ./deploy/
   ```

For detailed setup, see [QUICK_START.md](QUICK_START.md) and [docs/IMPLEMENTATION_STATUS.md](docs/IMPLEMENTATION_STATUS.md).

## Architecture & Standards

### Architecture Guidelines

All services follow **standardized architecture patterns**:
- ✅ Type-safe DTOs for all request/response
- ✅ Thin controllers with service layer separation
- ✅ Centralized exception handling
- ✅ Jakarta Bean Validation
- ✅ Consistent error responses

**⚠️ IMPORTANT:** All future services **MUST** follow the architecture guidelines above.

## Documentation

All links below point to existing documents in this repository.

| Document | Description |
|----------|--------------|
| [QUICK_START.md](QUICK_START.md) | Quick start: infrastructure, Eureka, services, build |
| [GETTING_STARTED.md](GETTING_STARTED.md) | Step-by-step setup for new developers |
| [docs/IMPLEMENTATION_STATUS.md](docs/IMPLEMENTATION_STATUS.md) | **Current implementation** (Eureka, config, services, build, what's done/not done) |
| [TROUBLESHOOTING.md](TROUBLESHOOTING.md) | Common issues and fixes (Docker, ports, DB, Redis, Kafka) |
| [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) | Phased implementation plan |
| [SRS.md](SRS.md) | Software Requirements Specification |

**Service READMEs:** Each module has its own README (e.g. `api-gateway/README.md`, `services/authentication-service/README.md`).  
**Full index:** [docs/README.md](docs/README.md).

## Implemented Services ✅

### 1. Operator Service (Port 8081)
- Operator CRUD operations
- API key management
- Operator validation
- **Swagger:** http://localhost:8081/swagger-ui.html

### 2. API Gateway (Port 8080)
- Eureka-integrated routing (`lb://service-name`) with Spring Cloud LoadBalancer
- API key authentication (for operators; in-memory cache)
- JWT authentication (for players)
- Circuit breaker (Resilience4j)
- CORS configuration
- **Swagger:** http://localhost:8080/swagger-ui.html

### 3. Authentication Service (Port 8087)
- **Internal login:** POST /api/v1/auth/login (operatorCode, username, password)
- **Casino launch:** POST /api/v1/auth/launch (X-Api-Key + player info; create session, return launch URL)
- JWT token generation and refresh
- Login page UI (Thymeleaf)
- **Swagger:** http://localhost:8087/swagger-ui.html

### 4. Session Service (Port 8085)
- Session creation/validation
- Session refresh
- Session termination
- Redis caching for session indicators
- Scheduled cleanup
- **Swagger:** http://localhost:8085/swagger-ui.html

### 5. Wallet Service (Port 8083)
- Wallet debit operations
- Wallet credit operations
- Balance queries
- Operator webhook integration
- Redis caching for operator configs
- Transaction history
- Idempotency handling
- Retry mechanism
- Circuit breaker
- **Swagger:** http://localhost:8083/swagger-ui.html

### 6. Bet Service (Port 8084)
- Bet placement (game-agnostic)
- Bet settlement with payout calculation
- Bet cancellation with refund
- Bet history and query
- Idempotency handling (prevents duplicate bets)
- Timing-critical bet confirmation (before game starts)
- Wallet Service integration
- Game Service integration (mock until games implemented)
- **Swagger:** http://localhost:8084/swagger-ui.html

## Configuration

- **Services (except API Gateway):** `application.properties` per service; no placeholders (literal values). Redis: `spring.data.redis.host`, `spring.data.redis.port`.
- **Dev profile:** `application-dev.properties` overrides DB (PostgreSQL, schema b2b) and Redis. Run with `--spring.profiles.active=dev`.
- **API Gateway:** `application.yml` (Eureka, routes, actuator).
- **Build:** Install `common-api` first (`mvn -pl common-api clean install`) so services resolve the shared dependency.

See [docs/IMPLEMENTATION_STATUS.md](docs/IMPLEMENTATION_STATUS.md) and [docs/guides/CONFIGURATION_GUIDE.md](docs/guides/CONFIGURATION_GUIDE.md) for details.

## Development

### Running Services Locally

Start **Eureka** first, then any of the services (they register with Eureka). Finally start the **API Gateway**.

```bash
# 1. Eureka (port 10000)
cd eureka-server && mvn spring-boot:run

# 2. Services (ports 8081, 8083, 8084, 8085, 8086, 8087)
cd services/operator-service && mvn spring-boot:run
cd services/authentication-service && mvn spring-boot:run
cd services/session-service && mvn spring-boot:run
cd services/wallet-service && mvn spring-boot:run
cd services/bet-service && mvn spring-boot:run
cd services/b2c-integration-service && mvn spring-boot:run

# 3. API Gateway (port 8080)
cd api-gateway && mvn spring-boot:run
```

### Build and Deploy (JARs)

From project root: `.\build.ps1` (Windows) or `./build.sh` (Linux/Mac). All runnable JARs are placed in `./deploy/`. Run with `java -jar deploy/<service>.jar` (Eureka first, then others, then gateway).

### Running Tests

```bash
# Test all services
scripts\test-all-endpoints.bat

# Test architecture compliance
scripts\test-architecture-compliance.bat

# Test DTO endpoints
scripts\test-dto-endpoints.bat

# Test specific service
scripts\test-operator-service.bat
scripts\test-authentication-service.bat
scripts\test-session-service.bat
scripts\test-wallet-service.bat
scripts\test-gateway.bat
```

### Database Migrations

We use Hibernate `ddl-auto: update` for schema management. Tables are auto-created on service startup.

## API Documentation

API documentation is available via Swagger/OpenAPI:
- **API Gateway:** http://localhost:8080/swagger-ui.html
- **Operator Service:** http://localhost:8081/swagger-ui.html
- **Authentication Service:** http://localhost:8087/swagger-ui.html
- **Session Service:** http://localhost:8085/swagger-ui.html
- **Wallet Service:** http://localhost:8083/swagger-ui.html
- **Bet Service:** http://localhost:8084/swagger-ui.html

## Contributing

1. Create a feature branch
2. Make your changes
3. Write/update tests
4. Submit a pull request

## License

[Add your license here]

## Contact

[Add contact information]

---

## Project Status

**Current Phase:** Foundation Complete ✅ | Ready for Game Services 🎯  
**Services Implemented:** 6/15+ (40%)  
**Core Services:** 6/6 (100%) ✅  
**Architecture Compliance:** 6/6 (100%) ✅  
**Version:** 0.1.0-SNAPSHOT

### Completed Services ✅
- Eureka Server (Port 10000)
- API Gateway (Port 8080) — Eureka + LoadBalancer, lb:// routes
- Operator Service (Port 8081)
- Authentication Service (Port 8087) — internal login + casino launch
- Session Service (Port 8085)
- Wallet Service (Port 8083)
- Bet Service (Port 8084)
- B2C Integration Service (Port 8086)

### Next Phase: Game Services 🎯
- Game Service Framework
- Result Engine
- Sample Game (Roulette)

See [docs/IMPLEMENTATION_STATUS.md](docs/IMPLEMENTATION_STATUS.md) for detailed status and next steps.

---

**Last Updated:** February 2026
