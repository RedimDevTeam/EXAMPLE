# Software Requirements Specification (SRS)
## B2B Gaming Platform

---

## 1. Introduction

### 1.1 Purpose

This document defines the Software Requirements Specification (SRS) for the B2B Gaming Platform. It outlines functional and non-functional requirements for providing gaming services to third-party operators, including game hosting, bet processing, result generation, and payout settlement.

### 1.2 Scope

The B2B Gaming Platform is designed to:

- Integrate with multiple operators
- Provide a portfolio of betting games
- Process player bets routed via operators
- Generate results and calculate payouts
- Manage financial settlements with operators

**Note:** Player onboarding, wallet funding, and player ownership remain the responsibility of operators and are out of scope for this platform.

### 1.3 Definitions

| Term | Description |
|------|-------------|
| Operator | Third-party business integrating with the platform |
| Player | End user belonging to an operator |
| Platform | B2B gaming system hosting games |
| Bet | Wager placed on a game |
| Settlement | Financial reconciliation between platform and operator |

---

## 2. Overall Description

### 2.1 Product Perspective

The platform is a multi-tenant gaming engine that enables operators to offer games to their players without owning game infrastructure.

Operators integrate via APIs or WebSocket interfaces to:

- Launch games
- Place bets
- Receive results
- Process settlements

### 2.2 System Architecture (High Level)

**External Systems**

- Operator Wallet Systems
- Operator Player Management Systems
- Payment Gateways (via operator)

**Platform Core Systems**

- Game Engine
- Bet Management System
- Result Engine
- Settlement System
- Operator Integration Gateway
- Reporting & Analytics

---

## 3. Functional Requirements

### 3.1 Operator Integration Management

The platform shall provide integration capabilities for operators.

**Requirements**

- Operator account creation and configuration
- API key / token authentication
- IP whitelisting (optional)
- Operator-specific game configuration
- Operator status control (active/inactive)

### 3.2 Game Provisioning

**Requirements**

- Support onboarding of any number of games
- Enable/disable games per operator
- Configure game parameters operator-wise
- Provide game launch URLs/APIs
- Support desktop and mobile access

### 3.3 Player Session Handling

**Requirements**

- Accept operator player token/session ID
- Validate session authenticity
- Maintain in-game session state
- Handle session timeout and reconnection

### 3.4 Bet Management

**Bet Placement Flow**

1. Receive bet request from operator
2. Validate game status and bet rules
3. Request wallet debit from operator
4. Confirm bet acceptance
5. Record bet transaction

**Requirements**

- Unique bet ID generation
- Support single and multiple bets
- Exposure tracking per game/operator
- Bet cancellation (if applicable)

### 3.5 Wallet Debit/Credit Integration

The platform shall not hold player funds but shall integrate with operator wallets and B2C provider wallets.

#### 3.5.1 B2B Integration (Operator Wallets)

**Debit Requirements**

- Send debit request before bet confirmation
- Handle debit success/failure
- Retry on transient failures
- Support two-step fund transfer flow (Request → Confirm)
- Support direct debit for shared wallet model

**Credit Requirements**

- Send payout credit requests
- Support partial and full payouts
- Maintain credit confirmation logs

#### 3.5.2 B2C Integration (Provider Wallets)

The platform shall support B2C providers who manage their own player wallets.

**Provider Wallet Model Requirements**

- Direct debit/credit operations (no two-step flow)
- Support JSON and XML envelope formats
- HMAC-SHA256 signature authentication
- Provider configuration management
- Idempotency handling for all operations
- Transaction tracking and audit trail

**Supported Operations**

- Debit player wallet
- Credit player wallet
- Refund transactions
- Cancel transactions
- Balance queries

**Legacy Support**

- XML envelope format for providers requiring legacy integration
- Automatic XML ↔ JSON conversion
- Signature validation for XML requests

### 3.6 Result Management

**Requirements**

- Generate results via game engines or RNG
- Ingest results from external systems (if required)
- Validate result integrity
- Publish results to operators in real time

### 3.7 Payout Calculation

**Requirements**

- Apply game payout rules
- Calculate operator exposure
- Compute player winnings
- Generate settlement instructions

### 3.8 Settlement & Reconciliation

**Requirements**

- Maintain bet vs payout ledger
- Calculate operator net position
- Provide settlement reports
- Support periodic reconciliation

### 3.9 Multi-Game Betting

**Requirements**

- Concurrent bet processing
- Cross-game wallet validation via operator
- Unified reporting across games

### 3.10 Reporting & Analytics

**Reports**

- Game-wise bet volume
- Operator turnover reports
- Win/Loss analysis
- Exposure reports
- Settlement reports
- Real-time dashboards

### 3.11 Platform Localization & Currency Management

The platform shall support multi-regional operations by enabling multiple languages, currencies, and hierarchical operator structures.

#### 3.11.1 Multi-Language Support

**Functional Capabilities**

1. Platform UI translatable into multiple languages
2. Game screens, bet spots, tickets, and reports localized
3. Language packs configurable without code changes
4. Operators select default language
5. Players inherit operator language (unless overridden)

**Scope**

- Lobby & navigation
- Game names & descriptions
- Bet spots
- Tickets & history
- Reports
- Notifications
- Responsible gaming messages

#### 3.11.2 Multiple Currency Support

**Capabilities**

1. Operators operate in different currencies
2. Players bet in operator currency
3. Transactions recorded in operator currency
4. Reports & ledgers reflect same currency
5. Configurable symbols & rounding rules

**Examples:** INR, USD, EUR, AED, MYR, THB

#### 3.11.3 Master → Local Operator Hierarchy

**Structure**
Platform
└── Master Operator
├── Local Operator(s)


**Master Operator Capabilities**

- Create Local Operators
- Configure currency & language
- Allocate credit limits
- Monitor revenue
- View consolidated reports

**Local Operator Capabilities**

- Manage players
- Offer games
- Handle fund transfers
- View reports

#### 3.11.4 Base Currency

Each operator has one base currency.

**Characteristics**

- All bets occur in this currency
- Transfers occur in this currency
- Settlements generated in this currency
- No cross-currency betting

#### 3.11.5 Base Language

**Characteristics**

- Default back-office language
- Default player language
- Used for tickets, reports, notifications

#### 3.11.6 Combined Currency & Language Mapping

| Master | Local | Currency | Language |
|--------|-------|----------|----------|
| Master IN | Local IN-1 | INR | English |
| Master IN | Local IN-2 | INR | Tamil |
| Master EU | Local EU-1 | EUR | German |
| Master MY | Local MY-1 | MYR | Malay |

### 3.12 Reporting & Ledger Considerations

- Reports generated in operator currency
- Master reports may convert currency
- FX rate configuration supported

### 3.13 Multi-Currency Settlement Ledger Design

#### 3.13.1 Ledger Objectives

