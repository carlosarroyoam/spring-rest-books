package com.carlosarroyoam.rest.books.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando una petición es sintácticamente válida pero viola una regla de negocio. Se
 * traduce a {@code 422 Unprocessable Entity}.
 */
public class BusinessException extends ApplicationException {
  public BusinessException(String key, Object... args) {
    super(HttpStatus.UNPROCESSABLE_ENTITY, key, args);
  }
}
