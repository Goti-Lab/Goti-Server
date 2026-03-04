package com.goti.game.dto.response;

import com.goti.constants.LeagueType;
import com.goti.constants.ReservationAvailableStatus;
import com.goti.domain.entity.game.BaseballGameEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
	ReservationAvailableStatus reservationAvailableStatus,
	LocalDateTime reservationOpenedAt,
	LocalDateTime reservationClosedAt
) {
	public static GameResponse from(BaseballGameEntity game) {
		return new GameResponse(
			game.getId(),
			game.getHomeTeamId(),
			game.getAwayTeamId(),
			game.getStadiumId(),
			game.getPlayDate(),
			game.getStartAt(),
			game.getLeagueType(),
			game.getReservationAvailableStatus(),
			game.getReservationOpenedAt(),
			game.getReservationClosedAt()
		);
	}
}
