package com.goti.queue.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.queue.domain.model.QueueMeta;
import com.goti.queue.dto.response.QueueStatusResponse;
import com.goti.queue.repository.QueueRedisRepository;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueStatusService {

	private final QueueRedisRepository queueRedisRepository;
	private final MeterRegistry meterRegistry;

	public QueueStatusResponse getStatus(UUID gameId, UUID userId) {
		if (userId == null) {
			throw new CustomException(ErrorCode.AUTH_INVALID);
		}

		QueueMeta queueMeta = queueRedisRepository.getMeta(gameId);
		if (queueMeta == null) {
			throw new CustomException(ErrorCode.QUEUE_META_NOT_FOUND);
		}

		long availableSlots = Math.max(0L, queueMeta.maxCapacity() - queueMeta.activeCount());
		long currentAllowedRank = Math.max(
			queueMeta.currentAllowedRank(),
			queueMeta.lastEnteredRank() + availableSlots
		);
		long publishedRank = currentAllowedRank;
		long waitingCount = queueRedisRepository.countWaitingUsers(gameId);
		Instant updatedAt = Instant.now();
		queueRedisRepository.updateStatusMeta(gameId, currentAllowedRank, publishedRank, updatedAt);
		meterRegistry.gauge("queue.waiting.size", Tags.of("gameId", gameId.toString()),
			queueMeta.maxCapacity() - queueMeta.activeCount());
		meterRegistry.gauge("queue.active.size", Tags.of("gameId", gameId.toString()),
			queueMeta.activeCount());
		log.debug(
			"action=STATUS gameId={} waitingCount={} activeCount={} availableSlots={} publishedRank={}",
			gameId,
			waitingCount,
			queueMeta.activeCount(),
			availableSlots,
			publishedRank
		);

		return new QueueStatusResponse(
			gameId,
			queueMeta.maxCapacity(),
			queueMeta.activeCount(),
			availableSlots,
			currentAllowedRank,
			publishedRank,
			updatedAt
		);
	}
}
