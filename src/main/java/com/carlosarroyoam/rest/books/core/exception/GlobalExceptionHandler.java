package com.carlosarroyoam.rest.books.core.exception;

import com.carlosarroyoam.rest.books.core.exception.dto.AppExceptionResponse;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler({ ResponseStatusException.class })
  public ResponseEntity<AppExceptionResponse> handleResponseStatus(ResponseStatusException ex,
      WebRequest request) {
    return buildResponseEntity(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason(),
        request);
  }

  @ExceptionHandler({ HttpMessageNotReadableException.class })
  public ResponseEntity<AppExceptionResponse> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex, WebRequest request) {
    return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  @ExceptionHandler({ MethodArgumentTypeMismatchException.class })
  public ResponseEntity<AppExceptionResponse> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex, WebRequest request) {
    return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  @ExceptionHandler({ NoHandlerFoundException.class })
  public ResponseEntity<AppExceptionResponse> handleNoHandlerFound(NoHandlerFoundException ex,
      WebRequest request) {
    return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  @ExceptionHandler({ NoResourceFoundException.class })
  public ResponseEntity<AppExceptionResponse> handleNoResourceFound(NoResourceFoundException ex,
      WebRequest request) {
    return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  @ExceptionHandler({ HttpRequestMethodNotSupportedException.class })
  public ResponseEntity<AppExceptionResponse> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, WebRequest request) {
    return buildResponseEntity(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request);
  }

  @ExceptionHandler({ AuthenticationException.class })
  public ResponseEntity<AppExceptionResponse> handleAuthenticationException(
      AuthenticationException ex, WebRequest request) {
    return buildResponseEntity(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
  }

  @ExceptionHandler({ AccessDeniedException.class })
  public ResponseEntity<AppExceptionResponse> handleAccessDeniedException(AccessDeniedException ex,
      WebRequest request) {
    return buildResponseEntity(HttpStatus.FORBIDDEN, ex.getMessage(), request);
  }

  @ExceptionHandler({ MethodArgumentNotValidException.class })
  public ResponseEntity<AppExceptionResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, WebRequest request) {
    Map<String, String> details = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage,
            (first, second) -> second));

    return buildResponseEntity(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid request data", request,
        details);
  }

  @ExceptionHandler({ Exception.class })
  public ResponseEntity<AppExceptionResponse> handleException(Exception ex, WebRequest request) {
    log.error("Unhandled exception:", ex);
    return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Whoops! Something went wrong",
        request);
  }

  private ResponseEntity<AppExceptionResponse> buildResponseEntity(HttpStatus status,
      String message, WebRequest request, Map<String, String> details) {
    AppExceptionResponse appExceptionDto = AppExceptionResponse.builder()
        .message(message)
        .error(status.getReasonPhrase())
        .status(status.value())
        .path(request.getDescription(false).replace("uri=", ""))
        .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
        .details(details)
        .build();

    return ResponseEntity.status(status).body(appExceptionDto);
  }

  private ResponseEntity<AppExceptionResponse> buildResponseEntity(HttpStatus status,
      String message, WebRequest request) {
    return buildResponseEntity(status, message, request, null);
  }
}
