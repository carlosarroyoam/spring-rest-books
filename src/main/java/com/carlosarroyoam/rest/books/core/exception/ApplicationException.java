package com.carlosarroyoam.rest.books.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base de las excepciones de dominio/aplicación. Cada subclase fija el código de estado HTTP con el
 * que {@link GlobalExceptionHandler} traduce el error a una respuesta {@link
 * org.springframework.http.ProblemDetail} (RFC 9457). {@link #getMessage()} devuelve el texto plano
 * que se expone como {@code detail}, sin decoración de estado.
 */
@Getter
public abstract class ApplicationException extends RuntimeException {
  private final HttpStatus status;

  /**
   * Crea la excepción con el estado HTTP y el mensaje descriptivo indicados.
   *
   * @param status el estado HTTP con el que debe responderse
   * @param message el mensaje descriptivo del fallo
   */
  protected ApplicationException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }

  /**
   * Crea la excepción con el estado HTTP, mensaje descriptivo y causa indicados.
   *
   * @param status el estado HTTP con el que debe responderse
   * @param message el mensaje descriptivo del fallo
   * @param cause la causa del fallo
   */
  protected ApplicationException(HttpStatus status, String message, Throwable cause) {
    super(message, cause);
    this.status = status;
  }
}
