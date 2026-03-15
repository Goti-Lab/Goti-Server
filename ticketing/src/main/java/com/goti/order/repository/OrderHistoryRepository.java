package com.goti.order.repository;

import java.util.Optional;
import java.util.UUID;

import com.goti.domain.entity.order.OrderHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistoryEntity, UUID> {
	Optional<OrderHistoryEntity> findByOrder_Id(UUID orderId);
}
