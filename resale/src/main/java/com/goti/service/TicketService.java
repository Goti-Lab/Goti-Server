package com.goti.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.goti.dto.response.ResaleTicketResponse;

import lombok.RequiredArgsConstructor;

/**
 * Ticket 도메인과의 통신 인터페이스 추후 RestClient 등으로 변경
 */
@Service
@RequiredArgsConstructor
public class TicketService {
	public ResaleTicketResponse getTicketInfo(UUID ticketId, UUID ownerId) {
		// TODO: 실제 구현 시 Ticket 도메인과 통신

		// 테스트용 더미 데이터
		return new ResaleTicketResponse(
			ticketId,
			UUID.randomUUID(),
			ownerId,
			"A구역 3열 15번",
			50000,
			LocalDateTime.now().plusDays(3)
		);
	}

	public boolean ticketOwnership(UUID ticketId, UUID userId) {

		ResaleTicketResponse ticket = getTicketInfo(ticketId, userId);

		return ticket.ownerId().equals(userId);
	}
}