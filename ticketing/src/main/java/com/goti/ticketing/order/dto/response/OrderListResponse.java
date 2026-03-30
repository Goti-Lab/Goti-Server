package com.goti.ticketing.order.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.goti.constants.OrderStatus;
import com.goti.ticketing.domain.entity.order.OrderEntity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 목록 응답")
public record OrderListResponse(
	@Schema(description = "주문 ID", example = "f4f4d89f-8a2f-4ec2-9b1f-4b3f8c5d7e11")
	UUID orderId,
	@Schema(description = "주문 번호", example = "ORD-260328123456")
	String orderNumber,
	@Schema(description = "주문 상태", example = "CONFIRMED")
	OrderStatus orderStatus,
	@Schema(description = "총 수량", example = "2")
	Integer totalQuantity,
	@Schema(description = "총 결제 금액", example = "42000")
	Integer totalAmount,
	@Schema(description = "주문 일시", example = "2026-03-16 10:15")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
	Instant orderedAt,
	@Schema(description = "경기 ID", example = "62c73f2d-87ab-4f5c-9d66-97d4d7771111")
	UUID gameId,
	@Schema(description = "구장 ID", example = "8347997b-886f-4e6e-80e6-ff64f1e9e057")
	UUID stadiumId
) {
	public static OrderListResponse from(OrderEntity order) {
		return new OrderListResponse(
			order.getId(),
			order.getOrderNumber(),
			order.getOrderStatus(),
			order.getTotalQuantity(),
			order.getTotalAmount(),
			order.getCreatedAt(),
			order.getGameSchedule().getId(),
			order.getGameSchedule().getStadiumId()
		);
	}
}
