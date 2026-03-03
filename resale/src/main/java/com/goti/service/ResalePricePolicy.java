package com.goti.domain.policy;

import org.springframework.stereotype.Component;

import com.goti.constants.messages.ErrorCode;
import com.goti.global.validation.Preconditions;

import lombok.Getter;

@Component
@Getter
public class ResalePricePolicy {

	private static final double MAX_PRICE_RATE = 1.3;
	private static final double MIN_PRICE_RATE = 0.7;

	public void validatePriceRange(Integer basePrice, Integer listingPrice) {
		Preconditions.validate(basePrice != null && basePrice > 0,
			ErrorCode.INVALID_BASE_PRICE);
		Preconditions.validate(listingPrice != null && listingPrice > 0,
			ErrorCode.INVALID_LISTING_PRICE);

		PriceRange range = calculatePriceRange(basePrice);

		Preconditions.validate(
			listingPrice >= range.minPrice() && listingPrice <= range.maxPrice(),
			ErrorCode.INVALID_PRICE_RANGE,
			String.format("%,d원 ~ %,d원", range.minPrice(), range.maxPrice())
		);
	}

	public PriceRange calculatePriceRange(Integer basePrice) {
		int maxPrice = (int)(basePrice * MAX_PRICE_RATE);
		int minPrice = (int)(basePrice * MIN_PRICE_RATE);
		return new PriceRange(minPrice, maxPrice);
	}

	public record PriceRange(int minPrice, int maxPrice) {
		
	}
}