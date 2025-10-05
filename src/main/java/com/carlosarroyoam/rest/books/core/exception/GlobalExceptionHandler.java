package com.carlosarroyoam.rest.books.core.exception;

import com.carlosarroyoam.rest.books.core.exception.dto.AppExceptionDto;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler({ ResponseStatusException.class })
  public ResponseEntity<AppExceptionDto> handleResponseStatus(ResponseStatusException ex,
      WebRequest request) {
    return buildResponseEntity(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason(),
        request);
  }

  @ExceptionHandler({ MethodArgumentNotValidException.class })
  public ResponseEntity<AppExceptionDto> handleValidation(MethodArgumentNotValidException ex,
      WebRequest request) {
    Map<String, String> details = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage,
            (first, second) -> second));

    return buildResponseEntity(HttpStatus.BAD_REQUEST, "Invalid request data", request, details);
  }

  @ExceptionHandler({ AuthorizationDeniedException.class })
  public ResponseEntity<AppExceptionDto> handleAuthorizationDenied(AuthorizationDeniedException ex,
      WebRequest request) {
    return buildResponseEntity(HttpStatus.FORBIDDEN, ex.getMessage(), request);
  }

  @ExceptionHandler({ NoHandlerFoundException.class })
  public ResponseEntity<AppExceptionDto> handleNotFound(NoHandlerFoundException ex,
      WebRequest request) {
    return buildResponseEntity(HttpStatus.NOT_FOUND, "Endpoint not found", request);
  }

  @ExceptionHandler({ NoResourceFoundException.class })
  public ResponseEntity<AppExceptionDto> handleNoResourceFound(NoResourceFoundException ex,
      WebRequest request) {
    return buildResponseEntity(HttpStatus.NOT_FOUND, "Static resource not found", request);
  }

  @ExceptionHandler({ HttpRequestMethodNotSupportedException.class })
  public ResponseEntity<AppExceptionDto> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, WebRequest request) {
    return buildResponseEntity(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request);
  }

  @ExceptionHandler({ Exception.class })
  public ResponseEntity<AppExceptionDto> handleGenericException(Exception ex, WebRequest request) {
    log.error("Unhandled exception:", ex);
    return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Whoops! Something went wrong",
        request);
  }

  private ResponseEntity<AppExceptionDto> buildResponseEntity(HttpStatus status, String message,
      WebRequest request, Map<String, String> details) {
    AppExceptionDto appExceptionDto = AppExceptionDto.builder()
        .message(message)
        .error(status.getReasonPhrase())
        .status(status.value())
        .path(request.getDescription(false).replace("uri=", ""))
        .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
        .details(details)
        .build();

    return ResponseEntity.status(status).body(appExceptionDto);
  }

  private ResponseEntity<AppExceptionDto> buildResponseEntity(HttpStatus status, String message,
      WebRequest request) {
    return buildResponseEntity(status, message, request, null);
  }
}
