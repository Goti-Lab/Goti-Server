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
import com.goti.queue.dto.response.QueueLeaveResponse;
import com.goti.queue.repository.QueueRedisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QueueLeaveService {

	private static final String LEAVE_LOCK_KEY_PREFIX = "lock:queue:leave:";

	private final QueueRedisRepository queueRedisRepository;
	private final QueueProperties queueProperties;
	private final DistributedLockManager distributedLockManager;

	@Transactional
	public QueueLeaveResponse leave(UUID gameId, UUID userId) {
		if (userId == null) {
			throw new CustomException(ErrorCode.AUTH_INVALID);
		}

		return distributedLockManager.withLock(LEAVE_LOCK_KEY_PREFIX + gameId + ":" + userId, () -> {
			QueueEntry currentEntry = queueRedisRepository.getEntry(gameId, userId);
			QueueMeta queueMeta = queueRedisRepository.getMeta(gameId);
			boolean activeUser = queueRedisRepository.isActiveUser(gameId, userId);

			if (!activeUser && (currentEntry == null || currentEntry.status() == QueueStatus.LEFT || currentEntry.status() == QueueStatus.EXPIRED)) {
				return new QueueLeaveResponse(gameId, false, currentEntry == null ? QueueStatus.LEFT : currentEntry.status());
			}

			if (currentEntry != null) {
				queueRedisRepository.saveEntry(
					gameId,
					userId,
					new QueueEntry(currentEntry.queueNumber(), currentEntry.issuedAt(), QueueStatus.LEFT),
					queueProperties.entryTtl()
				);
			}

			queueRedisRepository.removeActiveUser(gameId, userId);

			if (queueMeta != null && activeUser) {
				queueRedisRepository.updateLeaveMeta(gameId, Math.max(0L, queueMeta.activeCount() - 1), Instant.now());
			}

			return new QueueLeaveResponse(gameId, activeUser, QueueStatus.LEFT);
		});
	}
}
