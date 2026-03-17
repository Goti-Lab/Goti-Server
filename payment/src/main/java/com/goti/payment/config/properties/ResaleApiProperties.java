package com.goti.payment.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "internal.resale")
public record ResaleApiProperties(
	String baseUrl
) {
}
