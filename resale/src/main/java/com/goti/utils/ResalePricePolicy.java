package com.goti.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.goti.constants.messages.ErrorCode;
import com.goti.global.validation.Preconditions;

import lombok.Getter;

@Component
@Getter
public class ResalePricePolicy {

	private static final BigDecimal MAX_PRICE_RATE = new BigDecimal("1.3");
	private static final BigDecimal MIN_PRICE_RATE = new BigDecimal("0.7");

	public void validatePriceRange(Integer basePrice, Integer listingPrice) {
		Preconditions.validate(basePrice != null && basePrice > 0,
			ErrorCode.INVALID_BASE_PRICE);
		Preconditions.validate(listingPrice != null && listingPrice > 0,
			ErrorCode.INVALID_LISTING_PRICE);

		int maxPrice = BigDecimal.valueOf(basePrice)
			.multiply(MAX_PRICE_RATE)
			.setScale(0, RoundingMode.HALF_UP)
			.intValue();

		int minPrice = BigDecimal.valueOf(basePrice)
			.multiply(MIN_PRICE_RATE)
			.setScale(0, RoundingMode.HALF_UP)
			.intValue();

		Preconditions.validate(
			listingPrice >= minPrice && listingPrice <= maxPrice,
			ErrorCode.INVALID_PRICE_RANGE,
			String.format("%d원 ~ %d원", minPrice, maxPrice)
		);
	}
}