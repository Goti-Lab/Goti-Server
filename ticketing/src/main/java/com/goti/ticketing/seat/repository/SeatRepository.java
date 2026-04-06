package com.goti.ticketing.seat.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.goti.ticketing.domain.entity.seat.SeatSectionEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.goti.ticketing.domain.entity.seat.SeatEntity;

@Repository
public interface SeatRepository extends JpaRepository<SeatEntity, UUID>, SeatRepositoryCustom {

	@Query("""
		SELECT seat
			FROM SeatEntity seat
		WHERE seat.seatSection.id = :sectionId
		  AND seat.rowName = :rowName
		  AND seat.seatNum in :seatNums
	""")
	List<SeatEntity> findAllInRow(
		@Param("sectionId") UUID sectionId,
		@Param("rowName") String rowName,
		@Param("seatNums") Collection<Integer> seatNums
	);

	long countBySeatSection(SeatSectionEntity seatSection);
}
