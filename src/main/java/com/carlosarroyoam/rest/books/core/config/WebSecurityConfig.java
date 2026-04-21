package com.carlosarroyoam.rest.books.core.config;

import com.carlosarroyoam.rest.books.core.property.CorsProps;
import com.carlosarroyoam.rest.books.core.security.AuthoritiesConverter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
class WebSecurityConfig {
  private final CorsProps corsProps;

  public WebSecurityConfig(CorsProps corsProps) {
    this.corsProps = corsProps;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http,
      JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
    http.csrf(CsrfConfigurer::disable)
        .cors(Customizer.withDefaults())
        .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
        .sessionManagement(
            sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

    http.authorizeHttpRequests(requests -> requests.requestMatchers(HttpMethod.GET, "/books/**")
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
        .authenticated());

    return http.build();
  }

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

  @Bean
  JwtAuthenticationConverter authenticationConverter(AuthoritiesConverter authoritiesConverter) {
    JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
    authenticationConverter
        .setJwtGrantedAuthoritiesConverter(jwt -> authoritiesConverter.convert(jwt.getClaims()));
    return authenticationConverter;
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(corsProps.getAllowedOrigins());
    configuration.setAllowedMethods(corsProps.getAllowedMethods());
    configuration.setAllowedHeaders(corsProps.getAllowedHeaders());

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
