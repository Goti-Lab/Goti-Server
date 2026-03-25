package com.goti.service.listener;

import com.goti.dto.event.BookingCompletedEvent;
import com.goti.infra.cache.RedisCache;

import com.goti.infra.constants.redis.RedisKey;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueEventListener {

	private final RedisCache redisCache;
	
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleBookingCompleted(BookingCompletedEvent event) {

		String passedKey = RedisKey.QUEUE_PASSED.getKey(
			event.gameId(), event.memberId()
		);

		redisCache.delete(passedKey);

		log.info("대기열 이탈 처리 완료 (결제 완료 이벤트 수신) :: gameId={}, memberId={}",
			event.gameId(), event.memberId());
	}
}
