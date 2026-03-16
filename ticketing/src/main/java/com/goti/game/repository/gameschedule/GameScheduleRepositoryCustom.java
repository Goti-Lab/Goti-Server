package com.goti.game.repository.gameschedule;

import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.game.dto.request.GameScheduleSearchCondition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface GameScheduleRepositoryCustom {

	boolean existsDuplicateSchedule(
		UUID homeTeamId,
		UUID awayTeamId,
		LocalDateTime startAt
	);

	List<GameScheduleEntity> searchSchedules(GameScheduleSearchCondition request);


}
