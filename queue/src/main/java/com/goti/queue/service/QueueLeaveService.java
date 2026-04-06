package com.goti.queue.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.queue.config.properties.QueueProperties;
import com.goti.queue.constants.QueueStatus;
import com.goti.queue.domain.model.QueueEntry;
import com.goti.queue.dto.response.QueueLeaveResponse;
import com.goti.queue.repository.QueueRedisRepository;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueLeaveService {

	private final QueueRedisRepository queueRedisRepository;
	private final QueueProperties queueProperties;
	private final MeterRegistry meterRegistry;

	public QueueLeaveResponse leave(UUID gameId, UUID userId) {
		return processLeave(gameId, userId, LeaveReason.VOLUNTARY, true);
	}

	public QueueLeaveResponse expire(UUID gameId, UUID userId) {
		return processLeave(gameId, userId, LeaveReason.TTL_EXPIRED, false);
	}

	private QueueLeaveResponse processLeave(UUID gameId, UUID userId, LeaveReason reason, boolean validateUser) {
		if (validateUser && userId == null) {
			throw new CustomException(ErrorCode.AUTH_INVALID);
		}

		// Lua All-in-One: entry 읽기 + active 체크 + cleanup (1 RTT)
		List<Long> result = queueRedisRepository.executeLeave(gameId, userId);
		boolean released = result.get(0) == 1L;
		boolean processed = result.get(1) == 1L;

		if (!processed) {
			QueueLeaveResponse response = new QueueLeaveResponse(gameId, false, QueueStatus.LEFT);
			recordLeave(gameId, userId, reason, false);
			return response;
		}

		// entry 상태 업데이트 (2nd RTT) — JSON 직렬화 필요
		QueueEntry currentEntry = queueRedisRepository.getEntry(gameId, userId);
		if (currentEntry != null) {
			QueueStatus nextStatus = reason == LeaveReason.TTL_EXPIRED ? QueueStatus.EXPIRED : QueueStatus.LEFT;
			queueRedisRepository.saveEntry(
				gameId, userId,
				new QueueEntry(currentEntry.queueNumber(), currentEntry.issuedAt(), nextStatus),
				queueProperties.entryTtl()
			);
		}

		QueueStatus responseStatus = reason == LeaveReason.TTL_EXPIRED ? QueueStatus.EXPIRED : QueueStatus.LEFT;
		QueueLeaveResponse response = new QueueLeaveResponse(gameId, released, responseStatus);
		recordLeave(gameId, userId, reason, released);
		return response;
	}

	private void recordLeave(UUID gameId, UUID userId, LeaveReason reason, boolean released) {
		String matchId = gameId.toString();
		meterRegistry.counter("queue.leave", "match_id", matchId, "reason", reason.logValue.toLowerCase()).increment();
		if (reason == LeaveReason.VOLUNTARY) {
			meterRegistry.counter("queue.abandon", "match_id", matchId).increment();
		}
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
