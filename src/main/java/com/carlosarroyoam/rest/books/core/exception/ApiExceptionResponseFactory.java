package com.carlosarroyoam.rest.books.core.exception;

import com.carlosarroyoam.rest.books.core.exception.dto.AppExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ApiExceptionResponseFactory {
  public AppExceptionResponse build(HttpStatus status, String message, HttpServletRequest request) {
    return build(status, message, request, null);
  }

  public AppExceptionResponse build(
      HttpStatus status, String message, HttpServletRequest request, Map<String, String> details) {
    return AppExceptionResponse.builder()
        .message(message)
        .error(status.getReasonPhrase())
        .status(status.value())
        .path(resolvePath(request))
        .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
        .details(details)
        .build();
  }

  private String resolvePath(HttpServletRequest request) {
    Object uri = request.getAttribute("jakarta.servlet.error.request_uri");
    return uri != null ? uri.toString() : request.getRequestURI();
  }
}
