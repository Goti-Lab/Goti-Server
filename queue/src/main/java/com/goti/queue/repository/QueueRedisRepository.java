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

	public boolean isActiveUser(UUID gameId, UUID userId) {
		Boolean member = redisTemplate.opsForSet().isMember(
			RedisKey.QUEUE_ACTIVE_USERS.getKey(gameId),
			userId.toString()
		);
		return Boolean.TRUE.equals(member);
	}

	public void addActiveUser(UUID gameId, UUID userId, Duration ttl) {
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

	public void updateSeatEnterMeta(
		UUID gameId,
		long activeCount,
		long lastEnteredRank,
		Instant updatedAt
	) {
		redisTemplate.opsForHash().putAll(RedisKey.QUEUE_META.getKey(gameId), Map.of(
			QueueMetaField.ACTIVE_COUNT, activeCount,
			QueueMetaField.LAST_ENTERED_RANK, lastEnteredRank,
			QueueMetaField.UPDATED_AT, updatedAt.toString()
		));
	}

	public void updateLeaveMeta(UUID gameId, long activeCount, Instant updatedAt) {
		redisTemplate.opsForHash().putAll(RedisKey.QUEUE_META.getKey(gameId), Map.of(
			QueueMetaField.ACTIVE_COUNT, activeCount,
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
