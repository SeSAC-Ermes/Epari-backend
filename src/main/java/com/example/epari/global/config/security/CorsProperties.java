package com.example.epari.global.config.security;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "spring.security.cors")
@Getter
@Setter
public class CorsProperties {

	private List<String> allowedOrigins;

}
