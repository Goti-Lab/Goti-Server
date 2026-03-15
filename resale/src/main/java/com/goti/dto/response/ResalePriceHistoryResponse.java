package com.goti.dto.response;

import java.time.Instant;

import com.goti.domain.entity.resale.ResalePriceHistoryEntity;

public record ResalePriceHistoryResponse(
	Integer transactionPrice,
	Instant confirmedAt
) {
	public static ResalePriceHistoryResponse from(ResalePriceHistoryEntity history) {
		return new ResalePriceHistoryResponse(
			history.getTransactionPrice(),
			history.getCreatedAt()
		);
	}
}
