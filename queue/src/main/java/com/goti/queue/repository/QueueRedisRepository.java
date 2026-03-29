package com.goti.queue.repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goti.infra.constants.redis.RedisKey;
import com.goti.queue.constants.QueueMetaField;
import com.goti.queue.domain.model.QueueEntry;
import com.goti.queue.domain.model.QueueMeta;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class QueueRedisRepository {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	public QueueEntry getEntry(UUID gameId, UUID userId) {
		Object value = redisTemplate.opsForValue().get(RedisKey.QUEUE_ENTRY.getKey(gameId, userId));
		if (value == null) {
			return null;
		}
		if (value instanceof QueueEntry entry) {
			return entry;
		}
		return objectMapper.convertValue(value, QueueEntry.class);
	}

	public void saveEntry(UUID gameId, UUID userId, QueueEntry entry, Duration ttl) {
		redisTemplate.opsForValue().set(
			RedisKey.QUEUE_ENTRY.getKey(gameId, userId),
			entry,
			ttl
		);
	}

	public void deleteEntry(UUID gameId, UUID userId) {
		redisTemplate.delete(RedisKey.QUEUE_ENTRY.getKey(gameId, userId));
	}

	public long nextSequence(UUID gameId) {
		Long sequence = redisTemplate.opsForValue().increment(RedisKey.QUEUE_SEQUENCE.getKey(gameId));
		return sequence == null ? 1L : sequence;
	}

	public void addWaiting(UUID gameId, UUID userId, long queueNumber) {
		redisTemplate.opsForZSet().add(
			RedisKey.QUEUE_WAITING.getKey(gameId),
			userId.toString(),
			queueNumber
		);
	}

	public void removeWaiting(UUID gameId, UUID userId) {
		redisTemplate.opsForZSet().remove(
			RedisKey.QUEUE_WAITING.getKey(gameId),
			userId.toString()
		);
	}

	public long countWaitingUsers(UUID gameId) {
		Long count = redisTemplate.opsForZSet().zCard(RedisKey.QUEUE_WAITING.getKey(gameId));
		return count == null ? 0L : count;
	}

	public boolean isActiveUser(UUID gameId, UUID userId) {
		Boolean member = redisTemplate.opsForSet().isMember(
			RedisKey.QUEUE_ACTIVE_USERS.getKey(gameId),
			userId.toString()
		);
		return Boolean.TRUE.equals(member);
	}

	public void addActiveUser(UUID gameId, UUID userId) {
		redisTemplate.opsForSet().add(
			RedisKey.QUEUE_ACTIVE_USERS.getKey(gameId),
			userId.toString()
		);
	}

	public void removeActiveUser(UUID gameId, UUID userId) {
		redisTemplate.opsForSet().remove(
			RedisKey.QUEUE_ACTIVE_USERS.getKey(gameId),
			userId.toString()
		);
	}

	public void addExpirationUser(UUID gameId, UUID userId, Instant expiresAt) {
		redisTemplate.opsForZSet().add(
			RedisKey.QUEUE_EXPIRATION_USERS.getKey(""),
			expirationMember(gameId, userId),
			expiresAt.toEpochMilli()
		);
	}

	public void removeExpirationUser(UUID gameId, UUID userId) {
		redisTemplate.opsForZSet().remove(
			RedisKey.QUEUE_EXPIRATION_USERS.getKey(""),
			expirationMember(gameId, userId)
		);
	}

	public Set<Object> getExpiredUsers(Instant now) {
		return redisTemplate.opsForZSet().rangeByScore(
			RedisKey.QUEUE_EXPIRATION_USERS.getKey(""),
			0,
			now.toEpochMilli()
		);
	}

	public void initializeMetaIfAbsent(UUID gameId, long maxCapacity) {
		String metaKey = RedisKey.QUEUE_META.getKey(gameId);
		if (redisTemplate.hasKey(metaKey)) {
			return;
		}

		// TODO: 구조가 잡힌 뒤 queue open/init 단계에서만 메타를 생성하도록 변경
		redisTemplate.opsForHash().putAll(metaKey, Map.of(
			QueueMetaField.MAX_CAPACITY, maxCapacity,
			QueueMetaField.ACTIVE_COUNT, 0L,
			QueueMetaField.PUBLISHED_RANK, 0L,
			QueueMetaField.CURRENT_ALLOWED_RANK, 0L,
			QueueMetaField.LAST_ENTERED_RANK, 0L,
			QueueMetaField.UPDATED_AT, Instant.now().toString()
		));
	}

	public QueueMeta getMeta(UUID gameId) {
		Map<Object, Object> meta = redisTemplate.opsForHash().entries(RedisKey.QUEUE_META.getKey(gameId));
		if (meta.isEmpty()) {
			return null;
		}

		return new QueueMeta(
			longValue(meta.get(QueueMetaField.MAX_CAPACITY)),
			longValue(meta.get(QueueMetaField.ACTIVE_COUNT)),
			longValue(meta.get(QueueMetaField.PUBLISHED_RANK)),
			longValue(meta.get(QueueMetaField.CURRENT_ALLOWED_RANK)),
			longValue(meta.get(QueueMetaField.LAST_ENTERED_RANK)),
			Instant.parse(String.valueOf(meta.get(QueueMetaField.UPDATED_AT)))
		);
	}

	/**
	 * activeCount < maxCapacity일 때만 원자적으로 increment.
	 * Lua Script로 check-and-increment를 단일 Redis 명령으로 실행.
	 * @return true: 승격 성공, false: 수용량 초과 (increment 안 함)
	 */
	public boolean tryIncrementActiveCount(UUID gameId) {
		String script =
			"local key = KEYS[1] " +
			"local active = tonumber(redis.call('HGET', key, ARGV[1]) or '0') " +
			"local max = tonumber(redis.call('HGET', key, ARGV[2]) or '0') " +
			"if active < max then " +
			"  redis.call('HINCRBY', key, ARGV[1], 1) " +
			"  redis.call('HSET', key, ARGV[3], ARGV[4]) " +
			"  return 1 " +
			"else " +
			"  return 0 " +
			"end";

		Long result = redisTemplate.execute(
			org.springframework.data.redis.core.script.RedisScript.of(script, Long.class),
			java.util.List.of(RedisKey.QUEUE_META.getKey(gameId)),
			QueueMetaField.ACTIVE_COUNT,
			QueueMetaField.MAX_CAPACITY,
			QueueMetaField.UPDATED_AT,
			Instant.now().toString()
		);
		return result != null && result == 1L;
	}

	public long incrementActiveCount(UUID gameId) {
		Long result = redisTemplate.opsForHash().increment(
			RedisKey.QUEUE_META.getKey(gameId), QueueMetaField.ACTIVE_COUNT, 1L
		);
		redisTemplate.opsForHash().put(
			RedisKey.QUEUE_META.getKey(gameId), QueueMetaField.UPDATED_AT, Instant.now().toString()
		);
		return result == null ? 1L : result;
	}

	public long decrementActiveCount(UUID gameId) {
		Long result = redisTemplate.opsForHash().increment(
			RedisKey.QUEUE_META.getKey(gameId), QueueMetaField.ACTIVE_COUNT, -1L
		);
		redisTemplate.opsForHash().put(
			RedisKey.QUEUE_META.getKey(gameId), QueueMetaField.UPDATED_AT, Instant.now().toString()
		);
		return result == null ? 0L : Math.max(0L, result);
	}

	public void updateSeatEnterMeta(UUID gameId, long lastEnteredRank) {
		String metaKey = RedisKey.QUEUE_META.getKey(gameId);
		redisTemplate.opsForHash().putAll(metaKey, Map.of(
			QueueMetaField.LAST_ENTERED_RANK, lastEnteredRank,
			QueueMetaField.CURRENT_ALLOWED_RANK, lastEnteredRank,
			QueueMetaField.UPDATED_AT, Instant.now().toString()
		));
	}

	public void updateStatusMeta(UUID gameId, long currentAllowedRank, long publishedRank, Instant updatedAt) {
		redisTemplate.opsForHash().putAll(RedisKey.QUEUE_META.getKey(gameId), Map.of(
			QueueMetaField.CURRENT_ALLOWED_RANK, currentAllowedRank,
			QueueMetaField.PUBLISHED_RANK, publishedRank,
			QueueMetaField.UPDATED_AT, updatedAt.toString()
		));
	}

	private long longValue(Object value) {
		return ((Number)value).longValue();
	}

	private String expirationMember(UUID gameId, UUID userId) {
		return gameId + ":" + userId;
	}
}
