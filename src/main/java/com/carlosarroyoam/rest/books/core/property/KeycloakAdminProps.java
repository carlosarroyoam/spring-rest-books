package com.carlosarroyoam.rest.books.core.property;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "keycloak.admin")
@Getter
@Setter
public class KeycloakAdminProps {
  @NotNull(message = "server-url must not be null")
  private String serverUrl;

  @NotNull(message = "realm must not be null")
  private String realm;

  @NotNull(message = "client-id must not be null")
  private String clientId;

  @NotNull(message = "client-secret must not be null")
  private String clientSecret;

  @NotNull(message = "grant-type must not be null")
  private String grantType;
}
