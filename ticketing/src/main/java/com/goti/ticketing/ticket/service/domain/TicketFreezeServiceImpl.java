package com.goti.ticketing.ticket.service.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.ticketing.constants.TicketFreezeReason;
import com.goti.ticketing.domain.entity.ticket.TicketEntity;
import com.goti.ticketing.domain.entity.ticket.TicketFreezeEntity;
import com.goti.ticketing.ticket.repository.TicketFreezeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketFreezeServiceImpl implements TicketFreezeService {
	private static final long DEFAULT_FREEZE_HOURS = 12L;

	private final TicketFreezeRepository ticketFreezeRepository;

	@Override
	@Transactional
	public TicketFreezeEntity freezeTicket(
		TicketEntity ticket,
		TicketFreezeReason freezeReason
	) {
		LocalDateTime frozenUntil = LocalDateTime.now().plusHours(DEFAULT_FREEZE_HOURS);

		return ticketFreezeRepository.findByTicketId(ticket.getId())
			.map(existingFreeze -> {
				existingFreeze.refreeze(freezeReason, frozenUntil);
				return existingFreeze;
			})
			.orElseGet(() -> ticketFreezeRepository.save(
				TicketFreezeEntity.create(ticket, freezeReason, frozenUntil)
			));
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isFrozen(UUID ticketId) {
		return ticketFreezeRepository
			.findByTicketIdAndFrozenUntilAfter(
				ticketId,
				LocalDateTime.now()
			)
			.isPresent();
	}

	@Override
	@Transactional(readOnly = true)
	public TicketFreezeInfo getCurrentFreeze(UUID ticketId) {
		return ticketFreezeRepository
			.findByTicketIdAndFrozenUntilAfter(
				ticketId,
				LocalDateTime.now()
			)
			.map(TicketFreezeInfo::from)
			.orElse(null);
	}
}
