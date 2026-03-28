package com.goti.queue.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.queue.domain.model.QueueMeta;
import com.goti.queue.dto.response.QueueStatusResponse;
import com.goti.queue.repository.QueueRedisRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueStatusService {

	private final QueueRedisRepository queueRedisRepository;

	/**
	 * 대기열 상태 조회 — 순수 읽기 전용.
	 * Redis 쓰기 없음 → CDN TTL 캐시 적용 가능.
	 * publishedRank는 메타의 currentAllowedRank + availableSlots로 동적 계산만 하고 저장하지 않는다.
	 */
	public QueueStatusResponse getStatus(UUID gameId, UUID userId) {
		if (userId == null) {
			throw new CustomException(ErrorCode.AUTH_INVALID);
		}

		QueueMeta queueMeta = queueRedisRepository.getMeta(gameId);
		if (queueMeta == null) {
			throw new CustomException(ErrorCode.QUEUE_META_NOT_FOUND);
		}

		long availableSlots = Math.max(0L, queueMeta.maxCapacity() - queueMeta.activeCount());
		long publishedRank = Math.max(
			queueMeta.currentAllowedRank(),
			queueMeta.lastEnteredRank() + availableSlots
		);

		log.debug(
			"action=STATUS gameId={} activeCount={} availableSlots={} publishedRank={}",
			gameId,
			queueMeta.activeCount(),
			availableSlots,
			publishedRank
		);

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
