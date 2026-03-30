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

	// Lua script ARGV 직렬화용 — GenericJackson2JsonRedisSerializer는 ARGV를
	// JSON 직렬화하여 Hash 필드명과 불일치. StringRedisTemplate은 plain string 전달.
	private final org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

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
		if (stringRedisTemplate.hasKey(metaKey)) {
			return;
		}

		// TODO: 구조가 잡힌 뒤 queue open/init 단계에서만 메타를 생성하도록 변경
		stringRedisTemplate.opsForHash().putAll(metaKey, Map.of(
			QueueMetaField.MAX_CAPACITY, String.valueOf(maxCapacity),
			QueueMetaField.ACTIVE_COUNT, "0",
			QueueMetaField.PUBLISHED_RANK, "0",
			QueueMetaField.CURRENT_ALLOWED_RANK, "0",
			QueueMetaField.LAST_ENTERED_RANK, "0",
			QueueMetaField.UPDATED_AT, Instant.now().toString()
		));
	}

	public QueueMeta getMeta(UUID gameId) {
		Map<Object, Object> meta = stringRedisTemplate.opsForHash().entries(RedisKey.QUEUE_META.getKey(gameId));
		if (meta.isEmpty()) {
			return null;
		}

		return new QueueMeta(
			longFromString(meta.get(QueueMetaField.MAX_CAPACITY)),
			longFromString(meta.get(QueueMetaField.ACTIVE_COUNT)),
			longFromString(meta.get(QueueMetaField.PUBLISHED_RANK)),
			longFromString(meta.get(QueueMetaField.CURRENT_ALLOWED_RANK)),
			longFromString(meta.get(QueueMetaField.LAST_ENTERED_RANK)),
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

		// WHY: redisTemplate(GenericJackson2Json)은 ARGV를 JSON 직렬화하여
		// Hash 필드명("maxCapacity")과 불일치. StringRedisTemplate으로 plain string 전달.
		Long result = stringRedisTemplate.execute(
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
		Long result = stringRedisTemplate.opsForHash().increment(
			RedisKey.QUEUE_META.getKey(gameId), QueueMetaField.ACTIVE_COUNT, 1L
		);
		stringRedisTemplate.opsForHash().put(
			RedisKey.QUEUE_META.getKey(gameId), QueueMetaField.UPDATED_AT, Instant.now().toString()
		);
		return result == null ? 1L : result;
	}

	public long decrementActiveCount(UUID gameId) {
		Long result = stringRedisTemplate.opsForHash().increment(
			RedisKey.QUEUE_META.getKey(gameId), QueueMetaField.ACTIVE_COUNT, -1L
		);
		stringRedisTemplate.opsForHash().put(
			RedisKey.QUEUE_META.getKey(gameId), QueueMetaField.UPDATED_AT, Instant.now().toString()
		);
		return result == null ? 0L : Math.max(0L, result);
	}

	/**
	 * lastEnteredRank / currentAllowedRank를 원자적으로 갱신.
	 * 동시 seat-enter 시 race condition 방지 — max(기존값, 새값)으로만 갱신.
	 * WHY: putAll은 last-write-wins라서 늦게 도착한 낮은 queueNumber가 높은 값을 덮어쓸 수 있음.
	 */
	public void updateSeatEnterMeta(UUID gameId, long lastEnteredRank) {
		String script =
			"local key = KEYS[1] " +
			"local newRank = tonumber(ARGV[1]) " +
			"local curLast = tonumber(redis.call('HGET', key, ARGV[2]) or '0') " +
			"local curAllowed = tonumber(redis.call('HGET', key, ARGV[3]) or '0') " +
			"if newRank > curLast then " +
			"  redis.call('HSET', key, ARGV[2], newRank) " +
			"end " +
			"if newRank > curAllowed then " +
			"  redis.call('HSET', key, ARGV[3], newRank) " +
			"end " +
			"redis.call('HSET', key, ARGV[4], ARGV[5]) " +
			"return 1";

		stringRedisTemplate.execute(
			org.springframework.data.redis.core.script.RedisScript.of(script, Long.class),
			java.util.List.of(RedisKey.QUEUE_META.getKey(gameId)),
			String.valueOf(lastEnteredRank),
			QueueMetaField.LAST_ENTERED_RANK,
			QueueMetaField.CURRENT_ALLOWED_RANK,
			QueueMetaField.UPDATED_AT,
			Instant.now().toString()
		);
	}

	public void updateStatusMeta(UUID gameId, long currentAllowedRank, long publishedRank, Instant updatedAt) {
		stringRedisTemplate.opsForHash().putAll(RedisKey.QUEUE_META.getKey(gameId), Map.of(
			QueueMetaField.CURRENT_ALLOWED_RANK, String.valueOf(currentAllowedRank),
			QueueMetaField.PUBLISHED_RANK, String.valueOf(publishedRank),
			QueueMetaField.UPDATED_AT, updatedAt.toString()
		));
	}

	private long longFromString(Object value) {
		return Long.parseLong(String.valueOf(value));
	}

	private String expirationMember(UUID gameId, UUID userId) {
		return gameId + ":" + userId;
	}
}
