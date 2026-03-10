package com.goti.config.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "seat.hold")
public record ResaleHoldProperties(
	Duration ttl
) {
}
