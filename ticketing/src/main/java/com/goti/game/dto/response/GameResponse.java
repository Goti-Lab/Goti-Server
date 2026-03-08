package com.goti.game.dto.response;

import com.goti.constants.GameResult;
import com.goti.constants.GameStatus;
import com.goti.constants.LeagueType;
import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.game.BaseballGameStatusEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record GameResponse(
	UUID gameId,
	UUID homeTeamId,
	UUID awayTeamId,
	UUID stadiumId,
	LocalDate playDate,
	LocalTime startAt,
	LeagueType leagueType,
	GameStatus gameStatus,
	Integer homeTeamScore,
	Integer awayTeamScore,
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
