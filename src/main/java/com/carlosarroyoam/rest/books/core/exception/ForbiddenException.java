package com.carlosarroyoam.rest.books.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando el usuario autenticado no tiene permiso para la operación solicitada. Se traduce
 * a {@code 403 Forbidden}.
 */
public class ForbiddenException extends ApplicationException {
  public ForbiddenException(String message) {
    super(HttpStatus.FORBIDDEN, message);
  }
}