- Record all monetary movements
- Maintain operator balances
- Track utilization
- Support reconciliation
- Provide audit trails
- Prevent currency mixing

#### 3.13.2 Ledger Hierarchy
Platform Ledger
└── Operator Ledger
└── Player Utilization Ledger
└── Game Transaction Ledger


#### 3.13.3 Currency Segregation

- Entries recorded in base currency only
- No cross-currency postings
- Conversion allowed only in reports

#### 3.13.4 Ledger Account Types

**Operator Transfer Wallet Ledger**

| Entry | Type | Description |
|-------|------|-------------|
| Player Transfer In | Credit | Funds moved to platform |
| Withdrawal Out | Debit | Funds returned |

**Bet Utilization Ledger**

| Entry | Type |
|-------|------|
| Bet Placement | Debit |
| Bet Cancellation | Credit |
| Exposure Lock | Memo |

**Game Settlement Ledger**

| Entry | Type |
|-------|------|
| Player Win | Credit |
| Player Loss | Debit |
| Jackpot/Bonus | Credit |

**Operator Settlement Ledger**

| Entry | Type |
|-------|------|
| Operator Profit | Credit |
| Operator Loss | Debit |
| Revenue Share | Debit |
| Platform Fee | Debit |

#### 3.13.5 Ledger Entry Structure

| Field | Description |
|-------|-------------|
| Transaction ID | Unique ID |
| Operator ID | Linked operator |
| Player ID | Optional |
| Currency | Base currency |
| Amount | Value |
| Debit/Credit | Direction |
| Pre Balance | Before |
| Post Balance | After |
| Game ID | Optional |
| Bet/Ticket ID | Optional |
| Timestamp | Time |
| Settlement Cycle ID | Cycle ref |

#### 3.13.6 Player Fund Flow (Transfer Wallet)

1. Player requests transfer
2. Operator debits wallet
3. Platform ledger credited
4. Player bets across games
5. Bets deducted
6. Wins credited
7. Balance withdrawable

#### 3.13.7 Settlement Cycles

- Daily
- Weekly
- Monthly
- Custom

**Formula**
Operator Net = Bets – Wins – Fees – Taxes


#### 3.13.8 Multi-Game Ledger Handling

| Game | Bet | Result | Impact |
|------|-----|--------|--------|
| Roulette | 100 | Loss | Debit |
| Poker | 200 | Win 350 | Credit |
| Lottery | 50 | Pending | Exposure |

#### 3.13.9 Exposure Tracking

**Types**

- Open bets
- Tournament buy-ins
- Jackpots

**Formula**
Available Balance = Transfer Balance – Exposure


#### 3.13.10 Currency Conversion (Reporting)

- FX upload
- Rate locking
- Consolidated reporting
- FX gain/loss tracking

#### 3.13.11 Audit & Reconciliation

- Immutable entries
- Reversals only
- Operator reconciliation
- Provider reconciliation
- Bank reconciliation

#### 3.13.12 Sample Ledger Flow

| Step | Description | Debit | Credit | Balance |
|------|-------------|-------|--------|---------|
| 1 | Transfer | — | 10,000 | 10,000 |
| 2 | Roulette Bet | 1,000 | — | 9,000 |
| 3 | Poker Win | — | 2,500 | 11,500 |
| 4 | Lottery Bet | 500 | — | 11,000 |

#### 3.13.13 Compliance & Controls

- Rounding rules
- Bet limits per currency
- AML hooks
- Regulatory extracts

### 3.14 Forex Conversion Management

The B2B Gaming Platform shall support foreign exchange (Forex / FX) conversion for reporting, consolidation, and cross-regional financial analysis where operators function in different base currencies.

Forex conversion shall be used strictly for reporting and settlement reference purposes and shall not alter ledger postings, which always remain in the operator's base currency.

#### 3.14.1 Objectives

The Forex Conversion module shall:

1. Enable multi-currency financial reporting
2. Support master operator consolidated views
3. Provide settlement equivalence across currencies
4. Maintain transparency in FX rate usage
5. Ensure auditability of all conversion calculations

#### 3.14.2 Source of Exchange Rates

The platform shall integrate with reliable third-party Forex rate providers.

**Supported Sources (Configurable):**

- xe.com
- openexchangerates.org
- fixer.io
- oanda.com
- exchangerate-api providers
- Central bank reference feeds (if required)

The system shall allow configuring one or more providers with failover priority.

#### 3.14.3 Rate Fetch Mechanism

Exchange rates shall be retrieved via secure APIs.

**Capabilities**

- Automated scheduled rate pulls
- Manual rate upload (backup mode)
- Multiple fetch frequencies:
  - Real-time
  - Hourly
  - Daily (default)

All fetched rates shall be time-stamped and stored.

#### 3.14.4 Rate Storage & Versioning

The platform shall maintain a historical FX rate repository.

**Stored Attributes**

| Field | Description |
|-------|-------------|
| Rate ID | Unique identifier |
| Base Currency | Source currency |
| Target Currency | Converted currency |
| Exchange Rate | Conversion value |
| Rate Source | Provider name (e.g., XE) |
| Fetched Timestamp | Retrieval time |
| Effective Date | Business date |
| Status | Active / Archived |

Historical rates shall not be overwritten.

#### 3.14.5 Conversion Usage Areas

Forex conversion shall be applied only in the following contexts:

1. Master Operator consolidated reports
2. Platform revenue reporting
3. Multi-operator settlement summaries
4. Regulatory financial reporting
5. Tax computation references (if required)

It shall not be used for:

- Player betting transactions
- Game settlements
- Wallet debits/credits

(All transactional ledgers remain in base currency.)

#### 3.14.6 Rate Locking for Settlement

To avoid fluctuation disputes, the system shall support FX rate locking.

**Models**

1. Transaction Date Rate – Rate on bet date
2. Settlement Date Rate – Rate on settlement cycle date
3. Average Period Rate – Mean rate over cycle
4. Manual Locked Rate – Finance-configured

Rate locking rules shall be configurable per operator or master operator.

#### 3.14.7 Conversion Calculation

**Standard Formula**
Converted Amount = Native Amount × FX Rate


**Example**

| Operator | Native Currency | Amount | FX Rate (to USD) | USD Equivalent |
|----------|----------------|--------|------------------|----------------|
| IN Ops | INR | 1,000,000 | 0.012 | 12,000 |
| EU Ops | EUR | 50,000 | 1.10 | 55,000 |

#### 3.14.8 FX Gain / Loss Tracking

If settlement is executed across currencies, the platform shall compute FX variance.

**Ledger Treatment**

- FX Gain → Platform Financial Ledger
- FX Loss → Platform Financial Ledger
- Segregated from gaming P&L

#### 3.14.9 Failover & Reliability

To ensure uninterrupted FX availability:

- Multiple rate providers configurable
- Automatic fallback if primary fails
- Last known rate usable (time-limited)
- Manual override by finance admin

