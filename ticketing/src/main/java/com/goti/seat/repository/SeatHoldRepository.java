package com.goti.seat.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goti.constants.SeatHoldStatus;
import com.goti.domain.entity.seat.SeatHoldEntity;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHoldEntity, UUID>, SeatHoldRepositoryCustom {
	List<SeatHoldEntity> findByStatusAndExpiredAtBeforeOrderByExpiredAtAsc(
		SeatHoldStatus status,
		LocalDateTime now,
		Pageable pageable
	);
}
