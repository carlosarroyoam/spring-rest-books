# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Spring REST Books is a Java 17 / Spring Boot 3.5 (parent `3.5.16`) REST API for a bookstore
domain: books, authors, customers, carts, orders, payments, and shipments. It is an OAuth2 resource
server that validates Keycloak-issued JWTs, and it uses the Keycloak Admin Client to provision
customers. Persistence is an in-memory H2 database via Spring Data JPA. Lombok, MapStruct and the
Hibernate JPA metamodel generator (`Book_`, `Author_`, ...) run as annotation processors.

## Commands

Use the Maven wrapper (`./mvnw.cmd` on Windows, `./mvnw` elsewhere).

- Build: `./mvnw.cmd clean package`
- Run: `./mvnw.cmd spring-boot:run`, which serves on `http://localhost:8081`. Authenticated
  endpoints need Keycloak on `http://localhost:8080` with the `spring-rest-books` realm (import
  `realm-export.json`).
- Unit and slice tests: `./mvnw.cmd test` (Surefire runs `*Test` classes)
- All tests, including integration tests: `./mvnw.cmd verify` (Failsafe runs `*IT` classes in the
  `integration-test` phase; `mvn test` never runs them)
- One test class: `./mvnw.cmd test -Dtest=BookServiceTest`
- One test method: `./mvnw.cmd test -Dtest=BookServiceTest#givenBookExists_whenFindBookById_thenReturnsBook`
- One integration test: `./mvnw.cmd verify -Dit.test=BookControllerIT`

## Directory Layout

```
database/                  schema.sql, data.sql (added to the classpath root as a Maven resource dir)
docs/openapi/api-docs.yaml hand-maintained OpenAPI 3.1 contract
docs/postman/              Postman collection
realm-export.json          Keycloak realm for local development
src/main/java/com/carlosarroyoam/rest/books/
  BookServiceApplication.java
  author/ book/ cart/ customer/ order/ payment/ shipment/   feature modules
  core/
    config/         WebSecurityConfig, KeycloakAdminConfig
    constant/       CommonConstants, MessageKeys, CustomClaimNames
    exception/      ApplicationException hierarchy, GlobalExceptionHandler,
                    ProblemDetailFactory, ExceptionLogger
    filter/         CorrelationIdFilter, MdcUserContextFilter, RequestLoggingFilter
    pagination/     PagedResponse, PaginationResponse
    property/       CorsProps (app.cors.*), KeycloakAdminProps (keycloak.admin.*)
    security/       AuthoritiesConverter, CustomAuthenticationEntryPoint, CustomAccessDeniedHandler
    specification/  SpecificationBuilder
src/main/resources/        application.properties, messages.properties, logback-spring.xml
src/test/java/.../books/
  <module>/                *ServiceTest, *ControllerTest, *ControllerIT
  core/exception/          GlobalExceptionHandlerTest
  support/security/        JwtArgumentResolver
  support/testutils/       JsonUtils, TestMessageSources, TestObjectMappers
src/test/resources/
  application-test.properties
  responses/<module>/      expected JSON payloads for the ITs
  mappings/, __files/      WireMock stubs for the Keycloak Admin API
```

## Module Structure

Each feature module has the same shape:

- `<Name>Controller`, `<Name>Service`, `<Name>Repository` at the module root (the cart module also
  has a `CartItemRepository`; the customer module also has `KeycloakService`).
- `entity/`: JPA entities and their status/enum types (`BookStatus`, `PaymentMethod`, ...).
- `dto/`: `Create*Request`, `Update*Request` and `*Response` DTOs, plus `*Specs` filter objects
  that bind list query parameters through `@ModelAttribute`.

## Code Conventions

- **Controllers** are thin. They declare `produces`/`consumes = "application/json"`, validate input
  with `@Valid`, and return `ResponseEntity`: `201` with a `Location` header built from
  `UriComponentsBuilder` on create, `204` on update/delete. List endpoints take `Pageable` with
  `@PageableDefault(page = 0, size = 25, sort = "id")` and return `PagedResponse<T>`.
- **Authorization** is enforced with `@PreAuthorize("hasRole('App/Admin')")` or
  `hasRole('App/Customer')` on controller methods. Services carry no security annotations.
- **Services** own business rules and transactions: `@Transactional(readOnly = true)` on reads,
  `@Transactional` on writes. Lookups by id go through a private `find<Entity>ById` helper that
  throws `ResourceNotFoundException`. Deletes are soft: they set the status to `DELETED` and fill
  `deletedAt`/`updatedAt`. Timestamps come from `LocalDateTime.now()` and entities are built with
  Lombok `@Builder`.
- **Dependency injection** uses explicit constructors with `private final` fields (no
  `@RequiredArgsConstructor`, no field injection).
- **Mapping** uses a MapStruct mapper nested inside each response DTO (e.g.
  `BookResponse.BookResponseMapper`), reached through its `INSTANCE` field and configured with
  `NullValuePropertyMappingStrategy.IGNORE` and `ReportingPolicy.IGNORE`. Pages are wrapped with
  `PagedResponse.PagedResponseMapper.INSTANCE.toPagedResponse(page.map(...))`.
- **Filtering** builds a `Specification` with
  `SpecificationBuilder.<T>builder().likeIfPresent/equalsIfPresent/betweenIfPresent/inIfPresent(...)`
  using metamodel attributes (`root -> root.get(Book_.title)`).
