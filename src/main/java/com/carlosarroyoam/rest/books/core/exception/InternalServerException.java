package com.carlosarroyoam.rest.books.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando una operación en un sistema externo (p. ej. Keycloak) falla o deja un estado
 * inconsistente que no es atribuible al cliente. Se traduce a {@code 500 Internal Server Error}.
 */
public class InternalServerException extends ApplicationException {
  /**
   * Crea la excepción con el mensaje descriptivo indicado.
   *
   * @param message el mensaje descriptivo del fallo
   */
  public InternalServerException(String message) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, message);
  }

  /**
   * Crea la excepción con el mensaje descriptivo indicado y la causa que la originó.
   *
   * @param message el mensaje descriptivo del fallo
   * @param cause la excepción subyacente que provocó este fallo
   */
  public InternalServerException(String message, Throwable cause) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
  }
}
