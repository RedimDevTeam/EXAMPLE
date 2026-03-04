# Getting Started - B2B Gaming Platform

## ✅ Prerequisites Verified

Great! All prerequisites are installed:
- ✅ Java 17.0.15
- ✅ Maven 3.9.9
- ✅ Docker 28.1.1
- ✅ Docker Compose 2.35.1
- ✅ Git 2.49.0

## 🚀 Quick Start (5 Steps)

### Step 1: Create Project Structure

Run the setup script:
```bash
setup-project.bat
```

Or manually create directories if needed (they'll be created automatically when files are written).

### Step 2: Start Infrastructure

Start PostgreSQL, Redis, and Kafka:
```bash
cd infrastructure\docker-compose
docker-compose up -d
```

**Verify:**
```bash
docker exec -it b2b-postgres psql -U b2b_user -d b2b_platform -c "SELECT 1;"
docker exec -it b2b-redis redis-cli ping
docker exec -it b2b-kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
```

**Start Eureka** before other services (required for API Gateway and service discovery):
```bash
cd eureka-server
mvn spring-boot:run
```

### Step 3: Build Operator Service

Navigate to the operator service:
```bash
cd services\operator-service
```

Build the project:
```bash
mvn clean install
```

### Step 4: Run Operator Service

Start the service:
```bash
mvn spring-boot:run
```

The service will start on **http://localhost:8081**

### Step 5: Test the Service

**Check health:**
```bash
curl http://localhost:8081/actuator/health
```

**View API documentation:**
Open in browser: http://localhost:8081/swagger-ui.html

**Create an operator:**
```bash
curl -X POST http://localhost:8081/api/v1/operators ^
  -H "Content-Type: application/json" ^
  -d "{\"code\":\"OP001\",\"name\":\"Test Operator\",\"baseCurrency\":\"USD\",\"baseLanguage\":\"en\"}"
```

**Get all operators:**
```bash
curl http://localhost:8081/api/v1/operators
```

## 📁 Project Structure

```
b2b-platform/
├── common-api/               # Shared APIResponse, StatusCode (build first: mvn install)
├── eureka-server/            # Service discovery (port 10000)
├── api-gateway/              # API Gateway (port 8080); Eureka + lb:// routes
├── services/
│   ├── operator-service/     # Port 8081
│   ├── authentication-service/ # Port 8087
│   ├── session-service/      # Port 8085
│   ├── wallet-service/       # Port 8083
│   ├── bet-service/          # Port 8084
│   └── b2c-integration-service/ # Port 8086
├── infrastructure/docker-compose/  # PostgreSQL, Redis, Kafka
├── build.ps1, build.sh       # Build all JARs → deploy/
├── README.md
├── QUICK_START.md
└── docs/IMPLEMENTATION_STATUS.md  # Current implementation summary
```

## 🔧 Troubleshooting

### Port Already in Use

If port 8081 is already in use:
1. Find the process: `netstat -ano | findstr :8081`
2. Kill the process: `taskkill /PID <PID> /F`
3. Or change port in `application.properties` (or `application.yml` for api-gateway)

### Database Connection Issues

**Check PostgreSQL is running:**
```bash
docker ps
```

**Check PostgreSQL logs:**
```bash
docker logs b2b-postgres
```

**Connect to PostgreSQL:**
```bash
docker exec -it b2b-postgres psql -U b2b_user -d b2b_platform
```

### Redis Connection Issues

**Check Redis is running:**
```bash
docker exec -it b2b-redis redis-cli ping
```

Should return: `PONG`

### Maven Build Issues

**Clean and rebuild:**
```bash
mvn clean install -U
```

**Skip tests:**
```bash
mvn clean install -DskipTests
```

## 📝 Next Steps

All core services are implemented. To run the full platform:

1. Start **Eureka** first, then other services, then **API Gateway**. See [QUICK_START.md](QUICK_START.md).
2. Build all JARs: `.\build.ps1` or `./build.sh` → JARs in `deploy/`.
3. See [docs/IMPLEMENTATION_STATUS.md](docs/IMPLEMENTATION_STATUS.md) for current status and [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for roadmap.

## 🎯 Development Workflow

1. **Start infrastructure** (once per day)
   ```bash
   scripts\start-infrastructure.bat
   ```

2. **Make code changes** in your IDE

3. **Build and test**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Test APIs** using Swagger UI or Postman

5. **Commit changes**
   ```bash
   git add .
   git commit -m "feat: add operator service"
   ```

## 📚 Useful Commands

### Docker
```bash
# Start all services
docker-compose -f infrastructure\docker-compose\docker-compose.yml up -d

# Stop all services
docker-compose -f infrastructure\docker-compose\docker-compose.yml down

# View logs
docker-compose -f infrastructure\docker-compose\docker-compose.yml logs -f

# Restart a service
docker-compose -f infrastructure\docker-compose\docker-compose.yml restart postgres
```

### Maven
```bash
# Clean build
mvn clean install

# Run tests
mvn test

# Run application
mvn spring-boot:run

# Skip tests
mvn clean install -DskipTests
```

### Database
```bash
# Connect to PostgreSQL
docker exec -it b2b-postgres psql -U b2b_user -d b2b_platform

# List tables
\dt

# Describe table
\d operators

# Query operators
SELECT * FROM operators;
```

## ✅ Success Checklist

- [ ] Infrastructure running (PostgreSQL, Redis, RabbitMQ)
- [ ] Operator Service builds successfully
- [ ] Operator Service starts without errors
- [ ] Health endpoint returns 200 OK
- [ ] Swagger UI accessible
- [ ] Can create an operator via API
- [ ] Can retrieve operators via API

## 🆘 Need Help?

- Check `QUICK_START.md` for detailed setup
- Review `SRS.md` for requirements
- See `IMPLEMENTATION_PLAN.md` for roadmap
- Check service-specific README files

---

**Happy Coding! 🚀**
