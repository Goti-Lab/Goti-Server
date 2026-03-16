package com.goti.ticketing.seat.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goti.ticketing.constants.SeatHoldStatus;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHoldEntity, UUID>, SeatHoldRepositoryCustom {
	List<SeatHoldEntity> findByStatusAndExpiredAtBeforeOrderByExpiredAtAsc(
		SeatHoldStatus status,
		LocalDateTime now,
		Pageable pageable
	);
}
