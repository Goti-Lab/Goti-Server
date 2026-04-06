package com.goti.ticketing.order.dto.response;

import java.util.UUID;

import com.goti.constants.OrderStatus;
import com.goti.ticketing.domain.entity.order.OrderEntity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 결제 정보 응답")
public record OrderPaymentInfoResponse(
	@Schema(description = "주문 ID", example = "f4f4d89f-8a2f-4ec2-9b1f-4b3f8c5d7e11")
	UUID orderId,
	@Schema(description = "주문자 회원 ID", example = "2d3c4b5a-6e7f-4890-8c1a-2b3d4e5f6789")
	UUID memberId,
	@Schema(description = "주문 상태", example = "PENDING")
	OrderStatus orderStatus,
	@Schema(description = "총 결제 금액", example = "21000")
	Integer totalAmount
) {
	public static OrderPaymentInfoResponse from(OrderEntity order) {
		return new OrderPaymentInfoResponse(
			order.getId(),
			order.getMemberId(),
			order.getOrderStatus(),
			order.getTotalAmount()
		);
	}
}
