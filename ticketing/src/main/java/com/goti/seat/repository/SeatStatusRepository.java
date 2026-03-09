package com.goti.seat.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.goti.domain.entity.game.GameScheduleEntity;

import com.goti.domain.entity.seat.SeatEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.goti.domain.entity.seat.SeatStatusEntity;

@Repository
public interface SeatStatusRepository extends JpaRepository<SeatStatusEntity, UUID> {

	@Query("""
		SELECT ss
			FROM SeatStatusEntity ss
		WHERE ss.game.id = :gameId
  		AND ss.seat.seatSection.id = :sectionId
	""")
	List<SeatStatusEntity> findSeatStatuses(
		@Param("gameId") UUID gameId,
		@Param("sectionId") UUID sectionId
	);
	Optional<SeatStatusEntity> findByGameAndSeat(GameScheduleEntity game, SeatEntity seat);
}
