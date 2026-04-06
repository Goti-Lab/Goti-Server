package com.goti.ticketing.infra.api;

import java.util.UUID;

import com.goti.ticketing.infra.api.dto.response.ResaleListingMyPageCountResponse;

public interface TicketResaleClient {
	ResaleListingMyPageCountResponse getMySales(UUID userId);
}
