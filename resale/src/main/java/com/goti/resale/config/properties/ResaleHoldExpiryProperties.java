package com.goti.resale.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "seat.hold-expiry")
public record ResaleHoldExpiryProperties(
	boolean enabled,
	long fixedDelayMs,
	int batchSize
) {
}
