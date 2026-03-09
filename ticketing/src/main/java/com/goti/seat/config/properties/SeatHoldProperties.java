package com.goti.seat.config.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "seat.hold")
public record SeatHoldProperties(
	Duration ttl
) {
}
