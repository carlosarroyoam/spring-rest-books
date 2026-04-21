package com.carlosarroyoam.rest.books.core.security;

import com.carlosarroyoam.rest.books.core.exception.dto.AppExceptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
  private final ObjectMapper mapper;

  public CustomAccessDeniedHandler(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException ex) throws IOException {
    HttpStatus status = HttpStatus.FORBIDDEN;
    AppExceptionResponse appExceptionDto = AppExceptionResponse.builder()
        .message(ex.getMessage())
        .error(status.getReasonPhrase())
        .status(status.value())
        .path(resolvePath(request))
        .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
        .build();

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    mapper.writeValue(response.getOutputStream(), appExceptionDto);
  }

  private String resolvePath(HttpServletRequest request) {
    Object uri = request.getAttribute("jakarta.servlet.error.request_uri");
    return uri != null ? uri.toString() : request.getRequestURI();
  }
}
