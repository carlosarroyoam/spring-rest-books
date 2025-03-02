package com.carlosarroyoam.rest.books.config;

import com.carlosarroyoam.rest.books.config.security.JwtAuthConverter;
import com.carlosarroyoam.rest.books.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
class WebSecurityConfig {
  @Value("${app.cors.allowed-origins}")
  private String allowedOrigins;

  @Value("${app.cors.allowed-methods}")
  private String allowedMethods;

  @Value("${app.cors.allowed-headers}")
  private String allowedHeaders;

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(CsrfConfigurer::disable);
    http.cors(Customizer.withDefaults());
    http.headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin));
    http.sessionManagement(
        sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.oauth2ResourceServer(oauth2 -> {
      oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(customJwtConverter()));
    });

    http.authorizeHttpRequests(
        requests -> requests.requestMatchers(AntPathRequestMatcher.antMatcher("/"))
            .permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher(("/auth/**")))
            .permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/swagger-ui/**"))
            .permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/api-docs/**"))
            .permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**"))
            .permitAll()
            .anyRequest()
            .authenticated());

    return http.build();
  }

  @Bean
  AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
    var authenticationProvider = new DaoAuthenticationProvider();
    authenticationProvider.setUserDetailsService(userDetailsService);

    return new ProviderManager(authenticationProvider);
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(StringUtils.commaSeparatedToList(allowedOrigins));
    configuration.setAllowedMethods(StringUtils.commaSeparatedToList(allowedMethods));
    configuration.setAllowedHeaders(StringUtils.commaSeparatedToList(allowedHeaders));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }

  @Bean
  Converter<Jwt, AbstractAuthenticationToken> customJwtConverter() {
    return new JwtAuthConverter();
  }
}
