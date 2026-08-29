package com.carlosarroyoam.rest.books.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduce las excepciones no controladas de la aplicación a respuestas HTTP {@link ProblemDetail}
 * (RFC 9457, {@code application/problem+json}), delegando la construcción del cuerpo en {@link
 * ProblemDetailFactory}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private final ProblemDetailFactory problemDetailFactory;

  public GlobalExceptionHandler(ProblemDetailFactory problemDetailFactory) {
    this.problemDetailFactory = problemDetailFactory;
  }

  /**
   * Traduce una {@link ResponseStatusException} a una respuesta con el código de estado indicado en
   * la propia excepción.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return el {@link ProblemDetail} con el código de estado y mensaje de la excepción
   */
  @ExceptionHandler({ResponseStatusException.class})
  public ProblemDetail handleResponseStatus(
      ResponseStatusException ex, HttpServletRequest request) {
    return problemDetailFactory.build(ex.getStatusCode(), ex.getReason(), request);
  }

  /**
   * Traduce un cuerpo de petición ilegible o mal formado a {@code 400 Bad Request}.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return el {@link ProblemDetail} con estado {@code 400 Bad Request}
   */
  @ExceptionHandler({HttpMessageNotReadableException.class})
  public ProblemDetail handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    return problemDetailFactory.build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  /**
   * Traduce un parámetro de tipo incompatible con el esperado a {@code 400 Bad Request}.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return el {@link ProblemDetail} con estado {@code 400 Bad Request}
   */
  @ExceptionHandler({MethodArgumentTypeMismatchException.class})
  public ProblemDetail handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    return problemDetailFactory.build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  /**
   * Traduce una petición a una ruta sin handler registrado a {@code 404 Not Found}.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return el {@link ProblemDetail} con estado {@code 404 Not Found}
   */
  @ExceptionHandler({NoHandlerFoundException.class})
  public ProblemDetail handleNoHandlerFound(
      NoHandlerFoundException ex, HttpServletRequest request) {
    return problemDetailFactory.build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  /**
   * Traduce una petición a un recurso estático inexistente a {@code 404 Not Found}.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return el {@link ProblemDetail} con estado {@code 404 Not Found}
   */
  @ExceptionHandler({NoResourceFoundException.class})
  public ProblemDetail handleNoResourceFound(
      NoResourceFoundException ex, HttpServletRequest request) {
    return problemDetailFactory.build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  /**
   * Traduce el uso de un método HTTP no soportado por el endpoint a {@code 405 Method Not Allowed}.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return el {@link ProblemDetail} con estado {@code 405 Method Not Allowed}
   */
  @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
  public ProblemDetail handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    return problemDetailFactory.build(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request);
  }

  /**
   * Traduce un fallo de autenticación no capturado por {@link
   * com.carlosarroyoam.rest.books.core.security.CustomAuthenticationEntryPoint} a {@code 401
   * Unauthorized}.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return el {@link ProblemDetail} con estado {@code 401 Unauthorized}
   */
  @ExceptionHandler({AuthenticationException.class})
  public ProblemDetail handleAuthenticationException(
      AuthenticationException ex, HttpServletRequest request) {
    return problemDetailFactory.build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
  }

  /**
   * Traduce un fallo de autorización no capturado por {@link
   * com.carlosarroyoam.rest.books.core.security.CustomAccessDeniedHandler} a {@code 403 Forbidden}.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return el {@link ProblemDetail} con estado {@code 403 Forbidden}
   */
  @ExceptionHandler({AccessDeniedException.class})
  public ProblemDetail handleAccessDeniedException(
      AccessDeniedException ex, HttpServletRequest request) {
    return problemDetailFactory.build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
  }

  /**
   * Traduce errores de validación de {@code @Valid} a {@code 422 Unprocessable Entity}, incluyendo
   * el detalle de los mensajes de error por campo bajo la propiedad {@code errors}.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return el {@link ProblemDetail} con estado {@code 422 Unprocessable Entity} y el detalle por
   *     campo
   */
  @ExceptionHandler({MethodArgumentNotValidException.class})
  public ProblemDetail handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField,
                    FieldError::getDefaultMessage,
                    (existing, replacement) -> existing));

    return problemDetailFactory.build(
        HttpStatus.UNPROCESSABLE_ENTITY, "Invalid request data", request, errors);
  }

  /**
   * Captura cualquier excepción no controlada por los demás manejadores, la traduce a {@code 500
   * Internal Server Error} y registra el detalle en el log.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return el {@link ProblemDetail} con estado {@code 500 Internal Server Error}
   */
  @ExceptionHandler({Exception.class})
  public ProblemDetail handleException(Exception ex, HttpServletRequest request) {
    log.error("Whoops! Something went wrong: ", ex);

    return problemDetailFactory.build(
        HttpStatus.INTERNAL_SERVER_ERROR, "Whoops! Something went wrong", request);
  }
}
