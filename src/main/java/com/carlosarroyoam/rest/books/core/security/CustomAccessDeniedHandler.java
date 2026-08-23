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

/**
 * Traduce un {@link AccessDeniedException} a una respuesta {@code 403 Forbidden} con el cuerpo de
 * {@link AppExceptionResponse}, ya que Spring Security no pasa este tipo de excepción por {@link
 * com.carlosarroyoam.rest.books.core.exception.GlobalExceptionHandler}.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
  private final ApiExceptionResponseFactory apiExceptionResponseFactory;
  private final ObjectMapper mapper;

  public CustomAccessDeniedHandler(
      ApiExceptionResponseFactory apiExceptionResponseFactory, ObjectMapper mapper) {
    this.apiExceptionResponseFactory = apiExceptionResponseFactory;
    this.mapper = mapper;
  }

  /**
   * Escribe en la respuesta un {@link AppExceptionResponse} con estado {@code 403 Forbidden}.
   *
   * @param request petición HTTP en curso
   * @param response respuesta HTTP sobre la que se escribe el cuerpo de error
   * @param ex excepción de acceso denegado capturada
   */
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
