package com.goti.queue.scheduler;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.goti.infra.constants.redis.RedisKey;
import com.goti.queue.constants.QueueMetaField;
import com.goti.queue.repository.QueueRedisRepository;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 대기열 상태를 Prometheus Gauge 메트릭으로 주기적 발행.
 * 대시보드(queue-flow, war-room)에서 기대하는 게이지 메트릭:
 *   - queue_waiting_total   현재 대기 인원
 *   - queue_last_sequence   마지막 발급 순번
 *   - queue_max_entry       입장 허용선 (currentAllowedRank)
 */
@Slf4j
@Component
public class QueueMetricsScheduler {

	private final QueueRedisRepository queueRedisRepository;
	private final RedisTemplate<String, Object> redisTemplate;
	private final MeterRegistry meterRegistry;

	private final Map<String, AtomicLong> waitingGauges = new ConcurrentHashMap<>();
	private final Map<String, AtomicLong> sequenceGauges = new ConcurrentHashMap<>();
	private final Map<String, AtomicLong> maxEntryGauges = new ConcurrentHashMap<>();
	private final Map<String, AtomicLong> activeGauges = new ConcurrentHashMap<>();

	public QueueMetricsScheduler(
		QueueRedisRepository queueRedisRepository,
		RedisTemplate<String, Object> redisTemplate,
		MeterRegistry meterRegistry
	) {
		this.queueRedisRepository = queueRedisRepository;
		this.redisTemplate = redisTemplate;
		this.meterRegistry = meterRegistry;
	}

	@Scheduled(fixedDelay = 5000)
	public void publishQueueMetrics() {
		Set<String> metaKeys = redisTemplate.keys("queue:*:meta");
		if (metaKeys == null || metaKeys.isEmpty()) {
			return;
		}

		for (String metaKey : metaKeys) {
			try {
				Map<Object, Object> meta = redisTemplate.opsForHash().entries(metaKey);
				if (meta.isEmpty()) continue;

				// queue:{gameId}:meta → gameId 추출
				String gameId = metaKey.replace("queue:", "").replace(":meta", "");

				long activeCount = longValue(meta.get(QueueMetaField.ACTIVE_COUNT));
				long currentAllowedRank = longValue(meta.get(QueueMetaField.CURRENT_ALLOWED_RANK));
				long waitingCount = queueRedisRepository.countWaitingUsers(UUID.fromString(gameId));

				// sequence는 별도 키
				Object seqVal = redisTemplate.opsForValue().get(RedisKey.QUEUE_SEQUENCE.getKey(UUID.fromString(gameId)));
				long lastSequence = seqVal instanceof Number n ? n.longValue() : 0L;

				getOrCreateGauge(waitingGauges, "queue.waiting.total", gameId).set(waitingCount);
				getOrCreateGauge(sequenceGauges, "queue.last.sequence", gameId).set(lastSequence);
				getOrCreateGauge(maxEntryGauges, "queue.max.entry", gameId).set(currentAllowedRank);
				getOrCreateGauge(activeGauges, "queue.active.size", gameId).set(activeCount);

			} catch (Exception e) {
				log.warn("action=METRICS_PUBLISH_FAIL key={} error={}", metaKey, e.getMessage());
			}
		}
	}

	private AtomicLong getOrCreateGauge(Map<String, AtomicLong> store, String metricName, String matchId) {
		return store.computeIfAbsent(matchId, id -> {
			AtomicLong gauge = new AtomicLong(0);
			meterRegistry.gauge(metricName, Tags.of("match_id", id), gauge);
			return gauge;
		});
	}

	private long longValue(Object value) {
		return value instanceof Number n ? n.longValue() : 0L;
	}
}
