# News Feed System

A scalable news feed system built with Java 21, Spring Boot 3, and a microservice-oriented architecture. Users can publish posts and view their friends' posts on a news feed page.

## Architecture

![Components Diagram](docs/components-diagram.drawio)

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Load Balancer | Nginx | Distributes traffic across web servers |
| Web Servers | Spring Boot (x2) | Handle API requests |
| Post Service | Spring Boot | Manages post creation, caching, and storage |
| Post Cache | Redis | Caches recent/hot posts |
| Post DB | PostgreSQL | Long-term post storage |
| Fanout Service | Spring Boot | Publishes fanout events to message queue |
| Message Queue | RabbitMQ | Decouples fanout from request processing |
| Fanout Workers | Spring Boot (RabbitMQ listeners) | Consume fanout events, populate news feed caches |
| Graph DB | Neo4j | Stores friend relationships |
| User Cache | Redis | Caches user data |
| User DB | PostgreSQL | User storage |
| News Feed Cache | Redis | Per-user sorted set of post IDs |
| Monitoring | Prometheus + Grafana | Metrics collection and dashboards |

### Data Flow

**Publishing a post:**
1. Client sends `POST /v1/me/feed` with `content` and `auth_token`
2. Nginx routes to a web server
3. Post Service saves to Post DB and Post Cache
4. Fanout Service sends a message to RabbitMQ
5. Fanout Workers consume the message, look up friend IDs from Neo4j, and add the post to each friend's News Feed Cache

**Reading the feed:**
1. Client sends `GET /v1/me/feed` with `auth_token`
2. News Feed Service reads post IDs from the user's News Feed Cache (Redis sorted set)
3. Posts are hydrated from Post Cache (or Post DB on cache miss)
4. User data is enriched from User Cache (or User DB on cache miss)

## API

### Publish a Post
```
POST /v1/me/feed
Params:
  content: text of the post
  auth_token: authentication token
```

### Retrieve News Feed
```
GET /v1/me/feed
Params:
  auth_token: authentication token
```

## Prerequisites

- Docker and Docker Compose
- Java 21 (for local development)

## Quick Start

Start all services:
```bash
docker compose up --build
```

The system will be available at:
- **API**: http://localhost (via Nginx)
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Neo4j Browser**: http://localhost:7474

### Sample Requests

```bash
# Publish a post as Alice
curl -X POST "http://localhost/v1/me/feed?auth_token=token-alice&content=Hello+World"

# Read Bob's news feed (will see Alice's post if they are friends)
curl "http://localhost/v1/me/feed?auth_token=token-bob"
```

### Sample Users

| User | Auth Token |
|------|-----------|
| alice | token-alice |
| bob | token-bob |
| charlie | token-charlie |
| diana | token-diana |
| eve | token-eve |

**Friendships:** alice↔bob, alice↔charlie, alice↔eve, bob↔diana, charlie↔diana, diana↔eve

## Load Testing

Run the load test client:
```bash
docker compose --profile test up load-client
```

The load client will:
1. Create 20 test users with friendship relationships
2. Run publish and read scenarios for 60 seconds
3. Print results (request counts, success/fail rates, average latencies)

Configuration via environment variables:
- `LOAD_TEST_TARGET_URL` - target URL (default: `http://nginx`)
- Load test parameters are in `load-test-client/src/main/resources/application.yml`

## Monitoring

Grafana is pre-provisioned with a **News Feed System** dashboard showing:
- Request rate (req/s)
- Request latency p95
- Posts published (total)
- Fanout processing time
- JVM memory usage
- HTTP error rate

Access Grafana at http://localhost:3000 (admin/admin).

## Development

### Build locally
```bash
./gradlew build
```

### Run tests
```bash
./gradlew test
```

### Project Structure

```
news-feed-system/
├── news-feed-app/          # Main Spring Boot application
│   └── src/
│       ├── main/java/com/newsfeed/
│       │   ├── auth/       # Auth token filter
│       │   ├── config/     # Redis, RabbitMQ configuration
│       │   ├── post/       # Post controller, service, repository
│       │   ├── user/       # User service, repository
│       │   ├── graph/      # Neo4j graph service (friendships)
│       │   └── fanout/     # Fanout service, worker, news feed service
│       └── test/
├── load-test-client/       # Load testing application
├── infra/                  # Infrastructure configs
│   ├── nginx/              # Load balancer config
│   ├── prometheus/         # Metrics scraping config
│   ├── grafana/            # Dashboard provisioning
│   ├── postgres/           # DB init scripts
│   └── neo4j/              # Graph DB init script
├── docs/                   # Architecture diagrams
└── docker-compose.yml      # Full system orchestration
```

## Tech Stack

- **Java 21** with virtual threads support
- **Spring Boot 3.4** (Web, Data JPA, Data Neo4j, Data Redis, AMQP, Actuator)
- **Gradle 8.12** with Groovy DSL
- **Lombok** for boilerplate reduction
- **PostgreSQL 16** for relational storage
- **Neo4j 5** for graph-based friend relationships
- **Redis 7** for caching (posts, users, news feeds)
- **RabbitMQ 3** for message queuing
- **Nginx** for load balancing
- **Prometheus + Grafana** for monitoring
- **JUnit 5 + Mockito** for testing
