# Wallet Service

Wallet Management Service for the B2B Gaming Platform providing wallet operations (debit, credit, balance query) with operator webhook integration.

## Overview

The Wallet Service handles:
- Wallet debit operations (bet placement)
- Wallet credit operations (win payout)
- Balance queries
- Operator webhook integration (calls operator endpoints)
- Transaction history
- Idempotency handling
- Retry mechanism for operator calls
- Circuit breaker for operator integration

## Running the Service

### Prerequisites
- Java 17+
- Maven 3.8+
- Eureka Server running (port 10000) – start first
- PostgreSQL, Redis (infrastructure/docker-compose)
- Operator Service (for operator info; via Eureka or gateway)

### Start Infrastructure
```bash
cd ../../infrastructure/docker-compose
docker-compose up -d
```

### Run the Service
```bash
mvn spring-boot:run
```

The service will start on port **8083**.

## API Endpoints

### Debit Wallet
```bash
POST /api/v1/wallet/debit
Content-Type: application/json

{
  "operatorId": 1,
  "playerId": "player_123",
  "amount": 100.00,
  "currency": "USD",
  "reference": "bet_xyz789",
  "description": "Bet placement - Roulette"
}
```

**Response:**
```json
{
  "success": true,
  "transactionId": "txn_abc123",
  "balance": 900.00,
  "currency": "USD",
  "timestamp": "2026-02-06T18:00:00Z"
}
```

### Credit Wallet
```bash
POST /api/v1/wallet/credit
Content-Type: application/json

{
  "operatorId": 1,
  "playerId": "player_123",
  "amount": 250.00,
  "currency": "USD",
  "reference": "win_xyz789",
  "description": "Game win - Roulette"
}
```

### Get Balance
```bash
GET /api/v1/wallet/balance?operatorId=1&playerId=player_123
```

**Response:**
```json
{
  "success": true,
  "playerId": "player_123",
  "balance": 1150.00,
  "currency": "USD",
  "availableBalance": 1100.00,
  "lockedBalance": 50.00,
  "timestamp": "2026-02-06T18:10:00Z"
}
```

### Get Transaction History
```bash
GET /api/v1/wallet/transactions?playerId=player_123&limit=10
```

### Get Transaction
```bash
GET /api/v1/wallet/transactions/{transactionId}
```

## Operator Wallet Configuration

Before using wallet operations, configure operator wallet URLs:

```bash
POST /api/v1/wallet/config
Content-Type: application/json

{
  "operatorId": 1,
  "debitUrl": "https://operator.com/api/wallet/debit",
  "creditUrl": "https://operator.com/api/wallet/credit",
  "balanceUrl": "https://operator.com/api/wallet/balance",
  "transferUrl": "https://operator.com/api/wallet/transfer",
  "authType": "API_KEY",
  "authHeader": "X-API-Key",
  "authValue": "operator_api_key_here",
  "timeoutMs": 5000,
  "retryAttempts": 3,
  "enabled": true
}
```

## Database Schema

The service creates the following tables:

```sql
CREATE TABLE operator_wallet_config (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL UNIQUE,
    debit_url VARCHAR(500) NOT NULL,
    credit_url VARCHAR(500) NOT NULL,
    balance_url VARCHAR(500) NOT NULL,
    transfer_url VARCHAR(500),
    auth_type VARCHAR(50),
    auth_header VARCHAR(100),
    auth_value VARCHAR(255),
    timeout_ms INT,
    retry_attempts INT,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE wallet_transactions (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    player_id VARCHAR(100) NOT NULL,
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(18,2),
    currency VARCHAR(3),
    status VARCHAR(20) NOT NULL,
    operator_url VARCHAR(500),
    request_payload JSONB,
    response_payload JSONB,
    error_message TEXT,
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    completed_at TIMESTAMP
);
```

## Features

### Phase 1 (Current)
- ✅ Wallet debit operation
- ✅ Wallet credit operation
- ✅ Balance query
- ✅ Operator webhook integration
- ✅ Transaction history
- ✅ Idempotency handling
- ✅ Retry mechanism
- ✅ Circuit breaker

### Phase 2 (Future)
- [ ] Transfer operations (deposit/withdrawal)
- [ ] Async processing with message queue
- [ ] Transaction reconciliation
- [ ] Wallet analytics

## Testing

### 1. Configure Operator Wallet URLs
```bash
curl -X POST http://localhost:8083/api/v1/wallet/config \
  -H "Content-Type: application/json" \
  -d '{
    "operatorId": 1,
    "debitUrl": "https://operator.com/api/wallet/debit",
    "creditUrl": "https://operator.com/api/wallet/credit",
    "balanceUrl": "https://operator.com/api/wallet/balance",
    "authType": "API_KEY",
    "authHeader": "X-API-Key",
    "authValue": "test_key",
    "timeoutMs": 5000,
    "retryAttempts": 3,
    "enabled": true
  }'
```

### 2. Test Debit Operation
```bash
curl -X POST http://localhost:8083/api/v1/wallet/debit \
  -H "Content-Type: application/json" \
  -d '{
    "operatorId": 1,
    "playerId": "player_123",
    "amount": 100.00,
    "currency": "USD",
    "reference": "bet_001",
    "description": "Test bet"
  }'
```

### 3. Test Balance Query
```bash
curl "http://localhost:8083/api/v1/wallet/balance?operatorId=1&playerId=player_123"
```

## Swagger Documentation

Access Swagger UI at:
```
http://localhost:8083/swagger-ui.html
```

## Health Check

```bash
curl http://localhost:8083/actuator/health
```

---

**Last Updated:** February 6, 2026
