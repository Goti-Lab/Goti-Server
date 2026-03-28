package com.goti.ticketing.order.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.constants.OrderStatus;
import com.goti.ticketing.constants.OrderCancellationRequestType;
import com.goti.ticketing.domain.entity.order.OrderCancellationEntity;
import com.goti.ticketing.domain.entity.order.OrderEntity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 취소 응답")
public record OrderCancelResponse(
	@Schema(description = "취소 이력 ID", example = "11111111-1111-1111-1111-111111111111")
	UUID cancellationId,
	@Schema(description = "주문 ID", example = "22222222-2222-2222-2222-222222222222")
	UUID orderId,
	@Schema(description = "취소 요청 타입", example = "ORDER_FULL")
	OrderCancellationRequestType requestType,
	@Schema(description = "취소 후 주문 상태", example = "CANCELED")
	OrderStatus orderStatus,
	@Schema(description = "최종 환불 금액", example = "42000")
	Integer refundAmount,
	@Schema(description = "취소 수수료 금액", example = "2000")
	Integer cancellationFeeAmount,
	@Schema(description = "환불되지 않은 예매 수수료 금액(취소한 주문 상세 수 x 1000원 기준)", example = "2000")
	Integer bookingFeeAmount,
	@Schema(description = "결제 취소 후 결제 상태", example = "CANCELED")
	String paymentStatus,
	@Schema(description = "결제 수단", example = "CARD")
	String paymentMethod,
	@Schema(description = "결제 타입", example = "REFUND")
	String paymentType,
	@Schema(description = "환불 완료 일시", example = "2026-03-25T14:30:00")
	LocalDateTime refundAt,
	@Schema(description = "취소된 주문 상세 개수", example = "2")
	Integer canceledItemCount
) {
	public static OrderCancelResponse from(
		OrderCancellationEntity cancellation,
		OrderEntity order,
		Integer bookingFeeAmount,
		String paymentStatus,
		String paymentMethod,
		String paymentType,
		Integer canceledItemCount
	) {
		return new OrderCancelResponse(
			cancellation.getId(),
			order.getId(),
			cancellation.getRequestType(),
			order.getOrderStatus(),
			cancellation.getRefundAmountTotal(),
			cancellation.getFeeAmountTotal(),
			bookingFeeAmount,
			paymentStatus,
			paymentMethod,
			paymentType,
			cancellation.getCompletedAt(),
			canceledItemCount
		);
	}
}
