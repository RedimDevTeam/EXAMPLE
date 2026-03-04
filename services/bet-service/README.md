# Bet Service

Bet Management Service for the B2B Gaming Platform providing bet placement, settlement, cancellation, and history.

## Overview

The Bet Service handles:
- **Bet placement** - Place bets on games (game-agnostic)
- **Idempotency** - Prevents duplicate bet posts and duplicate settlements
- **Bet settlement** - Settle bets with payouts (called by Game Services)
- **Bet cancellation** - Cancel pending/accepted bets
- **Bet history** - Query bet details and player bet history
- **Wallet integration** - Debit on placement, credit on settlement/refund
- **Game Service integration** - Validate bet types and get odds (mock until Game Service implemented)

## Architecture

**Game-Agnostic Design:**
- Bet Service does NOT contain game-specific logic
- Game Services handle bet type validation, odds calculation, and payout calculation
- Bet Service stores game-specific bet types as strings but doesn't interpret them

**Integration Flow:**
1. **Bet Placement:** Bet Service → Game Service (validate) → Wallet Service (debit) → Bet Service (save)
2. **Bet Settlement:** Game Service (calculate payout) → Bet Service (settle) → Wallet Service (credit)

## Running the Service

### Prerequisites
- Java 17+
- Maven 3.8+
- **Eureka Server** running (port 10000) – start first.
- PostgreSQL, Redis (infrastructure/docker-compose).
- Wallet Service (for debit/credit via API Gateway; Feign uses gateway URL or Eureka).
- Game Service (optional – mock used until implemented).

### Start Infrastructure
```bash
cd ../../infrastructure/docker-compose
docker-compose up -d
```

### Run the Service
```bash
mvn spring-boot:run
```

The service will start on port **8084**.

## Configuration

### Configurable Properties

All settings are configurable via environment variables:

