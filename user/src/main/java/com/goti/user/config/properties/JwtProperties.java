package com.goti.user.config.properties;

import com.goti.util.PemKeyParser;

import io.jsonwebtoken.security.Keys;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.SecretKey;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
	String secret,
	String rsaPrivateKey,
	String rsaPublicKey,
	String issuer,
	Duration accessValidTime,
	Duration refreshValidTime,
	Duration socialVerifyValidTime
) {

	public SecretKey secretKey() {
		return Keys.hmacShaKeyFor(secret.getBytes());
	}

	public RSAPrivateKey rsaPrivateKeyParsed() {
		return PemKeyParser.parsePrivateKey(rsaPrivateKey);
	}

	public RSAPublicKey rsaPublicKeyParsed() {
		return PemKeyParser.parsePublicKey(rsaPublicKey);
	}

	public boolean hasRsaKeys() {
		return rsaPrivateKey != null && !rsaPrivateKey.isBlank()
			&& rsaPublicKey != null && !rsaPublicKey.isBlank();
	}
}
