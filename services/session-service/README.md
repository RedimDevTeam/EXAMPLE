# Session Service

Session Management Service for the B2B Gaming Platform providing player session creation, validation, refresh, and cleanup.

## Overview

The Session Service handles:
- Session creation after player login
- Session validation
- Session refresh (extend timeout)
- Session termination (logout)
- Redis storage for fast access
- PostgreSQL storage for audit trail
- Automatic cleanup of expired sessions

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

The service will start on port **8085**.

## API Endpoints

### Create Session
```bash
POST /api/v1/sessions
Content-Type: application/json

{
  "playerId": 1,
  "operatorId": 1,
  "jwtToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh_token_here",
  "ipAddress": "192.168.1.1",
  "userAgent": "Mozilla/5.0..."
}
```

**Response:**
```json
{
  "sessionId": "sess_abc123def456",
  "playerId": 1,
  "operatorId": 1,
  "expiresAt": "2026-02-07T21:00:00",
  "createdAt": "2026-02-06T21:00:00"
}
```

### Validate Session
```bash
GET /api/v1/sessions/{sessionId}
```

**Response:**
```json
{
  "sessionId": "sess_abc123def456",
  "playerId": 1,
  "operatorId": 1,
  "status": "ACTIVE",
  "expiresAt": "2026-02-07T21:00:00",
  "lastAccessedAt": "2026-02-06T21:30:00"
}
```

### Refresh Session
```bash
PUT /api/v1/sessions/{sessionId}/refresh
```

**Response:**
```json
{
  "sessionId": "sess_abc123def456",
  "expiresAt": "2026-02-07T22:00:00",
  "lastAccessedAt": "2026-02-06T21:30:00"
}
```

### End Session
```bash
DELETE /api/v1/sessions/{sessionId}
```

**Response:**
```json
{
  "message": "Session ended successfully",
  "sessionId": "sess_abc123def456"
}
```

### Get Player Sessions
```bash
GET /api/v1/sessions/player/{playerId}
```

## Configuration

### Configurable Properties

All settings are configurable via environment variables:

- `SERVER_PORT` - Service port (default: 8085)
- `DATABASE_URL` - PostgreSQL connection URL
- `DATABASE_USERNAME` - Database username
- `DATABASE_PASSWORD` - Database password
- `REDIS_HOST` - Redis host (default: localhost)
- `REDIS_PORT` - Redis port (default: 6379)
- `SESSION_DEFAULT_TIMEOUT_HOURS` - Default session timeout (default: 24)
- `SESSION_REFRESH_ON_ACCESS` - Refresh on access (default: true)
- `SESSION_CLEANUP_INTERVAL_MINUTES` - Cleanup interval (default: 60)

See [docs/CONFIGURATION_GUIDE.md](../../docs/CONFIGURATION_GUIDE.md) for details.

## Configuration

### Session Timeout
```yaml
session:
  default-timeout-hours: 24  # Default session timeout
  refresh-on-access: true    # Extend timeout on access
  cleanup-interval-minutes: 60  # Cleanup job frequency
```

## Database Schema

The service creates the following table:

```sql
CREATE TABLE sessions (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(100) UNIQUE NOT NULL,
    player_id BIGINT NOT NULL,
    operator_id BIGINT NOT NULL,
    jwt_token TEXT NOT NULL,
    refresh_token TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    last_accessed_at TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    INDEX idx_session_id (session_id),
    INDEX idx_player_id (player_id),
    INDEX idx_expires_at (expires_at)
);
```

## Redis Storage

- **Key Format:** `session:{sessionId}`
- **Value:** Session object (JSON)
- **TTL:** Matches session expiration time
- **Purpose:** Fast session lookup

## Features

### Phase 1 (Current)
- ✅ Session creation
- ✅ Session validation
- ✅ Session refresh
- ✅ Session termination
- ✅ Redis caching
- ✅ PostgreSQL storage
- ✅ Cleanup job

### Phase 2 (Future)
- [ ] Session analytics
- [ ] Concurrent session limits
- [ ] Session migration
- [ ] Multi-device support

## Integration with Authentication Service

After a player logs in:
1. Authentication Service generates JWT tokens
2. Authentication Service calls Session Service to create session
3. Session Service stores session in Redis and PostgreSQL
4. Session ID can be returned to client (optional)

## Cleanup Job

The service automatically cleans up expired sessions:
- **Frequency:** Every hour
- **Action:** Marks expired sessions as "EXPIRED" in database
- **Redis:** Removes expired sessions from cache

## Testing

### Create Session
```bash
curl -X POST http://localhost:8085/api/v1/sessions \
  -H "Content-Type: application/json" \
  -d '{
    "playerId": 1,
    "operatorId": 1,
    "jwtToken": "test_token",
    "refreshToken": "test_refresh",
    "ipAddress": "192.168.1.1",
    "userAgent": "Mozilla/5.0"
  }'
```

### Validate Session
```bash
curl http://localhost:8085/api/v1/sessions/sess_abc123def456
```

### Refresh Session
```bash
curl -X PUT http://localhost:8085/api/v1/sessions/sess_abc123def456/refresh
```

### End Session
```bash
curl -X DELETE http://localhost:8085/api/v1/sessions/sess_abc123def456
```

## Swagger Documentation

Access Swagger UI at:
```
http://localhost:8085/swagger-ui.html
```

## Health Check

```bash
curl http://localhost:8085/actuator/health
```

---

**Last Updated:** February 6, 2026
