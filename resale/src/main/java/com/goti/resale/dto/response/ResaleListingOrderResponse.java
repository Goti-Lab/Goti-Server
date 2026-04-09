package com.goti.resale.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.goti.resale.constants.ResaleListingOrderStatus;
import com.goti.resale.domain.entity.resale.ResaleListingOrderEntity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리셀 등록 그룹 상세 정보")
public record ResaleListingOrderResponse(
	@Schema(description = "리셀 주문 ID")
	UUID orderId,

	@Schema(description = "리셀 주문 번호")
	String orderNumber,

	@Schema(description = "등급 ID")
	UUID gradeId,

	@Schema(description = "주문 상태")
	ResaleListingOrderStatus orderStatus,

	@Schema(description = "생성 일시")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	Instant createdAt
) {
	public static ResaleListingOrderResponse from(ResaleListingOrderEntity entity) {
		return new ResaleListingOrderResponse(
			entity.getId(),
			entity.getOrderNumber(),
			entity.getGradeId(),
			entity.getOrderStatus(),
			entity.getCreatedAt()
		);
	}
}
