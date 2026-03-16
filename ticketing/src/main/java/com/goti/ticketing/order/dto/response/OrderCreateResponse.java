package com.goti.ticketing.order.dto.response;

import java.util.UUID;

import com.goti.constants.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "점유 좌석 기반 주문 생성 응답")
public record OrderCreateResponse(
	@Schema(description = "주문 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
	UUID orderId,

	@Schema(description = "주문 번호", example = "ORD-20260309-0001")
	String orderNumber,

	@Schema(description = "경기 ID", example = "11111111-1111-1111-1111-111111111111")
	UUID gameId,

	@Schema(description = "주문 상태", example = "PENDING")
	OrderStatus orderStatus,

	@Schema(description = "총 수량", example = "2")
	Integer totalQuantity,

	@Schema(description = "총 금액", example = "24000")
	Integer totalAmount
) {
	public static OrderCreateResponse from(
		UUID orderId,
		String orderNumber,
		UUID gameId,
		OrderStatus orderStatus,
		Integer totalQuantity,
		Integer totalAmount
	) {
		return new OrderCreateResponse(
			orderId,
			orderNumber,
			gameId,
			orderStatus,
			totalQuantity,
			totalAmount
		);
	}
}
