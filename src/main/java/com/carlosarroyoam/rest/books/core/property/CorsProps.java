package com.carlosarroyoam.rest.books.core.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.cors")
@Getter
@Setter
public class CorsProps {
  private String allowedOrigins;
  private String allowedMethods;
  private String allowedHeaders;
}
