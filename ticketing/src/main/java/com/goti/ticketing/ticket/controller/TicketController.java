package com.goti.ticketing.ticket.controller;

import static com.goti.global.api.ApiSuccessResponse.wrap;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.ticketing.ticket.dto.response.TicketPurchaseInfoResponse;
import com.goti.ticketing.ticket.dto.response.TicketQrResponse;
import com.goti.ticketing.ticket.dto.response.TicketResponse;
import com.goti.ticketing.ticket.service.application.TicketQrService;
import com.goti.ticketing.ticket.service.domain.TicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Ticket", description = "티켓 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tickets")
public class TicketController {
	private final TicketService ticketService;
	private final TicketQrService ticketQrService;

	@Operation(
		summary = "티켓 상세 조회",
		description = "티켓 상세 정보 조회 API"
	)
	@GetMapping("/{ticketId}")
	public ResponseEntity<ApiSuccessResponse<TicketResponse>> getDetail(
		@PathVariable UUID ticketId,
		@AuthenticationPrincipal(expression = "id") UUID userId
	) {
		return wrap(ticketService.getDetail(ticketId, userId));
	}

	@Operation(
		summary = "티켓 구매 내역 정보 목록 조회 (내부용)",
		description = "구매 내역 티켓 정보 목록 조회 내부용 API"
	)
	@GetMapping("/purchase-infos")
	public ResponseEntity<ApiSuccessResponse<List<TicketPurchaseInfoResponse>>> getPurchaseInfos(
		@RequestParam List<UUID> ticketIds
	) {
		return wrap(ticketService.getPurchaseInfos(ticketIds));
	}

	@Operation(
		summary = "모바일 티켓 QR 발급",
		description = "모바일 티켓 QR 토큰 발급 API"
	)
	@GetMapping("/{ticketId}/qr")
	public ResponseEntity<ApiSuccessResponse<TicketQrResponse>> getQr(
		@PathVariable UUID ticketId,
		@AuthenticationPrincipal(expression = "id") UUID userId
	) {
		return wrap(ticketQrService.create(ticketId, userId));
	}
}
