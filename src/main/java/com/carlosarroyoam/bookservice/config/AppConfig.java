package com.carlosarroyoam.bookservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.carlosarroyoam.bookservice.config.security.RsaKeys;

@Configuration
@EnableConfigurationProperties(RsaKeys.class)
public class AppConfig {
}
