package com.goti.payment.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.goti.payment.domain.entity.payment.PaymentEntity;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
	boolean existsByIdempotencyKey(String idempotencyKey);

	@Query("""
    SELECT p
    	FROM PaymentEntity p
    WHERE p.orderId = :orderId
    ORDER BY p.createdAt DESC
""")
	Optional<PaymentEntity> findLatestByOrderId(@Param("orderId") UUID orderId);
}
