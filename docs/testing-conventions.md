# Convenciones de testing para proyectos Spring Boot REST

Guía extraída de `spring-rest-books` (Java 17, Spring Boot 3.5, JUnit 5, Mockito, AssertJ, MockMvc,
Spring Security Test, WireMock). Está pensada para copiarse a otro proyecto Spring: los ejemplos usan
`<base.package>` como paquete raíz y `Book` como entidad de ejemplo.

Convención de lectura:

- **Regla** = se aplica en todos los tests del proyecto.
- **Evitar** = el proyecto de origen lo hace así, pero es una deuda; no se debe replicar (ver
  [sección 14](#14-desviaciones-conocidas-no-replicar-tal-cual)).

---

## 1. Tipos de test

| Tipo | Clase | Runner | Qué levanta | Qué verifica |
| --- | --- | --- | --- | --- |
| Unitario de servicio | `<X>ServiceTest` | Surefire (`test`) | Solo Mockito, sin Spring | Reglas de negocio, excepciones de dominio, interacción con repositorios |
| Slice de controller | `<X>ControllerTest` | Surefire (`test`) | MockMvc `standaloneSetup`, sin Spring | Contrato HTTP: rutas, status, headers, forma del JSON, mapeo de errores |
| Integración | `<X>ControllerIT` | Failsafe (`verify`) | `@SpringBootTest` completo + H2 + Spring Security | Flujo de punta a punta: seguridad, JPA, serialización, fixtures JSON |
| Infraestructura | `GlobalExceptionHandlerTest`, `<App>ApplicationTest` | Surefire | Mínimo necesario | Manejo global de errores, integridad de `messages.properties`, `contextLoads` |

Los repositorios y servicios **no** tienen tests propios con `@DataJpaTest`: la persistencia se cubre
a través de los ITs.

## 2. Dependencias y build

`pom.xml`:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.springframework.security</groupId>
  <artifactId>spring-security-test</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.skyscreamer</groupId>
  <artifactId>jsonassert</artifactId>
  <scope>test</scope>
</dependency>
<!-- Solo si el servicio consume APIs externas HTTP -->
<dependency>
  <groupId>org.wiremock.integrations</groupId>
  <artifactId>wiremock-spring-boot</artifactId>
  <version>3.10.0</version>
  <scope>test</scope>
</dependency>
<!-- Declarado en el proyecto de origen junto a WireMock (WebClient/WebTestClient en tests) -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webflux</artifactId>
  <scope>test</scope>
</dependency>
```

Failsafe ejecuta los `*IT` (Surefire solo corre `*Test`, por lo que `mvn test` **nunca** ejecuta los
ITs):

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-failsafe-plugin</artifactId>
  <executions>
    <execution>
      <goals>
        <goal>integration-test</goal>
        <goal>verify</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

Comandos (Maven wrapper; `./mvnw.cmd` en Windows, `./mvnw` en el resto):

| Objetivo | Comando |
| --- | --- |
| Unitarios y slices | `./mvnw.cmd test` |
| Todo, incluidos ITs | `./mvnw.cmd verify` |
| Una clase | `./mvnw.cmd test -Dtest=BookServiceTest` |
| Un método | `./mvnw.cmd test -Dtest=BookServiceTest#givenBookExists_whenFindById_thenReturnsBook` |
| Un IT | `./mvnw.cmd verify -Dit.test=BookControllerIT` |

## 3. Estructura de directorios

```
src/test/java/<base.package>/
  <module>/                     <X>ServiceTest, <X>ControllerTest, <X>ControllerIT
  core/exception/               GlobalExceptionHandlerTest
  support/security/             JwtArgumentResolver
  support/testutils/            JsonUtils, TestMessageSources, TestObjectMappers
  <App>ApplicationTest.java     contextLoads
src/test/resources/
  application-test.properties   perfil "test" para los ITs
  responses/<module>/           JSON esperado de los ITs
  mappings/                     stubs WireMock (request -> response)
  __files/                      cuerpos de respuesta de WireMock
```

Regla: los tests viven en el mismo paquete que la clase bajo prueba (acceso a miembros
package-private) y los helpers compartidos van bajo `support/`.

## 4. Nomenclatura

- **Clases**: `<Nombre>ServiceTest`, `<Nombre>ControllerTest`, `<Nombre>ControllerIT`. El sufijo
  decide qué plugin las ejecuta; no uses otro.
- **Métodos**: `given<State>_when<Action>_then<Outcome>`, en camelCase dentro de cada bloque.
  - En `*ServiceTest` la acción es el nombre del método del servicio: `whenFindById`, `whenCreate`.
  - En `*ControllerTest` y `*ControllerIT` la acción incluye la entidad: `whenFindBookById`,
    `whenCreateBook`.
- **`@DisplayName`** (en inglés) en todos los métodos:
  - Service: `"Given book does not exist, when find by id, then throws not found exception"`
  - Controller / IT: `"GET /books/{id} - Given book exists, when find by id, then returns book"`
- **Campos de prueba**: `@Mock` primero, luego `@Spy`, luego `@InjectMocks`, luego fixtures de estado
  (`private Book book;`).
- **Setup**: `@BeforeEach void setUp()` en service tests, `void setup()` en controller/IT; son
  `package-private`, sin `public`.
- Las clases de test son `package-private` (`class BookServiceTest`), sin `public`.
- Formato google-java-format (2 espacios, 100 columnas). Cuando un `@DisplayName` no cabe en la
  línea, se parte con `+` o se pasa el literal a la línea siguiente.

## 5. Plantilla `*ServiceTest`

Reglas:

1. `@ExtendWith(MockitoExtension.class)`; sin contexto de Spring.
2. Un `@Mock` por colaborador (repositorios, otros servicios, clientes externos).
3. El tiempo se controla con un `Clock` fijo inyectado mediante `@Spy`; el servicio usa
   `LocalDateTime.now(clock)`, nunca `LocalDateTime.now()`.
4. `@InjectMocks` sobre el servicio (se construye por constructor).
5. Las entidades de fixture se construyen con el `@Builder` de Lombok en `@BeforeEach`, con
   timestamps derivados del mismo `clock`.
6. Aserciones con AssertJ. Excepciones de dominio se verifican por tipo **y** por clave de mensaje
   (`MessageKeys`), no por el texto traducido.
7. Cada operación de negocio cubre como mínimo: camino feliz, recurso inexistente (not found) y
   conflicto/duplicado cuando aplique.

```java
@ExtendWith(MockitoExtension.class)
class BookServiceTest {
  @Mock private BookRepository bookRepository;
  @Mock private AuthorRepository authorRepository;
  @Spy private Clock clock = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC);
  @InjectMocks private BookService bookService;

  private Book book;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now(clock);

    book =
        Book.builder()
            .id(1L)
            .isbn("978-1-3035-0529-4")
            .title("Homo Deus: A Brief History of Tomorrow")
            .price(new BigDecimal("22.99"))
            .createdAt(now)
            .updatedAt(now)
            .build();
  }

  @Test
  @DisplayName("Given book exists, when find by id, then returns book")
  void givenBookExists_whenFindById_thenReturnsBook() {
    when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));

    BookResponse bookResponse = bookService.findById(1L);

    assertThat(bookResponse).isNotNull();
    assertThat(bookResponse.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Given book does not exist, when find by id, then throws not found exception")
  void givenBookDoesNotExist_whenFindById_thenThrowsNotFoundException() {
    when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.findById(1L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage(MessageKeys.BOOK_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Given book exists, when delete, then deletes book")
  void givenBookExists_whenDelete_thenDeletesBook() {
    when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));

    bookService.deleteById(1L);

    verify(bookRepository).save(any(Book.class));
  }
}
```

Patrones recurrentes:

- **Listados paginados**: se devuelve un `PageImpl` y se afirma sobre `items` y `pagination`
  (`page`, `size`, `totalItems`, `totalPages`).

  ```java
  when(bookRepository.findAll(ArgumentMatchers.<Specification<Book>>any(), any(Pageable.class)))
      .thenReturn(new PageImpl<>(books, pageable, books.size()));
  ```

  Usa `ArgumentMatchers.<Specification<T>>any()` para evitar la ambigüedad de sobrecargas.
- **Creación**: stub de `existsBy…` (`false`) y de `save(any(Entity.class))` devolviendo la entidad
  guardada con id; se afirma sobre el DTO de respuesta.
- **Actualización**: se afirma sobre la entidad mutada (`book.getTitle()`) y se hace
  `verify(repository).findById(1L)` y `verify(repository).save(any(Book.class))`.
- **Borrado lógico (soft delete)**: `verify(repository).save(any(...))` en vez de `delete`.
- **Colecciones**: `assertThat(list).hasSize(1).first().satisfies(item -> { ... })`.
- **Clientes externos** (`KeycloakServiceTest`): se mockea toda la cadena de recursos
  (`keycloak.realm(..)` → `realmResource.users()` → `usersResource`), se usa `mock(Response.class)`
  para respuestas HTTP y `verify(x, never()).create(any())` para afirmar efectos que **no** deben
  ocurrir. También se prueba la compensación (rollback manual: `verify(userResource).remove()`).
- Los stubs comunes a todos los tests de la clase van en `@BeforeEach`; con `MockitoExtension` el
  modo estricto está activo, así que un stub que un test no use lo hará fallar con
  `UnnecessaryStubbingException`. Muévelo al test que lo necesita, o declara el stub como
  `lenient()` solo si es realmente compartido.

## 6. Plantilla `*ControllerTest`

Reglas:

1. `@ExtendWith(MockitoExtension.class)` con `@Mock` del servicio y `@InjectMocks` del controller.
   No se levanta Spring ni Spring Security: **no** se prueba autorización aquí.
2. `MockMvcBuilders.standaloneSetup(controller)` con tres piezas obligatorias:
   - `GlobalExceptionHandler` **real** (con `ProblemDetailFactory`, `ExceptionLogger` y el
     `MessageSource` de `TestMessageSources.messages()`), para que los errores salgan con el mismo
     formato que en producción.
   - `MappingJackson2HttpMessageConverter(TestObjectMappers.snakeCase())` para replicar la
     configuración de Jackson de Spring Boot.
   - Los `HandlerMethodArgumentResolver` que necesite el controller:
     `PageableHandlerMethodArgumentResolver` para endpoints paginados o `JwtArgumentResolver` para
     los que reciben `Jwt`.
3. El request body se serializa con el mismo `ObjectMapper` snake_case:
   `mapper.writeValueAsString(request)`.
4. Se afirma el contrato HTTP, no la lógica: `status()`, `content().contentType(...)`,
   `jsonPath(...)` y headers.

```java
@ExtendWith(MockitoExtension.class)
class BookControllerTest {
  @Mock private BookService bookService;
  @InjectMocks private BookController bookController;

  private ObjectMapper mapper;
  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    mapper = TestObjectMappers.snakeCase();

    mockMvc =
        MockMvcBuilders.standaloneSetup(bookController)
            .setControllerAdvice(
                new GlobalExceptionHandler(
                    new ProblemDetailFactory(),
                    new ExceptionLogger(),
                    TestMessageSources.messages()))
            .setMessageConverters(
                new MappingJackson2HttpMessageConverter(TestObjectMappers.snakeCase()))
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
  }

  @Test
  @DisplayName("GET /books/{id} - Given book exists, when find by id, then returns book")
  void givenBookExists_whenFindBookById_thenReturnsBook() throws Exception {
    BookResponse book = BookResponse.builder().id(1L).title("Sapiens").build();

    when(bookService.findById(anyLong())).thenReturn(book);

    mockMvc
        .perform(get("/books/{bookId}", 1L).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Sapiens"));
  }

  @Test
  @DisplayName("POST /books - Given valid book data, when create, then returns created")
  void givenValidBookData_whenCreateBook_thenReturnsCreated() throws Exception {
    CreateBookRequest request = CreateBookRequest.builder().isbn("978-9-7389-4434-3").build();

    when(bookService.create(any(CreateBookRequest.class)))
        .thenReturn(BookResponse.builder().id(1L).build());

    mockMvc
        .perform(
            post("/books")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(header().string("location", "http://localhost/books/1"));
  }
}
```

Contrato mínimo por verbo (replicar en cada recurso):

| Verbo | Status | Qué se afirma |
| --- | --- | --- |
| `GET` colección | `200` | `content-type` JSON, `$.items.length()`, `$.pagination.page/size/total_items/total_pages` |
| `GET` por id | `200` | `content-type` JSON y los campos clave (`$.id`, …) |
| `POST` | `201` | header `location` = `http://localhost/<ruta>/<id>` (sin cuerpo) |
| `PUT` | `204` | solo el status |
| `DELETE` | `204` | solo el status |

Detalles:

- Las claves JSON en `jsonPath` van en **snake_case** (`$.pagination.total_items`).
- En los métodos `void` del servicio (update/delete) no hace falta stub: Mockito responde por defecto.
- Controllers con `Jwt`: usa `setCustomArgumentResolvers(new JwtArgumentResolver())`. El resolver
  entrega un `Jwt` con `preferred_username=carroyom` y `customer_id=1`, de modo que el test no
  depende de Spring Security.
- El `Pageable` real se arma con `queryParam("page", "0")` y `queryParam("size", "25")`.

## 7. Plantilla `*ControllerIT`

Reglas:

1. `@SpringBootTest` (entorno `MOCK`; no hace falta `RANDOM_PORT` porque no se usa un cliente HTTP
   real), `@ActiveProfiles("test")` y `@Transactional` de Spring (revierte los cambios de cada
   test, de modo que el estado de `data.sql` es siempre el inicial).
2. Se construye un `MockMvc` propio con Spring Security aplicado y una identidad JWT simulada **por
   defecto en todas las peticiones** (`defaultRequest`), con la autoridad `ROLE_<rol>` que exige el
   endpoint (`ROLE_App/Admin`, `ROLE_App/Customer`).
3. Las respuestas de lectura se comparan contra fixtures JSON en `responses/<module>/`.
4. Las escrituras afirman status y, en `POST`, el header `Location` (el id esperado depende del seed
   de `data.sql`).
5. Servicios externos HTTP se simulan con WireMock en un puerto fijo declarado también en
   `application-test.properties`.

```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookControllerIT {
  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private ObjectMapper mapper;
  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    mockMvc =
        MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .defaultRequest(
                get("/")
                    .with(
                        jwt()
                            .jwt(jwt -> jwt.claim("preferred_username", "carroyom"))
                            .authorities(new SimpleGrantedAuthority("ROLE_App/Admin"))))
            .build();
  }

  @Test
  @DisplayName("GET /books/{id} - Given book exists, when find by id, then returns book")
  void givenBookExists_whenFindBookById_thenReturnsBook() throws Exception {
    String expectedJson = JsonUtils.readJson("books/find-by-id.json");

    String responseJson =
        mockMvc
            .perform(get("/books/{bookId}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

    JSONAssert.assertEquals(expectedJson, responseJson, false);
  }

  @Test
  @DisplayName("POST /books - Given valid book data, when create, then returns created")
  void givenValidBookData_whenCreateBook_thenReturnsCreated() throws Exception {
    CreateBookRequest request = CreateBookRequest.builder() /* campos válidos */.build();

    mockMvc
        .perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/books/3"));
  }
}
```

### Errores de validación (ProblemDetail, RFC 9457)

Los errores se afirman con el formato `application/problem+json`:

```java
mockMvc
    .perform(post("/customers").contentType(MediaType.APPLICATION_JSON).content("{}"))
    .andExpect(status().isUnprocessableEntity())
    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
    .andExpect(jsonPath("$.type").value("about:blank"))
    .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
    .andExpect(jsonPath("$.status").value(422))
    .andExpect(jsonPath("$.detail").value("Invalid request data"))
    .andExpect(jsonPath("$.instance").value("/customers"))
    .andExpect(jsonPath("$.errors").isMap())
    .andExpect(jsonPath("$.errors.username").isNotEmpty());
```

### Casos negativos: 401, 403, 404 y 422

Dónde se prueba cada uno (el `standaloneSetup` de los `*ControllerTest` no tiene Spring Security):

| Código | Origen | `*ControllerTest` | `*ControllerIT` |
| --- | --- | --- | --- |
| `401` | `CustomAuthenticationEntryPoint` (ruta no pública sin token) | no aplica | sí |
| `403` | `@PreAuthorize` -> `AccessDeniedException` -> `GlobalExceptionHandler` | no aplica | sí |
| `404` | `ResourceNotFoundException` del servicio | sí (servicio mockeado con `thenThrow`) | sí (id inexistente, p. ej. `9999`) |
| `422` | `@Valid` -> `MethodArgumentNotValidException` | sí (`"{}"` + `verifyNoInteractions(service)`) | sí |

Reglas:

- Todos los errores se afirman con `application/problem+json` y los campos `status`, `title` e
  `instance`; `detail` solo cuando es estable (el texto de `messages.properties` en 404 y 422, no el
  mensaje interno de Spring Security en 401/403).
- **401**: sobrescribe la identidad por defecto de `defaultRequest` con `.with(anonymous())`.
- **403**: sobrescribe con otro `jwt()` con un rol que el endpoint no admite (`ROLE_App/Customer`
  en un endpoint de `App/Admin`). Envía un cuerpo **válido** en `POST`/`PUT` para demostrar que el
  rechazo es por autorización y no por validación.
- **404**: cubre cada verbo que busca por id (`GET`, `PUT`, `DELETE`).
- **422**: envía `"{}"` y afirma `$.errors.<campo>`. Las claves de `errors` son los nombres de campo
  Java en camelCase (`firstName`), no snake_case. En `*ControllerTest` afirma el mensaje exacto de la
  anotación; en el IT basta `isNotEmpty()`.
- Un helper privado evita repetir el JWT alterno:

```java
private static RequestPostProcessor customerJwt() {
  return jwt()
      .jwt(jwt -> jwt.claim("preferred_username", "customer"))
      .authorities(new SimpleGrantedAuthority("ROLE_App/Customer"));
}
```

```java
@Test
@DisplayName("POST /books - Given customer role, when create, then returns forbidden")
void givenCustomerRole_whenCreateBook_thenReturnsForbidden() throws Exception {
  mockMvc
      .perform(
          post("/books")
              .with(customerJwt())
              .contentType(MediaType.APPLICATION_JSON)
              .content(mapper.writeValueAsString(validCreateRequest())))
      .andExpect(status().isForbidden())
      .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
      .andExpect(jsonPath("$.status").value(403))
      .andExpect(jsonPath("$.title").value("Forbidden"))
      .andExpect(jsonPath("$.instance").value("/books"));
}
```

### WireMock (APIs externas)

```java
@EnableWireMock({@ConfigureWireMock(port = 8089)})
```

- `src/test/resources/mappings/<accion>-mapping.json`: define request (método, `url`, headers con
  `matches`) y response (`status`, headers, `bodyFileName`).
- `src/test/resources/__files/<accion>-response.json`: cuerpo devuelto.
- La URL base del servicio externo apunta a ese puerto en `application-test.properties`
  (`keycloak.admin.server-url=http://localhost:8089`).

Ejemplo de mapping:

```json
{
  "request": {
    "method": "POST",
    "url": "/admin/realms/fake-realm/users",
    "headers": { "Authorization": { "matches": "Bearer .*" } }
  },
  "response": {
    "status": 201,
    "headers": { "Location": "http://localhost:8089/admin/realms/fake-realm/users/418f2517-..." },
    "bodyFileName": "create-user-response.json"
  }
}
```

## 8. Helpers de `support/`

Los helpers existen para que `standaloneSetup` (sin contexto de Spring Boot) se comporte como la
aplicación real. Son clases `final` con constructor privado que lanza `IllegalAccessError`.

### `support/testutils/TestObjectMappers`

Replica el `ObjectMapper` de Spring Boot: SNAKE_CASE (`spring.jackson.property-naming-strategy`), el
mixin de `ProblemDetail` (forma plana RFC 9457), módulos `java.time` y `parameter-names` (necesario
para deserializar DTOs de Lombok `@Builder` sin constructor vacío).

```java
public static ObjectMapper snakeCase() {
  return Jackson2ObjectMapperBuilder.json()
      .findModulesViaServiceLoader(true)
      .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
      .build();
}
```

### `support/testutils/TestMessageSources`

Equivale al `MessageSource` que Boot autoconfigura, para que `GlobalExceptionHandler` resuelva
`messages.properties` en los tests standalone.

```java
public static MessageSource messages() {
  ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
  messageSource.setBasename("messages");
  messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
  return messageSource;
}
```

### `support/testutils/JsonUtils`

Lee `src/test/resources/responses/<path>` como UTF-8 y falla con un mensaje claro si no existe.

```java
public static String readJson(String path) {
  String resourcePath = "responses/" + path;

  try (InputStream inputStream =
      JsonUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {
    if (inputStream == null) {
      throw new IllegalArgumentException("Fixture not found on classpath: " + resourcePath);
    }

    return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
  } catch (IOException ex) {
    throw new UncheckedIOException("Failed to read fixture: " + resourcePath, ex);
  }
}
```

La ruta se pasa **relativa a `responses/` y sin `/` inicial**: `readJson("books/find-all.json")`.

### `support/security/JwtArgumentResolver`

`HandlerMethodArgumentResolver` que entrega un `Jwt` fijo a los controllers que lo reciben como
parámetro, sin necesidad de `SecurityContext`.

```java
public class JwtArgumentResolver implements HandlerMethodArgumentResolver {
  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return Jwt.class.isAssignableFrom(parameter.getParameterType());
  }

  @Override
  public Object resolveArgument(
      @NonNull MethodParameter parameter,
      @Nullable ModelAndViewContainer mavContainer,
      @NonNull NativeWebRequest webRequest,
      @Nullable WebDataBinderFactory binderFactory) {
    Instant issuedAt = Instant.now();
    Map<String, Object> headers = Map.of("alg", "none");
    Map<String, Object> claims = new HashMap<>();
    claims.put("preferred_username", "carroyom");
    claims.put("customer_id", 1L);

    return new Jwt("token-value", issuedAt, issuedAt.plusSeconds(3600), headers, claims);
  }
}
```

Adapta los claims a los que lea tu aplicación.

## 9. Perfil `test` (`src/test/resources/application-test.properties`)

```properties
# Datasource: H2 en memoria con nombre distinto al de desarrollo
spring.datasource.url=jdbc:h2:mem:it-testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.sql.init.mode=always
spring.sql.init.schema-locations=
spring.sql.init.data-locations=classpath:data.sql
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.defer-datasource-initialization=true

# Servicios externos apuntan a WireMock (puerto fijo 8089)
keycloak.admin.server-url=http://localhost:8089
keycloak.admin.realm=fake-realm
keycloak.admin.client-id=fake-client-id
keycloak.admin.client-secret=fake-client-secret
keycloak.admin.grant-type=client_credentials
```

Consecuencias que hay que tener presentes:

- En los ITs el esquema lo genera Hibernate (`create-drop`) y **no** `schema.sql`; solo se carga
  `data.sql`. Los mapeos de las entidades deben coincidir con `schema.sql` de desarrollo y
  `data.sql` debe cargarse sin errores contra el esquema generado por Hibernate.
- `defer-datasource-initialization=true` hace que `data.sql` corra **después** de que Hibernate cree
  las tablas.
- Los ids esperados en los tests (`Location: …/books/3`) dependen del seed de `data.sql`: si cambias
  el seed, actualiza los ITs y los fixtures.
- Nunca pongas secretos reales; usa valores `fake-*`.

## 10. Fixtures JSON de respuesta

- Ruta: `src/test/resources/responses/<module>/<caso>.json` (módulo en plural: `books`, `customers`).
- Nombre: `find-all.json`, `find-by-id.json`, `find-<hijos>-by-<padre>.json`
  (`find-authors-by-book.json`).
- Contenido: copia exacta de la respuesta esperada, **snake_case**, con `null` explícitos
  (`"deleted_at": null`) y timestamps fijos que provienen de `data.sql` (`2025-01-01T00:00:00`).
- Se comparan con `JSONAssert.assertEquals(expected, actual, false)` (modo no estricto: tolera campos
  extra en la respuesta y orden de campos, pero verifica los que aparecen en el fixture). Usa
  `true` (estricto) cuando necesites garantizar que no se filtren campos nuevos.
- Cuando cambie un DTO de respuesta, actualiza el fixture en el mismo commit.

## 11. Test de integridad de mensajes de error

`GlobalExceptionHandlerTest` incluye un test que recorre por reflexión las constantes públicas de
`MessageKeys` y exige que cada una tenga entrada en `messages.properties`:

```java
@Test
void givenMessagesKeys_whenResolve_thenEveryKeyHasEntryInMessagesProperties()
    throws IllegalAccessException {
  for (Field field : MessageKeys.class.getDeclaredFields()) {
    if (!Modifier.isPublic(field.getModifiers())) {
      continue;
    }
    String key = (String) field.get(null);

    String resolved = messageSource.getMessage(key, null, null, Locale.ENGLISH);

    assertThat(resolved).as("messages.properties entry for %s", key).isNotNull();
  }
}
```

Este test impide agregar una constante sin su texto. Mantén los demás casos del handler:
clave resuelta a texto, clave desconocida devuelta tal cual, e interpolación de argumentos
(`"Item {0} not found"` con `42` → `"Item 42 not found"`). Este test no usa Mockito ni Spring: se
instancia el handler a mano con `new MockHttpServletRequest("GET", "/books/1")`.

`<App>ApplicationTest` solo contiene `@SpringBootTest` + `void contextLoads() {}`.

## 12. Regla de mantenimiento: qué actualizar al cambiar comportamiento

| Cambio | Actualiza |
| --- | --- |
| Regla de negocio / excepción | `*ServiceTest` |
| Ruta, status, headers o forma del JSON | `*ControllerTest` |
| Comportamiento de punta a punta, seguridad, persistencia | `*ControllerIT` + fixture en `responses/` |
| Contrato público | `docs/openapi/api-docs.yaml` (o equivalente) |
| Nueva constante `MessageKeys` | `messages.properties` (lo verifica el test de la sección 11) |

## 13. Checklists

### Replicar la base en un proyecto nuevo

1. Añadir las dependencias de test y el plugin Failsafe (sección 2).
2. Crear el paquete `support/testutils` con `TestObjectMappers`, `TestMessageSources` y `JsonUtils`;
   crear `support/security/JwtArgumentResolver` si hay endpoints con `Jwt`.
3. Ajustar `TestObjectMappers` a la configuración Jackson real del proyecto (naming strategy,
   módulos) y `TestMessageSources` al basename de mensajes.
4. Crear `src/test/resources/application-test.properties` (sección 9) y el seed `data.sql`.
5. Registrar un bean `Clock` en el código de producción y usarlo en los servicios
   (`LocalDateTime.now(clock)`); sin esto no se pueden fijar timestamps en tests.
6. Crear `GlobalExceptionHandlerTest` y `<App>ApplicationTest`.
7. Crear los directorios `responses/`, `mappings/` y `__files/` (estos dos solo si hay WireMock).
8. Verificar con `./mvnw test` y `./mvnw verify`.

### Por cada feature / recurso nuevo

- [ ] `<X>ServiceTest`: camino feliz, not found, conflicto/duplicado, verificación de `save`.
- [ ] `<X>ControllerTest`: un test por endpoint con el contrato por verbo (sección 6), más 404 en los
      endpoints por id y 422 en cada `POST`/`PUT` con `@Valid`.
- [ ] `<X>ControllerIT`: lectura contra fixtures, escritura con status/Location, y los casos negativos
      de la sección 7: 401 (una vez por controller), 403 por cada endpoint protegido con rol, 404 por
      cada verbo que busca por id y 422 por cada `POST`/`PUT` con `@Valid`.
- [ ] Fixtures `responses/<x>/*.json` creados.
- [ ] Nombres `given…_when…_then…` y `@DisplayName` aplicados.
- [ ] `messages.properties` y `MessageKeys` sincronizados.

## 14. Deuda pendiente y errores a evitar

### Pendientes

1. **`JSONAssert` no estricto** (`false`) es deliberado para tolerar campos adicionales, pero también
   oculta regresiones por campos nuevos o filtrados. Decide por proyecto; usa estricto para DTOs
   sensibles.
2. **Los ITs no corren con `mvn test`.** Ejecuta `verify` en CI: una suite verde en `test` no dice
   nada sobre los `*IT`.
