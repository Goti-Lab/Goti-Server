package com.goti.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.goti.domain.entity.resale.ResalePriceHistoryEntity;

public interface ResalePriceHistoryRepository extends JpaRepository<ResalePriceHistoryEntity, UUID> {
	@Query("SELECT rph.transactionPrice FROM ResalePriceHistoryEntity rph " +
		"WHERE rph.gameId = :gameId " +
		"AND rph.seatId = :seatId " +
		"AND rph.transactionDate >= :date " +
		"ORDER BY rph.transactionTime DESC " +
		"LIMIT 1")
	Optional<Integer> findLatestTransactionPrice(
		@Param("gameId") UUID gameId,
		@Param("seatId") UUID seatId,
		@Param("date") LocalDate date
	);
}
