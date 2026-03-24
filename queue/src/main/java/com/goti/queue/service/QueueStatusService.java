package com.goti.queue.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.queue.domain.model.QueueMeta;
import com.goti.queue.dto.response.QueueStatusResponse;
import com.goti.queue.repository.QueueRedisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QueueStatusService {

	private final QueueRedisRepository queueRedisRepository;

	public QueueStatusResponse getStatus(UUID gameId, UUID userId) {
		if (userId == null) {
			throw new CustomException(ErrorCode.AUTH_INVALID);
		}

		QueueMeta queueMeta = queueRedisRepository.getMeta(gameId);
		if (queueMeta == null) {
			throw new CustomException(ErrorCode.QUEUE_META_NOT_FOUND);
		}

		long availableSlots = Math.max(0L, queueMeta.maxCapacity() - queueMeta.activeCount());
		long publishedRank = queueMeta.currentAllowedRank() + availableSlots;

		return new QueueStatusResponse(
			gameId,
			queueMeta.maxCapacity(),
			queueMeta.activeCount(),
			availableSlots,
			queueMeta.currentAllowedRank(),
			publishedRank,
			queueMeta.updatedAt()
		);
	}
}
