package com.goti.game.service.domain;

import com.goti.constants.LeagueType;
import com.goti.domain.entity.game.GameScheduleEntity;

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
