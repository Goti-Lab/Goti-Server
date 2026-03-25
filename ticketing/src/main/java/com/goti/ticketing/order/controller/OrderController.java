package com.goti.ticketing.order.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.List;
import java.util.UUID;

import com.goti.ticketing.domain.entity.order.OrderEntity;
import com.goti.ticketing.infra.api.dto.response.GameIdResponse;
import com.goti.ticketing.order.dto.request.OrderCancelRequest;
import com.goti.ticketing.order.dto.response.OrderCancelResponse;

import com.goti.ticketing.order.service.application.OrderCancelService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.ticketing.order.dto.request.OrderCreateRequest;
import com.goti.ticketing.order.dto.request.OrderPaymentConfirmRequest;
import com.goti.ticketing.order.dto.response.OrderCreateResponse;
import com.goti.ticketing.order.dto.response.OrderListResponse;
import com.goti.ticketing.order.dto.response.OrderPaymentInfoResponse;
import com.goti.ticketing.order.dto.response.OrderPaymentConfirmResponse;
import com.goti.ticketing.order.service.application.OrderCreateService;
import com.goti.ticketing.order.service.application.OrderPaymentConfirmService;
import com.goti.ticketing.order.service.domain.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Order", description = "주문 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
	private final OrderCreateService orderCreateService;
	private final OrderService orderService;
	private final OrderPaymentConfirmService orderPaymentConfirmService;

	@Operation(
		summary = "주문 생성",
		description = "주문 생성 API"
	)
	@PostMapping
	public ResponseEntity<ApiSuccessResponse<OrderCreateResponse>> create(
		@AuthenticationPrincipal(expression = "id") UUID memberId,
		@Valid @RequestBody OrderCreateRequest request
	) {
		return wrap(orderCreateService.create(request.toCommand(memberId)));
	}

	@Operation(
		summary = "내 주문 목록 조회",
		description = "주문 목록 조회 API"
	)
	@GetMapping
	public ResponseEntity<ApiSuccessResponse<List<OrderListResponse>>> getMyOrders(
		@AuthenticationPrincipal(expression = "id") UUID memberId
	) {
		return wrap(orderService.getMyOrders(memberId));
	}

	@Operation(
		summary = "주문 결제 정보 조회 (내부용)",
		description = "payment 모듈에서 결제 전 주문 정보 조회 API"
	)
	@GetMapping("/{orderId}/payment-order")
	public ResponseEntity<ApiSuccessResponse<OrderPaymentInfoResponse>> getPaymentOrder(
		@PathVariable UUID orderId,
		@RequestParam UUID memberId
	) {
		return wrap(orderService.getPaymentOrder(orderId, memberId));
	}


	@Operation(
		summary = "주문 취소",
		description = "주문 전체 또는 부분 취소 API"
	)
	@PostMapping("/{orderId}/cancellations")
	public ResponseEntity<ApiSuccessResponse<OrderCancelResponse>> cancel(
		@PathVariable UUID orderId,
		@AuthenticationPrincipal(expression = "id") UUID memberId,
		@Valid @RequestBody OrderCancelRequest request
	) {
		return wrap(orderCancelService.cancel(orderId, memberId, request));
	}

	//TODO: 추후 내부 호출용 API Controller 분리
	@Operation(
		summary = "주문 결제 완료 처리 (내부용)",
		description = "결제 완료 후 티켓 발급 API"
	)
	@PostMapping("/{orderId}/payment-confirmations")
	public ResponseEntity<ApiSuccessResponse<OrderPaymentConfirmResponse>> confirmPayment(
		@PathVariable UUID orderId,
		@Valid @RequestBody OrderPaymentConfirmRequest request
	) {
		return wrap(orderPaymentConfirmService.confirm(
			orderId,
			request.userId(),
			request.paymentId(),
			request.pgTid()
		));
	}

	@Operation(
		summary = "주문 참조 게임 조회(내부용)",
		description = "주문 참조 게임 일정 ID 조회 API"
	)
	@GetMapping("/{orderId}")
	public ResponseEntity<ApiSuccessResponse<GameIdResponse>> getGameId(
		@PathVariable UUID orderId
	) {
		OrderEntity order = orderService.get(orderId);
		UUID gameId = order.getGameSchedule().getId();
		var response = new GameIdResponse(gameId);
		return wrap(response);
	}

}
