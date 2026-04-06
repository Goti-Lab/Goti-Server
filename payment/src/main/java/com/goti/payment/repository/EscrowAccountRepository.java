package com.goti.payment.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.goti.payment.constants.EscrowStatus;
import com.goti.payment.domain.entity.payment.EscrowAccountEntity;

public interface EscrowAccountRepository extends JpaRepository<EscrowAccountEntity, UUID> {
	List<EscrowAccountEntity> findAllByTransactionIdIn(List<UUID> transactionIds);

	@Query("SELECT SUM(e.escrowAmount) "
		+ "FROM EscrowAccountEntity e "
		+ "WHERE e.sellerId = :sellerId "
		+ "AND e.escrowStatus = :status")
	Long sumUnsettledAmount(
		@Param("sellerId") UUID sellerId,
		@Param("status") EscrowStatus status
	);
}
