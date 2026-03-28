package com.goti.ticketing.order.dto.request;

import java.util.List;
import java.util.UUID;

import com.goti.ticketing.constants.OrderCancellationRequestType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderCancelRequest(
	@NotNull(message = "취소 요청 타입은 필수입니다.")
	OrderCancellationRequestType requestType,
	List<UUID> orderItemIds,
	@NotBlank(message = "멱등키는 비어 있을 수 없습니다.")
	String idempotencyKey
) {
}
