package com.goti.order.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goti.domain.entity.order.OrderEntity;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
	@EntityGraph(attributePaths = "gameSchedule")
	List<OrderEntity> findAllByMemberIdOrderByCreatedAtDesc(UUID memberId);
}
