package com.carlosarroyoam.rest.books.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando una operación en un sistema externo (p. ej. Keycloak) falla o deja un estado
 * inconsistente que no es atribuible al cliente. Se traduce a {@code 500 Internal Server Error}.
 */
public class InternalServerException extends ApplicationException {
  /**
   * Crea la excepción con la clave del mensaje y sus argumentos indicados.
   *
   * @param key la clave del mensaje en {@code messages.properties}
   * @param args los argumentos que reemplazan los placeholders del mensaje
   */
  public InternalServerException(String key, Object... args) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, key, args);
  }

  /**
   * Crea la excepción con la clave del mensaje, la causa que la originó y sus argumentos indicados.
   *
   * @param key la clave del mensaje en {@code messages.properties}
   * @param cause la excepción subyacente que provocó este fallo
   * @param args los argumentos que reemplazan los placeholders del mensaje
   */
  public InternalServerException(String key, Throwable cause, Object... args) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, key, cause, args);
  }
}
