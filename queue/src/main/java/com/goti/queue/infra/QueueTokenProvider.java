package com.goti.queue.infra;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.queue.config.properties.QueueProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QueueTokenProvider {

	private static final String KEY_ALGORITHM = "AES";
	private static final String GAME_ID_CLAIM = "gameId";
	private static final String QUEUE_NUMBER_CLAIM = "queueNumber";

	private final QueueProperties queueProperties;

	private SecretKey secretKey;
	private JwtParser jwtParser;

	@PostConstruct
	void init() {
		try {
			this.secretKey = secretKey();
			this.jwtParser = Jwts.parser()
				.decryptWith(secretKey)
				.build();
		} catch (GeneralSecurityException e) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, e);
		}
	}

	public String createToken(UUID gameId, UUID userId, long queueNumber, Instant issuedAt) {
		try {
			return Jwts.builder()
				.id(UUID.randomUUID().toString())
				.subject(userId.toString())
				.claim(GAME_ID_CLAIM, gameId.toString())
				.claim(QUEUE_NUMBER_CLAIM, queueNumber)
				.issuedAt(Date.from(issuedAt))
				.encryptWith(secretKey, Jwts.KEY.DIRECT, Jwts.ENC.A256GCM)
				.compact();
		} catch (JwtException e) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, e);
		}
	}

	public QueueTokenPayload parse(String token) {
		try {
			Claims claims = jwtParser.parseEncryptedClaims(token).getPayload();
			return new QueueTokenPayload(
				UUID.fromString(claims.getId()),
				UUID.fromString(claims.get(GAME_ID_CLAIM, String.class)),
				UUID.fromString(claims.getSubject()),
				claims.get(QUEUE_NUMBER_CLAIM, Long.class),
				claims.getIssuedAt().toInstant()
			);
		} catch (JwtException | IllegalArgumentException e) {
			throw new CustomException(ErrorCode.QUEUE_TOKEN_INVALID, e);
		}
	}

	private SecretKey secretKey() throws GeneralSecurityException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] keyBytes = digest.digest(queueProperties.tokenSecret().getBytes(StandardCharsets.UTF_8));
		return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
	}
}