#### 3.14.10 Audit & Compliance

Forex handling shall support:

- Rate source traceability
- Historical rate audits
- Settlement rate snapshots
- Regulatory disclosures

#### 3.14.11 Controls & Validation

The system shall enforce:

- Max variance threshold alerts
- Abnormal rate spike detection
- Currency pair validation
- Decimal precision rules per currency

---

## 4. Non-Functional Requirements

### 4.1 Performance

- High concurrency support
- Low-latency processing
- Real-time results

### 4.2 Scalability

- Horizontal scaling
- Load balancing
- Distributed sessions

### 4.3 Availability

- 24×7 uptime
- Failover support
- Disaster recovery

### 4.4 Security

- API authentication
- HTTPS/WSS encryption
- Audit logs
- Anti-fraud hooks

### 4.5 Compliance & Fairness

- RNG certification
- Game audit trails
- Result reproducibility

---

## 5. Integration Interfaces

### 5.1 Operator APIs (B2B Integration)

- Session validation
- Wallet debit
- Wallet credit
- Bet placement
- Bet status

### 5.2 B2C Provider APIs

The platform supports B2C providers who manage their own player wallets.

**Provider Wallet Operations (JSON):**
- `POST /api/v1/b2c/wallet/debit` - Debit player wallet
- `POST /api/v1/b2c/wallet/credit` - Credit player wallet
- `POST /api/v1/b2c/wallet/refund` - Refund transaction
- `POST /api/v1/b2c/wallet/cancel` - Cancel transaction
- `GET /api/v1/b2c/wallet/balance` - Get player balance

**Provider Wallet Operations (XML - Legacy Support):**
- `POST /api/v1/b2c/xml/wallet/debit` - Debit (XML envelope)
- `POST /api/v1/b2c/xml/wallet/credit` - Credit (XML envelope)
- `POST /api/v1/b2c/xml/wallet/refund` - Refund (XML envelope)
- `POST /api/v1/b2c/xml/wallet/cancel` - Cancel (XML envelope)
- `GET /api/v1/b2c/xml/wallet/balance` - Balance (XML)

**Provider Configuration:**
- `POST /api/v1/b2c/providers` - Create provider configuration
- `GET /api/v1/b2c/providers/{providerId}` - Get provider configuration
- `PUT /api/v1/b2c/providers/{providerId}` - Update provider configuration
- `DELETE /api/v1/b2c/providers/{providerId}` - Delete provider configuration
- `GET /api/v1/b2c/providers` - List all providers

**Key Features:**
- Direct wallet operations (no two-step flow)
- HMAC-SHA256 signature authentication
- XML envelope support for legacy providers
- Idempotency handling
- Transaction tracking and audit

### 5.3 WebSocket Interfaces

- Live game events
- Result broadcasting
- Exposure updates

---

## 6. Data Management

Platform maintains:

- Bet transactions
- Game results
- Operator ledgers
- Settlement records
- Audit logs

---

## 7. Out of Scope

- Player registration
- Player KYC
- Deposits/withdrawals
- Operator CRM
- Bonus management

---

## 8. Responsibility Matrix

| Function | Operator | Platform |
|----------|----------|----------|
| Player Ownership | ✔ | ✖ |
| Wallet Funds | ✔ | ✖ |
| Game Hosting | ✖ | ✔ |
| Bet Processing | Via API | ✔ |
| Result Generation | ✖/Optional | ✔ |
| Payout Calculation | ✖ | ✔ |
| Settlement | Shared | ✔ Primary |

---

## 9. Technology Stack & Architectural Design

This section defines the proposed technology stack, architectural approach, and supporting infrastructure components for the B2B Gaming Platform. The objective is to ensure scalability, high availability, real-time responsiveness, and isolation of game services.

### 9.1 System Architecture Overview

The platform follows a layered microservices architecture with clear separation of concerns:
┌─────────────────────────────────────────────────────────────┐
│ Load Balancer / CDN │
│ (Traffic Distribution & SSL Termination) │
└─────────────────────────────────────────────────────────────┘
↓
┌─────────────────────────────────────────────────────────────┐
│ API Gateway Layer │
│ ┌──────────────────────────────────────────────────────┐ │
│ │ • Request Routing & Load Balancing │ │
│ │ • Authentication & Authorization (JWT/OAuth) │ │
│ │ • Rate Limiting (per operator/player) │ │
│ │ • Request/Response Transformation │ │
│ │ • Circuit Breaker Pattern │ │
│ │ • Request Logging & Monitoring │ │
│ └──────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
↓
┌─────────────────────────────────────────────────────────────┐
│ Microservices Layer │
│ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │
│ │Operator │ │ Bet │ │ Wallet │ │Settlement│ │
│ │ Service │ │ Service │ │ Service │ │ Service │ │
│ └──────────┘ └──────────┘ └──────────┘ └──────────┘ │
│ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │
│ │Exposure │ │Reporting │ │ Game │ │Session │ │
│ │ Service │ │ Service │ │ Services │ │ Service │ │
│ └──────────┘ └──────────┘ └──────────┘ └──────────┘ │
│ (Roulette, Poker, Lottery, etc.) │
└─────────────────────────────────────────────────────────────┘
↓
┌─────────────────────────────────────────────────────────────┐
│ Message Queue & Event Streaming Layer │
│ ┌──────────────────────────────────────────────────────┐ │
│ │ • Async Bet Processing │ │
│ │ • Wallet Debit/Credit Events │ │
│ │ • Settlement Processing │ │
│ │ • Result Broadcasting │ │
│ │ • Event Sourcing for Ledger │ │
│ └──────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
↓
┌─────────────────────────────────────────────────────────────┐
│ Data & Storage Layer │
│ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ │
│ │ PostgreSQL │ │ Redis │ │ TimescaleDB │ │
│ │ (Primary DB) │ │ (Cache) │ │ (Metrics) │ │
│ └──────────────┘ └──────────────┘ └──────────────┘ │
│ ┌──────────────┐ ┌──────────────┐ │
│ │ PostgreSQL │ │ Object │ │
│ │ (Read Replica│ │ Storage │ │
│ │ for Reports)│ │ (S3/Blob) │ │
│ └──────────────┘ └──────────────┘ │
└─────────────────────────────────────────────────────────────┘


### 9.2 Application Technology Stack

The platform shall use a multi-technology stack optimized for performance, modularity, and maintainability.

#### 9.2.1 Operator Integration & Game Engine – Java Spring Boot

Java Spring Boot shall be used for:

- Operator integration services
- Wallet orchestration logic
- Bet management services
- Game engine services
- Settlement orchestration
- Exposure & liability processing

**Rationale**

- High concurrency handling
- Mature microservices ecosystem
- Strong transaction management
- Native support for WebSocket & messaging
- Enterprise-grade stability
- Rich ecosystem (Spring Cloud, Resilience4j, etc.)

**Key Spring Boot Modules**

