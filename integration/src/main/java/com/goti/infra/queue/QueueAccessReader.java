package com.goti.infra.queue;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goti.infra.constants.redis.RedisKey;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QueueAccessReader {
	private static final String ADMITTED_STATUS = "ADMITTED";

	private final RedisTemplate<String, Object> redisTemplate;
	private final StringRedisTemplate stringRedisTemplate;
	private final ObjectMapper objectMapper;

	public boolean isAdmitted(UUID gameId, UUID userId) {
		QueueEntryCacheValue entry = getEntry(gameId, userId);
		if (entry == null || !ADMITTED_STATUS.equals(entry.status())) {
			return false;
		}

		// Lua 스크립트(stringRedisTemplate)가 plain string으로 SADD하므로 동일한 serializer로 조회
		Boolean activeUser = stringRedisTemplate.opsForSet().isMember(
			RedisKey.QUEUE_ACTIVE_USERS.getKey(gameId),
			userId.toString()
		);
		return Boolean.TRUE.equals(activeUser);
	}

	private QueueEntryCacheValue getEntry(UUID gameId, UUID userId) {
		Object value = redisTemplate.opsForValue().get(RedisKey.QUEUE_ENTRY.getKey(gameId, userId));
		if (value == null) {
			return null;
		}
		if (value instanceof QueueEntryCacheValue entry) {
			return entry;
		}
		return objectMapper.convertValue(value, QueueEntryCacheValue.class);
	}

	private record QueueEntryCacheValue(
		long queueNumber,
		Instant issuedAt,
		String status
	) {
	}
}
