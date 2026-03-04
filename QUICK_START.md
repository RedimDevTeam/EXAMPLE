# Quick Start Guide - B2B Gaming Platform

## Current Status ✅

**Implemented Services:**
- ✅ Eureka Server (Port 10000) — start first
- ✅ API Gateway (Port 8080) — Eureka + LoadBalancer, lb:// routes
- ✅ Operator Service (Port 8081)
- ✅ Authentication Service (Port 8087) — internal login + casino launch
- ✅ Session Service (Port 8085)
- ✅ Wallet Service (Port 8083)
- ✅ Bet Service (Port 8084)
- ✅ B2C Integration Service (Port 8086)

**Infrastructure:**
- ✅ PostgreSQL (Docker)
- ✅ Redis (Docker)
- ✅ Kafka (Docker)

## Immediate Next Steps

### 1. Prerequisites Check ✅

Verify you have installed:
- [x] Java JDK 17 or higher
- [x] Maven 3.8+
- [x] Docker Desktop (for local infrastructure)
- [x] Docker Compose
- [x] Git
- [x] IDE (IntelliJ IDEA / VS Code / Eclipse)
- [x] PostgreSQL client (optional, for DB access)
- [x] Redis client (optional, for cache inspection)

**Check versions:**
```bash
java -version
mvn -version  # or gradle -version
docker --version
docker-compose --version
git --version
```

### 2. Project Initialization (30 minutes)

**Step 2.1: Create Project Structure**
```bash
# Create main directories
mkdir -p api-gateway
mkdir -p services/operator-service
mkdir -p services/bet-service
mkdir -p services/wallet-service
mkdir -p services/settlement-service
mkdir -p services/exposure-service
mkdir -p services/session-service
mkdir -p services/game-services/roulette-service
mkdir -p shared/common-models
mkdir -p shared/common-utilities
mkdir -p infrastructure/docker
mkdir -p infrastructure/kubernetes
mkdir -p infrastructure/docker-compose
mkdir -p docs
mkdir -p scripts
```

**Step 2.2: Initialize Git Repository**
```bash
git init
git add .
git commit -m "Initial project structure"
```

**Step 2.3: Create Root README**
- Add project description
- Add setup instructions
- Link to SRS and Implementation Plan

### 3. Set Up Local Infrastructure (1 hour)

**Step 3.1: Create Docker Compose File**

Create `infrastructure/docker-compose/docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: b2b-postgres
    environment:
      POSTGRES_DB: b2b_platform
      POSTGRES_USER: b2b_user
      POSTGRES_PASSWORD: b2b_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U b2b_user"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: b2b-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5

  kafka:
    image: bitnami/kafka:3.6
    container_name: b2b-kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_CFG_NODE_ID: 0
      KAFKA_CFG_PROCESS_ROLES: controller,broker
      KAFKA_CFG_CONTROLLER_QUORUM_VOTERS: 0@kafka:9093
      KAFKA_CFG_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
      KAFKA_CFG_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
    healthcheck:
      test: ["CMD-SHELL", "kafka-topics.sh --bootstrap-server localhost:9092 --list || exit 1"]
      interval: 10s
      timeout: 10s
      retries: 5
    volumes:
      - kafka_data:/bitnami/kafka

volumes:
  postgres_data:
  redis_data:
  kafka_data:
```

**Step 3.2: Start Infrastructure**
```bash
cd infrastructure/docker-compose
docker-compose up -d
```

**Step 3.3: Verify Services**
```bash
# Check PostgreSQL
docker exec -it b2b-postgres psql -U b2b_user -d b2b_platform -c "SELECT version();"

# Check Redis
docker exec -it b2b-redis redis-cli ping

# Check Kafka (list topics)
# docker exec -it b2b-kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
```

### 4. Services Already Created ✅

The following are implemented: Eureka Server (10000), API Gateway (8080), Operator (8081), Authentication (8087), Session (8085), Wallet (8083), Bet (8084), B2C Integration (8086). See [docs/IMPLEMENTATION_STATUS.md](docs/IMPLEMENTATION_STATUS.md) for details.

### 5. Running Services

**Start order:** Infrastructure → Eureka → other services → API Gateway.

```bash
# 1. Infrastructure
cd infrastructure/docker-compose
docker-compose up -d

# 2. Eureka (required first)
cd eureka-server
mvn spring-boot:run

# 3. Other services (any order)
cd services/operator-service && mvn spring-boot:run
cd services/authentication-service && mvn spring-boot:run
cd services/session-service && mvn spring-boot:run
cd services/wallet-service && mvn spring-boot:run
cd services/bet-service && mvn spring-boot:run
cd services/b2c-integration-service && mvn spring-boot:run

# 4. API Gateway
cd api-gateway
mvn spring-boot:run
```

