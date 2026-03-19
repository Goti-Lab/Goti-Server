package com.goti.ticketing.seat.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.goti.ticketing.constants.SeatHoldStatus;
import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.seat.SeatEntity;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;

public interface SeatHoldRepositoryCustom {
	List<SeatHoldEntity> findAllWithDetailsByIdIn(List<UUID> holdIds);

	Optional<SeatHoldEntity> findLatestActiveHold(
		GameScheduleEntity gameSchedule,
		SeatEntity seat,
		UUID userId,
		SeatHoldStatus status
	);
}
