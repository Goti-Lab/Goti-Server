package com.goti.queue.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.infra.lock.DistributedLockManager;
import com.goti.queue.config.properties.QueueProperties;
import com.goti.queue.constants.QueueStatus;
import com.goti.queue.domain.model.QueueEntry;
import com.goti.queue.domain.model.QueueMeta;
import com.goti.queue.dto.request.QueueSeatEnterRequest;
import com.goti.queue.dto.response.QueueSeatEnterResponse;
import com.goti.queue.infra.QueueTokenPayload;
import com.goti.queue.infra.QueueTokenProvider;
import com.goti.queue.repository.QueueRedisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QueueSeatEnterService {

	private static final String LOCK_KEY_PREFIX = "lock:queue:seat-enter:";

	private final QueueRedisRepository queueRedisRepository;
	private final QueueTokenProvider queueTokenProvider;
	private final QueueProperties queueProperties;
	private final DistributedLockManager distributedLockManager;

	@Transactional
	public QueueSeatEnterResponse enter(UUID gameId, UUID userId, QueueSeatEnterRequest request) {
		if (userId == null) {
			throw new CustomException(ErrorCode.AUTH_INVALID);
		}
		if (request.queueToken() == null || request.queueToken().isBlank()) {
			throw new CustomException(ErrorCode.QUEUE_TOKEN_REQUIRED);
		}

		String lockKey = LOCK_KEY_PREFIX + gameId + ":" + userId;
		return distributedLockManager.withLock(lockKey, () -> {
			// TODO: /seat-enter 부하 테스트 이후 Lua script 기반 원자 처리 전환 검토
			QueueTokenPayload payload = parseToken(request.queueToken());
			validateTokenIdentity(gameId, userId, payload);

			QueueEntry currentEntry = queueRedisRepository.getEntry(gameId, userId);
			if (currentEntry == null) {
				throw new CustomException(ErrorCode.QUEUE_ENTRY_NOT_FOUND);
			}

			validateEntryState(payload, currentEntry);

			if (queueRedisRepository.isActiveUser(gameId, userId) || currentEntry.status() == QueueStatus.ADMITTED) {
				throw new CustomException(ErrorCode.QUEUE_ALREADY_ADMITTED);
			}

			QueueMeta queueMeta = queueRedisRepository.getMeta(gameId);
			if (queueMeta == null) {
				throw new CustomException(ErrorCode.QUEUE_META_NOT_FOUND);
			}

			if (payload.queueNumber() > queueMeta.currentAllowedRank()) {
				throw new CustomException(ErrorCode.QUEUE_NOT_ALLOWED_YET);
			}

			if (queueMeta.activeCount() >= queueMeta.maxCapacity()) {
				throw new CustomException(ErrorCode.QUEUE_CAPACITY_FULL);
			}

			QueueEntry admittedEntry = new QueueEntry(
				currentEntry.queueNumber(),
				currentEntry.issuedAt(),
				QueueStatus.ADMITTED
			);

			queueRedisRepository.saveEntry(gameId, userId, admittedEntry, queueProperties.admittedTtl());
			queueRedisRepository.addActiveUser(gameId, userId, queueProperties.admittedTtl());
			queueRedisRepository.removeWaiting(gameId, userId);
			queueRedisRepository.updateSeatEnterMeta(
				gameId,
				queueMeta.activeCount() + 1,
				payload.queueNumber(),
				Instant.now()
			);

			return new QueueSeatEnterResponse(
				gameId,
				true,
				payload.queueNumber(),
				QueueStatus.ADMITTED
			);
		});
	}

	private QueueTokenPayload parseToken(String queueToken) {
		try {
			return queueTokenProvider.parse(queueToken);
		} catch (CustomException e) {
			throw new CustomException(ErrorCode.QUEUE_TOKEN_INVALID, e);
		}
	}

	private void validateTokenIdentity(UUID gameId, UUID userId, QueueTokenPayload payload) {
		if (!payload.gameId().equals(gameId) || !payload.userId().equals(userId)) {
			throw new CustomException(ErrorCode.QUEUE_TOKEN_INVALID);
		}
	}

	private void validateEntryState(QueueTokenPayload payload, QueueEntry currentEntry) {
		if (currentEntry.status() != QueueStatus.WAITING) {
			throw new CustomException(ErrorCode.QUEUE_ENTRY_MISMATCH);
		}
		if (currentEntry.queueNumber() != payload.queueNumber()) {
			throw new CustomException(ErrorCode.QUEUE_ENTRY_MISMATCH);
		}
	}
}
