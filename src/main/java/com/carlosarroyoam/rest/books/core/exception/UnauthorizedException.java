package com.carlosarroyoam.rest.books.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando la petición carece de credenciales válidas. Se traduce a {@code 401
 * Unauthorized}.
 */
public class UnauthorizedException extends ApplicationException {
  public UnauthorizedException(String message) {
    super(HttpStatus.UNAUTHORIZED, message);
  }
}
