package com.goti.ticketing.game.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.goti.ticketing.constants.GameResult;
import com.goti.ticketing.constants.GameStatus;
import com.goti.ticketing.constants.LeagueType;

import com.goti.ticketing.constants.TicketingStatus;

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

	@Schema(description = "홈 팀 ID", example = "550e8400-e29b-41d4-a716-446655440000")
	UUID homeTeamId,

	@Schema(description = "원정 팀 ID", example = "660f9511-f30c-52d5-b827-557766551111")
	UUID awayTeamId,

	@Schema(description = "경기장 ID", example = "770g0622-g41d-63e6-c938-668877662222")
	UUID stadiumId,

	@Schema(description = "경기 진행 상태", example = "FINISHED")
	GameStatus gameStatus,

	@Schema(description = "홈팀 현재 점수", example = "8")
	Integer homeTeamScore,

	@Schema(description = "원정팀 현재 점수", example = "5")
	Integer awayTeamScore,

	@Schema(description = "경기 결과", example = "WIN")
	GameResult gameResult,

	@Schema(description = "티켓팅 상태", example = "AVAILABLE")
	TicketingStatus ticketingStatus,

	@Schema(description = "티켓팅 오픈 일시 (yyyy-MM-dd HH:mm)", example = "2026-03-15 11:00")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime ticketingOpenedAt
) {}
