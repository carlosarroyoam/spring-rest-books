package com.carlosarroyoam.rest.books.core.security;

import com.carlosarroyoam.rest.books.core.exception.ApiExceptionResponseFactory;
import com.carlosarroyoam.rest.books.core.exception.dto.AppExceptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Traduce un intento de acceso no autenticado a una respuesta {@code 401 Unauthorized} con el
 * cuerpo de {@link AppExceptionResponse}, ya que Spring Security no pasa este caso por {@link
 * com.carlosarroyoam.rest.books.core.exception.GlobalExceptionHandler}.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ApiExceptionResponseFactory apiExceptionResponseFactory;
  private final ObjectMapper mapper;

  public CustomAuthenticationEntryPoint(
      ApiExceptionResponseFactory apiExceptionResponseFactory, ObjectMapper mapper) {
    this.apiExceptionResponseFactory = apiExceptionResponseFactory;
    this.mapper = mapper;
  }

  /**
   * Escribe en la respuesta un {@link AppExceptionResponse} con estado {@code 401 Unauthorized}.
   *
   * @param request petición HTTP en curso
   * @param response respuesta HTTP sobre la que se escribe el cuerpo de error
   * @param ex excepción de autenticación capturada
   */
  @Override
  public void commence(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
      throws IOException {
    HttpStatus status = HttpStatus.UNAUTHORIZED;
    AppExceptionResponse appExceptionResponse =
        apiExceptionResponseFactory.build(status, ex.getMessage(), request);

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    mapper.writeValue(response.getOutputStream(), appExceptionResponse);
  }
}
