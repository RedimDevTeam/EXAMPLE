# API Gateway Service

API Gateway for the B2B Gaming Platform using Spring Cloud Gateway.

## Overview

The API Gateway is the single entry point for client requests:
- **Eureka + LoadBalancer:** Routes use `lb://service-name`; discovery and load balancing are built-in.
- **Authentication:** JWT (player) and API Key (operator); API key validation via in-memory cache (Operator Service).
- **Circuit breaking** (Resilience4j), CORS, retry.

## Running the Service

### Prerequisites
- Java 17+
- Maven 3.8+
- **Eureka Server** running (port 10000) so the gateway can discover services
- Other services (operator, auth, session, wallet, bet) running and registered with Eureka

### Start Infrastructure
```bash
cd ../../infrastructure/docker-compose
docker-compose up -d
```

### Run the Service
```bash
mvn spring-boot:run
```

The service will start on port **8080**.

## Configuration

### Configurable Properties

Gateway uses `application.yml`. Key settings:

- `server.port` - Gateway port (default: 8080)
- `eureka.client.serviceUrl.defaultZone` - Eureka (default: http://localhost:10000/eureka/)
- Routes use `lb://service-name` (operator-service, authentication-service, session-service, wallet-service, bet-service). No static URLs for those.

See [docs/CONFIGURATION_GUIDE.md](../docs/CONFIGURATION_GUIDE.md) for complete configuration details.

### Routes (Eureka lb://)

- `/api/v1/operators/**` → lb://operator-service (API Key)
- `/internal/api/v1/operators/**` → lb://operator-service (no API Key; for Feign)
- `/api/v1/auth/**`, `/login/**` → lb://authentication-service
- `/api/v1/sessions/**` → lb://session-service (JWT)
- `/api/v1/wallet/**` → lb://wallet-service (JWT)
- `/internal/api/v1/wallet/**` → lb://wallet-service (no JWT; for Feign)
- `/api/v1/bets/**` → lb://bet-service (JWT)
- `/actuator/health`, `/actuator/gateway/routes` → Gateway

### Authentication

**API Key Authentication** (for admin endpoints):
```
X-API-Key: b2b_OP001_a1b2c3d4e5f6
```

**JWT Authentication** (for player endpoints):
```
Authorization: Bearer <jwt_token>
```

## Testing

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Test Operator Service Route (with API Key)
```bash
curl -H "X-API-Key: b2b_OP001_test" http://localhost:8080/api/v1/operators
```

### Gateway Routes
```bash
curl http://localhost:8080/actuator/gateway/routes
```

## Features

### Phase 1 (Current) ✅ COMPLETE
- ✅ Eureka-integrated routing (lb://service-name) with Spring Cloud LoadBalancer
- ✅ Health checks, Prometheus metrics
- ✅ API key validation (in-memory cache; populated from Operator Service)
- ✅ JWT token validation
- ✅ Circuit breaker (Resilience4j)
- ✅ Internal routes for Feign (service → gateway → target)

### Future
- [ ] Distributed tracing
- [ ] Advanced monitoring
- [ ] OAuth 2.0 support

## Development Notes

- Gateway uses reactive programming (WebFlux)
- LoadBalancer + Eureka: no static service URLs; start Eureka first
- Circuit breaker: Resilience4j; JWT: jjwt library

## Troubleshooting

### Service Not Routing
- Check if target service is running
- Verify route configuration in `application.yml`
- Check gateway logs

### Routes Not Working
- Ensure Eureka Server is running (port 10000)
- Ensure target service is up and registered: check http://localhost:10000
- Check gateway logs for LoadBalancer errors

### Authentication Failures
- Check API key format (should start with "b2b_")
- Verify JWT secret is configured
- Check token expiration

---

**Last Updated:** February 2026