**Build all JARs for deployment:**
```bash
.\build.ps1    # Windows; JARs in ./deploy/
./build.sh     # Linux/Mac
```

**Or use test scripts:**

```bash
# Test all services
scripts\test-all-endpoints.bat

# Test specific service
scripts\test-operator-service.bat
scripts\test-authentication-service.bat
scripts\test-session-service.bat
scripts\test-wallet-service.bat
```

### 6. Configuration

- **Services:** `application.properties` (no placeholders). Redis: `spring.data.redis.host`, `spring.data.redis.port`.
- **Dev profile:** `application-dev.properties` (DB and Redis overrides). Run with `--spring.profiles.active=dev`.
- **API Gateway:** `application.yml`. Routes use Eureka (`lb://service-name`).
- See [docs/IMPLEMENTATION_STATUS.md](docs/IMPLEMENTATION_STATUS.md).

---

### 7. Create First Microservice - Operator Service (Reference)

**Step 4.1: Initialize Spring Boot Project**

Using Spring Initializr or manually:

```bash
cd services/operator-service

# Using Spring Initializr CLI (if installed)
spring init \
  --dependencies=web,data-jpa,postgresql,validation,actuator,security \
  --groupId=com.b2bplatform \
  --artifactId=operator-service \
  --package-name=com.b2bplatform.operator \
  --name=operator-service \
  --java-version=17 \
  operator-service

# Or use Maven archetype
mvn archetype:generate \
  -DgroupId=com.b2bplatform \
  -DartifactId=operator-service \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false
```

**Step 4.2: Add Dependencies**

Update `pom.xml` or `build.gradle`:

**Maven (pom.xml):**
```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    
    <!-- Lombok (optional but recommended) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

**Step 4.3: Create Basic Structure**

```
operator-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/b2bplatform/operator/
│   │   │       ├── OperatorServiceApplication.java
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── model/
│   │   │       └── config/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   └── test/
└── pom.xml
```

**Step 4.4: Create Application Configuration**

`src/main/resources/application.yml`:
```yaml
spring:
  application:
    name: operator-service
  datasource:
    url: jdbc:postgresql://localhost:5432/b2b_platform
    username: b2b_user
    password: b2b_password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  data:
    redis:
      host: localhost
      port: 6379

server:
  port: 8081

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

**Step 4.5: Create Basic Entity**

`src/main/java/com/b2bplatform/operator/model/Operator.java`:
```java
package com.b2bplatform.operator.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "operators")
@Data
public class Operator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String code;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String status; // ACTIVE, INACTIVE
    
    private String baseCurrency;
    private String baseLanguage;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**Step 4.6: Create Repository**

`src/main/java/com/b2bplatform/operator/repository/OperatorRepository.java`:
```java
package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, Long> {
    Optional<Operator> findByCode(String code);
    boolean existsByCode(String code);
}
```

**Step 4.7: Create Service**

`src/main/java/com/b2bplatform/operator/service/OperatorService.java`:
```java
package com.b2bplatform.operator.service;

import com.b2bplatform.operator.model.Operator;
import com.b2bplatform.operator.repository.OperatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OperatorService {
    
    private final OperatorRepository operatorRepository;
    
    public List<Operator> getAllOperators() {
        return operatorRepository.findAll();
    }
    
    public Optional<Operator> getOperatorById(Long id) {
        return operatorRepository.findById(id);
    }
    
    public Optional<Operator> getOperatorByCode(String code) {
        return operatorRepository.findByCode(code);
    }
    
    @Transactional
    public Operator createOperator(Operator operator) {
        if (operatorRepository.existsByCode(operator.getCode())) {
            throw new IllegalArgumentException("Operator with code already exists");
        }
        operator.setStatus("ACTIVE");
        return operatorRepository.save(operator);
    }
    
    @Transactional
    public Operator updateOperator(Long id, Operator operatorDetails) {
        Operator operator = operatorRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Operator not found"));
        
        operator.setName(operatorDetails.getName());
        operator.setStatus(operatorDetails.getStatus());
        operator.setBaseCurrency(operatorDetails.getBaseCurrency());
        operator.setBaseLanguage(operatorDetails.getBaseLanguage());
        
        return operatorRepository.save(operator);
    }
    
    @Transactional
    public void deleteOperator(Long id) {
        operatorRepository.deleteById(id);
    }
}
```

**Step 4.8: Create Controller**

`src/main/java/com/b2bplatform/operator/controller/OperatorController.java`:
```java
package com.b2bplatform.operator.controller;

