package com.carlosarroyoam.rest.books.core.security;

import com.carlosarroyoam.rest.books.core.exception.ProblemDetailFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Traduce un intento de acceso no autenticado a una respuesta {@code 401 Unauthorized} con el
 * cuerpo de un {@link ProblemDetail}, ya que Spring Security no pasa este caso por {@link
 * com.carlosarroyoam.rest.books.core.exception.GlobalExceptionHandler}.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ProblemDetailFactory problemDetailFactory;
  private final ObjectMapper mapper;

  public CustomAuthenticationEntryPoint(
      ProblemDetailFactory problemDetailFactory, ObjectMapper mapper) {
    this.problemDetailFactory = problemDetailFactory;
    this.mapper = mapper;
  }

  /**
   * Escribe en la respuesta un {@link ProblemDetail} con estado {@code 401 Unauthorized}.
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
    ProblemDetail problemDetail = problemDetailFactory.build(status, ex.getMessage(), request);

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    mapper.writeValue(response.getOutputStream(), problemDetail);
  }
}
