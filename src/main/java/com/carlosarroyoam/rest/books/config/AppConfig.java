package com.carlosarroyoam.rest.books.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.carlosarroyoam.rest.books.config.security.RsaKeys;

@Configuration
@EnableConfigurationProperties(RsaKeys.class)
public class AppConfig {
}
