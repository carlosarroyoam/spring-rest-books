# AGENTS.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Purpose

Spring REST Books is a Java 17 / Spring Boot 3.5 REST API for a bookstore domain, covering catalog
management (books, authors), customer registration, shopping carts, orders, payments, and shipments.
Authorization is JWT-based (OAuth2 resource server) with Keycloak used for customer provisioning.

## Commands

- Build: `./mvnw.cmd clean package` (or `./mvnw` on non-Windows)
- Run locally: `./mvnw.cmd spring-boot:run` — server starts on `http://localhost:8081`
- Run unit tests: `./mvnw.cmd test` — runs `*ServiceTest` / `*ControllerTest` only (Surefire);
  `*ControllerIT` classes are run by Failsafe in the `integration-test` phase.
- Run all tests including integration tests: `./mvnw.cmd verify`
- Run one integration test: `./mvnw.cmd test -Dtest=BookControllerIT`
- Run a single test class: `./mvnw.cmd test -Dtest=BookServiceTest`
- Run a single test method: `./mvnw.cmd test -Dtest=BookServiceTest#givenBookExists_whenFindById_thenReturnsBook`

## Repository Layout

- `src/main/java/com/carlosarroyoam/rest/books`
  - Feature modules: `author`, `book`, `customer`, `cart`, `order`, `payment`, `shipment`
  - `core`: cross-cutting infrastructure — `config` (security/web), `constant` (`AppMessages`),
    `exception` handling, `filter` (request filters), `pagination`, `property`, `security`,
    `specification`
- `src/main/resources`
  - `application.properties`: default local runtime config (port `8081`, H2, CORS, Keycloak client)
  - `logback-spring.xml`: logging config (console pattern includes the MDC correlation id)
- `src/test/resources/application-test.properties`: integration-test config
- `database/schema.sql`, `database/data.sql`: database bootstrap (no migration tool; schema managed
  by hand); `pom.xml` adds `database` as an extra Maven resources directory so these land on the
  classpath root alongside `src/main/resources`, where Spring Boot's default
  `classpath:schema.sql`/`classpath:data.sql` detection picks them up
- `src/test/java/com/carlosarroyoam/rest/books`
  - `*ServiceTest`: Mockito-based unit tests for business logic
  - `*ControllerTest`: MockMvc slice tests with mocked services
  - `*ControllerIT`: full Spring Boot integration tests with H2, security, and WireMock
  - `support/`: shared test security helpers and test utilities
- `src/test/resources`
  - `responses/**`: expected JSON payloads for integration tests, one directory per module
  - `mappings/**`, `__files/**`: WireMock stubs for Keycloak-related tests
- `docs/openapi/api-docs.yaml`: OpenAPI contract, covers all seven feature modules
- `realm-export.json`: local Keycloak realm export used for dev setup

## Architecture Conventions

- Each feature module is split into `entity/` (JPA entities, enums) and `dto/` (request/response
  DTOs, nested MapStruct mappers, `*Specs` classes), with `Controller`/`Service`/`Repository`
  classes at the module root.
- Controllers are thin and delegate to services; services own business rules, validation beyond
  bean validation, logging, and transaction boundaries.
- Persistence uses Spring Data repositories plus JPA `Specification`s (via each module's `*Specs`
  and `core/specification/SpecificationBuilder`) for filtered/paginated queries.
- DTO/entity mapping is done with a nested MapStruct mapper interface inside each DTO class (e.g.
  `BookResponse.BookResponseMapper`), not as standalone mapper classes.
- All uncaught exceptions are normalized by `core/exception/GlobalExceptionHandler` into RFC 9457
  `ProblemDetail` responses (`application/problem+json`), built via `ProblemDetailFactory`.
  Domain/application errors are raised as subclasses of `core/exception/ApplicationException`, each fixing
  its own HTTP status: `ResourceNotFoundException` (404), `ResourceAlreadyExistsException` /
  `ConflictException` (409), `BusinessException` / `ValidationException` (422),
  `UnauthorizedException` (401), `ForbiddenException` (403), `InternalServerException` (500). Throw
  with the plain `AppMessages` constant as the message. The handler logs it once through
  `ExceptionLogger` (WARN without a stack trace for 4xx, ERROR with one for 5xx) and maps
  `getStatus()` / `getMessage()` onto the `ProblemDetail`; don't also log it in the service.
- Every request is assigned a correlation id exposed in the MDC as `requestId` by
  `core/filter/CorrelationIdFilter` (reuses/returns the `X-Request-Id` header, runs at
  `HIGHEST_PRECEDENCE`); `core/filter/MdcUserContextFilter` adds authenticated-user context to
  the MDC. Log through SLF4J so these keys appear in output.
- Security is enforced in two layers: request-level rules in `core/config/WebSecurityConfig` and
  method-level `@PreAuthorize` in services/controllers.
- Realm roles from Keycloak JWTs are converted to `ROLE_*` authorities by `AuthoritiesConverter`
  (drops Keycloak default/offline/uma roles).
- Pagination responses are wrapped in `core/pagination/PagedResponse`.
- Jackson is configured globally for snake_case JSON (`spring.jackson.property-naming-strategy`).
- Javadoc in this codebase is written in Spanish; match that convention when adding Javadoc.

## Security Notes

- `GET /books/**`, `GET /authors/**`, `POST /customers`, `/h2-console/**`, and `/actuator/**` are
  public; everything else requires JWT authentication.
- Customer creation has both a local DB effect and a Keycloak side effect via `KeycloakService`,
  which provisions the user and assigns the `App/Customer` realm role — treat it as a two-system
  write when touching customer registration.
- `application.properties` contains a Keycloak client secret for local dev; do not introduce real
  secrets alongside it.

## Test Strategy

When changing behavior in a module, update the matching layers together:
- `*ServiceTest` for business-rule changes
- `*ControllerTest` for HTTP contract changes (status codes, request/response shape)
- `*ControllerIT` plus the corresponding JSON fixture under `src/test/resources/responses/<module>`
  for end-to-end behavior (`./mvnw.cmd verify` runs them; `./mvnw.cmd test` does not)
