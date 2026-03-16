package com.goti.order.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.order.dto.request.OrderCreateRequest;
import com.goti.order.dto.request.OrderPaymentConfirmRequest;
import com.goti.order.dto.response.OrderCreateResponse;
import com.goti.order.dto.response.OrderListResponse;
import com.goti.order.dto.response.OrderPaymentConfirmResponse;
import com.goti.order.service.application.OrderCreateService;
import com.goti.order.service.application.OrderPaymentConfirmService;
import com.goti.order.service.domain.OrderService;

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
}
