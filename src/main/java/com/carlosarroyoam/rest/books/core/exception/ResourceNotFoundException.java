package com.carlosarroyoam.rest.books.core.exception;

import org.springframework.http.HttpStatus;

/** Se lanza cuando una entidad solicitada no existe. Se traduce a {@code 404 Not Found}. */
public class ResourceNotFoundException extends ApplicationException {
  public ResourceNotFoundException(String message) {
    super(HttpStatus.NOT_FOUND, message);
  }
}
