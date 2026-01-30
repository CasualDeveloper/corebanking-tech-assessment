# Core Banking Tech Assessment - Specification

## Overview

Spring Boot REST API demonstrating CRUD operations for a Card management system, built for a Cards IT technical assessment.

## Requirements Reference

Source: `Java_Assessment_2025.md`

## Timeline & Constraints

| Constraint | Value |
|------------|-------|
| Timeline | 2-3 days |
| Java Version | 21 |
| Build Tool | Maven |
| Database | MSSQL (via Docker) |
| Deliverables | Docker Compose, Postman Collection, Swagger UI |

## Out of Scope

- **Authentication/Authorization**: No login, JWT, Spring Security, or user roles
- **Frontend**: REST API only, tested via Postman/Swagger
- **CI/CD**: No GitHub Actions or pipeline configuration
- **Cloud Deployment**: Runs locally via Docker Compose only

---

## Domain Model

### Card Entity

| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | Primary key, auto-generated |
| cardNumber | String | Unique, masked in responses |
| cardholderName | String | Required |
| expiryDate | LocalDate | Required |
| status | Enum | ACTIVE, INACTIVE, BLOCKED, EXPIRED |
| cardType | Enum | CREDIT, DEBIT, PREPAID |
| creditLimit | BigDecimal | Nullable (for credit cards) |
| balance | BigDecimal | Current balance |
| createdAt | LocalDateTime | Auto-generated |
| updatedAt | LocalDateTime | Auto-updated |

---

## API Endpoints

### Card CRUD Operations

| Method | Endpoint | Description | Notes |
|--------|----------|-------------|-------|
| GET | `/api/cards` | List all cards | Paginated (fixed 10/page) |
| GET | `/api/cards/{id}` | Get card by ID | Returns 404 if not found |
| POST | `/api/cards` | Create new card | @Transactional |
| PUT | `/api/cards/{id}` | Update card | @Transactional |
| DELETE | `/api/cards/{id}` | Delete card | @Transactional, hard delete (extension) |

### Pagination Parameters

```
GET /api/cards?page=0&sort=createdAt,desc
```

> Page size is enforced at 10 regardless of client input.

Response format:
```json
{
  "content": [...],
  "pageable": {...},
  "totalElements": 100,
  "totalPages": 10,
  "number": 0,
  "size": 10
}
```

### Card Notifications

| Method | Endpoint | Description | Notes |
|--------|----------|-------------|-------|
| GET | `/api/cards/{id}/notifications` | Get card notifications | Uses external API |

Returns a notification feed for a card by calling a 3rd-party API.

Mapping rule:
- `userId = (cardId % 10) + 1`
- External call: `https://jsonplaceholder.typicode.com/posts?userId={userId}`

Response format:
```json
{
  "cardId": 42,
  "notifications": [
    { "userId": 3, "id": 21, "title": "...", "body": "..." }
  ]
}
```

### External API Integration

Demonstrates outbound HTTP calls using RestTemplate (simpler for assessment scope; WebClient alternative for reactive).

Usage in this project:
- Card notifications endpoint calls `https://jsonplaceholder.typicode.com/posts?userId={userId}`
- `userId = (cardId % 10) + 1`

---

## Technical Implementation

### Project Structure

```
src/main/java/com/assessment/corebanking/
├── CoreBankingApplication.java
├── config/
│   └── RestTemplateConfig.java
├── controller/
│   ├── CardController.java
├── service/
│   ├── CardService.java
│   └── ExternalApiService.java
├── repository/
│   └── CardRepository.java
├── entity/
│   └── Card.java
├── dto/
│   ├── CardRequest.java
│   ├── CardResponse.java
│   ├── CardNotificationResponse.java
│   └── ExternalPost.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── CardNotFoundException.java
│   └── ExternalApiException.java
├── aspect/
│   └── LoggingAspect.java
└── enums/
    ├── CardStatus.java
    └── CardType.java

src/main/resources/
├── application.yml
└── application-test.yml
```

### Request/Response Logging (Requirement #4)

AOP-based logging aspect that intercepts all controller methods:

```java
@Aspect
@Component
public class LoggingAspect {
    @Around("execution(* com.assessment.corebanking.controller.*.*(..))")
    public Object logRequestResponse(ProceedingJoinPoint joinPoint) {
        // Log request details
        // Execute method
        // Log response details
    }
}
```

Logs written to: `logs/application.log`

```yaml
# application.yml - logging config
logging:
  file:
    name: logs/application.log
  level:
    com.assessment.corebanking: DEBUG
```

> **Mask sensitive data in logs**: Ensure request/response logging redacts `cardNumber` (show last 4 digits only).

### Transaction Management (Requirement #5)

Per requirement: `@Transactional` for INSERT, UPDATE, GET methods.

```java
@Service
public class CardService {
    
    @Transactional
    public Card createCard(CardRequest request) { ... }
    
    @Transactional
    public Card updateCard(Long id, CardRequest request) { ... }
    
    @Transactional
    public void deleteCard(Long id) { ... }
    
    @Transactional  // As required - explicit transaction for GET
    public Card getCardById(Long id) { ... }
    
    @Transactional  // As required - explicit transaction for GET
    public Page<Card> getAllCards(Pageable pageable) { ... }
}
```

> **Production optimization note**: For read-heavy production systems, GET methods would typically use `@Transactional(readOnly = true)` to enable connection-level optimizations. The explicit `@Transactional` above satisfies the assessment requirement.

### Error Handling

Global exception handler with @ControllerAdvice:

