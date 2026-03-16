package com.goti.ticketing.order.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goti.ticketing.domain.entity.order.OrderEntity;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
	@EntityGraph(attributePaths = "gameSchedule")
	List<OrderEntity> findAllByMemberIdOrderByCreatedAtDesc(UUID memberId);
	Optional<OrderEntity> findByIdAndMemberId(UUID id, UUID memberId);
}
