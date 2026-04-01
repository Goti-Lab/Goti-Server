package com.goti.resale.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.goti.resale.constants.ResaleTransactionStatus;
import com.goti.resale.domain.entity.resale.ResaleTransactionEntity;

public interface ResaleTransactionRepository extends JpaRepository<ResaleTransactionEntity, UUID>, ResaleTransactionRepositoryCustom {

	List<ResaleTransactionEntity> findAllByResaleOrderId(UUID resaleOrderId);

	@Query("""
		SELECT count(transaction)
			FROM ResaleTransactionEntity transaction
		WHERE transaction.buyerId = :buyerId
			AND transaction.listing.gameId = :gameId
			AND transaction.transactionStatus = :status
		""")
	int countTransactions(
		@Param("buyerId") UUID buyerId,
		@Param("gameId") UUID gameId,
		@Param("status") ResaleTransactionStatus status
	);
}
