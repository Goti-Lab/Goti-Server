package com.goti.seat.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goti.domain.entity.seat.SeatGradeEntity;

@Repository
public interface SeatGradeRepository extends JpaRepository<SeatGradeEntity, UUID> {
	boolean existsByStadiumIdAndName(UUID stadiumId, String name);

	List<SeatGradeEntity> findAllByStadiumId(UUID stadiumId);
}
