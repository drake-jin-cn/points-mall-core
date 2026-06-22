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

## Tech Stack

| Layer | Technology |
|-------|------------|
| Framework | Java 17, Spring Boot 3.x |
| Database | PostgreSQL 15 + Spring Data JPA (Hibernate) |
| Cache | Redis + Redisson (distributed lock for cache breakdown) |
| Scheduler | Spring `@Scheduled` (no external job framework) |
| Migration | Flyway |
| Build | Maven |
| Docs | SpringDoc OpenAPI (`/v3/api-docs`) |

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

## Database Ownership

Manages two PostgreSQL databases:
- `points_core` — employees, departments, roles, attendance records, points ledger, points rules

Schema DDL and table relationship diagrams are documented in `.wiki/db/core-schema.md`.
