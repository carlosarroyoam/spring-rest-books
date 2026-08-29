package com.carlosarroyoam.rest.books.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando una petición entra en conflicto con el estado actual del recurso. Se traduce a
 * {@code 409 Conflict}.
 */
public class ConflictException extends ApplicationException {
  public ConflictException(String message) {
    super(HttpStatus.CONFLICT, message);
  }
}
