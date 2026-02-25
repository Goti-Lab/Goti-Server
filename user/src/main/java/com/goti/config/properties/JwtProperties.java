package com.goti.config.properties;

import io.jsonwebtoken.security.Keys;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.SecretKey;

import java.time.Duration;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
	String secret,
	Duration accessValidTime,
	Duration refreshValidTime
) {

	public SecretKey secretKey() {
		return Keys.hmacShaKeyFor(secret.getBytes());
	}
}
