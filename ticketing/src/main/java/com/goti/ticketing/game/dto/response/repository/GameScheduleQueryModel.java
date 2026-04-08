package com.goti.ticketing.game.dto.response.repository;

import com.goti.ticketing.constants.GameResult;
import com.goti.ticketing.constants.GameStatus;
import com.goti.ticketing.constants.LeagueType;
import com.goti.ticketing.constants.TicketingStatus;
import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;
import java.util.UUID;

public record GameScheduleQueryModel(
	UUID gameId,
	LocalDateTime startAt,
	LeagueType leagueType,
	UUID homeTeamId,
	UUID awayTeamId,
	UUID stadiumId,
	GameStatus gameStatus,
	Integer homeTeamScore,
	Integer awayTeamScore,
	GameResult gameResult,
	TicketingStatus ticketingStatus,
	LocalDateTime ticketingOpenedAt,
	LocalDateTime ticketingEndAt
) {
	@QueryProjection
	public GameScheduleQueryModel {
	}
}
