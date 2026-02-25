package com.goti.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external.api")
public record RestClientProperties(
	int connectTimeout,
	int readTimeout
) {
}
