package com.goti.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.constants.ResaleAvailableStatus;
import com.goti.constants.ResaleListingStatus;
import com.goti.domain.entity.resale.ResaleListingEntity;

public record ResaleListingResponse(
	UUID listingId,
	UUID ticketId,
	UUID sellerId,
	UUID gameId,
	String seatInfo,
	Integer dailyBasePrice,
	Integer listingPrice,
	ResaleListingStatus listingStatus,
	ResaleAvailableStatus availableStatus,
	Integer lastTransactionPrice,
	LocalDateTime listedAt,
	LocalDateTime soldAt,
	LocalDateTime canceledAt,
	Boolean isCancelable,
	Boolean isPurchasable,
	Integer minPrice,
	Integer maxPrice
) {
	public static ResaleListingResponse from(ResaleListingEntity entity) {
		int maxPrice = BigDecimal.valueOf(entity.getDailyBasePrice())
			.multiply(BigDecimal.valueOf(1.3))
			.setScale(0, RoundingMode.HALF_UP)
			.intValue();

		int minPrice = BigDecimal.valueOf(entity.getDailyBasePrice())
			.multiply(BigDecimal.valueOf(0.7))
			.setScale(0, RoundingMode.HALF_UP)
			.intValue();

		return new ResaleListingResponse(
			entity.getId(),
			entity.getTicketId(),
			entity.getSellerId(),
			entity.getGameId(),
			entity.getSeatInfo(),
			entity.getDailyBasePrice(),
			entity.getListingPrice(),
			entity.getListingStatus(),
			entity.getAvailableStatus(),
			entity.getLastTransactionPrice(),
			entity.getListedAt(),
			entity.getSoldAt(),
			entity.getCanceledAt(),
			entity.isCancelable(),
			entity.isPurchasable(),
			minPrice,
			maxPrice
		);
	}
}