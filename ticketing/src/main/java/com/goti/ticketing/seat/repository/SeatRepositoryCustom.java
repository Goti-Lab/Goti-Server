package com.goti.ticketing.seat.repository;

import java.util.List;
import java.util.UUID;

import com.goti.ticketing.domain.entity.seat.SeatEntity;
import com.goti.ticketing.domain.entity.seat.SeatSectionEntity;

public interface SeatRepositoryCustom {
	List<SeatEntity> findAllBySection(SeatSectionEntity section);

	List<SeatEntity> findAllByStadiumId(UUID stadiumId);
}
