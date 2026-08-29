package com.carlosarroyoam.rest.books.support.testutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Construye un {@link ObjectMapper} equivalente al que autoconfigura Spring Boot para usar en
 * {@code MockMvc.standaloneSetup(...)}, donde no hay contexto de Spring Boot que lo configure
 * automaticamente.
 */
public final class TestObjectMappers {
  private TestObjectMappers() {
    throw new IllegalAccessError("Illegal access to utility class");
  }

  /**
   * Crea un {@link ObjectMapper} via {@link Jackson2ObjectMapperBuilder}, igual que Spring Boot. El
   * builder: aplica {@code SNAKE_CASE} (replica {@code spring.jackson.property-naming-strategy});
   * registra {@code ProblemDetailJacksonMixin}, necesario para serializar {@link
   * org.springframework.http.ProblemDetail} en la forma plana de RFC 9457; y descubre via SPI los
   * mismos modulos que registra Spring Boot ({@code java.time.*} y {@code
   * jackson-module-parameter-names}, este ultimo necesario para deserializar DTOs con Lombok {@code
   * @Builder} que no tienen constructor sin argumentos).
   *
   * @return el {@link ObjectMapper} configurado
   */
  public static ObjectMapper snakeCase() {
    return Jackson2ObjectMapperBuilder.json()
        .findModulesViaServiceLoader(true)
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build();
  }
}
