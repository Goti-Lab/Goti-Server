package com.goti.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

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
	Integer lastTransactionPrice,
	LocalDateTime listedAt,
	LocalDateTime soldAt,
	LocalDateTime canceledAt,
	LocalDateTime defrostAt,
	Boolean canCancelImmediately,
	Integer minPrice,
	Integer maxPrice
) {
	public static ResaleListingResponse from(ResaleListingEntity entity) {
		int minPrice = (int)(entity.getDailyBasePrice() * 0.7);
		int maxPrice = (int)(entity.getDailyBasePrice() * 1.3);

		return new ResaleListingResponse(
			entity.getId(),
			entity.getTicketId(),
			entity.getSellerId(),
			entity.getGameId(),
			entity.getSeatInfo(),
			entity.getDailyBasePrice(),
			entity.getListingPrice(),
			entity.getListingStatus(),
			entity.getLastTransactionPrice(),
			entity.getListedAt(),
			entity.getSoldAt(),
			entity.getCanceledAt(),
			entity.getDefrostAt(),
			entity.isWithinOneHour(),
			minPrice,
			maxPrice
		);
	}
}