package com.goti.payment.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goti.payment.domain.entity.payment.EscrowAccountEntity;

public interface EscrowAccountRepository extends JpaRepository<EscrowAccountEntity, UUID> {
	List<EscrowAccountEntity> findAllByTransactionIdIn(List<UUID> transactionIds);
}
