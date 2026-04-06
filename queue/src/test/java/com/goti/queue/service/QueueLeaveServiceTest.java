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
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.queue.config.properties.QueueProperties;
import com.goti.queue.constants.QueueStatus;
import com.goti.queue.domain.model.QueueEntry;
import com.goti.queue.domain.model.QueueMeta;
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
	void active_user_leave_시_수용인원_감소와_left_저장() {
		UUID gameId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		QueueEntry currentEntry = new QueueEntry(12L, Instant.parse("2026-03-25T10:15:30Z"), QueueStatus.ADMITTED);
		QueueMeta queueMeta = new QueueMeta(
			5000L,
			10L,
			0L,
			20L,
			12L,
			Instant.parse("2026-03-25T10:20:00Z")
		);

		given(queueRedisRepository.getEntry(gameId, userId)).willReturn(currentEntry);
		given(queueRedisRepository.getMeta(gameId)).willReturn(queueMeta);
		given(queueRedisRepository.isActiveUser(gameId, userId)).willReturn(true);

		QueueLeaveResponse response = queueLeaveService.leave(gameId, userId);

		assertEquals(gameId, response.gameId());
		assertThat(response.released()).isTrue();
		assertEquals(QueueStatus.LEFT, response.status());

		ArgumentCaptor<QueueEntry> entryCaptor = ArgumentCaptor.forClass(QueueEntry.class);
		verify(queueRedisRepository).saveEntry(eq(gameId), eq(userId), entryCaptor.capture(), eq(queueProperties.entryTtl()));
		assertEquals(QueueStatus.LEFT, entryCaptor.getValue().status());

		verify(queueRedisRepository).removeActiveUser(gameId, userId);
		verify(queueRedisRepository).decrementActiveCount(gameId);
	}

	@Test
	void 이미_빠진_사용자의_leave_재호출은_noop() {
		UUID gameId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		QueueEntry leftEntry = new QueueEntry(12L, Instant.parse("2026-03-25T10:15:30Z"), QueueStatus.LEFT);

		given(queueRedisRepository.getEntry(gameId, userId)).willReturn(leftEntry);
		given(queueRedisRepository.getMeta(gameId)).willReturn(null);
		given(queueRedisRepository.isActiveUser(gameId, userId)).willReturn(false);

		QueueLeaveResponse response = queueLeaveService.leave(gameId, userId);

		assertThat(response.released()).isFalse();
		assertEquals(QueueStatus.LEFT, response.status());

		verify(queueRedisRepository, never()).removeActiveUser(any(), any());
		verify(queueRedisRepository, never()).decrementActiveCount(any());
	}

	@Test
	void active_user가_아니면_수용인원_감소_안함() {
		UUID gameId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		QueueEntry currentEntry = new QueueEntry(12L, Instant.parse("2026-03-25T10:15:30Z"), QueueStatus.ADMITTED);
		QueueMeta queueMeta = new QueueMeta(
			5000L,
			10L,
			0L,
			20L,
			12L,
			Instant.parse("2026-03-25T10:20:00Z")
		);

		given(queueRedisRepository.getEntry(gameId, userId)).willReturn(currentEntry);
		given(queueRedisRepository.getMeta(gameId)).willReturn(queueMeta);
		given(queueRedisRepository.isActiveUser(gameId, userId)).willReturn(false);

		QueueLeaveResponse response = queueLeaveService.leave(gameId, userId);

		assertThat(response.released()).isFalse();
		assertEquals(QueueStatus.LEFT, response.status());

		verify(queueRedisRepository).saveEntry(eq(gameId), eq(userId), any(QueueEntry.class), eq(queueProperties.entryTtl()));
		verify(queueRedisRepository).removeActiveUser(gameId, userId);
		verify(queueRedisRepository, never()).decrementActiveCount(any());
	}

	@Test
	void userId가_없으면_인증예외_반환() {
		assertThatThrownBy(() -> queueLeaveService.leave(UUID.randomUUID(), null))
			.isInstanceOf(CustomException.class)
			.extracting("error")
			.isEqualTo(ErrorCode.AUTH_INVALID);
	}
}
