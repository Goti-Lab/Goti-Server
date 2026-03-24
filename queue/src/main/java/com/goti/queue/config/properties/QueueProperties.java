package com.goti.queue.config.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "queue")
public record QueueProperties(
	long maxCapacity,
	Duration entryTtl,
	String tokenSecret
) {
}
