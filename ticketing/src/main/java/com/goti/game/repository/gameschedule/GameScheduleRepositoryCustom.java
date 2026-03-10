package com.goti.game.repository.gameschedule;

import java.time.LocalDateTime;
import java.util.UUID;

public interface GameScheduleRepositoryCustom {

	boolean existsDuplicateSchedule(
		UUID homeTeamId,
		UUID awayTeamId,
		LocalDateTime startAt
	);
}
