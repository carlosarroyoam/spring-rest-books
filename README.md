# Spring REST Books

Spring REST Books is a Spring Boot 3.5 REST API for a bookstore domain. The application currently covers catalog management, customer registration, shopping carts, orders, payments, and shipments, with JWT-based authorization and Keycloak integration for customer provisioning.

## Technology Stack

- **Framework:** Spring Boot 3.5
- **Language:** Java 17
- **Database:** H2 in-memory
- **ORM:** Spring Data JPA
- **Security:** Spring Security / OAuth2 resource server (JWT), Keycloak
- **Build Tool:** Maven

## Dependencies

Runtime:

- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-oauth2-resource-server
- spring-boot-starter-validation
- spring-boot-starter-actuator
- keycloak-admin-client
- h2
- lombok
- mapstruct

Test:

- spring-boot-starter-test
- spring-security-test
- wiremock-spring-boot

See [`pom.xml`](pom.xml) for exact versions.

## Prerequisites

- Java 17+
- Maven 3.8+ (or use the bundled `./mvnw` / `./mvnw.cmd` wrapper)
- A running Keycloak instance for authenticated endpoints (see [Keycloak Notes](#keycloak-notes))

## Build

```bash
./mvnw clean package
```

## Run

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8081`.

## Tests

```bash
./mvnw test                         # unit and MockMvc slice tests
./mvnw verify                       # unit + integration tests (*IT, via failsafe)
./mvnw test -Dtest=ClassName  # a single test class
```

`*ControllerIT` integration tests are not picked up by the default `mvn test` run.

## API Documentation

- OpenAPI 3.1 spec: `docs/openapi/api-docs.yaml` (hand-maintained)
- Postman collection: `docs/postman/postman_collection.json`

## Database Schema

Schema defined in `database/schema.sql` with initial data in `database/data.sql`.

## Keycloak Notes

Authenticated endpoints expect a Keycloak server on `http://localhost:8080` exposing the
`spring-rest-books` realm (JWT issuer `http://localhost:8080/realms/spring-rest-books`). Import
[`realm-export.json`](realm-export.json) to provision that realm and its `App` client/roles.
Public endpoints (`GET /books/**`, `GET /authors/**`, `POST /customers`) work without it.

Customer registration is not only a local database write: the application also provisions the
user in Keycloak through `KeycloakService` and assigns the `App/Customer` realm role, so a
reachable Keycloak with admin credentials configured in `application.properties` is required for
that flow.

## License

This project is licensed under Apache 2.0. See [`LICENSE`](LICENSE).
