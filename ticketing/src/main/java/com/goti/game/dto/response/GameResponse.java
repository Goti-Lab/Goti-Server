package com.goti.game.dto.response;

import com.goti.constants.GameResult;
import com.goti.constants.GameStatus;
import com.goti.constants.LeagueType;
import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.game.BaseballGameStatusEntity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Schema(description = "경기 일정 응답")
public record GameResponse(
	@Schema(description = "경기 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
	UUID gameId,
	@Schema(description = "홈팀 ID", example = "11111111-1111-1111-1111-111111111111")
	UUID homeTeamId,
	@Schema(description = "원정팀 ID", example = "22222222-2222-2222-2222-222222222222")
	UUID awayTeamId,
	@Schema(description = "구장 ID", example = "33333333-3333-3333-3333-333333333333")
	UUID stadiumId,
	@Schema(description = "경기 날짜", example = "2026-04-10")
	LocalDate playDate,
	@Schema(description = "경기 시작 시간", example = "18:30:00")
	LocalTime startAt,
	@Schema(description = "리그 종류", example = "REGULAR")
	LeagueType leagueType,
	@Schema(description = "경기 상태", example = "SCHEDULED")
	GameStatus gameStatus,
	@Schema(description = "홈팀 점수", example = "0")
	Integer homeTeamScore,
	@Schema(description = "원정팀 점수", example = "0")
	Integer awayTeamScore,
	@Schema(description = "경기 결과", example = "PENDING")
	GameResult gameResult
) {
	public static GameResponse from(GameScheduleEntity game, BaseballGameStatusEntity status) {
		return new GameResponse(
			game.getId(),
			game.getHomeTeamId(),
			game.getAwayTeamId(),
			game.getStadiumId(),
			game.getPlayDate(),
			game.getStartAt(),
			game.getLeagueType(),
			status.getGameStatus(),
			status.getHomeTeamScore(),
			status.getAwayTeamScore(),
			status.getGameResult()
		);
	}
}
