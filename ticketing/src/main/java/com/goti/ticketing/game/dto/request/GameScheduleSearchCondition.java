package com.goti.ticketing.game.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "게임 일정 조회 요청")
public record GameScheduleSearchCondition(
	@Schema(description = "야구구단(팀) ID", example = "550e8400-e29b-41d4-a716-446655440000")
	UUID teamId,

	@Schema(description = "조회 연도", example = "2026")
	Integer year,

	@Schema(description = "조회 월", example = "3")
	Integer month,

	@Schema(description = "조회 주차 (선택)", example = "3")
	Integer week,

	@Schema(description = "오늘 일정 여부 (true일 경우 오늘 데이터만 조회)", example = "false")
	boolean today
) {

	public boolean hasDateCondition() {
		return year != null && month != null;
	}
}
