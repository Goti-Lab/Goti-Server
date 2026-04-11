package com.goti.ticketing.seat.service.domain;

import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.seat.GameSeatSummaryEntity;

import java.util.List;
import java.util.UUID;

public interface GameSeatSummaryService {

	GameSeatSummaryEntity create(GameScheduleEntity gameSchedule, int totalSeats);

	List<GameSeatSummaryEntity> getRemainingSeatCounts(List<UUID> gameIds);

	void increase(UUID gameId, int count);

	void decrease(UUID gameId, int count);
}