- `SERVER_PORT` - Service port (default: 8084)
- `WALLET_SERVICE_URL` - Wallet Service URL (default: http://localhost:8083)
- `GAME_SERVICE_URL` - Game Service URL (default: http://localhost:8089)
- `SESSION_SERVICE_URL` - Session Service URL (default: http://localhost:8085)
- `DATABASE_URL` - PostgreSQL connection URL
- `DATABASE_USERNAME` - Database username
- `DATABASE_PASSWORD` - Database password
- `REDIS_HOST` - Redis host (default: localhost)
- `REDIS_PORT` - Redis port (default: 6379)
- `BET_MIN_AMOUNT` - Minimum bet amount (default: 0.01)
- `BET_MAX_AMOUNT` - Maximum bet amount (default: 10000.00)
- `BET_DEFAULT_CURRENCY` - Default currency (default: USD)

**Example:**
```bash
set SERVER_PORT=8084
set WALLET_SERVICE_URL=http://localhost:8083
set DATABASE_URL=jdbc:postgresql://localhost:5432/b2b_platform
set DATABASE_USERNAME=b2b_user
set DATABASE_PASSWORD=b2b_password
set REDIS_HOST=localhost
set REDIS_PORT=6379
```

See [docs/CONFIGURATION_GUIDE.md](../../docs/CONFIGURATION_GUIDE.md) for complete configuration details.

## API Endpoints

### Player Endpoints (via API Gateway with JWT)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/bets` | Place a bet |
| GET | `/api/v1/bets/{betId}` | Get bet details |
| GET | `/api/v1/bets/player/{playerId}` | Get player bet history |
| DELETE | `/api/v1/bets/{betId}` | Cancel a bet |

### Internal Endpoints (called by Game Services)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/bets/{betId}/settle` | Settle a bet |

## API Documentation

Once the service is running, access Swagger UI at:
http://localhost:8084/swagger-ui.html

## Example Requests

### Place a Bet (Baccarat Main Bet)
```bash
curl -X POST http://localhost:8084/api/v1/bets \
  -H "Content-Type: application/json" \
  -H "X-Player-Id: 1" \
  -H "X-Operator-Id: 1" \
  -d '{
    "gameCode": "BACCARAT",
    "gameRoundId": "round_12345",
    "betCategory": "MAIN_BET",
    "betType": "PLAYER",
    "betAmount": 100.00,
    "currency": "USD"
  }'
```

### Place a Bet (Roulette Straight Up)
```bash
curl -X POST http://localhost:8084/api/v1/bets \
  -H "Content-Type: application/json" \
  -H "X-Player-Id: 1" \
  -H "X-Operator-Id: 1" \
  -d '{
    "gameCode": "ROULETTE_EUROPEAN",
    "gameRoundId": "round_12345",
    "betCategory": "MAIN_BET",
    "betType": "STRAIGHT_UP",
    "betAmount": 10.00,
    "currency": "USD",
    "betDetails": {
      "number": 7
    }
  }'
```

### Get Bet Details
```bash
curl http://localhost:8084/api/v1/bets/BET_12345
```

### Get Player Bet History
```bash
curl http://localhost:8084/api/v1/bets/player/1
```

### Cancel a Bet
```bash
curl -X DELETE http://localhost:8084/api/v1/bets/BET_12345
```

### Settle a Bet (Internal - called by Game Service)
```bash
curl -X POST http://localhost:8084/api/v1/bets/BET_12345/settle \
  -H "Content-Type: application/json" \
  -d '{
    "result": "WIN",
    "payoutAmount": 200.00,
    "settlementDetails": {
      "winningHand": "PLAYER",
      "odds": 1.0
    }
  }'
```

## Database Schema

### `bets` Table
- `id` - Primary key
- `bet_id` - Unique bet identifier
- `player_id` - Player ID
- `operator_id` - Operator ID
- `game_code` - Game code (e.g., "BACCARAT", "BLACKJACK")
- `game_round_id` - Round identifier from Game Service
- `bet_category` - "MAIN_BET" or "SIDE_BET"
- `bet_type` - Game-specific bet type (e.g., "PLAYER", "BANKER", "ANTE")
- `bet_amount` - Bet amount
- `currency` - Currency code
- `status` - Bet status (PENDING, ACCEPTED, SETTLED, CANCELLED, REJECTED)
- `odds` - Odds provided by Game Service
- `payout_amount` - Payout amount calculated by Game Service
- `bet_details` - JSONB for flexible game-specific data
- `wallet_transaction_id` - Reference to Wallet Service transaction
- `created_at`, `updated_at`, `settled_at` - Timestamps

## Integration

### Wallet Service
- **Debit:** Called when placing a bet
- **Credit:** Called when settling a bet (win) or cancelling a bet (refund)

### Game Service (Future)
- **Validate Bet:** Validates bet type and returns odds
- **Calculate Payout:** Calculates payout based on result
- Currently uses mock validation until Game Service is implemented

## Health Check

- `GET /actuator/health` - Service health status

## Testing

See [docs/TESTING_GUIDE.md](../../docs/TESTING_GUIDE.md) for comprehensive testing instructions.

## Notes

- **Game-Agnostic:** Bet Service does NOT contain game-specific logic
- **Mock Game Service:** Uses mock validation until Game Service is implemented
- **Circuit Breaker:** Integrated with Resilience4j for Wallet Service calls
- **Transaction Safety:** Uses database transactions for bet placement and settlement
- **Idempotency:** Prevents duplicate bet posts and duplicate settlements due to network retries or concurrent requests. Database is source of truth; Redis is optimization layer for fast duplicate detection.
- **Bet ID Generation:** Deterministic based on request parameters (gameCode, gameRoundId, playerId, betType, betAmount, currency). Same request = Same bet ID.

## Related Documentation

- [Bet Service Implementation Plan](../docs/BET_SERVICE_IMPLEMENTATION_PLAN.md)
- [Bet Service Architecture](../docs/BET_SERVICE_ARCHITECTURE.md)
- [Critical Timing Flow](../docs/BET_SERVICE_CRITICAL_FLOW.md)
- [Idempotency Implementation](../docs/BET_SERVICE_IDEMPOTENCY.md)

---

**Last Updated:** February 6, 2026
