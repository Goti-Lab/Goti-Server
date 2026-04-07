package com.goti.ticketing.seat.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHoldEntity, UUID>, SeatHoldRepositoryCustom {
}
