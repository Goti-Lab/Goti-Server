package com.goti.ticketing.ticket.service.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.ticketing.domain.entity.ticket.TicketEntity;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.ticket.dto.response.TicketResponse;
import com.goti.ticketing.ticket.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketDetailService {
	private final TicketRepository ticketRepository;

	@Transactional(readOnly = true)
	public TicketResponse getDetail(
		UUID ticketId,
		UUID userId
	) {
		Preconditions.validate(
			userId != null,
			ErrorCode.AUTH_INVALID
		);

		TicketEntity ticket = ticketRepository.findByIdAndUserId(ticketId, userId)
			.orElseThrow(() -> new CustomException(ErrorCode.TICKET_NOT_FOUND));

		return TicketResponse.from(ticket);
	}
}
