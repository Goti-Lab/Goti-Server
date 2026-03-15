package com.goti.repository.history;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.goti.domain.entity.resale.ResalePriceHistoryEntity;

public interface ResalePriceHistoryRepository extends JpaRepository<ResalePriceHistoryEntity, UUID> {

	@Query("SELECT r FROM ResalePriceHistoryEntity r "
		+ "WHERE r.gameId = :gameId "
		+ "AND r.gradeId = :gradeId "
		+ "ORDER BY r.createdAt DESC "
		+ "LIMIT 1")
	Optional<ResalePriceHistoryEntity> findLatestByGameAndGrade(
		@Param("gameId") UUID gameId,
		@Param("gradeId") UUID gradeId
	);

	@Query("SELECT r FROM ResalePriceHistoryEntity r "
		+ "WHERE r.gameId = :gameId "
		+ "AND r.gradeId = :gradeId "
		+ "AND r.createdAt BETWEEN :startOfDay AND :endOfDay "
		+ "ORDER BY r.createdAt DESC "
		+ "LIMIT 1")
	Optional<ResalePriceHistoryEntity> findByGameAndGradeAndDate(
		@Param("gameId") UUID gameId,
		@Param("gradeId") UUID gradeId,
		@Param("startOfDay") Instant startOfDay,
		@Param("endOfDay") Instant endOfDay
	);

	@Query("SELECT r FROM ResalePriceHistoryEntity r "
		+ "WHERE r.gameId = :gameId "
		+ "AND r.gradeId = :gradeId "
		+ "AND r.createdAt >= :since "
		+ "ORDER BY r.createdAt ASC")
	List<ResalePriceHistoryEntity> findByGameAndGrade(
		@Param("gameId") UUID gameId,
		@Param("gradeId") UUID gradeId,
		@Param("since") Instant since
	);
}
