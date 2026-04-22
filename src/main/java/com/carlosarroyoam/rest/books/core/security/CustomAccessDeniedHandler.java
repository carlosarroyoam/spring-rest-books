package com.carlosarroyoam.rest.books.core.security;

import com.carlosarroyoam.rest.books.core.exception.ApiExceptionResponseFactory;
import com.carlosarroyoam.rest.books.core.exception.dto.AppExceptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
  private final ApiExceptionResponseFactory apiExceptionResponseFactory;
  private final ObjectMapper mapper;

  public CustomAccessDeniedHandler(
      ApiExceptionResponseFactory apiExceptionResponseFactory, ObjectMapper mapper) {
    this.apiExceptionResponseFactory = apiExceptionResponseFactory;
    this.mapper = mapper;
  }

  @Override
  public void handle(
      HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
      throws IOException {
    HttpStatus status = HttpStatus.FORBIDDEN;
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getMessage(), request);

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    mapper.writeValue(response.getOutputStream(), appExceptionResponse);
  }
}
