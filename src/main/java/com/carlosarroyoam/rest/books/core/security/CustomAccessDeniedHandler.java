package com.carlosarroyoam.rest.books.core.security;

import com.carlosarroyoam.rest.books.core.exception.ProblemDetailFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Traduce un {@link AccessDeniedException} a una respuesta {@code 403 Forbidden} con el cuerpo de
 * un {@link ProblemDetail}, ya que Spring Security no pasa este tipo de excepción por {@link
 * com.carlosarroyoam.rest.books.core.exception.GlobalExceptionHandler}.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
  private final ProblemDetailFactory problemDetailFactory;
  private final ObjectMapper mapper;

  public CustomAccessDeniedHandler(ProblemDetailFactory problemDetailFactory, ObjectMapper mapper) {
    this.problemDetailFactory = problemDetailFactory;
    this.mapper = mapper;
  }

  /**
   * Escribe en la respuesta un {@link ProblemDetail} con estado {@code 403 Forbidden}.
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
    ProblemDetail problemDetail = problemDetailFactory.build(status, ex.getMessage(), request);

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    mapper.writeValue(response.getOutputStream(), problemDetail);
  }
}
