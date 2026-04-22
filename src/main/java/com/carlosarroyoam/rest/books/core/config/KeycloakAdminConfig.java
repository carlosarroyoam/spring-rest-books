package com.carlosarroyoam.rest.books.core.config;

import com.carlosarroyoam.rest.books.core.property.KeycloakAdminProps;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminConfig {
  @Bean
  Keycloak keycloak(KeycloakAdminProps keycloakProps) {
    return KeycloakBuilder.builder()
        .serverUrl(keycloakProps.getServerUrl())
        .realm(keycloakProps.getRealm())
        .clientId(keycloakProps.getClientId())
        .clientSecret(keycloakProps.getClientSecret())
        .grantType(keycloakProps.getGrantType())
        .build();
  }
}