| Exception | HTTP Status | Response |
|-----------|-------------|----------|
| CardNotFoundException | 404 | `{"error": "Card not found", "id": 123}` |
| MethodArgumentNotValidException | 400 | `{"errors": [...]}` |
| ExternalApiException | 502 | `{"error": "External service unavailable"}` |
| Exception (fallback) | 500 | `{"error": "Internal server error"}` |

---

## Infrastructure

### Docker Compose

```yaml
services:
  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      mssql-init:
        condition: service_completed_successfully
    environment:
      # Local development only - do not use in production
      - SPRING_DATASOURCE_URL=jdbc:sqlserver://mssql:1433;databaseName=TESTDB;encrypt=true;trustServerCertificate=true
      - SPRING_DATASOURCE_USERNAME=sa
      - SPRING_DATASOURCE_PASSWORD=YourStrong!Passw0rd
    volumes:
      - ./logs:/app/logs

  mssql:
    image: mcr.microsoft.com/mssql/server:2022-latest
    ports:
      - "1433:1433"
    environment:
      - ACCEPT_EULA=Y
      - MSSQL_SA_PASSWORD=YourStrong!Passw0rd
      - MSSQL_PID=Developer
    healthcheck:
      test: ["CMD-SHELL", "/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P \"$$MSSQL_SA_PASSWORD\" -C -Q \"SELECT 1\" || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 30s
    volumes:
      - mssql_data:/var/opt/mssql

  mssql-init:
    image: mcr.microsoft.com/mssql/server:2022-latest
    depends_on:
      mssql:
        condition: service_healthy
    environment:
      - MSSQL_SA_PASSWORD=YourStrong!Passw0rd
    command: /bin/bash -c "/opt/mssql-tools18/bin/sqlcmd -S mssql -U sa -P \"$$MSSQL_SA_PASSWORD\" -C -Q \"IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'TESTDB') CREATE DATABASE TESTDB\""
    restart: "no"

volumes:
  mssql_data:
```

### Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build: `mvn clean package -DskipTests` creates the JAR in `target/`, then `docker compose up --build` copies it into the container.

### Application Configuration

```yaml
# application.yml
spring:
  datasource:
    # Local development only - do not use in production
    url: jdbc:sqlserver://localhost:1433;databaseName=TESTDB;encrypt=true;trustServerCertificate=true
    username: sa
    password: YourStrong!Passw0rd
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

> **Note**: Database `TESTDB` is automatically created by the `mssql-init` service on first run.

### Application Profiles

| Profile | Use Case |
|---------|----------|
| default | Local development (MSSQL via Docker) |
| test    | Integration tests (H2 in-memory for speed) |

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MSSQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
```

---

## Testing Strategy

### Integration Tests

- Use `@SpringBootTest` with H2 (test profile for speed)
- Test all CRUD endpoints via `MockMvc`
- Verify pagination behavior
- Mock external API calls with `@MockBean`

### Test Coverage Targets

| Layer | Coverage |
|-------|----------|
| Controller | All endpoints |
| Service | Core business logic |
| Repository | Custom queries only |

---

## Deliverables Checklist

- [ ] Spring Boot application with full CRUD
- [ ] Docker Compose (app + MSSQL)
- [ ] Postman Collection (`postman/CoreBanking.postman_collection.json`)
- [ ] Swagger UI at `/swagger-ui/index.html`
- [ ] README with setup instructions
- [ ] Request/response logging to file
- [ ] Log file volume mapping documented for Docker
- [ ] Paginated GET endpoint (10 records/page)
- [ ] External API integration (JSONPlaceholder)
- [ ] @Transactional on all service methods per requirements
- [ ] Integration tests
- [ ] Smoke test: `docker compose up -d && sleep 20 && curl http://localhost:8080/api/cards`
- [ ] Public Git repository URL shared for review

---

## Dependencies

```xml
<dependencies>
    <!-- Core -->
    <dependency>spring-boot-starter-web</dependency>
    <dependency>spring-boot-starter-data-jpa</dependency>
    <dependency>spring-boot-starter-validation</dependency>
    
    <!-- Database -->
    <dependency>mssql-jdbc</dependency>
    <dependency>postgresql (optional alternative)</dependency>
    <dependency>h2 (test scope)</dependency>
    
    <!-- Documentation -->
    <dependency>springdoc-openapi-starter-webmvc-ui</dependency>
    
    <!-- Logging -->
    <dependency>spring-boot-starter-aop</dependency>
    
    <!-- Testing -->
    <dependency>spring-boot-starter-test</dependency>
</dependencies>
```

---

## Notes

- Database name `TESTDB` per original requirements
- Card numbers should be masked in API responses (show last 4 digits only) — implemented in `CardResponse` DTO

### Database Choice Rationale

Original requirement specifies MSSQL as "preferred". The default configuration uses **MSSQL** to align with that preference.

**PostgreSQL alternative provided**: For easier local development and cross-platform usage, a PostgreSQL compose file is included. The application code is JPA/Hibernate-based and database-agnostic; swapping databases only requires config changes.

**Key point**: Switching to PostgreSQL requires only:
- Swap `mssql-jdbc` → `postgresql` dependency
- Update JDBC URL and driver class
- Zero Java code changes

H2 is used only for tests; it doesn't demonstrate real database connectivity as required.

### PostgreSQL Alternative Configuration

For assessors who prefer PostgreSQL, an alternative Docker Compose is provided:

```yaml
# docker-compose.postgres.yml
services:
  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/testdb
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres

  postgres:
    image: postgres:16
    ports:
      - "5432:5432"
    environment:
      - POSTGRES_DB=testdb
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

Usage: `docker compose -f docker-compose.postgres.yml up`

> **Note**: PostgreSQL auto-creates `testdb` via `POSTGRES_DB`.
