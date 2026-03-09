package com.goti.seat.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "seat.hold-expiry")
public record SeatHoldExpiryProperties(
	boolean enabled,
	long fixedDelayMs,
	int batchSize
) {
}
