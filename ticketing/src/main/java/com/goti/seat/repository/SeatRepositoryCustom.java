package com.goti.seat.repository;

import java.util.List;
import java.util.UUID;

import com.goti.domain.entity.seat.SeatEntity;

public interface SeatRepositoryCustom {
	List<SeatEntity> findAllBySection(UUID sectionId);
}
