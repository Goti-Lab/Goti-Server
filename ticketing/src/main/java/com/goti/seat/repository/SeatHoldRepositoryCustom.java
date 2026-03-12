package com.goti.seat.repository;

import java.util.List;
import java.util.UUID;

import com.goti.domain.entity.seat.SeatHoldEntity;

public interface SeatHoldRepositoryCustom {
	List<SeatHoldEntity> findAllWithDetailsByIdIn(List<UUID> holdIds);
}
