package com.goti.queue.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.queue.config.properties.QueueProperties;
import com.goti.queue.constants.QueueStatus;
import com.goti.queue.domain.model.QueueEntry;
import com.goti.queue.dto.response.QueueLeaveResponse;
import com.goti.queue.repository.QueueRedisRepository;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
@ExtendWith(MockitoExtension.class)
class QueueLeaveServiceTest {

	@Mock
	private QueueRedisRepository queueRedisRepository;

	private QueueProperties queueProperties;

	@InjectMocks
	private QueueLeaveService queueLeaveService;

	@BeforeEach
	void setUp() {
		queueProperties = new QueueProperties(
			5000L,
			Duration.ofMinutes(10),
			Duration.ofMinutes(15),
			"goti-2026-queue-token-secret-key-minimum-32-chars",
			Duration.ofSeconds(30)
		);
		queueLeaveService = new QueueLeaveService(
			queueRedisRepository,
			queueProperties,
			new SimpleMeterRegistry()
		);
	}

	@Test
	void active_user_leave_시_released_true() {
		UUID gameId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		QueueEntry currentEntry = new QueueEntry(12L, Instant.parse("2026-03-25T10:15:30Z"), QueueStatus.ADMITTED);

		given(queueRedisRepository.executeLeave(gameId, userId)).willReturn(List.of(1L, 1L));
		given(queueRedisRepository.getEntry(gameId, userId)).willReturn(currentEntry);

		QueueLeaveResponse response = queueLeaveService.leave(gameId, userId);

		assertEquals(gameId, response.gameId());
		assertThat(response.released()).isTrue();
		assertEquals(QueueStatus.LEFT, response.status());

		verify(queueRedisRepository).saveEntry(eq(gameId), eq(userId), any(QueueEntry.class), eq(queueProperties.entryTtl()));
	}

	@Test
	void 이미_빠진_사용자의_leave_재호출은_noop() {
		UUID gameId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();

		given(queueRedisRepository.executeLeave(gameId, userId)).willReturn(List.of(0L, 0L));

		QueueLeaveResponse response = queueLeaveService.leave(gameId, userId);

		assertThat(response.released()).isFalse();
		assertEquals(QueueStatus.LEFT, response.status());

		verify(queueRedisRepository, never()).saveEntry(any(), any(), any(), any());
	}

	@Test
	void non_active_user_leave시_released_false() {
		UUID gameId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		QueueEntry currentEntry = new QueueEntry(12L, Instant.parse("2026-03-25T10:15:30Z"), QueueStatus.ADMITTED);

		given(queueRedisRepository.executeLeave(gameId, userId)).willReturn(List.of(0L, 1L));
		given(queueRedisRepository.getEntry(gameId, userId)).willReturn(currentEntry);

		QueueLeaveResponse response = queueLeaveService.leave(gameId, userId);

		assertThat(response.released()).isFalse();
		assertEquals(QueueStatus.LEFT, response.status());

		verify(queueRedisRepository).saveEntry(eq(gameId), eq(userId), any(QueueEntry.class), eq(queueProperties.entryTtl()));
	}

	@Test
	void userId가_없으면_인증예외_반환() {
		assertThatThrownBy(() -> queueLeaveService.leave(UUID.randomUUID(), null))
			.isInstanceOf(CustomException.class)
			.extracting("error")
			.isEqualTo(ErrorCode.AUTH_INVALID);
	}
}
