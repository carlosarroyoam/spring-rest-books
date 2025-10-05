package com.carlosarroyoam.rest.books.core.exception;

import com.carlosarroyoam.rest.books.core.exception.dto.AppExceptionDto;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<AppExceptionDto> handleResponseStatusException(ResponseStatusException ex,
      WebRequest request) {
    HttpStatus statusCode = HttpStatus.valueOf(ex.getStatusCode().value());

    AppExceptionDto appExceptionDto = AppExceptionDto.builder()
        .message(ex.getReason())
        .error(statusCode.getReasonPhrase())
        .status(statusCode.value())
        .path(request.getDescription(false).replace("uri=", ""))
        .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
        .build();

    return new ResponseEntity<>(appExceptionDto, ex.getStatusCode());
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<AppExceptionDto> handleNoHandlerFoundException(NoHandlerFoundException ex,
      WebRequest request) {
    HttpStatus statusCode = HttpStatus.NOT_FOUND;

    AppExceptionDto appExceptionDto = AppExceptionDto.builder()
        .message("No endpoint found for: " + request.getDescription(false).replace("uri=", ""))
        .error(statusCode.getReasonPhrase())
        .status(statusCode.value())
        .path(request.getDescription(false).replace("uri=", ""))
        .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
        .build();

    return new ResponseEntity<>(appExceptionDto, statusCode);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<AppExceptionDto> handleNoResourceFoundException(NoResourceFoundException ex,
      WebRequest request) {
    HttpStatus statusCode = HttpStatus.NOT_FOUND;

    AppExceptionDto appExceptionDto = AppExceptionDto.builder()
        .message("No static resource: " + request.getDescription(false).replace("uri=", ""))
        .error(statusCode.getReasonPhrase())
        .status(statusCode.value())
        .path(request.getDescription(false).replace("uri=", ""))
        .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
        .build();

    return new ResponseEntity<>(appExceptionDto, statusCode);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<AppExceptionDto> handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException ex, WebRequest request) {
    HttpStatus statusCode = HttpStatus.BAD_REQUEST;

    AppExceptionDto appExceptionDto = AppExceptionDto.builder()
        .message(ex.getMessage())
        .error(statusCode.getReasonPhrase())
        .status(statusCode.value())
        .path(request.getDescription(false).replace("uri=", ""))
        .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
        .build();

    return new ResponseEntity<>(appExceptionDto, statusCode);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<AppExceptionDto> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, WebRequest request) {
    HttpStatus statusCode = HttpStatus.BAD_REQUEST;

    AppExceptionDto appExceptionDto = AppExceptionDto.builder()
        .message("Request data is not valid")
        .error(statusCode.getReasonPhrase())
        .status(statusCode.value())
        .path(request.getDescription(false).replace("uri=", ""))
        .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
        .details(ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage,
                (first, second) -> second)))
        .build();

    return new ResponseEntity<>(appExceptionDto, statusCode);
  }

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<AppExceptionDto> handleAuthorizationDeniedException(
      AuthorizationDeniedException ex, WebRequest request) {
    HttpStatus statusCode = HttpStatus.FORBIDDEN;

    AppExceptionDto appExceptionDto = AppExceptionDto.builder()
        .message(ex.getMessage())
        .error(statusCode.getReasonPhrase())
        .status(statusCode.value())
        .path(request.getDescription(false).replace("uri=", ""))
        .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
        .build();

    return new ResponseEntity<>(appExceptionDto, statusCode);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<AppExceptionDto> handleAllException(Exception ex, WebRequest request) {
    HttpStatus statusCode = HttpStatus.INTERNAL_SERVER_ERROR;

    AppExceptionDto appExceptionDto = AppExceptionDto.builder()
        .message("Whoops! Something went wrong")
        .error(statusCode.getReasonPhrase())
        .status(statusCode.value())
        .path(request.getDescription(false).replace("uri=", ""))
        .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
        .build();

    log.error("Exception:", ex);

    return new ResponseEntity<>(appExceptionDto, statusCode);
  }
}
