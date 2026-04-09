package com.goti.ticketing.queue.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "queue")
public record QueueTokenProperties(
	String tokenSecret
) {
}
