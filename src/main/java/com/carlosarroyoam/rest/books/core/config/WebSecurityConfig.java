package com.carlosarroyoam.rest.books.core.config;

import com.carlosarroyoam.rest.books.core.filter.MdcUserContextFilter;
import com.carlosarroyoam.rest.books.core.property.CorsProps;
import com.carlosarroyoam.rest.books.core.security.AuthoritiesConverter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configura la cadena de seguridad HTTP: CORS, sesiones sin estado, autenticación OAuth2 con JWT y
 * el manejo de errores de autenticación y autorización.
 */
@Configuration
@EnableMethodSecurity
class WebSecurityConfig {
  /**
   * Define la cadena de filtros de seguridad: deshabilita CSRF, habilita CORS, fuerza sesiones sin
   * estado y valida el JWT como resource server, dejando sin autenticación la lectura de libros y
   * autores, el alta de clientes y los endpoints de infraestructura.
   *
   * @param http builder de configuración de seguridad HTTP
   * @param corsConfigurationSource origen de la configuración de CORS
   * @param jwtAuthenticationConverter conversor de JWT a token de autenticación
   * @param authenticationEntryPoint manejador de peticiones no autenticadas
   * @param accessDeniedHandler manejador de peticiones sin autorización suficiente
   * @return la cadena de filtros de seguridad configurada
   */
  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      CorsConfigurationSource corsConfigurationSource,
      JwtAuthenticationConverter jwtAuthenticationConverter,
      AuthenticationEntryPoint authenticationEntryPoint,
      AccessDeniedHandler accessDeniedHandler)
      throws Exception {
    http.csrf(CsrfConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
        .sessionManagement(
            sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2ResourceServer(
            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
        .exceptionHandling(
            ex -> {
              ex.authenticationEntryPoint(authenticationEntryPoint);
              ex.accessDeniedHandler(accessDeniedHandler);
            })
        .addFilterAfter(new MdcUserContextFilter(), BearerTokenAuthenticationFilter.class);

    return http.authorizeHttpRequests(
            requests ->
                requests
                    .requestMatchers(HttpMethod.GET, "/books/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/authors/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/customers")
                    .permitAll()
                    .requestMatchers("/h2-console/**")
                    .permitAll()
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .build();
  }

  /**
   * Extrae los roles de {@code realm_access} del token y los expone como {@code GrantedAuthority}
   * con el prefijo {@code ROLE_}, descartando los roles por defecto de Keycloak.
   *
   * @return el conversor de authorities a partir de los claims del JWT
   */
  @Bean
  AuthoritiesConverter authoritiesConverter() {
    return claims -> {
      Object rawRealmAccess = claims.get("realm_access");
      if (!(rawRealmAccess instanceof Map<?, ?> realmAccess)) {
        return List.of();
      }

      Object rawRoles = realmAccess.get("roles");
      if (!(rawRoles instanceof Collection<?> roles)) {
        return List.of();
      }

      return roles.stream()
          .filter(String.class::isInstance)
          .map(String.class::cast)
          .filter(role -> !role.startsWith("default-"))
          .filter(role -> !role.equals("offline_access"))
          .filter(role -> !role.equals("uma_authorization"))
          .map(role -> "ROLE_" + role)
          .map(SimpleGrantedAuthority::new)
          .map(GrantedAuthority.class::cast)
          .toList();
    };
  }

  /**
   * Ensambla el conversor de autenticación de Spring Security a partir de {@link
   * AuthoritiesConverter}.
   *
   * @param authoritiesConverter conversor de los claims del JWT a authorities
   * @return el conversor de autenticación configurado
   */
  @Bean
  JwtAuthenticationConverter authenticationConverter(AuthoritiesConverter authoritiesConverter) {
    JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
    authenticationConverter.setJwtGrantedAuthoritiesConverter(
        jwt -> authoritiesConverter.convert(jwt.getClaims()));
    return authenticationConverter;
  }

  /**
   * Construye el origen de configuración de CORS a partir de {@link CorsProps}, aplicándola a todas
   * las rutas.
   *
   * @param corsProps propiedades de configuración de CORS
   * @return el origen de configuración de CORS
   */
  @Bean
  CorsConfigurationSource corsConfigurationSource(CorsProps corsProps) {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(corsProps.getAllowedOrigins());
    configuration.setAllowedMethods(corsProps.getAllowedMethods());
    configuration.setAllowedHeaders(corsProps.getAllowedHeaders());
    configuration.setExposedHeaders(corsProps.getExposedHeaders());
    configuration.setAllowCredentials(corsProps.getAllowCredentials());

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
