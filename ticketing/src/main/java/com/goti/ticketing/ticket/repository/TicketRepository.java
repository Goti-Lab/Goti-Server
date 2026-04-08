package com.goti.ticketing.ticket.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.goti.ticketing.constants.TicketStatus;
import com.goti.ticketing.domain.entity.ticket.TicketEntity;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, UUID> {
	Optional<TicketEntity> findByIdAndUserId(UUID ticketId, UUID userId);

	@Query("SELECT t FROM TicketEntity t " +
		"WHERE t.orderItemId IN :orderItemIds " +
		"AND t.userId IN :userId")
	List<TicketEntity> findAllByOrderItemIdIn(
		@Param("orderItemIds") List<UUID> orderItemIds,
		@Param("userId") UUID userId
	);

	List<TicketEntity> findAllByIdIn(Collection<UUID> ticketIds);

	int countByUserIdAndGameId(UUID userId, UUID gameId);

	int countByUserIdAndTicketStatusIn(UUID userId, Collection<TicketStatus> statuses);
}
