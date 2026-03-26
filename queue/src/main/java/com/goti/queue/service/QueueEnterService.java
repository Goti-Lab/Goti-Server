package com.goti.queue.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.infra.lock.DistributedLockManager;
import com.goti.queue.config.properties.QueueProperties;
import com.goti.queue.constants.QueueStatus;
import com.goti.queue.domain.model.QueueEntry;
import com.goti.queue.dto.request.QueueEnterRequest;
import com.goti.queue.dto.response.QueueEnterResponse;
import com.goti.queue.infra.QueueTokenProvider;
import com.goti.queue.repository.QueueRedisRepository;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueEnterService {

	private static final String LOCK_KEY_PREFIX = "lock:queue:enter:";

	private final QueueRedisRepository queueRedisRepository;
	private final QueueTokenProvider queueTokenProvider;
	private final QueueProperties queueProperties;
	private final DistributedLockManager distributedLockManager;
	private final MeterRegistry meterRegistry;

	@Transactional
	public QueueEnterResponse enter(QueueEnterRequest request, UUID userId) {
		if (userId == null) {
			throw new CustomException(ErrorCode.AUTH_INVALID);
		}

		String lockKey = buildLockKey(request.gameId(), userId);
		QueueEnterResponse response =  distributedLockManager.withLock(
			lockKey,
			ErrorCode.QUEUE_LOCK_ACQUIRE_FAILED,
			() -> {
			// TODO: /enter 부하 테스트 이후 Lua script 기반 원자 처리 전환 시도
			QueueEntry existingEntry = queueRedisRepository.getEntry(request.gameId(), userId);
			if (existingEntry != null) {
				queueRedisRepository.removeWaiting(request.gameId(), userId);
				queueRedisRepository.deleteEntry(request.gameId(), userId);
			}

			// TODO: 대기열 메타 초기화는 예매 오픈 시점의 별도 internal/admin API로 분리, enter API에서는 제거
			queueRedisRepository.initializeMetaIfAbsent(request.gameId(), queueProperties.maxCapacity());

			long queueNumber = queueRedisRepository.nextSequence(request.gameId());
			Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
			String queueToken = queueTokenProvider.createToken(request.gameId(), userId, queueNumber, issuedAt);

			QueueEntry queueEntry = new QueueEntry(
				queueNumber,
				issuedAt,
				QueueStatus.WAITING
			);

			queueRedisRepository.addWaiting(request.gameId(), userId, queueNumber);
			queueRedisRepository.saveEntry(request.gameId(), userId, queueEntry, queueProperties.entryTtl());

			return new QueueEnterResponse(
				queueToken,
				queueNumber,
				request.gameId(),
				issuedAt
			);
		});

		log.info("action=ENTER gameId={} userId={} queueNumber={}", request.gameId(), userId, response.queueNumber());
		meterRegistry.counter("queue.enter.total", "gameId", request.gameId().toString()).increment();

		return response;
	}

	private String buildLockKey(UUID gameId, UUID userId) {
		return LOCK_KEY_PREFIX + gameId + ":" + userId;
	}
}
