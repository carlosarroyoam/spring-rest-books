package com.carlosarroyoam.rest.books.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando la validación semántica de los datos de la petición falla, de forma coherente con
 * la validación de Bean Validation. Se traduce a {@code 422 Unprocessable Entity}.
 */
public class ValidationException extends ApplicationException {
  public ValidationException(String message) {
    super(HttpStatus.UNPROCESSABLE_ENTITY, message);
  }
}
