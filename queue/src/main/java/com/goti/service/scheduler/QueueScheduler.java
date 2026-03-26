package com.goti.service.scheduler;

import com.goti.infra.cache.RedisCache;

import com.goti.infra.constants.redis.RedisKey;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueScheduler {

	private final RedisCache redisCache;
	private final MeterRegistry meterRegistry;

	private static final long MAX_ALLOWED_COUNT = 100;
	private static final String COLON = ":";

	@Scheduled(fixedDelay = 1000)
	public void processQueue() {
		Set<String> pendingKeys = redisCache.getKeys(RedisKey.QUEUE_PENDING.getPrefix() + "*");
		if (pendingKeys == null || pendingKeys.isEmpty()) return;

		for (String pendingKey : pendingKeys) {
			try {
				String cachedGameId = pendingKey.replace(RedisKey.QUEUE_PENDING.getPrefix(), "");
				UUID gameId = UUID.fromString(cachedGameId);
				processGameQueue(gameId, pendingKey);
			} catch (Exception e) {
				log.error(
					"action=PROCESS_ERROR gameId={} message={}",
					pendingKey, e.getMessage()
				);
			}
		}
	}

	private void processGameQueue(UUID gameId, String pendingKey) {
		String passedPattern = RedisKey.QUEUE_PASSED.getPrefix() + gameId + COLON;
		long currentPassedCount = redisCache.countKeys(passedPattern);
		long availableSlots = MAX_ALLOWED_COUNT - currentPassedCount;

		long pendingSize = redisCache.zSize(pendingKey);

		// 메트릭: 실시간 대기 인원 및 통과 인원 (Gauge)
		meterRegistry.gauge(
			"queue.waiting.size",
			Tags.of("gameId", gameId.toString()),
			pendingSize
		);
		meterRegistry.gauge(
			"queue.active.size",
			Tags.of("gameId", gameId.toString()),
			currentPassedCount
		);

		log.info(
			"action=SLOT_RELEASE gameId={} currentPassed={} availableSlots={} maxCapacity={}",
			gameId, currentPassedCount, Math.max(0, availableSlots), MAX_ALLOWED_COUNT
		);

		if (availableSlots <= 0) return;

		Set<Object> candidates = redisCache.zRange(pendingKey, 0, availableSlots * 2);
		if (candidates == null || candidates.isEmpty()) return;
		int passCount = 0;
		for (Object memberObj : candidates) {
			if (passCount >= availableSlots) break;

			String memberIdStr = (String) memberObj;
			String activeKey = RedisKey.QUEUE_ACTIVE.getKey(gameId, memberIdStr);

			if (redisCache.hasKey(activeKey)) {
				promoteToPassed(gameId, memberIdStr);
				passCount++;
			} else {
				// 유령 유저 제거 로그 및 메트릭
				log.info(
					"action=LEAVE gameId={} userId={} reason=HEARTBEAT_EXPIRED",
					gameId, memberIdStr
				);

				meterRegistry.counter(
					"queue.leave.total",
					"gameId",
					gameId.toString(),
					"reason",
					"heartbeat_expired"
				).increment();

				redisCache.zRemove(pendingKey, memberIdStr);
			}
		}
	}

	private void promoteToPassed(UUID gameId, String memberIdStr) {
		String pendingKey = RedisKey.QUEUE_PENDING.getKey(gameId);
		String passedKey = RedisKey.QUEUE_PASSED.getKey(gameId, memberIdStr);
		String token = UUID.randomUUID().toString();

		redisCache.zRemove(pendingKey, memberIdStr);
		redisCache.set(passedKey, token, RedisKey.QUEUE_PASSED.getTtl());

		// 승격 로그 및 메트릭
		log.info(
			"action=ADMIT gameId={} userId={}",
			gameId, memberIdStr
		);
		meterRegistry.counter(
			"queue.admit.total", "gameId", gameId.toString()
		).increment();
	}

}
