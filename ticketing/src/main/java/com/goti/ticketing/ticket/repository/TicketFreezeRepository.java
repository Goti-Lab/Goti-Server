package com.goti.ticketing.ticket.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goti.ticketing.domain.entity.ticket.TicketFreezeEntity;

@Repository
public interface TicketFreezeRepository extends JpaRepository<TicketFreezeEntity, UUID> {
	Optional<TicketFreezeEntity> findByTicketId(UUID ticketId);

	Optional<TicketFreezeEntity> findByTicketIdAndFrozenUntilAfter(
		UUID ticketId,
		LocalDateTime dateTime
	);
}
