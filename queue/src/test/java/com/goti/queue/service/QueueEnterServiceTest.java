package com.goti.queue.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.infra.lock.DistributedLockManager;
import com.goti.queue.config.properties.QueueProperties;
import com.goti.queue.constants.QueueStatus;
import com.goti.queue.domain.QueueEntry;
import com.goti.queue.dto.request.QueueEnterRequest;
import com.goti.queue.dto.response.QueueEnterResponse;
import com.goti.queue.infra.QueueTokenProvider;
import com.goti.queue.repository.QueueRedisRepository;

@ExtendWith(MockitoExtension.class)
class QueueEnterServiceTest {

	@Mock
	private QueueRedisRepository queueRedisRepository;

	@Mock
	private QueueTokenProvider queueTokenProvider;

	@Mock
	private DistributedLockManager distributedLockManager;

	private QueueProperties queueProperties;

	@InjectMocks
	private QueueEnterService queueEnterService;

	@BeforeEach
	void setUp() {
		queueProperties = new QueueProperties(5000L, Duration.ofMinutes(10), "queue-token-secret-key-minimum-32-chars-long!!");
		queueEnterService = new QueueEnterService(
			queueRedisRepository,
			queueTokenProvider,
			queueProperties,
			distributedLockManager
		);
		lenient().when(distributedLockManager.withLock(any(), any()))
			.thenAnswer(invocation -> {
				Supplier<?> action = invocation.getArgument(1);
				return action.get();
			});
	}

	@Test
	void enterFirstTime() {
		UUID gameId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		QueueEnterRequest request = new QueueEnterRequest(gameId);

		given(queueRedisRepository.getEntry(gameId, userId)).willReturn(null);
		given(queueRedisRepository.nextSequence(gameId)).willReturn(1L);
		given(queueTokenProvider.createToken(eq(gameId), eq(userId), eq(1L), any(Instant.class)))
			.willReturn("queue-token");

		QueueEnterResponse response = queueEnterService.enter(request, userId);

		assertThat(response.queueToken()).isEqualTo("queue-token");
		assertThat(response.queueNumber()).isEqualTo(1L);
		assertThat(response.gameId()).isEqualTo(gameId);
		assertThat(response.issuedAt()).isNotNull();

		verify(queueRedisRepository).initializeMetaIfAbsent(gameId, queueProperties.maxCapacity());
		verify(queueRedisRepository).addWaiting(gameId, userId, 1L);
		verify(queueRedisRepository).saveEntry(eq(gameId), eq(userId), any(QueueEntry.class), eq(queueProperties.entryTtl()));
	}

	@Test
	void reEnterDeletesExistingEntryAndIssuesNewNumber() {
		UUID gameId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		QueueEnterRequest request = new QueueEnterRequest(gameId);
		QueueEntry existingEntry = new QueueEntry(3L, Instant.parse("2026-03-25T10:00:00Z"), QueueStatus.WAITING);

		given(queueRedisRepository.getEntry(gameId, userId)).willReturn(existingEntry);
		given(queueRedisRepository.nextSequence(gameId)).willReturn(4L);
		given(queueTokenProvider.createToken(eq(gameId), eq(userId), eq(4L), any(Instant.class)))
			.willReturn("new-queue-token");

		QueueEnterResponse response = queueEnterService.enter(request, userId);

		assertThat(response.queueNumber()).isEqualTo(4L);
		assertThat(response.queueToken()).isEqualTo("new-queue-token");

		verify(queueRedisRepository).removeWaiting(gameId, userId);
		verify(queueRedisRepository).deleteEntry(gameId, userId);
		verify(queueRedisRepository).addWaiting(gameId, userId, 4L);
	}

	@Test
	void saveWaitingEntryAsWaitingStatus() {
		UUID gameId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		QueueEnterRequest request = new QueueEnterRequest(gameId);

		given(queueRedisRepository.getEntry(gameId, userId)).willReturn(null);
		given(queueRedisRepository.nextSequence(gameId)).willReturn(11L);
		given(queueTokenProvider.createToken(eq(gameId), eq(userId), eq(11L), any(Instant.class)))
			.willReturn("queue-token");

		queueEnterService.enter(request, userId);

		ArgumentCaptor<QueueEntry> entryCaptor = ArgumentCaptor.forClass(QueueEntry.class);
		verify(queueRedisRepository).saveEntry(eq(gameId), eq(userId), entryCaptor.capture(), eq(queueProperties.entryTtl()));

		QueueEntry savedEntry = entryCaptor.getValue();
		assertThat(savedEntry.queueNumber()).isEqualTo(11L);
		assertThat(savedEntry.status()).isEqualTo(QueueStatus.WAITING);
		assertThat(savedEntry.issuedAt()).isNotNull();
	}

	@Test
	void throwWhenUserIdIsNull() {
		QueueEnterRequest request = new QueueEnterRequest(UUID.randomUUID());

		assertThatThrownBy(() -> queueEnterService.enter(request, null))
			.isInstanceOf(CustomException.class)
			.extracting("error")
			.isEqualTo(ErrorCode.AUTH_INVALID);

		verify(distributedLockManager, never()).withLock(any(), any());
	}

	@Test
	void useLockPerGameAndUser() {
		UUID gameId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		QueueEnterRequest request = new QueueEnterRequest(gameId);

		given(queueRedisRepository.getEntry(gameId, userId)).willReturn(null);
		given(queueRedisRepository.nextSequence(gameId)).willReturn(1L);
		given(queueTokenProvider.createToken(eq(gameId), eq(userId), eq(1L), any(Instant.class)))
			.willReturn("queue-token");

		queueEnterService.enter(request, userId);

		verify(distributedLockManager).withLock(eq("lock:queue:enter:" + gameId + ":" + userId), any());
	}
}
