# Spring REST Books

Spring REST Books is a Spring Boot 3.5 REST API for a bookstore domain. The current implementation covers books, authors, customers, and shopping carts, with JWT-based authorization and Keycloak integration for customer provisioning.

## Current Scope
Implemented modules:

- Books
- Authors
- Customers
- Carts
- Shared infrastructure for security, validation, exceptions, pagination, and utilities

Important: [`docs/openapi/api-docs.yaml`](docs/openapi/api-docs.yaml) also describes `orders`, `payments`, and `shipments`, but those modules are not implemented in the Java source yet.

## Tech Stack
- Java 17
- Spring Boot 3.5.6
- Spring Web
- Spring Data JPA
- Spring Security OAuth2 Resource Server
- H2 in-memory database
- Keycloak Admin Client
- Lombok
- MapStruct
- JUnit 5, Mockito, MockMvc, WireMock, JSONAssert
- Maven Wrapper

## Data Model
The application currently bootstraps these tables:
- `authors`
- `books`
- `book_authors`
- `customers`
- `carts`
- `cart_items`

Schema and seed data live in:
- [`src/main/resources/schema.sql`](src/main/resources/schema.sql)
- [`src/main/resources/data.sql`](src/main/resources/data.sql)

## Local Configuration
Default runtime configuration is in [`src/main/resources/application.properties`](src/main/resources/application.properties).

Key defaults:
- app name: `spring-rest-books`
- port: `8081`
- database: `jdbc:h2:mem:testdb`
- H2 console: enabled at `/h2-console`
- JWT issuer: `http://localhost:8080/realms/spring-rest-books`
- Keycloak admin base URL: `http://localhost:8080`

Test profile configuration is in [`src/main/resources/application-test.properties`](src/main/resources/application-test.properties).

## Running The Application
### Prerequisites
- Java 17 installed and available on `PATH`
- Keycloak available locally if you want authenticated flows to work end-to-end

### Start the API
On Windows:

```powershell
./mvnw.cmd spring-boot:run
```

On macOS/Linux:

```bash
./mvnw spring-boot:run
```

The API starts on `http://localhost:8081`.

## Running Tests
On Windows:

```powershell
./mvnw.cmd test
```

On macOS/Linux:

```bash
./mvnw test
```

## Testing Strategy
The repository uses three test layers:

- `*ServiceTest`: unit tests for service logic with Mockito
- `*ControllerTest`: standalone MockMvc tests with mocked services
- `*ControllerIT`: Spring Boot integration tests using H2, Spring Security, JSON fixtures, and WireMock stubs

Useful test assets live under:
- [`src/test/resources/responses`](src/test/resources/responses)
- [`src/test/resources/mappings`](src/test/resources/mappings)
- [`src/test/resources/__files`](src/test/resources/__files)

## Project Structure
```text
src/main/java/com/carlosarroyoam/rest/books
|- author
|- book
|- cart
|- customer
`- core

src/main/resources
|- application.properties
|- application-test.properties
|- schema.sql
`- data.sql

src/test/java/com/carlosarroyoam/rest/books
|- author
|- book
|- cart
|- customer
|- common
`- core
```

## Implementation Notes
- Controllers are thin and delegate to services.
- Services hold business rules and transaction boundaries.
- Filtered list endpoints use JPA `Specification`s.
- DTO mapping uses nested MapStruct mappers inside DTO classes.
- Errors are normalized by `GlobalExceptionHandler`.
- JSON output uses snake_case via Jackson configuration.

## Keycloak Notes
Customer registration is not only a local database write. The application also provisions the user in Keycloak through `KeycloakService` and assigns the `App/Customer` realm role.

The repository includes [`realm-export.json`](realm-export.json), which appears to be the local realm export used for Keycloak setup.

## Documentation Files
- API draft: [`docs/openapi/api-docs.yaml`](docs/openapi/api-docs.yaml)
- Agent guide: [`AGENTS.md`](AGENTS.md)

## License
This project is licensed under Apache 2.0. See [`LICENSE`](LICENSE).
