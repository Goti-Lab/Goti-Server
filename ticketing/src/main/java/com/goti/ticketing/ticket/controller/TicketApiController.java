package com.goti.ticketing.ticket.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.ticketing.ticket.dto.request.TicketTransferRequest;
import com.goti.ticketing.ticket.dto.response.ResaleTicketGameInfoResponse;
import com.goti.ticketing.ticket.dto.response.ResaleTicketResponse;
import com.goti.ticketing.ticket.dto.response.TicketResponse;
import com.goti.ticketing.ticket.service.application.TicketResaleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Ticket Internal", description = "티켓 내부 연동 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tickets/resales")
public class TicketApiController {

	private final TicketResaleService ticketResaleService;

	@Operation(
		summary = "티켓 정보 조회",
		description = "리셀용 티켓 정보 조회API"
	)
	@GetMapping("/{ticketId}")
	public ResponseEntity<ApiSuccessResponse<ResaleTicketResponse>> getResaleTicketInfo(
		@PathVariable UUID ticketId,
		@RequestParam UUID userId
	) {
		return wrap(ticketResaleService.getResaleTicketInfo(ticketId, userId));
	}

	@Operation(
		summary = "소유 티켓 수 조회",
		description = "사용자의 특정 경기 티켓 소유 수 조회 API"
	)
	@GetMapping("/count")
	public ResponseEntity<ApiSuccessResponse<Integer>> getOwnedTicketCount(
		@RequestParam UUID userId,
		@RequestParam UUID gameId
	) {
		return wrap(ticketResaleService.getOwnedTicketCountWithGame(userId, gameId));
	}

	@Operation(
		summary = "만료 경기 목록 조회",
		description = "특정 시간 이전의 만료된 경기 ID 목록 조회 API"
	)
	@GetMapping("/expired")
	public ResponseEntity<ApiSuccessResponse<List<UUID>>> getExpiredGameIds(
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime threshold
	) {
		return wrap(ticketResaleService.getExpiredGameIds(threshold));
	}

	@Operation(
		summary = "예정 경기 목록 조회",
		description = "향후 진행될 경기 및 등급 정보 목록 조회 API"
	)
	@GetMapping("/upcoming")
	public ResponseEntity<ApiSuccessResponse<List<ResaleTicketGameInfoResponse>>> getUpcomingGames() {
		return wrap(ticketResaleService.getUpcomingGames());
	}

	@Operation(
		summary = "티켓 리셀 등록",
		description = "티켓 리셀에 의한 등록 상태 변경 API")
	@PatchMapping("/{ticketId}/listing")
	public ResponseEntity<ApiSuccessResponse<Void>> markAsResaleListing(
		@PathVariable UUID ticketId,
		@RequestParam UUID userId
	) {
		ticketResaleService.markAsResaleListing(ticketId, userId);
		return wrap(null);
	}

	@Operation(
		summary = "티켓 리셀 취소",
		description = "티켓 리셀 취소 상태 변경 API"
	)
	@PatchMapping("/{ticketId}/cancel")
	public ResponseEntity<ApiSuccessResponse<Void>> cancelResaleListing(
		@PathVariable UUID ticketId,
		@RequestParam UUID userId
	) {
		ticketResaleService.cancelResaleListing(ticketId, userId);
		return wrap(null);
	}

	@Operation(
		summary = "리셀 완료",
		description = "티켓 소유권 이전 API"
	)
	@PostMapping("/{ticketId}/transfer")
	public ResponseEntity<ApiSuccessResponse<TicketResponse>> transferOwnership(
		@PathVariable UUID ticketId,
		@Valid @RequestBody TicketTransferRequest request
	) {
		return wrap(ticketResaleService.transferOwnership(
			ticketId,
			request.buyerId(),
			request.buyerNickname(),
			request.buyerEmail(),
			request.buyerPhone(),
			request.transactionId(),
			request.transactionPrice()
		));
	}
}
