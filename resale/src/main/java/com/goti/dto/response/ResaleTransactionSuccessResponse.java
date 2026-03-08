package com.goti.dto.response;

import java.util.UUID;

import com.goti.domain.entity.resale.ResaleTransactionEntity;

public record ResaleTransactionSuccessResponse(
	UUID ticketId,
	Integer buyerFee,
	Integer buyerTotal
) {
	public static ResaleTransactionSuccessResponse result(ResaleTransactionEntity entity) {
		return new ResaleTransactionSuccessResponse(
			entity.getListing().getTicketId(),
			entity.getBuyerFee(),
			entity.getBuyerTotal()
		);
	}
}
