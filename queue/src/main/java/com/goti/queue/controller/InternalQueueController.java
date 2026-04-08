package com.goti.queue.controller;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goti.queue.service.QueueCleanupService;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

/**
 * 내부 전용 Queue 관리 API.
 * - Swagger(OpenAPI) 문서에서 완전 제외 (@Hidden)
 * - Istio Gateway에서 /internal/* 라우팅 없음 → 외부 접근 불가
 * - queue.cleanup-api.enabled=true 일 때만 활성화 (기본 비활성)
 *
 * WARNING: cleanup은 해당 game의 모든 대기열 데이터를 삭제함.
 * 진행 중인 game에서 호출하면 활성 사용자 전원이 강제 퇴장됨.
 * 안정화 후 설정값으로 비활성화할 것.
 */
@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/queue")
@ConditionalOnProperty(name = "queue.cleanup-api.enabled", havingValue = "true")
public class InternalQueueController {

	private final QueueCleanupService queueCleanupService;

	@DeleteMapping("/{gameId}")
	public ResponseEntity<Void> cleanupGame(@PathVariable UUID gameId) {
		queueCleanupService.cleanupGame(gameId);
		return ResponseEntity.noContent().build();
	}
}
