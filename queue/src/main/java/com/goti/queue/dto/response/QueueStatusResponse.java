package com.goti.queue.dto.response;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대기열 상태 조회 응답")
public record QueueStatusResponse(
	@Schema(description = "경기 ID", example = "11111111-1111-1111-1111-111111111111")
	UUID gameId,

	@Schema(description = "전체 수용 가능 인원", example = "3000")
	long maxCapacity,

	@Schema(description = "현재 입장 중인 인원", example = "2100")
	long activeCount,

	@Schema(description = "즉시 추가 입장 가능한 인원", example = "900")
	long availableSlots,

	@Schema(description = "현재 실제 입장 허용된 가장 높은 순번", example = "3000")
	long currentAllowedRank,

	@Schema(description = "프론트에 전달하는 입장 가능 순번", example = "3900")
	long publishedRank,

	@Schema(description = "메타 정보 마지막 갱신 시각", example = "2026-03-25T10:15:30Z")
	Instant updatedAt
) {
}
