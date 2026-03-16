package com.goti.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goti.constants.ResaleTransactionStatus;
import com.goti.domain.entity.resale.ResaleTransactionEntity;

public interface ResaleTransactionRepository extends JpaRepository<ResaleTransactionEntity, UUID> {

	List<ResaleTransactionEntity> findAllByResaleOrderId(UUID resaleOrderId);

	int countByBuyerIdAndListing_GameIdAndTransactionStatus(UUID buyerId, UUID gameId, ResaleTransactionStatus status);

}
