package com.carlosarroyoam.rest.books.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza al intentar crear un recurso cuya clave única ya existe (ISBN, nombre de usuario, correo
 * electrónico, pago de una orden). Se traduce a {@code 409 Conflict}.
 */
public class ResourceAlreadyExistsException extends ApplicationException {
  public ResourceAlreadyExistsException(String message) {
    super(HttpStatus.CONFLICT, message);
  }
}
