package com.carlosarroyoam.rest.books.support.testutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

/**
 * Construye un {@link ObjectMapper} que replica {@code spring.jackson.property-naming-strategy}
 * (SNAKE_CASE) para usar en {@code MockMvc.standaloneSetup(...)}, donde no hay contexto de Spring
 * Boot que aplique esa configuracion automaticamente al {@code ObjectMapper} por defecto.
 */
public final class TestObjectMappers {
  private TestObjectMappers() {
    throw new IllegalAccessError("Illegal access to utility class");
  }

  /**
   * Crea un {@link ObjectMapper} en snake_case, con {@code findAndRegisterModules()} para descubrir
   * via SPI los mismos modulos que registra Spring Boot automaticamente (soporte de {@code
   * java.time.*} y, sobre todo, {@code jackson-module-parameter-names}, necesario para deserializar
   * DTOs con Lombok {@code @Builder} que no tienen constructor sin argumentos).
   *
   * @return el {@link ObjectMapper} configurado
   */
  public static ObjectMapper snakeCase() {
    return new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .findAndRegisterModules();
  }
}
