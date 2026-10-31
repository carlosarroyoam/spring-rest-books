package com.carlosarroyoam.rest.books.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando la petición carece de credenciales válidas. Se traduce a {@code 401
 * Unauthorized}.
 */
public class UnauthorizedException extends ApplicationException {
  public UnauthorizedException(String key, Object... args) {
    super(HttpStatus.UNAUTHORIZED, key, args);
  }
}
