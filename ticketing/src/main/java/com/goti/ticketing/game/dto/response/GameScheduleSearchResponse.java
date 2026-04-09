package com.goti.ticketing.game.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.goti.ticketing.constants.GameResult;
import com.goti.ticketing.constants.GameStatus;

import com.goti.ticketing.constants.LeagueType;
import com.goti.ticketing.constants.TicketingStatus;

import com.goti.ticketing.game.dto.response.repository.GameScheduleQueryModel;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "야구 경기 일정 및 통합 상태 조회 응답")
public record GameScheduleSearchResponse(
	@Schema(description = "게임 식별 ID", example = "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6")
	UUID gameId,

	@Schema(description = "경기 시작 일시 (yyyy-MM-dd HH:mm)", example = "2026-03-20 18:30")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime startAt,

	@Schema(description = "리그 타입", example = "REGULAR")
	LeagueType leagueType,

	@Schema(description = "홈 팀 표시명", example = "삼성")
	String homeTeamDisplayName,

	@Schema(description = "원정 팀 표시명", example = "KIA")
	String awayTeamDisplayName,

	@Schema(description = "구장 위치", example = "대구")
	String stadiumLocation,

	@Schema(description = "경기 진행 상태", example = "FINISHED")
	GameStatus gameStatus,

	@Schema(description = "홈팀 현재 점수", example = "8")
	Integer homeTeamScore,

	@Schema(description = "원정팀 현재 점수", example = "5")
	Integer awayTeamScore,

	@Schema(description = "경기 결과", example = "WIN")
	GameResult gameResult,

	@Schema(description = "예매(티켓팅) 상태", example = "AVAILABLE")
	TicketingStatus ticketingStatus,

	@Schema(description = "예매(티켓팅) 오픈 일시 (yyyy-MM-dd HH:mm)", example = "2026-03-15 11:00")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime ticketingOpenedAt,

	@Schema(description = "예매(티켓팅) 마감 일시 (yyyy-MM-dd HH:mm)", example = "2026-03-27 19:00")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime ticketingEndAt,

	@Schema(description = "잔여 좌석 수", example = "12543")
	Long remainingSeatCount
) {
	public static GameScheduleSearchResponse from(
		GameScheduleQueryModel model,
		String homeTeamDisplayName,
		String awayTeamDisplayName,
		String stadiumLocation,
		Long seatCount
	) {
		return new GameScheduleSearchResponse(
			model.gameId(),
			model.startAt(),
			model.leagueType(),
			homeTeamDisplayName,
			awayTeamDisplayName,
			stadiumLocation,
			model.gameStatus(),
			model.homeTeamScore(),
			model.awayTeamScore(),
			model.gameResult(),
			model.ticketingStatus(),
			model.ticketingOpenedAt(),
			model.ticketingEndAt(),
			seatCount
		);
	}

}
