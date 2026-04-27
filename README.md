# Spring REST Books

Spring REST Books is a Spring Boot 3.5 REST API for a bookstore domain. The application currently covers catalog management, customer registration, shopping carts, orders, payments, and shipments, with JWT-based authorization and Keycloak integration for customer provisioning.

## Technology Stack

- **Framework:** Spring Boot 3.x
- **Language:** Java 17
- **Database:** H2 In memory
- **ORM:** Spring Data JPA
- **Build Tool:** Maven

## Dependencies

- spring-boot-starter-web
- spring-boot-starter-data-jpa
- h2
- lombok
- mapstruct

## Prerequisites

- Java 17+
- Maven 3.8+

## Build

```bash
./mvnw clean package
```

## Run

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`

## API Documentation

See `docs/openapi/api-docs.yaml` for OpenAPI specification.

## Database Schema

Schema defined in `src/main/resources/schema.sql` with initial data in `src/main/resources/data.sql`.

## Keycloak Notes
Customer registration is not only a local database write. The application also provisions the user in Keycloak through `KeycloakService` and assigns the `App/Customer` realm role.

The repository includes [`realm-export.json`](realm-export.json), which appears to be the local realm export used for Keycloak setup.

## License
This project is licensed under Apache 2.0. See [`LICENSE`](LICENSE).
