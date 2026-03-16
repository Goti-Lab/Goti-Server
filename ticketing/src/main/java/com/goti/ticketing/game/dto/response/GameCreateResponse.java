package com.goti.ticketing.game.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.goti.ticketing.constants.GameStatus;
import com.goti.ticketing.constants.LeagueType;

import com.goti.ticketing.constants.TicketingStatus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "경기 등록 응답")
public record GameCreateResponse(

	@Schema(description = "경기 ID", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
	UUID gameId,

	@Schema(description = "홈팀 ID", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
	UUID homeTeamId,

	@Schema(description = "원정팀 ID", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
	UUID awayTeamId,

	@Schema(description = "구장 ID", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
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
	GameStatus gameStatus,

	@Schema(
		description = "게임 예매 오픈 일시 (yyyy-MM-dd HH:mm)",
		example = "2026-08-20 11:00"
	)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime ticketingOpenedAt,

	@Schema(
		description = "게임 예매 마감 일시 (yyyy-MM-dd HH:mm)",
		example = "2026-08-26 19:00"
	)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime ticketingEndAt,

	@Schema(description = "게임 예매 상태", example = "SCHEDULED")
	TicketingStatus ticketingStatus

) {
	public static GameCreateResponse from(
		UUID gameId,
		UUID homeTeamId,
		UUID awayTeamId,
		UUID stadiumId,
		LocalDateTime startAt,
		LeagueType leagueType,
		GameStatus gameStatus,
		LocalDateTime ticketingOpenedAt,
		LocalDateTime ticketingEndAt,
		TicketingStatus ticketingStatus
	) {
		return new GameCreateResponse(
			gameId,
			homeTeamId,
			awayTeamId,
			stadiumId,
			startAt,
			leagueType,
			gameStatus,
			ticketingOpenedAt,
			ticketingEndAt,
			ticketingStatus
		);
	}
}
