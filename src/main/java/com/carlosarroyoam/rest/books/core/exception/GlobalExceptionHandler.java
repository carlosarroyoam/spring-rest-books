package com.carlosarroyoam.rest.books.core.exception;

import com.carlosarroyoam.rest.books.core.exception.dto.AppExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * Traduce las excepciones no controladas de la aplicación a respuestas HTTP consistentes con
 * {@link AppExceptionResponse}, delegando la construcción del cuerpo en {@link
 * ApiExceptionResponseFactory}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private final ApiExceptionResponseFactory apiExceptionResponseFactory;

  public GlobalExceptionHandler(ApiExceptionResponseFactory apiExceptionResponseFactory) {
    this.apiExceptionResponseFactory = apiExceptionResponseFactory;
  }

  /**
   * Traduce una {@link ResponseStatusException} a una respuesta con el código de estado indicado
   * en la propia excepción.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return la respuesta de error con el código de estado y mensaje de la excepción
   */
  @ExceptionHandler({ResponseStatusException.class})
  public ResponseEntity<AppExceptionResponse> handleResponseStatus(
      ResponseStatusException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getReason(), request);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }

  /**
   * Traduce un cuerpo de petición ilegible o mal formado a {@code 400 Bad Request}.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return la respuesta de error con estado {@code 400 Bad Request}
   */
  @ExceptionHandler({HttpMessageNotReadableException.class})
  public ResponseEntity<AppExceptionResponse> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getMessage(), request);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }

  /**
   * Traduce un parámetro de tipo incompatible con el esperado a {@code 400 Bad Request}.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return la respuesta de error con estado {@code 400 Bad Request}
   */
  @ExceptionHandler({MethodArgumentTypeMismatchException.class})
  public ResponseEntity<AppExceptionResponse> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getMessage(), request);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }

  /**
   * Traduce una petición a una ruta sin handler registrado a {@code 404 Not Found}.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return la respuesta de error con estado {@code 404 Not Found}
   */
  @ExceptionHandler({NoHandlerFoundException.class})
  public ResponseEntity<AppExceptionResponse> handleNoHandlerFound(
      NoHandlerFoundException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getMessage(), request);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }

  /**
   * Traduce una petición a un recurso estático inexistente a {@code 404 Not Found}.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return la respuesta de error con estado {@code 404 Not Found}
   */
  @ExceptionHandler({NoResourceFoundException.class})
  public ResponseEntity<AppExceptionResponse> handleNoResourceFound(
      NoResourceFoundException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getMessage(), request);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }

  /**
   * Traduce el uso de un método HTTP no soportado por el endpoint a {@code 405 Method Not
   * Allowed}.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return la respuesta de error con estado {@code 405 Method Not Allowed}
   */
  @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
  public ResponseEntity<AppExceptionResponse> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getMessage(), request);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }

  /**
   * Traduce un fallo de autenticación no capturado por {@link
   * com.carlosarroyoam.rest.books.core.security.CustomAuthenticationEntryPoint} a {@code 401
   * Unauthorized}.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return la respuesta de error con estado {@code 401 Unauthorized}
   */
  @ExceptionHandler({AuthenticationException.class})
  public ResponseEntity<AppExceptionResponse> handleAuthenticationException(
      AuthenticationException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.UNAUTHORIZED;
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getMessage(), request);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }

  /**
   * Traduce un fallo de autorización no capturado por {@link
   * com.carlosarroyoam.rest.books.core.security.CustomAccessDeniedHandler} a {@code 403
   * Forbidden}.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return la respuesta de error con estado {@code 403 Forbidden}
   */
  @ExceptionHandler({AccessDeniedException.class})
  public ResponseEntity<AppExceptionResponse> handleAccessDeniedException(
      AccessDeniedException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.FORBIDDEN;
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getMessage(), request);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }

  /**
   * Traduce errores de validación de {@code @Valid} a {@code 422 Unprocessable Entity}, incluyendo
   * el detalle de los mensajes de error por campo.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return la respuesta de error con estado {@code 422 Unprocessable Entity} y el detalle por
   *     campo
   */
  @ExceptionHandler({MethodArgumentNotValidException.class})
  public ResponseEntity<AppExceptionResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
    Map<String, String> details =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField,
                    FieldError::getDefaultMessage,
                    (existing, replacement) -> existing));

    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, "Invalid request data", request, details);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }

  /**
   * Captura cualquier excepción no controlada por los demás manejadores, la traduce a {@code 500
   * Internal Server Error} y registra el detalle en el log.
   *
   * @param ex excepción capturada
   * @param request petición HTTP en curso
   * @return la respuesta de error con estado {@code 500 Internal Server Error}
   */
  @ExceptionHandler({Exception.class})
  public ResponseEntity<AppExceptionResponse> handleException(
      Exception ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, "Whoops! Something went wrong", request);

    log.error("Whoops! Something went wrong: ", ex);

    return ResponseEntity.status(status).body(appExceptionResponse);
  }
}
