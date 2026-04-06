package com.goti.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloudflare.turnstile")
public record CloudflareTurnstileProperties(
	String secret,
	String verifyUrl
) {
}
