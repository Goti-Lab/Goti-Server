package com.goti.ticketing.seat.repository;

import java.util.List;
import java.util.UUID;

import com.goti.ticketing.domain.entity.seat.SeatEntity;

public interface SeatRepositoryCustom {
	List<SeatEntity> findAllBySection(UUID sectionId);
}
