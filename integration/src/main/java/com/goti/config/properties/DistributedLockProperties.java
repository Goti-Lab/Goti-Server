package com.goti.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "seat.lock")
public record DistributedLockProperties(
	long waitSeconds
) {
}
