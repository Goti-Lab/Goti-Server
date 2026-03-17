package com.goti.resale.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goti.resale.constants.ResaleTransactionStatus;
import com.goti.resale.domain.entity.resale.ResaleTransactionEntity;

public interface ResaleTransactionRepository extends JpaRepository<ResaleTransactionEntity, UUID> {

	List<ResaleTransactionEntity> findAllByResaleOrderId(UUID resaleOrderId);

	int countByBuyerIdAndListing_GameIdAndTransactionStatus(UUID buyerId, UUID gameId, ResaleTransactionStatus status);

}
