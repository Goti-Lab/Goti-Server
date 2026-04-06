package com.goti.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "infra.api.endpoints")
public record ApiEndpointProperties(
	String ticketing,
	String stadium,
	String payment,
	String resale,
	String ticketing
) {
}
