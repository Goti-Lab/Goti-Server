package com.goti.service;

import com.goti.dto.response.QueueStatusResponse;
import com.goti.dto.response.QueueValidateResponse;
import com.goti.infra.cache.RedisCache;
import com.goti.infra.constants.redis.RedisKey;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

	private final RedisCache redisCache;
	private final MeterRegistry meterRegistry;
	private static final long MAX_ALLOWED_COUNT = 100;

	public QueueValidateResponse validate(UUID gameId, UUID memberId) {
		// 메트릭: 전체 진입 시도 횟수 카운트
		meterRegistry.counter(
			"queue.enter.total", "gameId", gameId.toString()
		).increment();

		String pendingKey = RedisKey.QUEUE_PENDING.getKey(gameId);
		String passedKey = RedisKey.QUEUE_PASSED.getKey(gameId, memberId);
		String memberIdStr = memberId.toString();

		// 기존 권한 박탈 (재진입 시)
		if (redisCache.hasKey(passedKey)) {
			log.info(
				"action=INVALIDATE gameId={} userId={} reason=RE_ENTER",
				gameId, memberId
			);
			redisCache.delete(passedKey);
		}

		// 대기열 페널티 (새로고침 시)
		Double existingScore = redisCache.zScore(pendingKey, memberIdStr);
		if (existingScore != null) {
			redisCache.zAdd(pendingKey, memberIdStr, (double) System.currentTimeMillis());
			Long rank = redisCache.zRank(pendingKey, memberIdStr);
			long currentRank = (rank != null ? rank + 1 : 1L);

			log.info(
				"action=ENTER gameId={} userId={} result=WAITING rank={} reason=REFRESH_PENALTY",
				gameId, memberId, currentRank
			);

			return new QueueValidateResponse(gameId, false, currentRank, null);
		}

		// 수용량 체크
		String passedPattern = RedisKey.QUEUE_PASSED.getPrefix() + gameId;
		long currentPassedCount = redisCache.countKeys(passedPattern);
		long waitingSize = redisCache.zSize(pendingKey);

		if (currentPassedCount >= MAX_ALLOWED_COUNT || waitingSize > 0) {
			redisCache.zAdd(pendingKey, memberIdStr, (double) System.currentTimeMillis());
			Long rank = redisCache.zRank(pendingKey, memberIdStr);
			long currentRank = (rank != null ? rank + 1 : 1L);

			log.info(
				"action=ENTER gameId={} userId={} result=WAITING rank={} reason=FULL_OR_QUEUE_EXISTS",
				gameId, memberId, currentRank
			);

			return new QueueValidateResponse(gameId, false, currentRank, null);
		}

		// 즉시 통과
		String token = UUID.randomUUID().toString();
		redisCache.set(passedKey, token, RedisKey.QUEUE_PASSED.getTtl());

		meterRegistry.counter(
			"queue.admit.total", "gameId", gameId.toString()
		).increment();

		log.info(
			"action=ENTER gameId={} userId={} result=PASSED_IMMEDIATELY",
			gameId, memberId
		);

		return new QueueValidateResponse(gameId, true, 0L, token);
	}

	public QueueStatusResponse getStatus(UUID gameId, UUID memberId) {
		String memberIdStr = memberId.toString();
		String pendingKey = RedisKey.QUEUE_PENDING.getKey(gameId);
		String passedKey = RedisKey.QUEUE_PASSED.getKey(gameId, memberId);

		// Heartbeat 갱신 (유령 유저 방지용)
		String activeKey = RedisKey.QUEUE_ACTIVE.getKey(gameId, memberIdStr);
		redisCache.set(
			activeKey, "active", RedisKey.QUEUE_ACTIVE.getTtl()
		);

		String token = redisCache.get(passedKey, String.class);
		if (token != null) {
			log.info(
				"action=CHECK_STATUS gameId={} userId={} result=PASSED",
				gameId, memberId
			);
			return new QueueStatusResponse(gameId, true, 0L, token);
		}

		Long rank = redisCache.zRank(pendingKey, memberIdStr);
		if (rank == null) {
			log.warn(
				"action=CHECK_STATUS gameId={} userId={} result=EXPIRED",
				gameId, memberId
			);
			throw new RuntimeException("대기열 정보가 만료되었습니다. 다시 진입해주세요.");
		}

		log.info(
			"action=CHECK_STATUS gameId={} userId={} result=WAITING rank={}",
			gameId, memberId, rank + 1
		);

		return new QueueStatusResponse(gameId, false, rank + 1, null);
	}


}