- **JSON** is snake_case globally (`spring.jackson.property-naming-strategy=SNAKE_CASE`).
- **Configuration properties** are `@Component @ConfigurationProperties @Validated` classes in
  `core/property` with bean validation messages.
- **Javadoc** is written in Spanish. Log messages, error texts and identifiers are in English.
- **Formatting** follows google-java-format style (2-space indent, 100 columns). The build does
  not enforce it, so keep new code consistent with it by hand.

## Error Handling

- Domain errors are subclasses of `core/exception/ApplicationException`, and each subclass fixes
  its HTTP status: `ResourceNotFoundException` 404, `ResourceAlreadyExistsException` and
  `ConflictException` 409, `BusinessException` and `ValidationException` 422,
  `UnauthorizedException` 401, `ForbiddenException` 403, `InternalServerException` 500.
- Throw them with an `MessageKeys` constant, which is a key into `messages.properties` (for example
  `book.not-found`). When you add a constant, add its text to `messages.properties`.
  `GlobalExceptionHandler` resolves the key through `MessageSource` (fixed `en_US` locale) and falls
  back to the raw string.
- `GlobalExceptionHandler` turns every error into an RFC 9457 `ProblemDetail`
  (`application/problem+json`) via `ProblemDetailFactory`. Bean validation failures become 422 with
  per-field messages under `errors`, malformed bodies and type mismatches become 400, and anything
  unhandled becomes 500.
- `ExceptionLogger` is the only place errors are logged: WARN without a stack trace for 4xx, ERROR
  with a stack trace for 5xx. Spring Security's `CustomAuthenticationEntryPoint` and
  `CustomAccessDeniedHandler` use it too. Do not also log thrown exceptions in services.

## Security

- `WebSecurityConfig` sets up a stateless JWT resource server (issuer
  `http://localhost:8080/realms/spring-rest-books`), with CSRF disabled and CORS taken from
  `app.cors.*`.
- Public routes are `GET /books/**`, `GET /authors/**`, `POST /customers`, `/h2-console/**` and
  `/actuator/**`. Every other route requires a JWT.
- Realm roles in `realm_access.roles` become `ROLE_<role>` authorities. Keycloak's `default-*`,
  `offline_access` and `uma_authorization` roles are dropped. The `AuthoritiesConverter` interface
  lives in `core/security`; its bean is defined in `WebSecurityConfig`.
- Customer registration writes to two systems: `CustomerService` saves the local row, and
  `KeycloakService` creates the Keycloak user and assigns the `App/Customer` realm role. If the user
  already exists in Keycloak, registration is rejected.
- `application.properties` holds a client secret for the local Keycloak dev realm only. Never add
  real secrets.

## Logging and Observability

- `CorrelationIdFilter` (`HIGHEST_PRECEDENCE`) reuses or generates `X-Request-Id`, sends it back in
  the response, and puts it in the MDC as `requestId`.
- `MdcUserContextFilter` runs after bearer-token authentication. It is added manually in
  `WebSecurityConfig` and is not a `@Component`. It puts `preferred_username` in the MDC as
  `username`.
- `RequestLoggingFilter` logs method, path, status and duration for each request.
- `logback-spring.xml` adds `[traceId=..., username=...]` to the default Spring Boot console
  pattern. Log through SLF4J so these MDC keys show up.

## Database

- Local runs use H2 in memory (`jdbc:h2:mem:testdb`) with `ddl-auto=none`. The schema comes only
  from `database/schema.sql` and seed data from `database/data.sql`; there is no migration tool.
  Schema changes go into `schema.sql` by hand.
- Integration tests (`application-test.properties`) use Hibernate `create-drop` and load only
  `data.sql` (`schema-locations` is empty). Entity mappings therefore have to match `schema.sql`,
  and `data.sql` has to load against the schema Hibernate generates.

## Testing

- Test methods are named `given<State>_when<Action>_then<Outcome>`.
- `*ServiceTest`: Mockito unit tests (`@ExtendWith(MockitoExtension.class)`).
- `*ControllerTest`: standalone MockMvc with mocked services, the real `GlobalExceptionHandler`
  (built from `TestMessageSources.messages()`), and `TestObjectMappers.snakeCase()`.
- `*ControllerIT`: `@SpringBootTest(RANDOM_PORT)`, `@ActiveProfiles("test")`, `@Transactional`,
  MockMvc with Spring Security and a mocked `jwt()` that carries `ROLE_App/Admin` or
  `ROLE_App/Customer`. WireMock on port 8089 stands in for Keycloak. Responses are compared against
  the JSON fixtures in `src/test/resources/responses/<module>/`.
- When you change behavior, update every layer it touches: the `*ServiceTest` for business rules,
  the `*ControllerTest` for the HTTP contract, the `*ControllerIT` plus its fixture for end-to-end
  behavior, and `docs/openapi/api-docs.yaml` when the contract changes.

## Javadoc

All Javadoc is written in **Spanish**. Rules: third-person present-tense first sentence, `@throws` only for domain exceptions thrown by `UserService`/`AuthService`, REST methods guarded by `@RolesAllowed` end their first sentence with "Requiere rol {@code X}.", no `@author`/`@since`.
