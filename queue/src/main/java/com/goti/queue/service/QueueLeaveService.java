package com.goti.queue.service;

import java.time.Instant;
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
import com.goti.queue.domain.model.QueueMeta;
import com.goti.queue.dto.response.QueueLeaveResponse;
import com.goti.queue.repository.QueueRedisRepository;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueLeaveService {

	private static final String LEAVE_LOCK_KEY_PREFIX = "lock:queue:leave:";

	private final QueueRedisRepository queueRedisRepository;
	private final QueueProperties queueProperties;
	private final DistributedLockManager distributedLockManager;
	private final MeterRegistry meterRegistry;

	@Transactional
	public QueueLeaveResponse leave(UUID gameId, UUID userId) {
		return processLeave(gameId, userId, LeaveReason.VOLUNTARY, true);
	}

	@Transactional
	public QueueLeaveResponse expire(UUID gameId, UUID userId) {
		return processLeave(gameId, userId, LeaveReason.TTL_EXPIRED, false);
	}

	private QueueLeaveResponse processLeave(UUID gameId, UUID userId, LeaveReason reason, boolean validateUser) {
		if (validateUser && userId == null) {
			throw new CustomException(ErrorCode.AUTH_INVALID);
		}

		return distributedLockManager.withLock(
			LEAVE_LOCK_KEY_PREFIX + gameId + ":" + userId,
			ErrorCode.QUEUE_LOCK_ACQUIRE_FAILED,
			() -> {
				QueueEntry currentEntry = queueRedisRepository.getEntry(gameId, userId);
				QueueMeta queueMeta = queueRedisRepository.getMeta(gameId);
				boolean activeUser = queueRedisRepository.isActiveUser(gameId, userId);

				if (!activeUser && (currentEntry == null || currentEntry.status() == QueueStatus.LEFT || currentEntry.status() == QueueStatus.EXPIRED)) {
					QueueLeaveResponse response = new QueueLeaveResponse(
						gameId,
						false,
						currentEntry == null ? QueueStatus.LEFT : currentEntry.status()
					);
					recordLeave(gameId, userId, reason, response.released());
					return response;
				}

				if (currentEntry != null) {
					QueueStatus nextStatus = reason == LeaveReason.TTL_EXPIRED ? QueueStatus.EXPIRED : QueueStatus.LEFT;
					queueRedisRepository.saveEntry(
						gameId,
						userId,
						new QueueEntry(currentEntry.queueNumber(), currentEntry.issuedAt(), nextStatus),
						queueProperties.entryTtl()
					);
				}

				queueRedisRepository.removeActiveUser(gameId, userId);
				queueRedisRepository.removeExpirationUser(gameId, userId);

				if (queueMeta != null && activeUser) {
					queueRedisRepository.updateLeaveMeta(gameId, Math.max(0L, queueMeta.activeCount() - 1), Instant.now());
				}

				QueueStatus responseStatus = reason == LeaveReason.TTL_EXPIRED ? QueueStatus.EXPIRED : QueueStatus.LEFT;
				QueueLeaveResponse response = new QueueLeaveResponse(gameId, activeUser, responseStatus);
				recordLeave(gameId, userId, reason, response.released());
				return response;
			}
		);
	}

	private void recordLeave(UUID gameId, UUID userId, LeaveReason reason, boolean released) {
		String metricReason = reason == LeaveReason.TTL_EXPIRED ? "ttl_expired" : "voluntary";
		meterRegistry.counter("queue.leave.total", "gameId", gameId.toString(), "reason", metricReason).increment();
		log.info("action=LEAVE gameId={} userId={} reason={} released={}", gameId, userId, reason.logValue, released);
	}

	private enum LeaveReason {
		VOLUNTARY("VOLUNTARY"),
		TTL_EXPIRED("TTL_EXPIRED");

		private final String logValue;

		LeaveReason(String logValue) {
			this.logValue = logValue;
		}
	}
}
