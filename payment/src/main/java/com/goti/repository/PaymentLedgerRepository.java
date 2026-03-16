package com.goti.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goti.domain.entity.payment.PaymentLedgerEntity;

public interface PaymentLedgerRepository extends JpaRepository<PaymentLedgerEntity, UUID> {
	Optional<PaymentLedgerEntity> findByOrderId(UUID orderId);
}
