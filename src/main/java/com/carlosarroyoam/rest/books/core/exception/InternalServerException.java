package com.carlosarroyoam.rest.books.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando una operación en un sistema externo (p. ej. Keycloak) falla o deja un estado
 * inconsistente que no es atribuible al cliente. Se traduce a {@code 500 Internal Server Error}.
 */
public class InternalServerException extends ApplicationException {
  public InternalServerException(String message) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, message);
  }

  public InternalServerException(String message, Throwable cause) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
  }
}
