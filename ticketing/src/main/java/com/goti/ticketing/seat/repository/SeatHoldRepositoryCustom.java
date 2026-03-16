package com.goti.ticketing.seat.repository;

import java.util.List;
import java.util.UUID;

import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;

public interface SeatHoldRepositoryCustom {
	List<SeatHoldEntity> findAllWithDetailsByIdIn(List<UUID> holdIds);
}
