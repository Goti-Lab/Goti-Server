package com.goti.service.scheduler;

import com.goti.infra.cache.RedisCache;

import com.goti.infra.constants.redis.RedisKey;

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
	private static final long MAX_ALLOWED_COUNT = 100;
	private static final String COLON = ":";

	@Scheduled(fixedDelay = 1000)
	public void processQueue() {
		Set<String> pendingKeys = redisCache.getKeys(RedisKey.QUEUE_PENDING.getPrefix() + "*");
		if (pendingKeys == null || pendingKeys.isEmpty())
			return;

		for (String pendingKey : pendingKeys) {
			try {
				String cachedGameId = pendingKey.replace(RedisKey.QUEUE_PENDING.getPrefix(), "");
				UUID gameId = UUID.fromString(cachedGameId);
				processGameQueue(gameId, pendingKey);
			}
			catch (Exception e) {
				log.error("대기열 처리 중 오류 발생 - Key: {}, Error: {}", pendingKey, e.getMessage());
			}
		}

	}

	private void processGameQueue(UUID gameId, String pendingKey) {
		String passedPattern = RedisKey.QUEUE_PASSED.getPrefix() + gameId + COLON;
		long currentPassedCount = redisCache.countKeys(passedPattern);
		long availableSlots = MAX_ALLOWED_COUNT - currentPassedCount;

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
				log.info("유령 유저 제거 :: gameId = {}, memberId = {}", gameId, memberIdStr);
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

		log.info("대기자 승격 완료 :: gameId = {}, memberId = {}", gameId, memberIdStr);
	}

}
