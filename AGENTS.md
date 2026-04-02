# AGENTS.md

## Purpose
This repository is a Java 17 / Spring Boot 3.5 REST API for a bookstore domain. The modules are:

- `author`
- `book`
- `customer`
- `cart`
- `order`
- `payment`
- `shipment`
- shared `core` infrastructure

Use this file as the operating guide for agents making changes in this repo.

## Stack
- Java 17
- Maven Wrapper (`mvnw`, `mvnw.cmd`)
- Spring Boot 3.5.6
- Spring Web, Validation, Security, OAuth2 Resource Server
- Spring Data JPA + H2
- Keycloak Admin Client
- Lombok
- MapStruct
- JUnit 5, Mockito, MockMvc, WireMock, JSONAssert

## Repository Layout
- `src/main/java/com/carlosarroyoam/rest/books`
  - `author`, `book`, `customer`, `cart`, `order`, `payment`, `shipment`: feature modules
  - `core`: security, config, exceptions, DTOs, validation, utilities, constants
- `src/main/resources`
  - `application.properties`: default local runtime config
  - `application-test.properties`: integration-test config
  - `schema.sql`, `data.sql`: database bootstrap
- `src/test/java/com/carlosarroyoam/rest/books`
  - `*ServiceTest`: unit tests for business logic
  - `*ControllerTest`: controller-slice tests with mocked services
  - `*ControllerIT`: Spring Boot integration tests with database and security
- `src/test/resources`
  - `responses/**`: expected JSON payloads for integration tests
  - `mappings/**`, `__files/**`: WireMock stubs for Keycloak-related tests
- `docs/openapi/api-docs.yaml`: API contract draft

## Architecture Conventions
- Controllers are thin and mostly delegate to services.
- Services own business rules, validation beyond bean validation, logging, and transaction boundaries.
- Persistence uses Spring Data repositories plus JPA `Specification`s for filtered queries.
- DTO/entity mapping is done with nested MapStruct mapper interfaces inside DTO classes.
- Errors are normalized by `GlobalExceptionHandler`.
- Security is enforced both in `WebSecurityConfig` request rules and method-level `@PreAuthorize`.

## Coding Patterns To Preserve
- Keep feature code grouped by module package.
- Prefer constructor injection.
- Use `ResponseStatusException` for domain/application HTTP errors.
- Reuse `AppMessages` constants for user-facing exception messages when applicable.
- When adding list filters, extend the module `*Specification` rather than pushing query logic into controllers.
- When adding request/response models, follow the existing Lombok + nested MapStruct style.
- Keep pagination responses wrapped in `PagedResponseDto`.
- Respect Jackson snake_case output configured globally in `application.properties`.

## Security And Auth Notes
- GET access to `/books/**` and `/authors/**` is public.
- `POST /customers` is public.
- Most other endpoints require JWT authentication.
- Admin-only mutations commonly use `ROLE_App/Admin`.
- Customer provisioning flows through `KeycloakService`, which also assigns `App/Customer`.
- Do not commit new secrets. `application.properties` currently contains a client secret and should be treated carefully.

## Data And Runtime Notes
- Local runtime uses in-memory H2 on port `8081`.
- Schema is managed with `schema.sql`; production-style migrations are not present.
- Seed data lives in `data.sql` and is used by local runs and tests.
- Customer creation has both local DB effects and Keycloak side effects.
- `realm-export.json` appears to hold local Keycloak realm setup.

## Tests
Verified on March 17, 2026:

- `./mvnw.cmd test`

Result:
- Build passed
- 65 tests passed

Test strategy in this repo:
- `*ServiceTest`: Mockito-heavy unit tests
- `*ControllerTest`: standalone MockMvc tests with mocked services and global exception handler
- `*ControllerIT`: full Spring Boot integration tests using H2, security, JSON fixtures, and WireMock where needed

When changing behavior:
- Update or add service tests for business rules.
- Update or add controller tests for HTTP contract changes.
- Update or add integration tests and JSON fixtures for end-to-end API behavior.

## API Contract Caveat
`docs/openapi/api-docs.yaml` documents `orders`, `payments`, and `shipments`, but those modules are not implemented in `src/main/java` today. Treat the OpenAPI file as partially ahead of the codebase. Do not assume every documented endpoint exists.

## Recommended Workflow For Agents
1. Inspect the target feature package and its matching tests before editing.
2. Make the smallest change that fits the existing module boundaries.
3. Update tests in the same feature area.
4. Run `./mvnw.cmd test` after meaningful changes.
5. If HTTP payloads change, update JSON fixtures under `src/test/resources/responses`.

## Safe Change Boundaries
- Safe to change:
  - feature controllers, services, repositories, specifications, DTOs, entities
  - shared exception handling and validation with corresponding test updates
  - test fixtures and WireMock mappings
- Be careful when changing:
  - `WebSecurityConfig`
  - `KeycloakService`
  - `schema.sql` / `data.sql`
  - shared DTO mapping patterns

## File-Specific Pointers
- Application entrypoint: `src/main/java/com/carlosarroyoam/rest/books/BookServiceApplication.java`
- Security config: `src/main/java/com/carlosarroyoam/rest/books/core/config/WebSecurityConfig.java`
- Exception handler: `src/main/java/com/carlosarroyoam/rest/books/core/exception/GlobalExceptionHandler.java`
- API spec: `docs/openapi/api-docs.yaml`

## What Not To Do
- Do not introduce a new architectural pattern for a small feature change.
- Do not move mapping logic into controllers.
- Do not bypass the service layer for business rules.
- Do not assume OpenAPI and implementation are fully aligned.
- Do not remove or overwrite user changes outside the task scope.
