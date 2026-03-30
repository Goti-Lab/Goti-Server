package com.goti.resale.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.goti.resale.domain.entity.resale.ResalePriceHistoryEntity;

public record ResalePriceHistoryResponse(
	Integer transactionPrice,
	BigDecimal changePercent,
	Instant confirmedAt
) {
	public static ResalePriceHistoryResponse from(ResalePriceHistoryEntity history) {
		return new ResalePriceHistoryResponse(
			history.getTransactionPrice(),
			history.getChangePercent(),
			history.getCreatedAt()
		);
	}
}
