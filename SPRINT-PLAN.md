# Sprint Plan

This plan breaks the project into demoable sprints with atomic, committable tasks. Each task includes a validation step (tests or a concrete manual check) to confirm completion.

## Sprint 1: Scaffold & MSSQL Runtime

Goal: Spring Boot app builds and runs against MSSQL via Docker; H2 test profile works.

1.1 Bootstrap Spring Boot project (Maven) with dependencies: web, data-jpa, validation, aop, springdoc-openapi, mssql-jdbc, h2 (test).
- Validation: `mvn test` (context loads).

1.2 Add base package structure from `SPEC.md` and a minimal ContextLoad test.
- Validation: `mvn test`.

1.3 Add Dockerfile (JRE runtime) and `docker-compose.yml` for MSSQL (healthcheck + init + logs volume).
- Validation: `mvn clean package -DskipTests` then `docker compose up --build` (app starts and connects to DB).

1.4 Confirm MSSQL init aligns with datasource:
- `TESTDB` created by init service and `application.yml` points to `databaseName=TESTDB`
- Validation: connect to MSSQL container and verify `TESTDB` exists.

1.5 Add `application.yml` for MSSQL datasource and logging config; add `application-test.yml` for H2 (MODE=MSSQL).
- Validation: `mvn test` and verify logs file is created at `./logs/application.log` when running in Docker.

Demo: `docker compose up --build` shows app starting and connecting to MSSQL.

## Sprint 2: Domain Model & Persistence

Goal: Card entity persists correctly; CRUD service works with fixed pagination.

2.1 Implement `CardStatus` and `CardType` enums plus `Card` entity with JPA constraints:
- `@GeneratedValue(strategy = IDENTITY)`
- `@Column(unique = true)` on `cardNumber`
- Precision/scale for `creditLimit` and `balance`
- Auditing fields (`createdAt`, `updatedAt`) with entity listeners
- Validation annotations for required fields: `cardholderName`, `expiryDate`, `status`, `cardType`, `balance`
- Validation: JPA test verifies schema and audit fields are auto-populated.

2.2 Enable auditing (`@EnableJpaAuditing`) and add repository `CardRepository`.
- Validation: repository test (save/find by id).

2.3 Create DTOs (`CardRequest`, `CardResponse`) and mapper with masking logic (last 4 digits only).
- Validation: unit tests for mapping and masking behavior.

2.4 Implement domain rules for `creditLimit` vs `cardType`:
- `creditLimit` allowed only for CREDIT cards; reject for DEBIT/PREPAID
- Validation: service/validator tests for allowed/blocked combinations.

2.5 Implement `CardService` CRUD with `@Transactional` on all methods and fixed page size (10) in service layer.
- Validation: service tests for create/update/get/delete and pagination enforcement.

Demo: Service tests pass; a simple integration test can create and read a card via repository/service.

## Sprint 3: API Layer, Validation, Logging

Goal: REST endpoints with validation, error handling, and request/response logging.

3.1 Implement `CardController` endpoints (CRUD + pagination). Enforce fixed page size (ignore client `size`).
- Validation: MockMvc tests for happy paths and verify response `size` remains 10 even when `size` param provided.

3.2 Add exceptions (`CardNotFoundException`, `ExternalApiException`) and `GlobalExceptionHandler` mapping 400/404/500/502.
- Validation: MockMvc tests for invalid input and not-found errors with response bodies matching spec (`{"error": ...}`, `{"errors": [...]}`).

3.3 Implement `LoggingAspect` with redaction of `cardNumber` in request and response logs.
- Validation: unit test for redaction helper + manual check in `logs/application.log`.

3.4 Add integration test suite (`@SpringBootTest`) using H2 test profile covering CRUD + pagination.
- Validation: `mvn test` (integration tests pass).

Demo: `GET /api/cards` works, pagination fixed at 10, validation and error handling behave correctly.

## Sprint 4: External API Integration

Goal: Card notifications via external API with proper error handling.

4.1 Add `RestTemplate` config (timeouts) and `ExternalPost` DTO; implement service to fetch notifications.
- Compute `userId = (cardId % 10) + 1` and call `/posts?userId={userId}`.
- Validation: unit test with `MockRestServiceServer` for successful fetch and timeout handling.

4.2 Implement `GET /api/cards/{id}/notifications` in `CardController` and map failures to 502 via `ExternalApiException`.
- Verify card exists before fetching notifications.
- Return `{ cardId, notifications }`.
- Validation: MockMvc tests for success and 404 when card not found.

Demo: `GET /api/cards/{id}/notifications` returns JSONPlaceholder posts as notifications.

## Sprint 5: Docs & Deliverables

Goal: Reviewer can run, test, and review easily.

5.1 Write README: setup steps, MSSQL default, PostgreSQL alternative, test commands, smoke test, log location and volume mapping (`./logs`), submission steps (public Git URL), prerequisites (Java 21, Maven, Docker).
- Validation: follow README steps locally end-to-end.

5.2 Create Postman collection at `postman/CoreBanking.postman_collection.json`.
- Validation: import and run collection against local app.

5.3 Add `docker-compose.postgres.yml` alternative for Postgres.
- Validation: `docker compose -f docker-compose.postgres.yml up`.

5.4 Document Swagger UI access and endpoint list.
- Validation: open `/swagger-ui/index.html` locally.

Demo: Full application run with MSSQL; optional Postgres variant works; docs and Postman available.
