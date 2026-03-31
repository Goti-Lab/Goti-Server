package com.goti.ticketing.ticket.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "티켓 마이페이지 통합 대시보드 응답")
public record TicketMyPageDashboardResponse(
	@Schema(description = "소유 티켓 수")
	int ownedTicketCount,

	@Schema(description = "리셀 판매 중인 티켓 수")
	Long listingCount,

	@Schema(description = "리셀 판매 완료된 티켓 수")
	Long soldCount,

	@Schema(description = "미정산 금액")
	Long unsettledAmount
) {
}