- Spring WebFlux (reactive programming for high throughput)
- Spring Data JPA (database access)
- Spring Security (authentication/authorization)
- Spring Cloud Gateway (API gateway)
- Resilience4j (circuit breaker, retry, rate limiting)

#### 9.2.2 Reports & Back Office – .NET

Microsoft .NET shall be used for:

- Operator back-office portals
- Master operator dashboards
- Financial reporting systems
- Settlement reporting
- Administrative tools
- Ledger & reconciliation views

**Rationale**

- Strong UI & reporting frameworks
- Seamless integration with BI tools
- Rich data visualization capabilities
- Enterprise authentication support
- Excellent performance for reporting workloads

#### 9.2.3 Backend Database – PostgreSQL

PostgreSQL shall serve as the primary transactional database.

**Data Domains**

- Bet transactions
- Game results
- Operator ledgers
- Settlement records
- Player utilization records
- Exposure tracking

**Rationale**

- ACID compliance
- High transactional integrity
- Strong indexing & partitioning
- JSON support for flexible schemas
- Proven scalability in fintech/gaming workloads
- Advanced features (partitioning, full-text search, extensions)

**Database Architecture Strategy**

**Primary Database (Write Operations)**

- ACID transactions for financial operations
- Partitioning by operator_id and date
- Optimized indexes for common queries
- Connection pooling (HikariCP recommended)

**Read Replicas (Reporting)**

- Multiple read replicas for reporting queries
- Read-only access to prevent accidental writes
- Optimized indexes for analytical queries
- Separate connection pools

**Partitioning Strategy**

-- Example: Partition bets table by operator and date
CREATE TABLE bets (
    bet_id UUID PRIMARY KEY,
    operator_id INT NOT NULL,
    bet_date DATE NOT NULL,
    amount DECIMAL(18,2),
    status VARCHAR(50),
    created_at TIMESTAMP,
    ...
) PARTITION BY LIST (operator_id);

-- Create partitions per operator
CREATE TABLE bets_op1_2026_02 PARTITION OF bets
FOR VALUES IN (1);

CREATE TABLE bets_op2_2026_02 PARTITION OF bets
FOR VALUES IN (2);

#### Indexing Strategy

•	Primary indexes on bet_id, transaction_id
•	Composite indexes on (operator_id, bet_date, status)
•	Indexes on foreign keys (player_id, game_id)
•	Partial indexes for active/open bets
•	GIN indexes for JSON columns

### 9.3 API Gateway Layer

#### Purpose

The API Gateway serves as the single entry point for all client requests, providing cross-cutting concerns.

#### Technology
•	Spring Cloud Gateway (recommended) OR
•	Kong API Gateway OR
•	AWS API Gateway (if cloud-native)

#### Responsibilities

##### Request Routing
•	Route requests to appropriate microservices
•	Load balancing across service instances
•	Service discovery integration

##### Authentication & Authorization
•	JWT token validation
•	OAuth 2.0 / OIDC support
•	API key validation for operators
•	Role-based access control (RBAC)

##### Rate Limiting
•	Per-operator rate limits
•	Per-player rate limits
•	Per-endpoint rate limits
•	Sliding window algorithm
•	Redis-backed counters

##### Circuit Breaker
•	Protect downstream services
•	Fail-fast for unavailable services
•	Fallback responses
•	Health check integration

##### Request/Response Transformation
•	Request validation
•	Response formatting
•	Protocol translation (if needed)

##### Monitoring & Logging
•	Request/response logging
•	Performance metrics
•	Error tracking
•	Distributed tracing integration

#### Configuration Example
spring:
  cloud:
    gateway:
      routes:
        - id: bet-service
          uri: lb://bet-service
          predicates:
            - Path=/api/v1/bets/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
            - name: CircuitBreaker
              args:
                name: betServiceCircuitBreaker

### 9.4 Message Queue & Event Streaming

#### Purpose

Message queues are mandatory (not optional) for async processing, event-driven architecture, and resilience.

#### Technology Options

##### Apache Kafka (Recommended for high throughput)
•	Event streaming platform
•	High throughput and low latency
•	Event sourcing support
•	Replay capability
•	Suitable for: Bet events, settlement events, result broadcasting
RabbitMQ (Alternative for reliability)
•	Mature message broker
•	Reliable message delivery
•	Multiple exchange types
•	Suitable for: Wallet operations, settlement processing

#### Event Topics/Queues
Topic/Queue	Purpose	Consumer Services
bet.requests	Bet placement requests	Bet Service
bet.results	Bet processing results	Operator Service, Reporting
wallet.debit	Wallet debit requests	Wallet Service
wallet.credit	Wallet credit requests	Wallet Service
settlement.process	Settlement processing	Settlement Service
game.results	Game result events	Bet Service, Settlement Service
exposure.updates	Exposure changes	Exposure Service, Reporting

#### Benefits
•	Async Processing: Wallet calls don't block bet processing
•	Resilience: Retry failed operations automatically
•	Scalability: Process messages in parallel
•	Decoupling: Services communicate via events
•	Event Sourcing: Complete audit trail

#### Message Flow Example
Bet Request → API Gateway → Bet Service
                              ↓
                         Publish to Queue
                              ↓
                    Wallet Service (async)
                              ↓
                    Operator Wallet API
                              ↓
                    Publish Result Event
                              ↓
              Bet Service, Reporting, Operator

### 9.5 Real-Time Communication Layer

#### WebSocket Framework

WebSocket technology shall be used for real-time, bidirectional communication.

#### Use Cases
•	Live bet placement updates
•	Game state broadcasts
•	Result publishing
•	Exposure updates
•	Dealer/game events
•	Lobby updates

#### Benefits
•	Low latency communication
•	Persistent connections
•	Reduced polling overhead
•	Scalable event broadcasting

#### WebSocket Scaling Strategy

**Challenge:** WebSocket connections are stateful and require sticky sessions.

**Solution Architecture**

##### Sticky Sessions
•	Load balancer routes WebSocket connections to same server
•	Session affinity based on operator_id or player_id
•	Configured at load balancer level

##### Redis Pub/Sub for Cross-Node Communication
   Node 1 (WebSocket) → Redis Pub/Sub → Node 2 (WebSocket)
                              ↓
                    Broadcast to all connected clients

##### WebSocket Connection Registry
•	Store active connections in Redis
•	Map: player_id → [node_id, connection_id]
•	Enable cross-node messaging

##### Connection Management
•	Heartbeat/ping-pong for connection health
•	Automatic reconnection on disconnect
•	Connection pooling per operator

#### Technology
•	Spring WebSocket (Java)
•	SignalR (.NET for back-office)
•	Socket.IO (optional fallback)

#### Implementation Pattern
// WebSocket handler with Redis pub/sub
@Configuration
@EnableWebSocket
public class WebSocketConfig {
    @Bean
    public WebSocketHandler gameEventHandler() {
        return new GameEventHandler(redisTemplate);
    }
}

