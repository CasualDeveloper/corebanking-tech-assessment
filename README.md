# Core Banking Tech Assessment

Spring Boot REST API for card management with CRUD endpoints, request/response logging, and external notifications.

## Prerequisites

- Java 21
- Maven 3.9+
- Docker Desktop (for MSSQL runtime)

## Quick start (MSSQL default)

Build the JAR, then start the stack:

```bash
mvn clean package -DskipTests
docker compose up --build
```

Smoke check:

```bash
docker compose up -d && sleep 20 && curl http://localhost:8080/api/cards
```

## Tests

```bash
mvn test
```

Test profile uses H2 in-memory with MSSQL compatibility (`MODE=MSSQLServer`).

## Swagger

- UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## API endpoints

- `GET /api/cards` (pagination fixed at 10; supports `page` and `sort`)
- `GET /api/cards/{id}`
- `POST /api/cards`
- `PUT /api/cards/{id}`
- `DELETE /api/cards/{id}`
- `GET /api/cards/{id}/notifications`

## Postman

Import: `postman/CoreBanking.postman_collection.json`

The collection uses variables:

- `baseUrl` (default `http://localhost:8080`)
- `cardId` (set after creating a card)

## Submission

Public repository URL:

- https://github.com/CasualDeveloper/corebanking-tech-assessment

## Logs

- File: `logs/application.log`
- Docker volume mapping: `./logs:/app/logs`
- Card numbers are masked in logs and responses (last 4 digits only)

## PostgreSQL alternative

An optional compose file is provided at `docker-compose.postgres.yml`.

The PostgreSQL driver is already included as a runtime dependency. The compose file sets the driver class.

Then run:

```bash
docker compose -f docker-compose.postgres.yml up --build
```

## Notes

- `creditLimit` is only valid for `CREDIT` cards.
- Card numbers are masked in API responses.
- Default database is MSSQL (see `docker-compose.yml`).
