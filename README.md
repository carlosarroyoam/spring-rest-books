# Spring REST Books

Spring REST Books is a Spring Boot 3.5 REST API for a bookstore domain. The application currently covers catalog management, customer registration, shopping carts, orders, payments, and shipments, with JWT-based authorization and Keycloak integration for customer provisioning.

## Current Scope
Implemented modules:

- Books
- Authors
- Customers
- Carts
- Orders
- Payments
- Shipments
- Shared infrastructure for security, validation, exceptions, pagination, and utilities

## Core Flow
A typical purchase flow in the current implementation is:

1. Browse books and authors.
2. Register a customer.
3. Add books to the customer's cart.
4. Create an order from selected items.
5. Create a payment for the order.
6. Generate and track the shipment.
7. Update payment or shipment status as the order lifecycle evolves.

## Tech Stack
- Java 17
- Spring Boot 3.5.6
- Spring Web
- Spring Data JPA
- Spring Security OAuth2 Resource Server
- Spring Validation
- H2 in-memory database
- Keycloak Admin Client
- Lombok
- MapStruct
- JUnit 5, Mockito, MockMvc, WireMock, JSONAssert
- Maven Wrapper

## Data Model
The application bootstraps these tables:

- `authors`
- `books`
- `book_authors`
- `customers`
- `carts`
- `cart_items`
- `orders`
- `order_items`
- `payments`
- `shipments`

Schema and seed data live in:

- [`src/main/resources/schema.sql`](src/main/resources/schema.sql)
- [`src/main/resources/data.sql`](src/main/resources/data.sql)

The default seed includes sample records for catalog, customers, carts, orders, payments, and shipments so the API can be explored immediately after startup.

## Security Model
Current access rules are:

- `GET /books/**` and `GET /authors/**` are public.
- `POST /customers` is public.
- Cart endpoints require an authenticated customer.
- Order, payment, and shipment management endpoints are currently admin-only.
- Customer provisioning uses Keycloak and assigns the `App/Customer` role.

## Local Configuration
Default runtime configuration is in [`src/main/resources/application.properties`](src/main/resources/application.properties).

Key defaults:

- app name: `spring-rest-books`
- port: `8081`
- database: `jdbc:h2:mem:testdb`
- H2 console: `http://localhost:8081/h2-console`
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

Verified on April 1, 2026:

- `./mvnw.cmd test`
- Result: `99` tests passed

## API And Collections
Available documentation:

- OpenAPI draft: [`docs/openapi/api-docs.yaml`](docs/openapi/api-docs.yaml)
- Postman collection: [`docs/postman/postman_collection.json`](docs/postman/postman_collection.json)
- Agent guide: [`AGENTS.md`](AGENTS.md)

The OpenAPI and Postman collection include the current `orders`, `payments`, and `shipments` endpoints, including status update operations.

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
|- orders
|- payment
|- shipment
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
|- orders
|- payment
|- shipment
|- common
`- core
```

## Implementation Notes
- Controllers are thin and delegate to services.
- Services hold business rules and transaction boundaries.
- Filtered list endpoints use JPA `Specification`s where applicable.
- DTO mapping uses nested MapStruct mappers inside DTO classes.
- Errors are normalized by `GlobalExceptionHandler`.
- JSON output uses snake_case via Jackson configuration.
- Payment and shipment status changes propagate to the related order status.

## Keycloak Notes
Customer registration is not only a local database write. The application also provisions the user in Keycloak through `KeycloakService` and assigns the `App/Customer` realm role.

The repository includes [`realm-export.json`](realm-export.json), which appears to be the local realm export used for Keycloak setup.

## License
This project is licensed under Apache 2.0. See [`LICENSE`](LICENSE).
