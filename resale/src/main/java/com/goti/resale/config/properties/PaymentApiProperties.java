package com.goti.resale.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "internal.payment")
public record PaymentApiProperties(
	String baseUrl
) {
}
