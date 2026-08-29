package com.carlosarroyoam.rest.books.core.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro que asigna a cada petición un identificador de correlación y lo expone en el MDC bajo la
 * clave {@code requestId} durante todo su procesamiento, de forma que cualquier línea de log
 * emitida mientras se atiende la petición pueda correlacionarse.
 *
 * <p>Se registra con {@link Ordered#HIGHEST_PRECEDENCE} para ejecutarse por fuera de la cadena de
 * filtros de Spring Security y envolver así la totalidad del procesamiento. Reutiliza el valor de
 * la cabecera {@code X-Request-Id} si la petición ya la trae (p. ej. de un gateway) y, en cualquier
 * caso, devuelve el identificador usado en la misma cabecera de la respuesta.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {
  public static final String REQUEST_ID_HEADER = "X-Request-Id";
  public static final String REQUEST_ID_KEY = "requestId";

  /**
   * Resuelve el identificador de correlación de la petición, lo publica en el MDC y en la cabecera
   * de respuesta, y lo retira del MDC al completar la cadena de filtros.
   *
   * @param request la petición HTTP entrante
   * @param response la respuesta HTTP saliente
   * @param filterChain la cadena de filtros a continuar
   */
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {
    String requestId = resolveRequestId(request);
    MDC.put(REQUEST_ID_KEY, requestId);
    response.setHeader(REQUEST_ID_HEADER, requestId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(REQUEST_ID_KEY);
    }
  }

  /**
   * Devuelve el identificador de correlación de la cabecera {@code X-Request-Id} si viene
   * informado, o genera un UUID aleatorio en caso contrario.
   *
   * @param request la petición HTTP entrante
   * @return el identificador de correlación a usar
   */
  private String resolveRequestId(HttpServletRequest request) {
    String headerValue = request.getHeader(REQUEST_ID_HEADER);
    return StringUtils.hasText(headerValue) ? headerValue : UUID.randomUUID().toString();
  }
}
