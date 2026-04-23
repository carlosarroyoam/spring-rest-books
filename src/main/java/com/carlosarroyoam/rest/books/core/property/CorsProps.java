package com.carlosarroyoam.rest.books.core.property;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application.cors")
@Getter
@Setter
public class CorsProps {
  @NotEmpty(message = "allowed-origins must not be empty")
  private List<String> allowedOrigins;

  @NotEmpty(message = "allowed-methods must not be empty")
  private List<String> allowedMethods;

  @NotEmpty(message = "allowed-headers must not be empty")
  private List<String> allowedHeaders;

  @NotEmpty(message = "exposed-headers must not be empty")
  private List<String> exposedHeaders;

  @NotNull(message = "allow-credentials must not be null")
  private Boolean allowCredentials;
}
