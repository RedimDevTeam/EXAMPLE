# Operator Service

Operator Management Service for the B2B Gaming Platform.

## Overview

This service handles:
- Operator account creation and management
- Operator configuration
- Operator status management

## Running the Service

### Prerequisites
- Java 17+
- Maven 3.8+
- **Eureka Server** running (port 10000) – start first for discovery.
- PostgreSQL, Redis (infrastructure/docker-compose).

### Start Infrastructure
```bash
cd ../../infrastructure/docker-compose
docker-compose up -d
```

### Run the Service
```bash
mvn spring-boot:run
```

The service will start on port 8081.

## API Documentation

Once the service is running, access Swagger UI at:
http://localhost:8081/swagger-ui.html

## API Endpoints

- `GET /api/v1/operators` - Get all operators
- `GET /api/v1/operators/{id}` - Get operator by ID
- `GET /api/v1/operators/code/{code}` - Get operator by code
- `POST /api/v1/operators` - Create operator
- `PUT /api/v1/operators/{id}` - Update operator
- `DELETE /api/v1/operators/{id}` - Delete operator

## Configuration

### Configurable Properties

All settings are configurable via environment variables:

- `SERVER_PORT` - Service port (default: 8081)
- `DATABASE_URL` - PostgreSQL connection URL
- `DATABASE_USERNAME` - Database username
- `DATABASE_PASSWORD` - Database password
- `REDIS_HOST` - Redis host (default: localhost)
- `REDIS_PORT` - Redis port (default: 6379)

**Example:**
```bash
set SERVER_PORT=8081
set DATABASE_URL=jdbc:postgresql://localhost:5432/b2b_platform
set DATABASE_USERNAME=b2b_user
set DATABASE_PASSWORD=b2b_password
set REDIS_HOST=localhost
set REDIS_PORT=6379
```

See [docs/CONFIGURATION_GUIDE.md](../../docs/CONFIGURATION_GUIDE.md) for complete configuration details.

## Health Check

- `GET /actuator/health` - Service health status

## Example Request

Create an operator:
```bash
curl -X POST http://localhost:8081/api/v1/operators \
  -H "Content-Type: application/json" \
  -d '{
    "code": "OP001",
    "name": "Test Operator",
    "baseCurrency": "USD",
    "baseLanguage": "en"
  }'
```
