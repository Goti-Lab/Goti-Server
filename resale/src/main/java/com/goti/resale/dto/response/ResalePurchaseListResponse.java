package com.goti.resale.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.goti.resale.constants.ResaleOrderStatus;
import com.goti.resale.domain.entity.resale.ResaleOrderEntity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리셀 구매 내역 목록 응답")
public record ResalePurchaseListResponse(
	@Schema(description = "리셀 주문 ID", example = "f4f4d89f-8a2f-4ec2-9b1f-4b3f8c5d7e11")
	UUID orderId,
	@Schema(description = "주문 번호", example = "ORD-260328123456")
	String orderNumber,
	@Schema(description = "주문 상태", example = "COMPLETED")
	ResaleOrderStatus orderStatus,
	@Schema(description = "총 수량", example = "2")
	Integer totalQuantity,
	@Schema(description = "총 결제 금액", example = "42000")
	Integer totalAmount,
	@Schema(description = "주문 일시", example = "2026-03-16 10:15")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
	Instant orderedAt,
	@Schema(description = "경기 ID", example = "62c73f2d-87ab-4f5c-9d66-97d4d7771111")
	UUID gameId,
	@Schema(description = "좌석 정보 목록", example = "[\"1루 K8석(3)\", \"109구역 1열 8번\"]")
	List<String> seatInfos
) {
	public static ResalePurchaseListResponse of(
		ResaleOrderEntity order,
		UUID gameId,
		List<String> seatInfos
	) {
		return new ResalePurchaseListResponse(
			order.getId(),
			order.getOrderNumber(),
			order.getOrderStatus(),
			seatInfos.size(),
			order.getTotalAmount(),
			order.getCreatedAt(),
			gameId,
			seatInfos
		);
	}
}
