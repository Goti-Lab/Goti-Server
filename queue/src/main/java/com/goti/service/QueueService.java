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
		String pendingZsetKey = RedisKey.QUEUE_PENDING.getKey(gameId); // queue:pending:1
		String passedPattern = "queue:passed:" + gameId; // 통과자 카운트용 패턴
		String userPassKey = RedisKey.QUEUE_PASSED.getKey(gameId, memberId);
		String memberIdStr = memberId.toString();

		String existingToken = redisCache.get(userPassKey, String.class);
		if (existingToken != null) {
			return new QueueValidateResponse(gameId, true, 0L, existingToken);
		}

		Double existingScore = redisCache.zScore(pendingZsetKey, memberId);
		if (existingScore != null) {
			log.info("유저 재진입 감지(순번 밀림) :: memberId = {}", memberId);

			redisCache.zAdd(pendingZsetKey, memberIdStr, System.currentTimeMillis());

			Long newRank = redisCache.zRank(pendingZsetKey, memberIdStr);
			return new QueueValidateResponse(gameId, false, (newRank != null ? newRank + 1 : 1L), null);
		}
		long currentPassedCount = redisCache.countKeys(passedPattern);

		long waitingSize = redisCache.zSize(pendingZsetKey);

		if (currentPassedCount >= MAX_ALLOWED_COUNT || waitingSize > 0) {
			// 대기열(Pending) 등록
			redisCache.zAdd(pendingZsetKey, memberIdStr, System.currentTimeMillis());
			Long rank = redisCache.zRank(pendingZsetKey, memberIdStr);

			return new QueueValidateResponse(gameId, false, (rank != null ? rank + 1 : 1L), null);
		}

		String token = UUID.randomUUID().toString();
		// 키: queue:passed:gameId:userId, 값: token
		redisCache.set(userPassKey, token, RedisKey.QUEUE_PASSED.getTtl());

		// 즉시 진입 허용 및 토큰 발급
		return new QueueValidateResponse(gameId, true, 0L, token);
	}

	public QueueStatusResponse getStatus(UUID gameId, UUID memberId) {
		String memberIdStr = memberId.toString();
		String pendingKey = RedisKey.QUEUE_PENDING.getKey(gameId);
		String passedKey = RedisKey.QUEUE_PASSED.getKey(gameId, memberId);

		// active 키 생성 (TTL은 폴링 주기보다 넉넉하게 10초)
		// 이 키가 없으면 스케줄러가 "이 유저 나갔네?" 하고 대기열에서 지움
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
