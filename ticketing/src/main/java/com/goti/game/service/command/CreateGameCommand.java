package com.goti.game.service.command;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import com.goti.constants.GameStatus;
import com.goti.constants.LeagueType;
import com.goti.constants.ReservationAvailableStatus;

public record CreateGameCommand(
	UUID homeTeamId,
	UUID awayTeamId,
	UUID stadiumId,
	LocalDate playDate,
	LocalTime startAt,
	LeagueType leagueType,
	LocalDateTime reservationOpenedAt,
	LocalDateTime reservationClosedAt
) {}