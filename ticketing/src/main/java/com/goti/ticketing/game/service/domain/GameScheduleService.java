package com.goti.ticketing.game.service.domain;

import com.goti.ticketing.constants.LeagueType;
import com.goti.ticketing.domain.entity.game.GameScheduleEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public interface GameScheduleService {

	GameScheduleEntity create(
		UUID homeTeamId,
		UUID awayTeamId,
		UUID stadiumId,
		LocalDateTime startAt,
		LeagueType leagueType
	);
}
