package com.carlosarroyoam.rest.books;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada de la aplicación Spring Boot del servicio de Libros. */
@SpringBootApplication
public class BookServiceApplication {
  /**
   * Arranca el contexto de Spring Boot.
   *
   * @param args argumentos de línea de comandos
   */
  public static void main(String[] args) {
    SpringApplication.run(BookServiceApplication.class, args);
  }
}
