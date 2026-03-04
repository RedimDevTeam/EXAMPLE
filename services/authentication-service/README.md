# Authentication Service

Authentication Service for the B2B Gaming Platform providing player login, JWT token generation, and session management.

## Overview

The Authentication Service handles:
- **Internal login** – `POST /api/v1/auth/login` (operatorCode, username, password); JWT returned.
- **Casino launch** – `POST /api/v1/auth/launch` with header `X-Api-Key` and player info; validates operator, find/create player, create session (DB + Redis), return launch URL and token.
- JWT token generation and validation; token refresh.
- Platform-provided login page UI (Thymeleaf).
- Integration with Operator Service (API key validation) and Session Service (session create).
- Account lockout after failed login attempts.

## Running the Service

### Prerequisites
- Java 17+
- Maven 3.8+
- **Eureka Server** running (port 10000) – start first.
- PostgreSQL, Redis (infrastructure/docker-compose).
- Operator Service and Session Service (for casino launch flow) – typically via Eureka; start after Eureka.

### Start Infrastructure
```bash
cd ../../infrastructure/docker-compose
docker-compose up -d
```

### Run the Service
```bash
mvn spring-boot:run
```

The service will start on port **8087**.

## API Endpoints

### Authentication

#### Login
```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "operatorCode": "OP001",
  "username": "player123",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh_token_here",
  "expiresIn": 3600,
  "playerId": "player_123",
  "operatorId": "1",
  "operatorCode": "OP001",
  "username": "player123"
}
```

#### Refresh Token
```bash
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "refresh_token_here"
}
```

**Response:**
```json
{
  "token": "new_access_token",
  "expiresIn": 3600
}
```

#### Get Current User
```bash
GET /api/v1/auth/me
Authorization: Bearer <jwt_token>
```

**Response:**
```json
{
  "playerId": "1",
  "operatorId": "1",
  "username": "player123"
}
```

### Login Page

#### Default Login Page
```bash
GET /login
```

#### Operator-Specific Login Page
```bash
GET /login/{operatorCode}
```

Example: `GET /login/OP001`

## Configuration

### Configurable Properties

All settings are configurable via environment variables:

- `SERVER_PORT` - Service port (default: 8087)
- `OPERATOR_SERVICE_URL` - Operator Service URL (default: http://localhost:8081)
- `DATABASE_URL` - PostgreSQL connection URL
- `DATABASE_USERNAME` - Database username
- `DATABASE_PASSWORD` - Database password
- `REDIS_HOST` - Redis host (default: localhost)
- `REDIS_PORT` - Redis port (default: 6379)
- `JWT_SECRET` - JWT signing secret (MUST change in production)
- `JWT_ACCESS_TOKEN_EXPIRATION` - Access token expiration (default: 3600 seconds)
- `JWT_REFRESH_TOKEN_EXPIRATION` - Refresh token expiration (default: 604800 seconds)

See [docs/CONFIGURATION_GUIDE.md](../../docs/CONFIGURATION_GUIDE.md) for details.

## Configuration

### JWT Settings
```yaml
jwt:
  secret: ${JWT_SECRET:dev-secret-key-change-in-production}
  access-token-expiration: 3600  # 1 hour
  refresh-token-expiration: 604800  # 7 days
```

### Authentication Settings
```yaml
auth:
  max-login-attempts: 5
  lockout-duration-minutes: 30
```

### Operator Service Integration
```yaml
operator:
  service:
    url: http://localhost:8081
    timeout: 2000ms
```

## Database Schema

The service creates the following table:

```sql
CREATE TABLE player_credentials (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    player_id VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    failed_login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(operator_id, username)
);
```

## Security Features

1. **Password Security**
   - BCrypt password hashing
   - Passwords never stored in plaintext

2. **Account Lockout**
   - Lock account after 5 failed login attempts
   - 30-minute lockout duration (configurable)

3. **JWT Tokens**
   - Short-lived access tokens (1 hour)
   - Longer-lived refresh tokens (7 days)
   - Secure token generation and validation

4. **HTTPS Recommended**
   - All endpoints should use HTTPS in production

## Testing

### Create Test Player

You'll need to create a player in the database. Use SQL or create a test data script:

```sql
INSERT INTO player_credentials (operator_id, player_id, username, password_hash, status)
VALUES (
  1,
  'player_001',
  'testplayer',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: test123
  'ACTIVE'
);
```

### Test Login
```bash
curl -X POST http://localhost:8087/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "operatorCode": "OP001",
    "username": "testplayer",
    "password": "test123"
  }'
```

### Test Login Page
```bash
# Open in browser
http://localhost:8087/login/OP001
```

## Integration with API Gateway

The API Gateway routes are already configured:
- `/api/v1/auth/**` → Authentication Service (port 8087)
- `/login/**` → Login Page (port 8087)

## Features

### Phase 1 (Current)
- ✅ Player login/logout
- ✅ JWT token generation
- ✅ Token refresh
- ✅ Login page UI
- ✅ Operator validation
- ✅ Account lockout

### Phase 2 (Future)
- [ ] Password reset flow
- [ ] Email verification
- [ ] Multi-factor authentication
- [ ] Social login
- [ ] Session management integration

## Swagger Documentation

Access Swagger UI at:
```
http://localhost:8087/swagger-ui.html
```

## Health Check

```bash
curl http://localhost:8087/actuator/health
```

---

**Last Updated:** February 6, 2026
