package com.goti.order.dto.response;

import java.util.UUID;

import com.goti.constants.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 결제 완료 처리 응답")
public record OrderPaymentConfirmResponse(
	@Schema(description = "주문 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
	UUID orderId,

	@Schema(description = "주문 상태", example = "CONFIRMED")
	OrderStatus orderStatus,

	@Schema(description = "발급된 티켓 수", example = "2")
	Integer issuedTicketCount
) {
	public static OrderPaymentConfirmResponse from(
		UUID orderId,
		OrderStatus orderStatus,
		Integer issuedTicketCount
	) {
		return new OrderPaymentConfirmResponse(orderId, orderStatus, issuedTicketCount);
	}
}
