package com.goti.service.listener;

import com.goti.dto.event.BookingCompletedEvent;
import com.goti.infra.cache.RedisCache;

import com.goti.infra.constants.redis.RedisKey;

import io.micrometer.core.instrument.MeterRegistry;
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
	private final MeterRegistry meterRegistry;
	
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleBookingCompleted(BookingCompletedEvent event) {

		String passedKey = RedisKey.QUEUE_PASSED.getKey(
			event.gameId(), event.memberId()
		);

		redisCache.delete(passedKey);

		log.info("action=LEAVE gameId={} userId={} reason=BOOKING_COMPLETED",
			event.gameId(), event.memberId());

		// 메트릭 기록 (정상 이탈 카운트, '정상 완료' 지표로 활용 가능)
		meterRegistry.counter(
			"queue.leave.total",
			"gameId",
			event.gameId().toString(),
			"reason",
			"booking_completed"
		).increment();
	}
}
