package com.goti.queue.repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.goti.queue.constants.QueueMetaField;
import com.goti.queue.constants.QueueRedisKey;
import com.goti.queue.domain.model.QueueEntry;
import com.goti.queue.domain.model.QueueMeta;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class QueueRedisRepository {

	private final RedisTemplate<String, Object> redisTemplate;

	public QueueEntry getEntry(UUID gameId, UUID userId) {
		Object value = redisTemplate.opsForValue().get(QueueRedisKey.ENTRY.getKey(gameId, userId));
		return value instanceof QueueEntry entry ? entry : null;
	}

	public void saveEntry(UUID gameId, UUID userId, QueueEntry entry, Duration ttl) {
		redisTemplate.opsForValue().set(
			QueueRedisKey.ENTRY.getKey(gameId, userId),
			entry,
			ttl
		);
	}

	public void deleteEntry(UUID gameId, UUID userId) {
		redisTemplate.delete(QueueRedisKey.ENTRY.getKey(gameId, userId));
	}

	public long nextSequence(UUID gameId) {
		Long sequence = redisTemplate.opsForValue().increment(QueueRedisKey.SEQUENCE.getKey(gameId));
		return sequence == null ? 1L : sequence;
	}

	public void addWaiting(UUID gameId, UUID userId, long queueNumber) {
		redisTemplate.opsForZSet().add(
			QueueRedisKey.WAITING.getKey(gameId),
			userId.toString(),
			queueNumber
		);
	}

	public void removeWaiting(UUID gameId, UUID userId) {
		redisTemplate.opsForZSet().remove(
			QueueRedisKey.WAITING.getKey(gameId),
			userId.toString()
		);
	}

	public void initializeMetaIfAbsent(UUID gameId, long maxCapacity) {
		String metaKey = QueueRedisKey.META.getKey(gameId);
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
		Map<Object, Object> meta = redisTemplate.opsForHash().entries(QueueRedisKey.META.getKey(gameId));
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

	private long longValue(Object value) {
		return ((Number)value).longValue();
	}
}
