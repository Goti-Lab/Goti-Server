package com.goti.order.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.UUID;

import com.goti.order.dto.response.OrderCreateResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.order.dto.request.OrderCreateRequest;
import com.goti.order.service.application.OrderCreateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Order", description = "주문 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/games")
public class OrderController {
	private final OrderCreateService orderCreateService;

	@Operation(
		summary = "주문 생성",
		description = "주문 생성 API"
	)
	@PostMapping("/{gameId}/orders")
	public ResponseEntity<ApiSuccessResponse<OrderCreateResponse>> create(
		@PathVariable UUID gameId,
		@AuthenticationPrincipal(expression = "id") UUID memberId,
		@Valid @RequestBody OrderCreateRequest request
	) {
		return wrap(orderCreateService.create(request.toCommand(gameId, memberId)));
	}
}