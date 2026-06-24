# points-mall-core

> Java Spring Boot core service — owns the most sensitive business data: employee accounts, attendance records, and the points ledger.

## Responsibilities

- **Employee Management** — department hierarchy, employee profiles, role seeding (admin / employee)
- **Authentication** — password login (bcrypt hashed), JWT token issuance delegated to BFF; internal token validation
- **Attendance Check-in** — daily check-in API, duplicate-check guard, attendance record history
- **Points Rule Engine** — configurable rules: attendance bonus, birthday bonus, holiday bonus; rule persistence in DB
- **Points Issuance & Deduction** — transactional points balance update + append-only ledger entries
- **Scheduled Tasks** — `@Scheduled` jobs: daily attendance points distribution, monthly birthday bonus, holiday bonus
- **Read/Write Separation (simulated)** — dual `DataSource` config routing write ops to primary DSN, reads to replica DSN
- **Redis Cache** — cache-aside for hot queries (employee balance, product list); full three-layer defense: null caching / distributed lock / TTL jitter

## Why This Tech Stack

Spring Boot 4 (Java 25) is the choice for the service that carries the heaviest business logic: the points ledger, attendance rules, and employee hierarchy. The Spring ecosystem's maturity means every concern — transactions, scheduling, caching, data access — has a well-tested, production-proven solution. Java 25 is the latest LTS (released September 2025), and Spring Boot 4.x is specifically designed for Java 21+, making this the correct modern pairing.

The combination also enables the most interview-relevant Java talking points: Virtual Threads (Project Loom) for high-concurrency scenarios, and Spring Data JPA with Flyway for disciplined schema evolution.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Framework | Java 25, Spring Boot 4.1 |
| Database | PostgreSQL 15 + Spring Data JPA (Hibernate 7) |
| Cache | Redis + Redisson (distributed lock for cache breakdown) |
| Scheduler | Spring `@Scheduled` (no external job framework) |
| Migration | Flyway |
| Build | Maven |
| Docs | SpringDoc OpenAPI (`/v3/api-docs`) |

## Docker

```bash
docker build -t points-mall-core .
docker run --env-file .env -p 8080:8080 points-mall-core
```

## Local Development

```bash
mvn spring-boot:run
# API: http://localhost:8080
```

## Key Environment Variables

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/points_core
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your-password
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
JWT_SECRET=your-secret
```

## Code Quality

```bash
mvn spotless:check  # Check formatting (google-java-format)
mvn spotless:apply  # Auto-fix formatting
```

Formatting runs automatically on staged `.java` files via the pre-commit hook. CI (`mvn verify`) runs on every PR via `.github/workflows/ci.yml` in this repository.

## Database Ownership

Manages two PostgreSQL databases:
- `points_core` — employees, departments, roles, attendance records, points ledger, points rules

Schema DDL and table relationship diagrams are documented in `.wiki/db/core-schema.md`.
