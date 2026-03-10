package com.goti.game.dto.response;

import com.goti.constants.GameStatus;
import com.goti.constants.LeagueType;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "경기 등록 응답")
public record GameCreateResponse(

	@Schema(description = "경기 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
	UUID gameId,

	@Schema(description = "홈팀 ID", example = "11111111-1111-1111-1111-111111111111")
	UUID homeTeamId,

	@Schema(description = "원정팀 ID", example = "22222222-2222-2222-2222-222222222222")
	UUID awayTeamId,

	@Schema(description = "구장 ID", example = "33333333-3333-3333-3333-333333333333")
	UUID stadiumId,

	@Schema(
		description = "경기 시작 일시 (연월일 및 시분)",
		example = "2026-03-27 18:30",
		pattern = "yyyy-MM-dd HH:mm"
	)
	LocalDateTime startAt,

	@Schema(description = "리그 종류", example = "REGULAR")
	LeagueType leagueType,

	@Schema(description = "경기 상태", example = "SCHEDULED")
	GameStatus gameStatus

) {
	public static GameCreateResponse from(
		UUID gameId,
		UUID homeTeamId,
		UUID awayTeamId,
		UUID stadiumId,
		LocalDateTime startAt,
		LeagueType leagueType,
		GameStatus gameStatus
	) {
		return new GameCreateResponse(
			gameId, homeTeamId, awayTeamId, stadiumId, startAt, leagueType, gameStatus
		);
	}
}
