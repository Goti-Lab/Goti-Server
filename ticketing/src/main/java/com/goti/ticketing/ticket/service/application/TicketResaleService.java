package com.goti.ticketing.ticket.service.application;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.ticketing.constants.TicketFreezeReason;
import com.goti.ticketing.domain.entity.ticket.TicketEntity;
import com.goti.ticketing.ticket.dto.response.ResaleTicketResponse;
import com.goti.ticketing.ticket.dto.response.TicketResponse;
import com.goti.ticketing.ticket.service.domain.TicketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketResaleService {

	private final TicketService ticketService;
	private final TicketFreezeManagementService ticketFreezeManagementService;

	@Transactional(readOnly = true)
	public ResaleTicketResponse getResaleTicketInfo(UUID ticketId, UUID userId) {
		return ticketService.getResaleTicketInfo(ticketId, userId);
	}

	@Transactional
	public void markAsResaleListing(UUID ticketId, UUID userId) {
		TicketEntity ticket = ticketService.get(ticketId);
		validateOwnership(ticket, userId);
		ticket.markAsResaleListing();
	}

	@Transactional
	public void cancelResaleListing(UUID ticketId, UUID userId) {
		TicketEntity ticket = ticketService.get(ticketId);
		validateOwnership(ticket, userId);

		if (ticket.getUpdatedAt().isBefore(Instant.from(LocalDateTime.now().minusHours(1)))) {
			log.info("리셀 등록 1시간 경과 후 취소로 인한 티켓 동결: {}", ticketId);
			ticketFreezeManagementService.freezeTicket(ticketId, TicketFreezeReason.RESALE_CANCEL_AFTER_ONE_HOUR);
		}

		ticket.restoreFromResale();
	}

	@Transactional
	public TicketResponse transferOwnership(
		UUID ticketId,
		UUID buyerId,
		String buyerNickname,
		String buyerEmail,
		String buyerPhone,
		UUID transactionId,
		Integer transactionPrice
	) {
		TicketEntity oldTicket = ticketService.get(ticketId);

		TicketEntity newTicket = ticketService.createByResale(
			oldTicket,
			buyerId,
			buyerNickname,
			buyerEmail,
			buyerPhone,
			transactionId,
			transactionPrice
		);

		return TicketResponse.from(newTicket);
	}

	private void validateOwnership(TicketEntity ticket, UUID userId) {
		if (!ticket.getUserId().equals(userId)) {
			throw new CustomException(ErrorCode.AUTH_PERMISSION_DENIED);
		}
	}
}