// Redis subscriber for cross-node events
@Component
public class RedisEventSubscriber {
    @EventListener
    public void handleGameResult(GameResultEvent event) {
        // Broadcast to all connected clients on this node
        webSocketHandler.broadcast(event);
    }
}

### 9.6 Authentication & Session Security

#### JWT (JSON Web Token) Authentication

JWT shall be used for secure player and operator session management.

#### Usage
•	Player game session authentication
•	Operator API authentication
•	Game launch validation
•	Wallet transaction authorization

#### Capabilities
•	Stateless authentication
•	Token expiry enforcement
•	Refresh token support
•	Encrypted claims (JWE)

#### Token Structure
{
  "sub": "player_123",
  "operator_id": "op_456",
  "roles": ["player"],
  "exp": 1234567890,
  "iat": 1234567890
}

#### Long-Lived Player Sessions

To support uninterrupted betting:
•	Extended session tokens shall be supported (configurable TTL)
•	Token refresh mechanisms shall be implemented
•	Idle timeout rules shall be configurable
•	Secure token storage (HttpOnly cookies recommended)

#### OAuth 2.0 / OIDC Support

For enhanced security, the platform shall support:
•	OAuth 2.0 authorization code flow
•	OpenID Connect (OIDC) for identity
•	Integration with operator identity providers
•	SSO (Single Sign-On) capabilities

#### API Key Management

For operator authentication:
•	API key generation and rotation
•	Key expiration and revocation
•	Per-key rate limits
•	Key usage analytics

### 9.7 Caching & In-Memory Storage

#### Redis – Distributed Cache Layer

Redis shall be used for high-speed in-memory storage.

#### Use Cases
•	Player session cache
•	Active game states
•	Exposure snapshots
•	Wallet balance cache
•	Rate limiting counters
•	WebSocket session mapping
•	Distributed locking
•	Pub/Sub messaging
Benefits
•	Sub-millisecond read/write
•	Reduces DB load
•	Supports pub/sub messaging
•	Enables distributed locking
•	High availability with Redis Cluster

#### Redis Architecture

##### Redis Cluster Configuration
•	Minimum 3 master nodes + 3 replica nodes
•	Automatic failover
•	Data sharding across nodes
•	Replication for high availability

##### Cache Strategies

###### Cache-Aside Pattern
 
   Read: Check cache → If miss, read DB → Store in cache
   Write: Update DB → Invalidate cache

###### Write-Through Pattern
 
   Write: Update DB → Update cache

##### TTL Policies
•	Session data: 30 minutes
•	Game state: 5 minutes
•	Balance cache: 1 minute
•	Rate limit counters: Per window

##### Cache Invalidation
•	Event-driven invalidation via Redis pub/sub
•	TTL-based expiration
•	Manual invalidation on updates

### 9.8 Microservices Architecture

The platform shall be built using a Microservices Architecture.

#### Design Principle

Each major function shall operate as an independent service.

#### Core Services
Service	Responsibility	Technology
Operator Service	Operator management, configuration	Spring Boot
Bet Service	Bet placement, validation, processing	Spring Boot
Wallet Service	Wallet debit/credit orchestration (B2B Integration)	Spring Boot
B2C Integration Service	B2C provider wallet integration, XML/JSON support	Spring Boot
Settlement Service	Settlement calculation, reconciliation	Spring Boot
Exposure Service	Exposure tracking, limits	Spring Boot
Reporting Service	Analytics, reports generation	.NET
Session Service	Player session management	Spring Boot
Game Services	Individual game engines (Roulette, Poker, etc.)	Spring Boot

#### Key Objective

Service isolation to prevent cascading failures.

#### Primary Reason

If one game service goes down, other games and platform services must remain operational without impact.

#### Failure Isolation Example
Scenario	Impact
Roulette service outage	Only Roulette unavailable
Poker engine failure	Casino games unaffected
Lottery delay	Real-time games continue
Wallet service slow	Bet processing continues (async)

#### Additional Benefits
•	Independent deployment
•	Technology flexibility
•	Horizontal scaling per game
•	Faster release cycles
•	Load isolation
•	Team autonomy

#### Service Communication Patterns

##### Synchronous (REST)
•	For immediate responses
•	Use with circuit breaker
•	Timeout configuration required

##### Asynchronous (Message Queue)
•	For long-running operations
•	Wallet operations
•	Settlement processing
•	Event broadcasting

##### Real-Time (WebSocket)
•	Game events
•	Live updates
•	Result broadcasting

### 9.9 Service Communication

Microservices shall communicate via:
•	REST APIs (synchronous) - For immediate responses
•	WebSockets (real-time events) - For live updates
•	Message Queues (async processing) - Mandatory for resilience

#### Service Discovery
•	Consul (recommended) OR
•	Eureka (Spring Cloud) OR
•	Kubernetes Service Discovery (if using K8s)

#### Load Balancing
•	Client-side load balancing (Spring Cloud LoadBalancer)
•	Server-side load balancing (Nginx, HAProxy)
•	Health checks for service instances

#### Circuit Breaker Pattern
Purpose: Prevent cascading failures when services are unavailable.
Implementation: Resilience4j (Java)
Configuration:
@CircuitBreaker(name = "walletService", fallbackMethod = "fallback")
public WalletResponse debitWallet(WalletRequest request) {
    return walletClient.debit(request);
}
States:
•	Closed: Normal operation
•	Open: Failing, reject requests immediately
•	Half-Open: Testing if service recovered

#### Retry Strategy
•	Exponential backoff
•	Maximum retry attempts
•	Configurable per service
•	Dead letter queue for failed messages

### 9.10 Configuration Management

#### External Configuration Service

The platform shall use external configuration management for:
•	Environment-specific settings
•	Feature flags
•	Service endpoints
•	Rate limits
•	Circuit breaker thresholds
Technology
•	Spring Cloud Config (recommended for Spring Boot)
•	Consul (key-value store + service discovery)
•	etcd (Kubernetes-native)
Benefits
•	Centralized configuration
•	Dynamic updates without restart
•	Environment separation
•	Version control integration
•	Security (encrypted values)

### 9.11 Observability & Monitoring

#### Distributed Tracing

**Purpose:** Track requests across microservices for debugging and performance analysis.

**Technology:**
•	Jaeger (recommended) OR
•	Zipkin OR
•	OpenTelemetry (vendor-neutral)
Implementation:
•	Trace ID propagation across services
•	Span creation for each service call
•	Performance metrics per span
•	Error tracking

#### Metrics Collection

**Technology:** Prometheus + Grafana

**Key Metrics:**
•	Request rate (per service, per endpoint)
•	Response time (p50, p95, p99)
•	Error rate
•	Active connections
•	Queue depth
•	Database connection pool usage
•	Cache hit/miss ratio

#### Logging

