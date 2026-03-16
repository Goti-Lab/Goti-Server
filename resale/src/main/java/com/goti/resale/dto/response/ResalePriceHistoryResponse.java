package com.goti.resale.dto.response;

import java.time.Instant;

import com.goti.resale.domain.entity.resale.ResalePriceHistoryEntity;

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
