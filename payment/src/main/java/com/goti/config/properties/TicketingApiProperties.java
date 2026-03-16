package com.goti.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "internal.ticketing")
public record TicketingApiProperties(
	String baseUrl
) {
}
