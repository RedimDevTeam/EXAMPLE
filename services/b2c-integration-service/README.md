# B2C Integration Service

B2C Provider Wallet Integration Service for the B2B Gaming Platform.

## Overview

This service handles **B2C Integration (Scenario 2)** - Provider Wallet Model, where B2C Providers manage their own player wallets. The gaming platform calls provider APIs to debit/credit player balances.

**Key Features:**
- Provider wallet operations (debit/credit/refund/cancel)
- Provider configuration management
- XML envelope support (for legacy providers)
- HMAC-SHA256 signature validation
- Circuit breaker for provider API calls
- Idempotency handling

## Architecture

**Provider Wallet Model:**
- Provider holds wallet balance
- Gaming platform calls provider APIs
- Direct operations (no two-step flow)
- Supports JSON and XML formats

## Running the Service

### Prerequisites
- Java 17+
- Maven 3.8+
- Eureka Server running (port 10000) – start first
- PostgreSQL, Redis (infrastructure/docker-compose)

### Start Infrastructure
```bash
cd ../../infrastructure/docker-compose
docker-compose up -d
```

### Run the Service
```bash
mvn spring-boot:run
```

The service will start on port **8086**.

## Configuration

### Configurable Properties

All settings are configurable via environment variables:

- `SERVER_PORT` - Service port (default: 8086)
- `DATABASE_URL` - PostgreSQL connection URL
- `DATABASE_USERNAME` - Database username
- `DATABASE_PASSWORD` - Database password
- `REDIS_HOST` - Redis host (default: localhost)
- `REDIS_PORT` - Redis port (default: 6379)
- `B2C_PROVIDER_TIMEOUT_MS` - Provider API timeout (default: 5000ms)
- `B2C_PROVIDER_RETRY_ATTEMPTS` - Retry attempts (default: 3)

**Example:**
```bash
set SERVER_PORT=8086
set DATABASE_URL=jdbc:postgresql://localhost:5432/b2b_platform
set DATABASE_USERNAME=b2b_user
set DATABASE_PASSWORD=b2b_password
set REDIS_HOST=localhost
set REDIS_PORT=6379
```

## API Documentation

Once the service is running, access Swagger UI at:
http://localhost:8086/swagger-ui.html

## API Endpoints

### Provider Wallet Operations (JSON)
- `POST /api/v1/b2c/wallet/debit` - Debit player wallet
- `POST /api/v1/b2c/wallet/credit` - Credit player wallet
- `POST /api/v1/b2c/wallet/refund` - Refund transaction
- `POST /api/v1/b2c/wallet/cancel` - Cancel transaction
- `GET /api/v1/b2c/wallet/balance` - Get player balance

### XML Envelope Support (Legacy Providers)
- `POST /api/v1/b2c/xml/wallet/debit` - Debit (XML)
- `POST /api/v1/b2c/xml/wallet/credit` - Credit (XML)
- `POST /api/v1/b2c/xml/wallet/refund` - Refund (XML)
- `POST /api/v1/b2c/xml/wallet/cancel` - Cancel (XML)
- `GET /api/v1/b2c/xml/wallet/balance` - Balance (XML)

### Provider Configuration
- `POST /api/v1/b2c/providers` - Create provider config
- `GET /api/v1/b2c/providers/{providerId}` - Get provider config
- `PUT /api/v1/b2c/providers/{providerId}` - Update provider config
- `DELETE /api/v1/b2c/providers/{providerId}` - Delete provider config
- `GET /api/v1/b2c/providers` - List all providers

## Health Check

- `GET /actuator/health` - Service health status

## Security

### HMAC Signature
All requests to providers use HMAC-SHA256 signature:
```
Signature = HMAC-SHA256(payload + API_SECRET)
Header: X-SIGNATURE = Signature
```

### Authentication Headers
- `X-Provider-Id` - Provider identifier
- `X-Signature` - HMAC signature

## Related Documentation

- [B2C Integration Implementation Plan](../../docs/integration/B2C_INTEGRATION_IMPLEMENTATION_PLAN.md)
- [B2B vs B2C Integration Separation](../../docs/integration/B2B_VS_B2C_INTEGRATION_SEPARATION.md)
- [Unified Integration Standard](../../docs/integration/B2B_B2C_Unified_Gaming_Integration_Standard_v1_1.md)

---

**Last Updated:** February 14, 2026
