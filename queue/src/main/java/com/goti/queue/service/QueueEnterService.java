package com.goti.queue.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.queue.config.properties.QueueProperties;
import com.goti.queue.constants.QueueStatus;
import com.goti.queue.domain.QueueEntry;
import com.goti.queue.dto.request.QueueEnterRequest;
import com.goti.queue.dto.response.QueueEnterResponse;
import com.goti.queue.infra.QueueTokenProvider;
import com.goti.queue.repository.QueueRedisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QueueEnterService {

	private final QueueRedisRepository queueRedisRepository;
	private final QueueTokenProvider queueTokenProvider;
	private final QueueProperties queueProperties;

	@Transactional
	public QueueEnterResponse enter(QueueEnterRequest request, UUID userId) {
		if (userId == null) {
			throw new CustomException(ErrorCode.AUTH_INVALID);
		}

		QueueEntry existingEntry = queueRedisRepository.getEntry(request.gameId(), userId);
		if (existingEntry != null) {
			queueRedisRepository.removeWaiting(request.gameId(), userId);
			queueRedisRepository.deleteEntry(request.gameId(), userId);
		}

		// TODO: 대기열 메타 초기화는 예매 오픈 시점의 별도 internal/admin API로 분리, enter API에서는 제거
		queueRedisRepository.initializeMetaIfAbsent(request.gameId(), queueProperties.maxCapacity());

		long queueNumber = queueRedisRepository.nextSequence(request.gameId());
		Instant issuedAt = Instant.now();
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
	}
}
