package com.carlosarroyoam.rest.books.core.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración del reloj de la aplicación. Los servicios obtienen la hora actual de este {@link
 * Clock} en lugar de llamar a {@code now()} sin argumentos, lo que permite fijarla en las pruebas.
 */
@Configuration
public class ClockConfig {
  /**
   * Reloj del sistema con la zona horaria por defecto de la JVM, equivalente al comportamiento de
   * {@code LocalDateTime.now()}.
   *
   * @return el {@link Clock} de la aplicación
   */
  @Bean
  Clock clock() {
    return Clock.systemDefaultZone();
  }
}
