package com.carlosarroyoam.rest.books.core.exception;

import com.carlosarroyoam.rest.books.core.exception.dto.AppExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Construye instancias de {@link AppExceptionResponse} con formato consistente. */
@Component
public class ApiExceptionResponseFactory {

  /**
   * Construye una respuesta de error sin detalles adicionales.
   *
   * @param status código de estado HTTP a reportar
   * @param message mensaje de error
   * @param request petición HTTP en curso
   * @return la respuesta de error construida
   */
  public AppExceptionResponse build(HttpStatus status, String message, HttpServletRequest request) {
    return build(status, message, request, null);
  }

  /**
   * Construye una respuesta de error incluyendo detalles adicionales, como errores de validación
   * por campo.
   *
   * @param status código de estado HTTP a reportar
   * @param message mensaje de error
   * @param request petición HTTP en curso
   * @param details detalles adicionales del error, por ejemplo mensajes por campo; puede ser {@code
   *     null}
   * @return la respuesta de error construida
   */
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

  /**
   * Resuelve la ruta de la petición, priorizando el atributo de error de Servlet sobre la URI
   * actual, ya que este es el que refleja la ruta original cuando el error se procesa a través del
   * reenvío de error de Spring.
   *
   * @param request petición HTTP en curso
   * @return la ruta a incluir en la respuesta de error
   */
  private String resolvePath(HttpServletRequest request) {
    Object uri = request.getAttribute("jakarta.servlet.error.request_uri");
    return uri != null ? uri.toString() : request.getRequestURI();
  }
}