import com.b2bplatform.operator.model.Operator;
import com.b2bplatform.operator.service.OperatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/operators")
@RequiredArgsConstructor
public class OperatorController {
    
    private final OperatorService operatorService;
    
    @GetMapping
    public ResponseEntity<List<Operator>> getAllOperators() {
        return ResponseEntity.ok(operatorService.getAllOperators());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Operator> getOperatorById(@PathVariable Long id) {
        return operatorService.getOperatorById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Operator> createOperator(@RequestBody Operator operator) {
        try {
            Operator created = operatorService.createOperator(operator);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Operator> updateOperator(
            @PathVariable Long id,
            @RequestBody Operator operator) {
        try {
            Operator updated = operatorService.updateOperator(id, operator);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOperator(@PathVariable Long id) {
        operatorService.deleteOperator(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Step 4.9: Create Main Application Class**

`src/main/java/com/b2bplatform/operator/OperatorServiceApplication.java`:
```java
package com.b2bplatform.operator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OperatorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OperatorServiceApplication.class, args);
    }
}
```

**Step 4.10: Test the Service**

```bash
# Build and run
cd services/operator-service
mvn clean install
mvn spring-boot:run

# Test endpoints
curl http://localhost:8081/api/v1/operators
curl -X POST http://localhost:8081/api/v1/operators \
  -H "Content-Type: application/json" \
  -d '{"code":"OP001","name":"Test Operator","baseCurrency":"USD","baseLanguage":"en"}'
```

### 5. Set Up API Gateway (Next Step)

After Operator Service is working, proceed to set up API Gateway following the same pattern.

---

## Daily Development Workflow

1. **Morning:**
   - Pull latest changes
   - Review tasks for the day
   - Start with tests (TDD approach)

2. **Development:**
   - Write code following standards
   - Commit frequently with meaningful messages
   - Run tests before committing

3. **End of Day:**
   - Push changes to feature branch
   - Update progress in project management tool
   - Document any blockers

---

## Useful Commands

### Docker
```bash
# Start all services
docker-compose -f infrastructure/docker-compose/docker-compose.yml up -d

# Stop all services
docker-compose -f infrastructure/docker-compose/docker-compose.yml down

# View logs
docker-compose -f infrastructure/docker-compose/docker-compose.yml logs -f

# Restart a service
docker-compose -f infrastructure/docker-compose/docker-compose.yml restart postgres
```

### Database
```bash
# Connect to PostgreSQL
docker exec -it b2b-postgres psql -U b2b_user -d b2b_platform

# List tables
\dt

# Describe table
\d operators
```

### Maven
```bash
# Clean and build
mvn clean install

# Run tests
mvn test

# Run application
mvn spring-boot:run

# Skip tests
mvn clean install -DskipTests
```

---

## Troubleshooting

### Port Already in Use
```bash
# Find process using port
lsof -i :8081  # macOS/Linux
netstat -ano | findstr :8081  # Windows

# Kill process
kill -9 <PID>  # macOS/Linux
taskkill /PID <PID> /F  # Windows
```

### Docker Issues
```bash
# Reset Docker
docker-compose down -v  # Removes volumes too
docker system prune -a  # Clean up (careful!)
```

### Database Connection Issues
- Check if PostgreSQL is running: `docker ps`
- Verify credentials in `application.yml`
- Check PostgreSQL logs: `docker logs b2b-postgres`

---

## Next Steps After Quick Start

### Completed Services ✅
1. ✅ Operator Service with API key management
2. ✅ API Gateway (routing, authentication, rate limiting)
3. ✅ Authentication Service (login, JWT, auto-registration)
4. ✅ Session Service (session management, Redis caching)
5. ✅ Wallet Service (debit/credit, webhooks, Redis caching)

### Next Recommended Service
6. **Bet Service** - Core business logic for bet processing

Refer to `IMPLEMENTATION_PLAN.md` and `docs/NEXT_STEPS.md` for detailed steps.

## Documentation

- [docs/SERVICES_OVERVIEW.md](docs/SERVICES_OVERVIEW.md) - Complete services overview
- [docs/CONFIGURATION_GUIDE.md](docs/CONFIGURATION_GUIDE.md) - Configuration reference
- [docs/TESTING_GUIDE.md](docs/TESTING_GUIDE.md) - Testing instructions
- [docs/NEXT_STEPS.md](docs/NEXT_STEPS.md) - Next implementation steps

---

**Last Updated:** February 6, 2026
