package com.goti.ticketing.ticket.service.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.ticketing.infra.api.TicketPaymentClient;
import com.goti.ticketing.infra.api.TicketResaleClient;
import com.goti.ticketing.infra.api.dto.response.ResaleListingMyPageCountResponse;
import com.goti.ticketing.infra.api.dto.response.UnsettledAmountResponse;
import com.goti.ticketing.ticket.dto.response.TicketMyPageDashboardResponse;
import com.goti.ticketing.ticket.service.domain.TicketService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketMyPageService {
	private final TicketService ticketService;
	private final TicketResaleClient ticketResaleClient;
	private final TicketPaymentClient ticketPaymentClient;

	@Transactional(readOnly = true)
	public TicketMyPageDashboardResponse getDashboard(UUID userId) {
		int ownedTicketCount = ticketService.getOwnedTicketCount(userId);
		ResaleListingMyPageCountResponse resaleCount = ticketResaleClient.getMySales(userId);
		UnsettledAmountResponse unsettledAmount = ticketPaymentClient.getUnsettledAmounts(userId);

		return new TicketMyPageDashboardResponse(
			ownedTicketCount,
			resaleCount.listingCount(),
			resaleCount.soldCount(),
			unsettledAmount.unsettledAmount() != null ? unsettledAmount.unsettledAmount() : 0L
		);
	}
}
