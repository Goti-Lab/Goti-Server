package com.goti.ticketing.seat.dto.response;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "경기 좌석 상태 초기화 응답")
public record GameSeatStatusInitResponse(
	@Schema(description = "경기 ID", example = "11111111-1111-1111-1111-111111111111")
	UUID gameId,
	@Schema(description = "새로 생성된 좌석 상태 개수", example = "480")
	int createdCount,
	@Schema(description = "이미 존재해 생성하지 않고 건너뛴 좌석 상태 개수", example = "0")
	int skippedCount
) {
}
