package com.goti.service;

import java.util.UUID;

import com.goti.dto.response.ResaleTicketResponse;

/**
 * Ticket 도메인과의 통신 인터페이스
 */
public interface TicketService {
	ResaleTicketResponse getTicketInfoForResale(UUID ticketId, UUID userId);

	void updateTicketStatusToResale(UUID ticketId);

	void restoreTicketFromResale(UUID ticketId);
}