package com.carlosarroyoam.rest.books.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
@SecurityScheme(name = OpenApiConfig.SECURITY_SCHEME_NAME, type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
public class OpenApiConfig {

	public static final String SECURITY_SCHEME_NAME = "bearerAuth";

	@Bean
	OpenAPI customOpenAPI() {
		String appTitle = "Spring Boot Rest Api";
		String appDescription = "Spring Boot Rest Api";
		String appVersion = "0.0.1-SNAPSHOT";
		Contact contactInfo = new Contact().email("carlosarroyoam@gmail.com");
		License license = new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0.html");

		return new OpenAPI().info(new Info().title(appTitle).version(appVersion).description(appDescription)
				.contact(contactInfo).license(license));
	}

	@Bean
	ModelResolver modelResolverDefault() {
		ObjectMapper copy = Json.mapper().copy();
		return new ModelResolver(copy.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE));
	}

}
