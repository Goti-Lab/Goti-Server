package com.goti.seat.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goti.domain.entity.seat.SeatSectionEntity;

@Repository
public interface SeatSectionRepository extends JpaRepository<SeatSectionEntity, UUID> {
	boolean existsByStadiumIdAndSectionCode(UUID stadiumId, String sectionCode);

	@EntityGraph(attributePaths = "seatGrade")
	List<SeatSectionEntity> findAllByStadiumId(UUID stadiumId);
}
