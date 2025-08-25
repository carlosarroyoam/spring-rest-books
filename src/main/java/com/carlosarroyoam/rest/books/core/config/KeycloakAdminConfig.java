package com.carlosarroyoam.rest.books.core.config;

import com.carlosarroyoam.rest.books.core.property.KeycloakAdminProps;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminConfig {
  private final KeycloakAdminProps properties;

  public KeycloakAdminConfig(KeycloakAdminProps properties) {
    this.properties = properties;
  }

  @Bean
  Keycloak keycloak() {
    return KeycloakBuilder.builder()
        .serverUrl(properties.getServerUrl())
        .realm(properties.getRealm())
        .clientId(properties.getClientId())
        .clientSecret(properties.getClientSecret())
        .grantType(properties.getGrantType())
        .build();
  }
}
