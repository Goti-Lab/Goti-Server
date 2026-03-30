package com.goti.ticketing.game.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.goti.ticketing.constants.LeagueType;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

@Schema(description = "경기 일정 생성 요청")
public record GameCreateRequest(
	@Schema(description = "홈팀 ID", example = "11111111-1111-1111-1111-111111111111")
	@NotNull(message = "홈팀 ID는 필수 항목입니다.")
	UUID homeTeamId,

	@Schema(description = "원정팀 ID", example = "22222222-2222-2222-2222-222222222222")
	@NotNull(message = "원정팀 ID는 필수 항목입니다.")
	UUID awayTeamId,

	@Schema(description = "구장 ID", example = "33333333-3333-3333-3333-333333333333")
	@NotNull(message = "구장 ID는 필수 항목입니다.")
	UUID stadiumId,

	@Schema(
		description = "경기 시작 일시 (연월일 및 시분)",
		example = "2026-03-27 18:30",
		pattern = "yyyy-MM-dd HH:mm"
	)
	@NotNull(message = "경기 시작 일시는 필수 항목입니다.")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime startAt,

	@Schema(description = "리그 종류", example = "REGULAR")
	@NotNull(message = "리그 종류는 필수 항목입니다.")
	LeagueType leagueType

) {
}
