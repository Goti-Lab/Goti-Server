package com.goti.queue.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.goti.exception.CustomException;
import com.goti.queue.config.properties.QueueProperties;

class QueueTokenProviderTest {

	private QueueTokenProvider queueTokenProvider;

	@BeforeEach
	void setUp() {
		queueTokenProvider = new QueueTokenProvider(
			new QueueProperties(
				5000L,
				java.time.Duration.ofMinutes(10),
				java.time.Duration.ofMinutes(15),
				"goti-2026-queue-token-secret-key-minimum-32-chars",
				java.time.Duration.ofSeconds(30)
			)
		);
		ReflectionTestUtils.invokeMethod(queueTokenProvider, "init");
	}

	@Test
	void createTokenAndParse() {
		UUID gameId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		long queueNumber = 123L;
		Instant issuedAt = Instant.parse("2026-03-25T10:15:30Z");

		String token = queueTokenProvider.createToken(gameId, userId, queueNumber, issuedAt);
		QueueTokenPayload payload = queueTokenProvider.parse(token);

		assertThat(payload.gameId()).isEqualTo(gameId);
		assertThat(payload.userId()).isEqualTo(userId);
		assertThat(payload.queueNumber()).isEqualTo(queueNumber);
		assertThat(payload.issuedAt()).isEqualTo(issuedAt);
		assertThat(payload.tokenId()).isNotNull();
	}

	@Test
	void parseTamperedToken() {
		UUID gameId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		String token = queueTokenProvider.createToken(gameId, userId, 1L, Instant.now());
		String tamperedToken = token.substring(0, token.length() - 2) + "ab";

		assertThatThrownBy(() -> queueTokenProvider.parse(tamperedToken))
			.isInstanceOf(CustomException.class);
	}
}
