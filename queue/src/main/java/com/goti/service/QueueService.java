package com.goti.service;

import com.goti.dto.response.QueueStatusResponse;
import com.goti.dto.response.QueueValidateResponse;
import com.goti.infra.cache.RedisCache;
import com.goti.infra.constants.redis.RedisKey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

	private final RedisCache redisCache;
	private static final long MAX_ALLOWED_COUNT = 100;


	public QueueValidateResponse validate(UUID gameId, UUID memberId) {
		String pendingKey = RedisKey.QUEUE_PENDING.getKey(gameId);
		String passedPattern = RedisKey.QUEUE_PASSED.getPrefix() + gameId;
		String passedKey = RedisKey.QUEUE_PASSED.getKey(gameId, memberId);
		String memberIdStr = memberId.toString();

		String existingToken = redisCache.get(passedKey, String.class);
		if (existingToken != null) {
			return new QueueValidateResponse(gameId, true, 0L, existingToken);
		}

		Double existingScore = redisCache.zScore(pendingKey, memberId);
		if (existingScore != null) {

			redisCache.zAdd(pendingKey, memberIdStr, System.currentTimeMillis());

			Long newRank = redisCache.zRank(pendingKey, memberIdStr);
			return new QueueValidateResponse(gameId, false, (newRank != null ? newRank + 1 : 1L), null);
		}
		long currentPassedCount = redisCache.countKeys(passedPattern);

		long waitingSize = redisCache.zSize(pendingKey);

		if (currentPassedCount >= MAX_ALLOWED_COUNT || waitingSize > 0) {
			redisCache.zAdd(pendingKey, memberIdStr, System.currentTimeMillis());
			Long rank = redisCache.zRank(pendingKey, memberIdStr);

			return new QueueValidateResponse(gameId, false, (rank != null ? rank + 1 : 1L), null);
		}

		String token = UUID.randomUUID().toString();
		redisCache.set(passedKey, token, RedisKey.QUEUE_PASSED.getTtl());

		return new QueueValidateResponse(gameId, true, 0L, token);
	}

	public QueueStatusResponse getStatus(UUID gameId, UUID memberId) {
		String memberIdStr = memberId.toString();
		String pendingKey = RedisKey.QUEUE_PENDING.getKey(gameId);
		String passedKey = RedisKey.QUEUE_PASSED.getKey(gameId, memberId);
		String activeKey = RedisKey.QUEUE_ACTIVE.getKey(gameId, memberIdStr);
		redisCache.set(activeKey, "active", RedisKey.QUEUE_ACTIVE.getTtl());

		String token = redisCache.get(passedKey, String.class);
		if (token != null) {
			return new QueueStatusResponse(gameId, true, 0L, token);
		}

		Long rank = redisCache.zRank(pendingKey, memberIdStr);
		if (rank == null) {
			throw new RuntimeException("대기열 정보가 만료되었습니다. 다시 진입해주세요.");
		}

		return new QueueStatusResponse(gameId, false, rank + 1, null);
	}


}
