package com.goti.ticketing.queue.infra;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.ticketing.queue.config.properties.QueueTokenProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QueueTokenReader {

	private static final String KEY_ALGORITHM = "AES";
	private static final String GAME_ID_CLAIM = "gameId";
	private static final String QUEUE_NUMBER_CLAIM = "queueNumber";

	private final QueueTokenProperties queueTokenProperties;

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

	public QueueTokenPayload parse(String token) {
		try {
			Claims claims = jwtParser.parseEncryptedClaims(token).getPayload();
			String tokenId = claims.getId();
			String gameId = claims.get(GAME_ID_CLAIM, String.class);
			String userId = claims.getSubject();
			Long queueNumber = claims.get(QUEUE_NUMBER_CLAIM, Long.class);
			Date issuedAt = claims.getIssuedAt();

			if (tokenId == null || gameId == null || userId == null || queueNumber == null || issuedAt == null) {
				throw new CustomException(ErrorCode.QUEUE_TOKEN_INVALID);
			}

			return new QueueTokenPayload(
				UUID.fromString(tokenId),
				UUID.fromString(gameId),
				UUID.fromString(userId),
				queueNumber,
				issuedAt.toInstant()
			);
		} catch (JwtException | IllegalArgumentException e) {
			throw new CustomException(ErrorCode.QUEUE_TOKEN_INVALID);
		}
	}

	private SecretKey secretKey() throws GeneralSecurityException {
		String tokenSecret = queueTokenProperties.tokenSecret();
		if (!StringUtils.hasText(tokenSecret)) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] keyBytes = digest.digest(tokenSecret.getBytes(StandardCharsets.UTF_8));
		return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
	}
}
