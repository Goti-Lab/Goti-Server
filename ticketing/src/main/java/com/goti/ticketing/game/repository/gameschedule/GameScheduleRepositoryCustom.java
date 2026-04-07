package com.goti.ticketing.game.repository.gameschedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.goti.ticketing.game.dto.request.GameScheduleSearchCondition;
import com.goti.ticketing.game.dto.response.GameScheduleSearchResponse;

public interface GameScheduleRepositoryCustom {

	boolean existsDuplicateSchedule(
		UUID homeTeamId,
		UUID awayTeamId,
		LocalDateTime startAt
	);

	List<GameScheduleSearchResponse> searchSchedules(GameScheduleSearchCondition request);

	Map<UUID, Long> findRemainingSeatCounts(List<UUID> scheduleIds);

	Optional<GameScheduleSearchResponse> findScheduleByGameId(UUID gameId);

}
