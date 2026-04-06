package com.goti.queue.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
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

	private final QueueRedisRepository queueRedisRepository;
	private final QueueTokenProvider queueTokenProvider;
	private final QueueProperties queueProperties;
	private final MeterRegistry meterRegistry;

	public QueueEnterResponse enter(QueueEnterRequest request, UUID userId) {
		if (userId == null) {
			throw new CustomException(ErrorCode.AUTH_INVALID);
		}

		String matchId = request.gameId().toString();

		// Lua All-in-One: 기존 entry 체크 + cleanup + meta init + sequence + ZADD (1 RTT)
		List<Long> luaResult = queueRedisRepository.enterQueue(
			request.gameId(), userId, queueProperties.maxCapacity()
		);
		long queueNumber = luaResult.get(0);
		long oldQueueNumber = luaResult.get(1);

		if (oldQueueNumber >= 0) {
			meterRegistry.counter("queue.enter.duplicate", "match_id", matchId).increment();
			log.info("action=ENTER gameId={} userId={} oldQueueNumber={} newQueueNumber={}",
				request.gameId(), userId, oldQueueNumber, queueNumber);
		}

		Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
		String queueToken = queueTokenProvider.createToken(request.gameId(), userId, queueNumber, issuedAt);

		QueueEntry queueEntry = new QueueEntry(queueNumber, issuedAt, QueueStatus.WAITING);
		queueRedisRepository.saveEntry(request.gameId(), userId, queueEntry, queueProperties.entryTtl()); // 2nd RTT

		log.info("action=ENTER gameId={} userId={} queueNumber={}", request.gameId(), userId, queueNumber);
		meterRegistry.counter("queue.enter", "match_id", matchId).increment();
		meterRegistry.counter("queue.token.issued", "match_id", matchId).increment();

		return new QueueEnterResponse(queueToken, queueNumber, request.gameId(), issuedAt);
	}
}
