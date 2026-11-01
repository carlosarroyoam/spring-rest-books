# Convenciones de Javadoc para proyectos Spring Boot REST

Guía extraída de `spring-rest-books` (Java 17, Spring Boot 3.5, Spring Data JPA, MapStruct, Spring
Security). Describe cómo se escribe Javadoc en el proyecto y fija una regla donde el código actual
es inconsistente. Los ejemplos son fragmentos reales del proyecto, con `Author` como entidad de
ejemplo.

Convención de lectura:

- **Regla** = se aplica a todo el código nuevo del proyecto.
- **Evitar** = existe en el código actual, pero es una deuda; no se debe replicar (ver
  [sección 12](#12-desviaciones-conocidas-no-replicar-tal-cual)).

Este documento describe las convenciones; no obliga a reescribir el código existente.

---

## 1. Idioma

**Regla**: todo el Javadoc se escribe en **español**, con tildes y signos de puntuación correctos.
Identificadores, mensajes de log, textos de error (`messages.properties`) y `@DisplayName` de los
tests van en **inglés**.

**Evitar**: omitir tildes ("automaticamente", "via", "modulos"). Hoy solo ocurre en el Javadoc de
`TestMessageSources` y `TestObjectMappers`.

## 2. Qué se documenta y qué no

| Elemento | ¿Javadoc? | Notas |
| --- | --- | --- |
| Clases, interfaces, enums, records | Sí | Siempre. Una línea basta si lo dice todo. |
| Controllers, servicios, repositorios | Sí | Clase y todos los métodos propios (incluidos los privados). |
| Entidades | Sí (clase) | Los campos no se documentan. |
| Métodos `@Bean`, `@Override` | Sí | Sin `{@inheritDoc}`: se escribe la descripción real. |
| Métodos privados con lógica | Sí | Por ejemplo `find<Entity>ById`, `calculate*`, `resolve*`. |
| Constructor privado de clase de utilidad | Sí | "Constructor privado que impide instanciar la clase." |
| Clases de configuración, filtros, handlers, propiedades | Sí | Ver [sección 9](#9-infraestructura-excepciones-configuración-filtros). |
| DTOs (`Create*Request`, `Update*Request`, `*Response`, `*Specs`) | Sí (clase) | Una línea; los campos no se documentan: el nombre y la validación ya describen el contrato. |
| Mappers MapStruct anidados en DTOs | No | Salvo que tengan lógica no obvia. |
| Campos de entidades, constantes de enum, constantes de `MessageKeys` | No | |
| Constructores de inyección y de excepciones | No | Salvo los de `ApplicationException`/`InternalServerException`. |
| Métodos `@Test` | No | Se describen con el nombre `given<State>_when<Action>_then<Outcome>`. |

## 3. Primera frase

**Regla**: la primera frase resume el propósito, termina en punto y cabe en una línea o dos.

- Métodos: tercera persona, presente de indicativo. Verbos habituales: `Busca`, `Lista`, `Crea`,
  `Actualiza`, `Elimina lógicamente`, `Registra`, `Calcula`, `Construye`, `Resuelve`, `Traduce`.
- Tipos: sustantivo o fórmula fija según el rol (tabla siguiente).

| Tipo | Fórmula | Ejemplo |
| --- | --- | --- |
| Controller | "Expone las operaciones REST sobre {@link X}: ..." | `AuthorController` |
| Servicio | "Contiene la lógica de negocio de {@link X}: ..." | `AuthorService` |
| Repositorio | "Acceso a datos de {@link X} mediante Spring Data JPA." | `AuthorRepository` |
| Entidad | "Representa ..." | `Author` |
| DTO | Request: "Datos requeridos para ..."; Response: "Representación de ... devuelta por la API."; Specs: "Filtros opcionales para listar ..., enlazados desde los parámetros de consulta." | `CreateAuthorRequest` |
| Enum de estados | "Estados posibles de un {@link X}." | `AuthorStatus` |
| Excepción | "Se lanza cuando ... Se traduce a {@code 404 Not Found}." | `ResourceNotFoundException` |
| Filtro / propiedades / constantes | Sustantivo: "Filtro que ...", "Propiedades de ...", "Constantes ..." | `MdcUserContextFilter` |

```java
/**
 * Expone las operaciones REST sobre {@link com.carlosarroyoam.rest.books.author.entity.Author}:
 * listado paginado con filtros, consulta por id, alta, actualización, baja lógica y consulta de
 * libros asociados.
 */
```

```java
/** Se lanza cuando una entidad solicitada no existe. Se traduce a {@code 404 Not Found}. */
```

Vocabulario fijo para operaciones CRUD, para que el mismo concepto se lea igual en todos los módulos:

| Operación | Redacción |
| --- | --- |
| Lookup por id | "Busca un autor por su id." |
| Listado paginado | "Lista autores de forma paginada, aplicando los filtros de {@link AuthorSpecs}." |
| Alta | "Crea un nuevo autor." |
| Actualización | "Actualiza el nombre y la biografía de un autor." |
| Baja | Controller: "Elimina lógicamente un autor." Servicio: ver [sección 7](#7-servicios-reglas-de-negocio-y-errores). |

## 4. Formato

**Regla**:

- `/** texto */` en una sola línea si todo cabe (entidades simples, repositorios, enums, constructores
  privados). Si no, bloque multilínea con ` * ` y una línea en blanco antes de los tags.
- Máximo 100 columnas, al estilo google-java-format. El build no lo verifica; se mantiene a mano.
- `<p>` solo para separar párrafos en Javadoc de infraestructura largo (ejemplo:
  `CorrelationIdFilter`, `MdcUserContextFilter`). No se usan `<ul>`, `<pre>` ni `<b>`.
- Una sola frase de contexto adicional va a continuación de la primera, en el mismo párrafo.

## 5. Tags

**Regla**:

| Tag | Uso |
| --- | --- |
| `@param` | En todos los métodos con parámetros de controllers, servicios y utilidades. |
| `@return` | En todo método que devuelve valor. Se omite en `void` y en `ResponseEntity<Void>` de `update`/`deleteById`. En `create` se mantiene: "respuesta 201 con la cabecera {@code Location} del autor creado". |
| `@param <T>` | En clases y métodos genéricos, para **cada** parámetro de tipo. |
| `{@code}`, `{@link}` | Ver [sección 6](#6-code-y-link). |
| **No se usan** | `@author`, `@since`, `@version`, `@see`, `@throws`. |

Redacción de `@param` y `@return`: frase nominal en **minúscula**, sin punto final y sin guion.

```java
/**
 * Lista autores de forma paginada, aplicando los filtros de {@link AuthorSpecs}.
 *
 * @param authorSpecs filtros opcionales de búsqueda
 * @param pageable configuración de página y orden
 * @return la página de autores encontrados
 */
```

Los `@param` de objetos de infraestructura se redactan igual en todo el proyecto:

- `Jwt jwt` → "token del cliente autenticado"
- `UriComponentsBuilder builder` → "utilizado para construir la URI del recurso creado"
- `Pageable pageable` → "configuración de página y orden"
- `*Specs` → "filtros opcionales de búsqueda"

**`@throws`**: no se usa. Los errores se describen en prosa dentro de la descripción (ver
[sección 7](#7-servicios-reglas-de-negocio-y-errores)).

## 6. `{@code}` y `{@link}`

**Regla**:

- `{@code ...}` para: valores de enum (`{@code ACTIVE}`, `{@code DELETED}`), estados HTTP
  (`{@code 404 Not Found}`), propiedades y prefijos (`{@code app.cors}`, `{@code messages.properties}`),
  roles (`{@code App/Admin}`), headers (`{@code Location}`), anotaciones (`{@code @Valid}`) y
  literales (`{@code null}`, `{@code true}`).
- `{@link ...}` para tipos del dominio y de Spring que existen en el classpath, y para miembros
  (`{@link #TAX_RATE}`, `{@link CustomClaimNames#CUSTOMER_ID}`).
- Preferir el nombre corto con `import`. Usar el nombre completo solo si evita un import cíclico o
  innecesario solo para el Javadoc.
- Procurar no partir un `{@code ...}` entre dos líneas. Si el formateador lo parte, sigue siendo
  válido, pero se prefiere reescribir la frase.

## 7. Servicios: reglas de negocio y errores

**Regla**: el Javadoc del servicio explica las reglas de negocio que el nombre del método no dice.

Baja lógica (siempre con esta redacción):

```java
/**
 * Marca un autor como {@code DELETED} y registra la fecha de baja, sin eliminar el registro.
 *
 * @param authorId id del autor a eliminar
 */
```

Otras reglas que se mencionan en prosa:

- Unicidad: "rechazando ISBN duplicados."
- Transiciones de estado: "Registra el pago de una orden con estado {@code COMPLETED}, marca la
  orden como {@code CONFIRMED} y crea su envío si todavía no existe."
- Estado inicial: "Crea un autor con estado {@code ACTIVE}."
- Compensación entre sistemas: "Si la provisión en Keycloak falla ..., el alta del cliente se
  revierte por completo."

**Errores**: se describen en prosa, nombrando el estado HTTP y no la clase ni la clave de
`MessageKeys`:

```java
/**
 * Busca un autor por id o lanza {@code 404 Not Found} si no existe.
 *
 * @param authorId id del autor a buscar
 * @return el autor encontrado
 */
private Author findAuthorById(Long authorId) { ... }
```

**Evitar**: dejar sin mención de error los métodos públicos de servicio que pueden lanzar
`ResourceNotFoundException` o `ResourceAlreadyExistsException`; la regla es mencionarlo en la
descripción cuando sea relevante para quien llama.

## 8. Controllers: HTTP y autorización

**Regla**:

- No repetir el verbo ni la ruta: ya están en `@GetMapping`, `@PostMapping`, etc.
- La autorización se documenta **por método**, como segunda frase de la descripción, con la
  redacción exacta `Requiere rol {@code App/Admin}.` (o `App/Customer`). El Javadoc de la clase no
  repite el rol.
- Los endpoints públicos se declaran como tales: "Endpoint público, sin autenticación requerida."
- `create` documenta el `201` con `Location` en el `@return`.
- La autorización real vive en `@PreAuthorize("hasRole('App/Admin')")` o
  `@PreAuthorize("hasRole('App/Customer')")`; el Javadoc debe coincidir con la anotación.

```java
/**
 * Crea un nuevo autor. Requiere rol {@code App/Admin}.
 *
 * @param request datos del autor a crear
 * @param builder utilizado para construir la URI del recurso creado
 * @return respuesta 201 con la cabecera {@code Location} del autor creado
 */
@PostMapping(consumes = "application/json")
@PreAuthorize("hasRole('App/Admin')")
public ResponseEntity<Void> create(...)
```

```java
/**
 * Actualiza el nombre y la biografía de un autor. Requiere rol {@code App/Admin}.
 *
 * @param authorId id del autor a actualizar
 * @param request nuevos datos del autor
 */
```

## 9. Infraestructura: excepciones, configuración, filtros

**Regla**:

- **Excepciones de dominio**: "Se lanza cuando ... Se traduce a {@code <status>}." Una sola línea.
- **Configuración y propiedades**: "Propiedades de ... (prefijo {@code app.cors})." / "Configuración
  del reloj ...".
- **Filtros y handlers**: primer párrafo con el qué y el cuándo; segundo párrafo (`<p>`) con
  decisiones de diseño no obvias (por qué no es `@Component`, qué casos no cubre). Ejemplo:
  `MdcUserContextFilter`, `CorrelationIdFilter`.
- **Clases de constantes**: "Constantes ..." / "Nombres de los claims ...". Constructor privado con la
  redacción estándar. `MessageKeys` explica dónde vive el texto: "El texto de cada clave vive en
  {@code messages.properties} y lo resuelve {@link GlobalExceptionHandler}."

```java
/** Constructor privado que impide instanciar la clase. */
private MessageKeys() {}
```

## 10. Código de test

**Regla**:

- Las clases de apoyo de `support/testutils` (`JsonUtils`, `TestMessageSources`,
  `TestObjectMappers`) llevan Javadoc con la misma estructura (primera frase + `@param`/`@return`).
- Los `@Test` no llevan Javadoc: el nombre `given<State>_when<Action>_then<Outcome>` y
  `@DisplayName` en inglés describen el caso. Ver `docs/testing-conventions.md`.
- Las clases `*Test`/`*IT` no requieren Javadoc.

## 11. Checklist de revisión

Antes de abrir un PR con código nuevo:

- [ ] La primera frase está en español, en tercera persona y presente, y termina en punto.
- [ ] Cada método de controller/servicio tiene `@param` y `@return` (salvo `void` y
      `ResponseEntity<Void>` de `update`/`deleteById`).
- [ ] Cada parámetro de tipo genérico tiene `@param <X>`.
- [ ] Los endpoints protegidos terminan su descripción con `Requiere rol {@code ...}.` y coinciden con
      `@PreAuthorize`; los públicos lo dicen.
- [ ] No hay `@author`, `@since`, `@see` ni `@throws`.
- [ ] Los errores esperados se mencionan en prosa con el estado HTTP.
- [ ] Los enlaces `{@link}` resuelven (el IDE no marca avisos en el Javadoc).
- [ ] Si agregaste un nuevo módulo, tu controller/servicio/repositorio siguen las fórmulas de la
      [sección 3](#3-primera-frase).

## 12. Desviaciones conocidas (no replicar tal cual)

Detectadas al revisar el código actual. No son bugs de Javadoc salvo que se indique; son
inconsistencias que esta guía corrige para el código nuevo.

1. **Rol solo a nivel de clase** en `OrderController`, `PaymentController` y `ShipmentController`
   ("Todas las operaciones requieren rol {@code App/Admin}."). La regla es por método
   ([sección 8](#8-controllers-http-y-autorización)).
2. **`CustomerController.update`** no tiene `@PreAuthorize` ni nota de autorización, a diferencia de
   los demás endpoints del controller. Se señala aquí por coherencia de documentación; revisar si es
   intencional.
3. **GET públicos sin marcar** en `AuthorController` y `BookController`, mientras que
   `CustomerController.create` sí dice "Endpoint público, sin autenticación requerida.".
4. **Verbo distinto para la misma operación**: "Lista autores de forma paginada" (controller) frente a
   "Busca autores de forma paginada" (servicio). Para `findAll` se prefiere "Lista" en ambas capas.
5. **Constructores de excepciones**: solo `ApplicationException` e `InternalServerException` los
   documentan; el resto no.
6. **`@param <Y>` ausente** en `equalsIfPresent`, `betweenIfPresent` e `inIfPresent` de
   `SpecificationBuilder` (`<T>` sí está documentado).
7. **Constructor privado**: `CustomClaimNames` usa "Constructor privado: clase de constantes no
   instanciable."; el resto usa "Constructor privado que impide instanciar la clase.". Usar la
   segunda.
8. **Tildes ausentes** en `TestMessageSources` y `TestObjectMappers`.
9. **Ejemplo obsoleto** en `JsonUtils.readJson`: `{@code "users/find-by-id.json"}`; no existe
   `responses/users/`. Los directorios reales son `authors`, `books`, `carts`, `customers`, `orders`,
   `payments`, `shipments`.
10. **Descripción que no coincide con la implementación**: `CartController.findByUsername` dice
    "Consulta el carrito del cliente autenticado" pero busca por el claim `customer_id`, no por
    username. Los métodos `findBookAuthors` de `AuthorController` y `BookController` tienen nombre
    ambiguo respecto a lo que devuelven.
11. **Mappers anidados**: solo `OrderResponse.OrderResponseMapper` y `PagedResponse.PagedResponseMapper`
    tienen Javadoc; el resto no. La regla es no documentarlos salvo lógica no obvia
    ([sección 2](#2-qué-se-documenta-y-qué-no)).
12. **Texto obsoleto en `CLAUDE.md`** (corregido junto con este documento): hacía referencia a
    `UserService`/`AuthService`, `@RolesAllowed` y `@throws`, que no existen o no se usan.
