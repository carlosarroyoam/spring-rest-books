package com.carlosarroyoam.rest.books.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

/**
 * Punto único de registro en el log de las excepciones que se traducen a una respuesta HTTP de
 * error. Lo usan {@link GlobalExceptionHandler} y los manejadores de Spring Security ({@link
 * com.carlosarroyoam.rest.books.core.security.CustomAuthenticationEntryPoint}, {@link
 * com.carlosarroyoam.rest.books.core.security.CustomAccessDeniedHandler}) para que todo el logging
 * de errores de la API tenga el mismo formato y la misma política de severidad: los errores de
 * cliente (4xx) se registran en {@code WARN} sin traza de pila y los errores de servidor (5xx) en
 * {@code ERROR} con traza de pila.
 */
@Component
public class ExceptionLogger {
  private static final Logger log = LoggerFactory.getLogger(ExceptionLogger.class);

  /**
   * Registra la excepción según el estado HTTP con el que se responderá.
   *
   * @param status el estado HTTP de la respuesta de error
   * @param detail el mensaje descriptivo del error, puede ser nulo
   * @param request la petición HTTP que originó el error
   * @param ex la excepción capturada
   */
  public void log(HttpStatusCode status, String detail, HttpServletRequest request, Throwable ex) {
    String method = request.getMethod();
    String path = resolvePath(request);

    if (status.is5xxServerError()) {
      log.error("{} {} -> {}", method, path, detail, ex);
    } else {
      log.warn("{} {} -> {}", method, path, detail);
    }
  }

  /**
   * Resuelve la ruta de la petición que originó el error, prefiriendo el atributo de error del
   * servlet cuando la excepción se maneja a través del reenvío de error de Spring.
   *
   * @param request la petición HTTP
   * @return la ruta resuelta
   */
  private String resolvePath(HttpServletRequest request) {
    Object uri = request.getAttribute("jakarta.servlet.error.request_uri");
    return uri != null ? uri.toString() : request.getRequestURI();
  }
}
