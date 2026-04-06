package com.goti.queue.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.queue.config.properties.QueueProperties;
import com.goti.queue.constants.QueueStatus;
import com.goti.queue.domain.model.QueueEntry;
import com.goti.queue.dto.request.QueueSeatEnterRequest;
import com.goti.queue.dto.response.QueueSeatEnterResponse;
import com.goti.queue.infra.QueueTokenPayload;
import com.goti.queue.infra.QueueTokenProvider;
import com.goti.queue.repository.QueueRedisRepository;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueSeatEnterService {

	// Lua tryAdmit 반환 코드 — Lua script과 동기화 필수
	private static final int ADMIT_OK = 1;
	private static final int ADMIT_ENTRY_NOT_FOUND = -1;
	private static final int ADMIT_STATUS_MISMATCH = -2;
	private static final int ADMIT_QUEUENUMBER_MISMATCH = -3;
	private static final int ADMIT_ALREADY_ADMITTED = -4;
	private static final int ADMIT_NOT_ALLOWED_YET = -5;
	private static final int ADMIT_CAPACITY_FULL = -6;

	private final QueueRedisRepository queueRedisRepository;
	private final QueueTokenProvider queueTokenProvider;
	private final QueueProperties queueProperties;
	private final MeterRegistry meterRegistry;

	public QueueSeatEnterResponse enter(UUID gameId, UUID userId, QueueSeatEnterRequest request) {
		if (userId == null) {
			throw new CustomException(ErrorCode.AUTH_INVALID);
		}

		try {
			QueueTokenPayload payload = queueTokenProvider.parse(request.queueToken());
			if (!payload.gameId().equals(gameId) || !payload.userId().equals(userId)) {
				throw new CustomException(ErrorCode.QUEUE_TOKEN_INVALID);
			}

			Instant expiresAt = payload.issuedAt().plus(queueProperties.admittedTtl());

			// Lua All-in-One: 검증 + 승격 + 정리 (1 RTT)
			List<Long> result = queueRedisRepository.tryAdmit(
				gameId, userId, payload.queueNumber(), expiresAt
			);

			int code = result.get(0).intValue();
			if (code != ADMIT_OK) {
				throw switch (code) {
					case ADMIT_ENTRY_NOT_FOUND -> new CustomException(ErrorCode.QUEUE_ENTRY_NOT_FOUND);
					case ADMIT_STATUS_MISMATCH, ADMIT_QUEUENUMBER_MISMATCH -> new CustomException(ErrorCode.QUEUE_ENTRY_MISMATCH);
					case ADMIT_ALREADY_ADMITTED -> new CustomException(ErrorCode.QUEUE_ALREADY_ADMITTED);
					case ADMIT_NOT_ALLOWED_YET -> new CustomException(ErrorCode.QUEUE_NOT_ALLOWED_YET);
					case ADMIT_CAPACITY_FULL -> new CustomException(ErrorCode.QUEUE_CAPACITY_FULL);
					default -> new CustomException(ErrorCode.QUEUE_ENTRY_MISMATCH);
				};
			}

			long queueNumber = result.get(1);
			QueueEntry admittedEntry = new QueueEntry(queueNumber, payload.issuedAt(), QueueStatus.ADMITTED);
			queueRedisRepository.saveEntry(gameId, userId, admittedEntry, queueProperties.admittedTtl()); // 2nd RTT

			Instant now = Instant.now();
			String matchId = gameId.toString();
			meterRegistry.counter("queue.seat_enter", "match_id", matchId).increment();
			meterRegistry.counter("seat.enter.verify", "match_id", matchId, "result", "pass").increment();
			long waitMs = Duration.between(payload.issuedAt(), now).toMillis();
			meterRegistry.timer("queue.wait.duration", "match_id", matchId)
				.record(waitMs, TimeUnit.MILLISECONDS);

			log.info("action=SEAT_ENTER gameId={} userId={} queueNumber={} waitDurationMs={}", gameId, userId, queueNumber, waitMs);

			return new QueueSeatEnterResponse(gameId, true, queueNumber, QueueStatus.ADMITTED);
		} catch (CustomException e) {
			meterRegistry.counter("seat.enter.verify", "match_id", gameId.toString(), "result", "fail").increment();
			log.info("action=SEAT_ENTER_BLOCKED gameId={} userId={} reason={}", gameId, userId, e.error().name());
			throw e;
		}
	}
}
