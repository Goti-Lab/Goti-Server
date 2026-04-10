package com.goti.ticketing.order.dto.response;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime orderedAt,
	@Schema(description = "경기 ID", example = "62c73f2d-87ab-4f5c-9d66-97d4d7771111")
	UUID gameId,
	@Schema(description = "구장 ID", example = "8347997b-886f-4e6e-80e6-ff64f1e9e057")
	UUID stadiumId,
	@Schema(description = "경기 제목", example = "두산 베어스 vs LG 트윈스")
	String gameTitle,
	@Schema(description = "경기 일시", example = "2026-03-27 18:30")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime gameDate,
	@Schema(description = "구장 지역", example = "대구")
	String stadiumLocation,
	@Schema(description = "등급별 좌석 정보 묶음")
	List<SeatGradeInfoResponse> seatGradeGroups
) {
	public static OrderListResponse of(
		OrderEntity order,
		String gameTitle,
		LocalDateTime gameDate,
		String stadiumLocation,
		List<SeatGradeInfoResponse> seatGradeGroups
	) {
		return new OrderListResponse(
			order.getId(),
			order.getOrderNumber(),
			order.getOrderStatus(),
			order.getTotalQuantity(),
			order.getTotalAmount(),
			LocalDateTime.ofInstant(order.getCreatedAt(), ZoneId.of("Asia/Seoul")),
			order.getGameSchedule().getId(),
			order.getGameSchedule().getStadiumId(),
			gameTitle,
			gameDate,
			stadiumLocation,
			seatGradeGroups
		);
	}
}
