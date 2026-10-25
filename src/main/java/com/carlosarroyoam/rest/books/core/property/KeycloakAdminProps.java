package com.carlosarroyoam.rest.books.core.property;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/** Propiedades de conexión al Admin Client de Keycloak bajo el prefijo {@code keycloak.admin}. */
@Component
@ConfigurationProperties(prefix = "keycloak.admin")
@Validated
@Getter
@Setter
public class KeycloakAdminProps {
  @NotBlank(message = "keycloak.admin.server-url must not be empty")
  private String serverUrl;

  @NotBlank(message = "keycloak.admin.realm must not be empty")
  private String realm;

  @NotBlank(message = "keycloak.admin.client-id must not be empty")
  private String clientId;

  @NotBlank(message = "keycloak.admin.client-secret must not be empty")
  private String clientSecret;

  @NotBlank(message = "keycloak.admin.grant-type must not be empty")
  private String grantType;
}
