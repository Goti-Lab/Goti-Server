package com.goti.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goti.domain.entity.resale.ResalePriceHistoryEntity;

public interface ResalePriceHistoryRepository extends JpaRepository<ResalePriceHistoryEntity, UUID> {
	Optional<ResalePriceHistoryEntity> findFirstByGradeIdOrderByTransactionTimeDesc(UUID gradeId);

	Optional<ResalePriceHistoryEntity> findLastTransactionByGameIdAndGradeIdAndTransactionDate(
		UUID gameId,
		UUID gradeId,
		LocalDate date
	);
}
