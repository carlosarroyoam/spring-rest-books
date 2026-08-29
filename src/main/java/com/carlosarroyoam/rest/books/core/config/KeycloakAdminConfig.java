package com.carlosarroyoam.rest.books.core.config;

import com.carlosarroyoam.rest.books.core.property.KeycloakAdminProps;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configura el cliente administrativo de Keycloak usado para provisionar usuarios. */
@Configuration
public class KeycloakAdminConfig {
  /**
   * Crea el cliente administrativo de Keycloak a partir de {@link KeycloakAdminProps}.
   *
   * @param keycloakAdminProps propiedades de conexión al realm de Keycloak
   * @return el cliente {@link Keycloak} configurado
   */
  @Bean
  Keycloak keycloak(KeycloakAdminProps keycloakAdminProps) {
    return KeycloakBuilder.builder()
        .serverUrl(keycloakAdminProps.getServerUrl())
        .realm(keycloakAdminProps.getRealm())
        .clientId(keycloakAdminProps.getClientId())
        .clientSecret(keycloakAdminProps.getClientSecret())
        .grantType(keycloakAdminProps.getGrantType())
        .build();
  }
}