**Technology:**
•	ELK Stack (Elasticsearch, Logstash, Kibana) OR
•	Loki + Grafana (lightweight alternative)

**Log Levels:**
•	ERROR: System errors, failures
•	WARN: Warning conditions
•	INFO: General information
•	DEBUG: Detailed debugging (development only)

**Structured Logging:**
{
  "timestamp": "2026-02-06T10:00:00Z",
  "level": "INFO",
  "service": "bet-service",
  "trace_id": "abc123",
  "operator_id": "op_456",
  "message": "Bet placed successfully",
  "bet_id": "bet_789"
}

#### Application Performance Monitoring (APM)

**Technology:**
•	New Relic OR
•	Datadog OR
•	Application Insights (.NET)
Capabilities:
•	Real-time performance monitoring
•	Error tracking and alerting
•	Database query analysis
•	Custom dashboards

### 9.12 Data Architecture & Patterns

#### CQRS (Command Query Responsibility Segregation)

**Purpose:** Separate read and write models for optimal performance.

**Write Model (PostgreSQL Primary):**
•	Optimized for transactions
•	ACID compliance
•	Normalized schema
Read Model (PostgreSQL Read Replica or Data Warehouse):
•	Optimized for queries
•	Denormalized for reporting
•	Materialized views
•	Separate indexes

#### Event Sourcing (Optional but Recommended)

**Purpose:** Complete audit trail and replay capability for financial transactions.

**Implementation:**
•	Store all financial events as immutable records
•	Rebuild balances from events
•	Event store (PostgreSQL or dedicated event store)
•	Projections for read models
Benefits:
•	Complete audit trail
•	Replay capability
•	Better reconciliation
•	Immutability guarantees

#### Saga Pattern

**Purpose:** Manage distributed transactions across services.

**Use Case:** Settlement processing across multiple services.

**Patterns:**
•	Choreography: Services publish events
•	Orchestration: Central coordinator manages flow
Example Flow:
Settlement Start → Calculate Net → Update Ledger → Notify Operator
                      ↓ (if failure)
                  Compensate (rollback)

### 9.13 Scalability & Deployment Considerations

The architecture shall support:
•	Containerization: Docker containers for all services
•	Orchestration: Kubernetes (recommended) or Docker Swarm
•	Auto-scaling: Horizontal pod autoscaling based on CPU/memory/custom metrics
•	Load Balancing: Kubernetes Service or external load balancer
•	Resource Limits: CPU and memory limits per service

#### Kubernetes Deployment Strategy

**Deployment Pattern:** Rolling updates with zero downtime

**Scaling Strategy:**
•	Horizontal scaling (add more pods)
•	Vertical scaling (increase resources)
•	Auto-scaling based on metrics

**Resource Management:**
resources:
  requests:
    memory: "512Mi"
    cpu: "500m"
  limits:
    memory: "1Gi"
    cpu: "1000m"

#### Database Scaling
•	Read Replicas: Scale read operations
•	Partitioning: Distribute data across partitions
•	Connection Pooling: Optimize database connections
•	Query Optimization: Indexes, query analysis

### 9.14 High Availability Design

To ensure uptime:
•	Active-Active Service Deployment: Multiple instances across zones
•	Database Replication: Primary + multiple replicas
•	Redis Clustering: High availability cache
•	WebSocket Node Balancing: Sticky sessions with failover
•	Failover Routing: Automatic failover to healthy instances
•	Health Checks: Liveness and readiness probes

#### Disaster Recovery Strategy
•	RTO (Recovery Time Objective): < 1 hour
•	RPO (Recovery Point Objective): < 15 minutes
Components:
•	Multi-Region Deployment: Active-active or active-passive
•	Database Backups: Daily full backups + continuous WAL archiving
•	Data Replication: Cross-region replication
•	Failover Procedures: Automated failover scripts
•	Testing: Regular DR drills

#### Backup Strategy
•	Full backups: Daily
•	Incremental backups: Every 6 hours
•	Transaction log backups: Continuous
•	Retention: 30 days
•	Off-site storage: Yes

### 9.15 Security Architecture

Security controls shall include:

#### Network Security
•	HTTPS/WSS encryption (TLS 1.3)
•	VPN for internal services
•	Network segmentation
•	DDoS protection (CloudFlare, AWS Shield)

#### Application Security
•	JWT token validation
•	API gateway authentication
•	OAuth 2.0 / OIDC support
•	Encrypted WebSocket (WSS)
•	Role-based back-office access (RBAC)
•	Input validation and sanitization
•	SQL injection prevention
•	XSS protection

#### Data Security
•	Encryption at rest (database encryption)
•	Encryption in transit (TLS)
•	PII data masking in logs
•	Secure key management (HashiCorp Vault, AWS KMS)

#### API Security
•	API key rotation
•	Rate limiting per operator
•	IP whitelisting (optional)
•	Request signing (HMAC)

#### Audit & Compliance
•	Immutable audit logs
•	Access logging
•	Change tracking
•	Compliance reporting (GDPR, PCI DSS if applicable)

#### Security Monitoring
•	Intrusion detection
•	Anomaly detection
•	Security event alerting
•	Regular security audits

### 9.16 Rate Limiting Strategy

**Purpose:** Protect services from abuse and ensure fair usage.

**Implementation:** Redis-backed rate limiting

**Limits:**
Level	Limit	Window
Per Operator	10,000 req/min	1 minute
Per Player	100 req/min	1 minute
Per Endpoint	Varies	Varies

**Algorithms:**
•	Sliding Window: More accurate, Redis-based
•	Token Bucket: Smooth rate limiting
•	Fixed Window: Simpler, less accurate

**Configuration:**
rate-limits:
  operator:
    requests-per-minute: 10000
    burst-capacity: 15000
  player:
    requests-per-minute: 100
    burst-capacity: 150

### 9.17 Technology Stack Summary
Layer	Technology	Purpose
API Gateway	Spring Cloud Gateway / Kong	Request routing, auth, rate limiting
Game & Operator Services	Java Spring Boot	Core business logic
Back Office & Reporting	.NET	Reporting and admin UI
Database (Write)	PostgreSQL	Primary transactional database
Database (Read)	PostgreSQL Read Replicas	Reporting and analytics
Message Queue	Apache Kafka / RabbitMQ	Async processing, event streaming
Real-Time Communication	WebSockets	Live updates, game events
Authentication	JWT + OAuth 2.0	Secure authentication
Cache / Session Store	Redis Cluster	High-speed caching
Service Discovery	Consul / Eureka	Service registration
Configuration	Spring Cloud Config / Consul	External configuration
Tracing	Jaeger / Zipkin	Distributed tracing
Metrics	Prometheus + Grafana	Performance monitoring
Logging	ELK Stack / Loki	Centralized logging
Containerization	Docker	Application packaging
Orchestration	Kubernetes	Container orchestration
Architecture Style	Microservices	Service isolation

