package com.goti.queue.infra;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QueueTokenProvider {

	private static final String TRANSFORMATION = "AES/GCM/NoPadding";
	private static final String KEY_ALGORITHM = "AES";
	private static final int IV_LENGTH = 12;
	private static final int TAG_LENGTH = 128;

	private final ObjectMapper objectMapper;

	@Value("${jwt.secret}")
	private String secret;

	public String createToken(UUID gameId, UUID userId, long queueNumber, Instant issuedAt) {
		QueueTokenPayload payload = new QueueTokenPayload(
			UUID.randomUUID(),
			gameId,
			userId,
			queueNumber,
			issuedAt
		);
		try {
			byte[] iv = UUID.randomUUID().toString().substring(0, IV_LENGTH).getBytes(StandardCharsets.UTF_8);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey(), new GCMParameterSpec(TAG_LENGTH, iv));
			byte[] encrypted = cipher.doFinal(objectMapper.writeValueAsBytes(payload));

			return encode(iv) + "." + encode(encrypted);
		} catch (GeneralSecurityException | JsonProcessingException e) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, e);
		}
	}

	public QueueTokenPayload parse(String token) {
		try {
			String[] parts = token.split("\\.");
			if (parts.length != 2) {
				throw new CustomException(ErrorCode.AUTH_INVALID);
			}

			byte[] iv = decode(parts[0]);
			byte[] encrypted = decode(parts[1]);

			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, secretKey(), new GCMParameterSpec(TAG_LENGTH, iv));
			byte[] decrypted = cipher.doFinal(encrypted);

			return objectMapper.readValue(decrypted, QueueTokenPayload.class);
		} catch (CustomException e) {
			throw e;
		} catch (GeneralSecurityException | IOException e) {
			throw new CustomException(ErrorCode.AUTH_INVALID, e);
		}
	}

	private SecretKey secretKey() throws GeneralSecurityException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] keyBytes = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
		return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
	}

	private String encode(byte[] value) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
	}

	private byte[] decode(String value) {
		return Base64.getUrlDecoder().decode(value);
	}
}
