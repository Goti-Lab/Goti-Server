package com.goti.seat.repository;

import com.goti.domain.entity.seat.SeatStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeatStatusRepository extends JpaRepository<SeatStatusEntity, UUID> {
	Optional<SeatStatusEntity> findByGame_IdAndSeat_Id(UUID gameId, UUID seatId);
}
