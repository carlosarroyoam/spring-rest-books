package com.carlosarroyoam.rest.books.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

/** Construye instancias de {@link ProblemDetail} (RFC 9457) con formato consistente. */
@Component
public class ProblemDetailFactory {

  /**
   * Construye una respuesta de error sin errores de validación.
   *
   * @param status código de estado HTTP a reportar
   * @param detail explicación legible específica de esta ocurrencia; puede ser {@code null}
   * @param request petición HTTP en curso
   * @return el {@link ProblemDetail} construido
   */
  public ProblemDetail build(HttpStatusCode status, String detail, HttpServletRequest request) {
    return build(status, detail, request, null);
  }

  /**
   * Construye una respuesta de error incluyendo el detalle de los errores de validación por campo
   * bajo la propiedad de extensión {@code errors}.
   *
   * @param status código de estado HTTP a reportar
   * @param detail explicación legible específica de esta ocurrencia; puede ser {@code null}
   * @param request petición HTTP en curso
   * @param errors mensajes de validación por campo; puede ser {@code null}
   * @return el {@link ProblemDetail} construido
   */
  public ProblemDetail build(
      HttpStatusCode status,
      String detail,
      HttpServletRequest request,
      Map<String, String> errors) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(status);
    problemDetail.setInstance(URI.create(resolvePath(request)));

    if (detail != null) {
      problemDetail.setDetail(detail);
    }

    if (errors != null) {
      problemDetail.setProperty("errors", errors);
    }

    return problemDetail;
  }

  /**
   * Resuelve la ruta de la petición, priorizando el atributo de error de Servlet sobre la URI
   * actual, ya que este es el que refleja la ruta original cuando el error se procesa a través del
   * reenvío de error de Spring.
   *
   * @param request petición HTTP en curso
   * @return la ruta a incluir como {@code instance} en la respuesta de error
   */
  private String resolvePath(HttpServletRequest request) {
    Object uri = request.getAttribute("jakarta.servlet.error.request_uri");
    return uri != null ? uri.toString() : request.getRequestURI();
  }
}
