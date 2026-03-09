package com.goti.repository.history;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import com.goti.domain.entity.resale.ResalePriceHistoryEntity;

public interface ResalePriceHistoryRepositoryCustom {
	Optional<ResalePriceHistoryEntity> findLatestByGradeId(UUID gradeId);

	Optional<ResalePriceHistoryEntity> findLatestByGameIdAndGradeIdAndDate(
		UUID gameId,
		UUID gradeId,
		LocalDate transactionDate
	);
}
