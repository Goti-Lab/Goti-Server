package com.goti.queue.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.goti.queue.repository.QueueRedisRepository;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Game 단위 대기열 Redis 데이터 일괄 정리 서비스.
 * WARNING: 진행 중인 game에서 호출하면 활성 사용자 전원이 강제 퇴장됨.
 * game 종료 후 또는 dev 테스트 정리 용도로만 사용할 것.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueueCleanupService {

	private final QueueRedisRepository queueRedisRepository;
	private final MeterRegistry meterRegistry;

	/**
	 * 해당 gameId의 모든 queue 키를 일괄 삭제.
	 * 삭제 대상: sequence, meta, waiting, active-users, expiration:users 내 해당 game 엔트리
	 */
	public void cleanupGame(UUID gameId) {
		// 파괴적 작업이므로 의도적으로 warn 레벨
		log.warn("action=QUEUE_CLEANUP gameId={}", gameId);
		// 메트릭에 gameId 태그 미포함 — UUID 카디널리티가 높아 Prometheus 시계열 폭증 방지. gameId는 로그로 추적.
		Timer.Sample sample = Timer.start(meterRegistry);
		try {
			queueRedisRepository.cleanupGame(gameId);
			meterRegistry.counter("queue.cleanup").increment();
		} catch (Exception e) {
			log.error("action=QUEUE_CLEANUP_FAILED gameId={} error={}", gameId, e.getMessage());
			meterRegistry.counter("queue.cleanup.error").increment();
			throw e;
		} finally {
			sample.stop(meterRegistry.timer("queue.cleanup.duration"));
		}
	}
}
