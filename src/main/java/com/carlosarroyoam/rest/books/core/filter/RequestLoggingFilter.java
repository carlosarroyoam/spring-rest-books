package com.carlosarroyoam.rest.books.core.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Registra en el log el método, la ruta, el código de estado y la duración de cada petición HTTP.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

  /**
   * Ejecuta el resto de la cadena de filtros y registra en el log el resultado y la duración de la
   * petición una vez completada.
   *
   * @param request petición HTTP en curso
   * @param response respuesta HTTP en curso
   * @param filterChain resto de la cadena de filtros a ejecutar
   */
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {
    long startTime = System.currentTimeMillis();
    filterChain.doFilter(request, response);
    long duration = System.currentTimeMillis() - startTime;

    log.info(
        "{} {} {} - {} ms",
        request.getMethod(),
        request.getRequestURI(),
        response.getStatus(),
        duration);
  }
}