### 9.18 Architecture Decision Records (ADRs)
ADR-001: Microservices over Monolith
•	Decision: Use microservices architecture
•	Rationale: Service isolation, independent scaling, technology flexibility
•	Consequences: Increased complexity, network latency, distributed transactions
ADR-002: Kafka over RabbitMQ
•	Decision: Use Apache Kafka for event streaming
•	Rationale: Higher throughput, event sourcing support, replay capability
•	Consequences: More complex setup, requires Zookeeper/Kafka cluster
ADR-003: PostgreSQL over NoSQL
•	Decision: Use PostgreSQL as primary database
•	Rationale: ACID compliance, strong consistency, SQL support
•	Consequences: Scaling challenges (mitigated by read replicas and partitioning)
ADR-004: API Gateway Pattern
•	Decision: Implement dedicated API Gateway
•	Rationale: Centralized cross-cutting concerns, security, rate limiting
•	Consequences: Single point of failure (mitigated by HA deployment)
ADR-005: CQRS for Reporting
•	Decision: Separate read/write models
•	Rationale: Optimize read queries without impacting writes
•	Consequences: Data synchronization complexity (mitigated by read replicas)

---

## 10. Deployment Plan

This section outlines the deployment strategy, procedures, and operational considerations for the B2B Gaming Platform.

### 10.1 Deployment Architecture

#### Environment Structure
 
Development → Staging → Production
     ↓           ↓          ↓
  Dev DB     Stage DB    Prod DB
  Dev Redis  Stage Redis Prod Redis

#### Infrastructure Components
•	Application Servers: Kubernetes clusters per environment
•	Database: PostgreSQL with read replicas
•	Cache: Redis Cluster
•	Message Queue: Kafka cluster (Production) / RabbitMQ (Dev/Staging)
•	Load Balancer: Nginx/HAProxy or cloud-native (AWS ALB, Azure LB)
•	CDN: CloudFlare or AWS CloudFront
•	Monitoring: Prometheus + Grafana stack

### 10.2 Environment Configuration

#### 10.2.1 Development Environment

**Purpose:** Local development and testing

**Configuration:**
•	Single-node Kubernetes (minikube/k3d) or Docker Compose
•	PostgreSQL single instance
•	Redis single instance
•	RabbitMQ single instance
•	Minimal resource allocation

**Access:** Developers only

#### 10.2.2 Staging Environment

**Purpose:** Pre-production testing and validation

**Configuration:**
•	Multi-node Kubernetes cluster (3 nodes minimum)
•	PostgreSQL primary + 1 read replica
•	Redis cluster (3 nodes)
•	Kafka cluster (3 brokers) or RabbitMQ cluster
•	Production-like resource allocation
•	Production data sanitized copy

**Access:** QA team, developers, stakeholders

#### 10.2.3 Production Environment

**Purpose:** Live production system

**Configuration:**
•	Multi-node Kubernetes cluster (5+ nodes per region)
•	PostgreSQL primary + 2+ read replicas
•	Redis cluster (6 nodes: 3 master + 3 replica)
•	Kafka cluster (5+ brokers)
•	High availability setup
•	Multi-region deployment (if required)

**Access:** Operations team only

### 10.3 Deployment Strategy

#### 10.3.1 Deployment Model

##### Blue-Green Deployment (Recommended for zero-downtime)
•	Two identical production environments (Blue and Green)
•	Deploy new version to inactive environment
•	Test new version thoroughly
•	Switch traffic to new environment
•	Keep old environment for quick rollback

##### Rolling Deployment (Alternative)
•	Gradual rollout to production
•	Update pods in batches (e.g., 25% at a time)
•	Monitor health after each batch
•	Continue or rollback based on metrics

#### 10.3.2 Deployment Phases

##### Phase 1: Pre-Deployment
1.	Code freeze and final testing
2.	Create deployment branch
3.	Build Docker images
4.	Run automated tests
5.	Security scanning
6.	Approval from stakeholders

##### Phase 2: Database Migration
1.	Backup production database
2.	Run database migrations (if any)
3.	Verify migration success
4.	Test rollback procedure

##### Phase 3: Application Deployment
1.	Deploy to staging environment
2.	Run smoke tests
3.	Deploy to production (blue-green or rolling)
4.	Monitor health checks
5.	Gradual traffic shift (if blue-green)

##### Phase 4: Post-Deployment
1.	Verify all services are healthy
2.	Run integration tests
3.	Monitor metrics for 1-2 hours
4.	Validate critical user flows
5.	Document deployment

##### Phase 5: Rollback (if needed)
1.	Identify issues
2.	Stop traffic to new version
3.	Rollback to previous version
4.	Investigate root cause
5.	Plan fix deployment

### 10.4 Deployment Procedures

#### 10.4.1 Microservices Deployment Order

**Critical Path (Must deploy in order):**
1.	Database migrations (if any)
2.	Configuration service
3.	Service discovery
4.	API Gateway
5.	Core services (Operator, Session)
6.	Business services (Bet, Wallet, Settlement)
7.	Game services (can deploy independently)
8.	Reporting service

**Parallel Deployment (Can deploy simultaneously):**
•	Individual game services
•	Non-critical services
•	Background workers

#### 10.4.2 Database Deployment

**Migration Strategy:**
•	Use database migration tools (Flyway, Liquibase)
•	Version-controlled migrations
•	Backward-compatible changes preferred
•	Test migrations on staging first

**Procedure:**
# 1. Backup database
pg_dump -Fc platform_db > backup_$(date +%Y%m%d).dump

# 2. Run migrations
flyway migrate -configFiles=flyway.conf

# 3. Verify migration
flyway info

# 4. Test application connectivity

**Rollback Procedure:**

# 1. Restore from backup
pg_restore -d platform_db backup_YYYYMMDD.dump

# 2. Or rollback specific migration
flyway repair
flyway migrate -target=previous_version

#### 10.4.3 Application Deployment

##### Kubernetes Deployment
# Example: bet-service deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bet-service
  namespace: production
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: bet-service
  template:
    metadata:
      labels:
        app: bet-service
        version: v1.2.0
    spec:
      containers:
      - name: bet-service
        image: registry/bet-service:v1.2.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5

##### Deployment Commands

# 1. Build and push Docker image
docker build -t registry/bet-service:v1.2.0 .
docker push registry/bet-service:v1.2.0

# 2. Update Kubernetes deployment
kubectl set image deployment/bet-service \
  bet-service=registry/bet-service:v1.2.0 \
  -n production

# 3. Monitor rollout
kubectl rollout status deployment/bet-service -n production

# 4. Verify pods
kubectl get pods -l app=bet-service -n production

### 10.5 Configuration Management

#### 10.5.1 Environment Variables

**Sensitive Data:** Use Kubernetes Secrets or external secret management (HashiCorp Vault)

**Non-Sensitive Data:** ConfigMaps

**Example Secret:**
apiVersion: v1
kind: Secret
metadata:
  name: db-credentials
  namespace: production
