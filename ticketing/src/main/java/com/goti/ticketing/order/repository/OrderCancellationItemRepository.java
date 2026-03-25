package com.goti.ticketing.order.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goti.ticketing.domain.entity.order.OrderCancellationItemEntity;

@Repository
public interface OrderCancellationItemRepository extends JpaRepository<OrderCancellationItemEntity, UUID> {
}
