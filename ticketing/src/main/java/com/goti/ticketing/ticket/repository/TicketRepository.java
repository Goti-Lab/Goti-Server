package com.goti.ticketing.ticket.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goti.ticketing.domain.entity.ticket.TicketEntity;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, UUID> {
	Optional<TicketEntity> findByIdAndUserId(UUID ticketId, UUID userId);
	List<TicketEntity> findAllByOrderItemIdIn(Collection<UUID> orderItemIds);

	int countByUserIdAndGameId(UUID userId, UUID gameId);
}
