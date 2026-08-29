package com.carlosarroyoam.rest.books.core.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro que expone en el MDC, bajo la clave {@code username}, la identidad del usuario autenticado
 * durante el procesamiento de la petición, de forma que cualquier línea de log emitida mientras se
 * atiende pueda atribuirse a quien la originó.
 *
 * <p>Se instala en la cadena de Spring Security justo después de la autenticación del bearer token
 * (ver {@code WebSecurityConfig}), por lo que el {@code SecurityContext} ya está poblado y el
 * filtro cubre controladores, servicios, {@code @RestControllerAdvice} y el manejo de 403. Un 401
 * por token ausente o inválido se resuelve antes de este filtro, así que esa traza llevará {@code
 * requestId} pero no {@code username}.
 *
 * <p>No se anota como {@code @Component} para no registrarse además como filtro de servlet
 * independiente; se instancia manualmente en la configuración de seguridad.
 */
public class MdcUserContextFilter extends OncePerRequestFilter {
  public static final String USERNAME_KEY = "username";
  private static final String PREFERRED_USERNAME_CLAIM = "preferred_username";

  /**
   * Publica el nombre de usuario en el MDC antes de continuar la cadena y lo retira al completarla.
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
    boolean populated = populateMdc();

    try {
      filterChain.doFilter(request, response);
    } finally {
      if (populated) {
        MDC.remove(USERNAME_KEY);
      }
    }
  }

  /**
   * Resuelve el nombre de usuario a partir de la autenticación vigente y, si existe, lo publica en
   * el MDC.
   *
   * @return {@code true} si se publicó una clave en el MDC que deba retirarse después
   */
  private boolean populateMdc() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      return false;
    }

    String username = extractUsername(authentication);
    if (!StringUtils.hasText(username)) {
      return false;
    }

    MDC.put(USERNAME_KEY, username);
    return true;
  }

  /**
   * Extrae el nombre de usuario del token de Keycloak. Cubre el caso normal de resource server con
   * JWT (principal {@link Jwt}) y, por robustez, tokens validados por introspección ({@link
   * AbstractOAuth2TokenAuthenticationToken}).
   *
   * @param authentication la autenticación vigente
   * @return el nombre de usuario, o {@code null} si no puede determinarse
   */
  private String extractUsername(Authentication authentication) {
    if (authentication.getPrincipal() instanceof Jwt jwt) {
      String preferredUsername = jwt.getClaimAsString(PREFERRED_USERNAME_CLAIM);
      return StringUtils.hasText(preferredUsername) ? preferredUsername : jwt.getSubject();
    }

    if (authentication instanceof AbstractOAuth2TokenAuthenticationToken<?> tokenAuthentication) {
      Object preferredUsername =
          tokenAuthentication.getTokenAttributes().get(PREFERRED_USERNAME_CLAIM);
      return preferredUsername != null ? preferredUsername.toString() : authentication.getName();
    }

    return authentication.getName();
  }
}
