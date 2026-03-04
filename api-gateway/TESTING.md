# API Gateway Testing Guide

## Prerequisites

1. **Eureka Server** running (port 10000). Start first: `cd eureka-server && mvn spring-boot:run`
2. **Target services** running and registered with Eureka (e.g. operator-service, authentication-service).
3. **API Gateway** running (port 8080): `cd api-gateway && mvn spring-boot:run`

Routes use `lb://service-name`; the gateway discovers instances from Eureka and load-balances.

## Quick Tests

### Health and routes
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/gateway/routes
```

### Without API key (operators – should fail with 401)
```bash
curl http://localhost:8080/api/v1/operators
```

### With API key (operators – should return list)
```bash
curl -H "X-API-Key: b2b_OP001_test" http://localhost:8080/api/v1/operators
```

### Auth (public)
```bash
curl http://localhost:8080/api/v1/auth/health
```

## Common Issues

- **502 Bad Gateway / Connection refused:** Target service not running or not registered with Eureka. Check http://localhost:10000 for registered instances; start the missing service.
- **Filter errors:** Check gateway logs. Ensure API key is valid (operator exists and key matches in Operator Service).
- **No instances for service:** Eureka has no instance of that service. Start the service and wait for registration.

---

**Last Updated:** February 2026