type: Opaque
data:
  username: <base64-encoded>
  password: <base64-encoded>

#### 10.5.2 External Configuration
•	Use Spring Cloud Config or Consul
•	Version control configuration files
•	Environment-specific overrides
•	Hot reload capability (where applicable)

### 10.6 Health Checks & Monitoring

#### 10.6.1 Health Check Endpoints

**Liveness Probe:** /actuator/health/liveness
•	Checks if application is running
•	Failure triggers pod restart

**Readiness Probe:** /actuator/health/readiness
•	Checks if application is ready to serve traffic
•	Failure removes pod from load balancer

**Startup Probe:** /actuator/health/startup
•	Checks if application has started
•	Useful for slow-starting applications

#### 10.6.2 Monitoring During Deployment

**Key Metrics to Monitor:**
•	Error rate (should remain < 0.1%)
•	Response time (p95 should remain stable)
•	Request rate (should match expected traffic)
•	Database connection pool usage
•	Memory and CPU usage
•	Queue depth (message queue)

**Alerting Thresholds:**
•	Error rate > 1%: Warning
•	Error rate > 5%: Critical (consider rollback)
•	Response time increase > 50%: Warning
•	Pod restarts > 3 in 5 minutes: Critical

### 10.7 Rollback Procedures

#### 10.7.1 Quick Rollback (Kubernetes)
# Rollback to previous version
kubectl rollout undo deployment/bet-service -n production

# Rollback to specific revision
kubectl rollout undo deployment/bet-service --to-revision=5 -n production

# Check rollout history
kubectl rollout history deployment/bet-service -n production

#### 10.7.2 Database Rollback
•	Restore from backup (if migration failed)
•	Run reverse migration scripts
•	Verify data integrity

#### 10.7.3 Rollback Decision Criteria

**Immediate Rollback Required:**
•	Critical errors affecting > 10% of requests
•	Data corruption detected
•	Security vulnerability exposed
•	Complete service outage

**Consider Rollback:**
•	Error rate > 5%
•	Performance degradation > 50%
•	Multiple service failures
•	Customer complaints increasing

### 10.8 Deployment Checklist

#### Pre-Deployment
•	[ ] All tests passing (unit, integration, e2e)
•	[ ] Code review completed
•	[ ] Security scan passed
•	[ ] Performance testing completed
•	[ ] Database backup created
•	[ ] Rollback plan documented
•	[ ] Stakeholder approval obtained
•	[ ] Deployment window scheduled

#### During Deployment
•	[ ] Database migration executed successfully
•	[ ] Services deployed in correct order
•	[ ] Health checks passing
•	[ ] Monitoring dashboards active
•	[ ] Smoke tests executed
•	[ ] No critical errors in logs

#### Post-Deployment
•	[ ] All services healthy
•	[ ] Integration tests passing
•	[ ] Performance metrics normal
•	[ ] Error rates within acceptable range
•	[ ] User acceptance testing completed
•	[ ] Documentation updated
•	[ ] Team notified of deployment success

### 10.9 Deployment Timeline

**Typical Deployment Window:** 2-4 hours

**Breakdown:**
•	Pre-deployment checks: 30 minutes
•	Database migration: 15-30 minutes
•	Application deployment: 30-60 minutes
•	Health checks & validation: 30-60 minutes
•	Monitoring period: 60-90 minutes
•	Documentation: 15 minutes

**Emergency Deployment:** 30-60 minutes (with reduced validation)

### 10.10 Deployment Communication Plan

**Stakeholders to Notify:**
•	Operations team
•	Development team leads
•	QA team
•	Product owners
•	Customer support (if user-facing changes)

**Communication Channels:**
•	Slack/Teams channel for real-time updates
•	Email for deployment summary
•	Status page for external stakeholders

**Communication Timeline:**
•	24 hours before: Deployment notification
•	1 hour before: Final reminder
•	During deployment: Real-time updates
•	After deployment: Success/failure notification
•	24 hours after: Post-deployment report

### 10.11 Disaster Recovery During Deployment

**Scenarios:**
•	Deployment Failure: Rollback immediately
•	Partial Deployment: Complete or rollback (no partial state)
•	Database Migration Failure: Restore from backup
•	Service Unavailable: Rollback affected services
•	Data Corruption: Immediate rollback + data restore

**Recovery Procedures:**
•	Documented runbooks for each scenario
•	On-call engineer available during deployment
•	Escalation path defined
•	Communication plan activated

## 11. Future Extensions

The following architectural components may be extended in future versions:
•	Deployment Architecture
•	Deployment architecture diagram
•	Kubernetes pod/service layout
•	Multi-cloud deployment strategy
•	Data Architecture
•	DB sharding & partition strategy
•	Advanced analytics with data lake architecture
•	Real-time streaming analytics (Kafka Streams, Flink)
•	Infrastructure
•	Service Mesh (Istio/Linkerd) for advanced traffic management
•	WebSocket scaling model (sticky sessions / Redis pub-sub)
•	Disaster recovery architecture
•	API & Integration
•	GraphQL API layer for flexible client queries
•	Advanced API versioning strategies
•	Advanced Features
•	Machine Learning integration for fraud detection
•	Blockchain integration for result verification (if required)
•	Advanced AI-powered game recommendations
•	Analytics & Reporting
•	Real-time business intelligence dashboards
•	Predictive analytics for operator behavior
•	Advanced reporting with custom visualizations

---

---

## 12. B2B vs B2C Integration Models

The platform supports two distinct integration models for different use cases.

### 12.1 B2B Integration (Scenario 1)

**Target Users:** Operators (B2B partners)

**Wallet Models:**
- **Shared Wallet Model:** Operator holds wallet, platform requests funds seamlessly
- **Fund Transfer Model:** Platform holds wallet, player transfers funds via two-step flow

**Service:** `wallet-service` (enhanced with B2B integration features)

**Base Path:** `/api/v1/b2b/` (for fund transfer operations)

**Key Features:**
- Two-step fund transfer flow (Request → Confirm)
- Direct debit/credit for shared wallet
- Operator wallet configuration
- Operational APIs (block/unblock user, kickout)

### 12.2 B2C Integration (Scenario 2)

**Target Users:** B2C Providers (who manage their own players)

**Wallet Model:**
- **Provider Wallet Model:** Provider holds wallet, platform calls provider APIs

**Service:** `b2c-integration-service` (separate microservice)

**Base Path:** `/api/v1/b2c/`

**Key Features:**
- Direct debit/credit operations
- JSON and XML envelope support
- Provider configuration management
- HMAC-SHA256 signature authentication
- Legacy provider support

**Separation:**
- B2B and B2C integrations are completely separate
- Different services, endpoints, and data models
- No mixing of B2B and B2C flows

---

**Document Version:** 2.1  
**Last Updated:** February 14, 2026  
**Status:** Draft
