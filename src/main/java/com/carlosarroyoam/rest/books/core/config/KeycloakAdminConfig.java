package com.carlosarroyoam.rest.books.core.config;

import com.carlosarroyoam.rest.books.core.property.KeycloakAdminProps;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminConfig {
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
