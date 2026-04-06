package com.goti.queue.scheduler;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.data.redis.core.StringRedisTemplate;
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
 *
 * QueueRedisRepository와 동일하게 StringRedisTemplate 사용 (직렬화 일관성)
 */
@Slf4j
@Component
public class QueueMetricsScheduler {

	private final QueueRedisRepository queueRedisRepository;
	private final StringRedisTemplate stringRedisTemplate;
	private final MeterRegistry meterRegistry;

	private final Map<String, AtomicLong> waitingGauges = new ConcurrentHashMap<>();
	private final Map<String, AtomicLong> sequenceGauges = new ConcurrentHashMap<>();
	private final Map<String, AtomicLong> maxEntryGauges = new ConcurrentHashMap<>();
	private final Map<String, AtomicLong> activeGauges = new ConcurrentHashMap<>();

	public QueueMetricsScheduler(
		QueueRedisRepository queueRedisRepository,
		StringRedisTemplate stringRedisTemplate,
		MeterRegistry meterRegistry
	) {
		this.queueRedisRepository = queueRedisRepository;
		this.stringRedisTemplate = stringRedisTemplate;
		this.meterRegistry = meterRegistry;
	}

	@Scheduled(fixedDelay = 5000)
	public void publishQueueMetrics() {
		// KEYS(O(N) blocking) → SCAN(non-blocking cursor)으로 변경
		Set<String> metaKeys = new java.util.HashSet<>();
		try (var cursor = stringRedisTemplate.scan(
				org.springframework.data.redis.core.ScanOptions.scanOptions()
					.match("queue:*:meta").count(100).build())) {
			cursor.forEachRemaining(metaKeys::add);
		}
		if (metaKeys.isEmpty()) {
			return;
		}

		for (String metaKey : metaKeys) {
			try {
				Map<Object, Object> meta = stringRedisTemplate.opsForHash().entries(metaKey);
				if (meta.isEmpty()) continue;

				// queue:{gameId}:meta → gameId 추출
				String gameId = metaKey.replace("queue:", "").replace(":meta", "");

				long activeCount = longFromString(meta.get(QueueMetaField.ACTIVE_COUNT));
				long currentAllowedRank = longFromString(meta.get(QueueMetaField.CURRENT_ALLOWED_RANK));
				long waitingCount = queueRedisRepository.countWaitingUsers(UUID.fromString(gameId));

				// sequence는 별도 키
				String seqVal = stringRedisTemplate.opsForValue().get(RedisKey.QUEUE_SEQUENCE.getKey(UUID.fromString(gameId)));
				long lastSequence = seqVal != null ? Long.parseLong(seqVal) : 0L;

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

	private long longFromString(Object value) {
		return Long.parseLong(String.valueOf(value));
	}
}
