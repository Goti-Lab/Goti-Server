package com.goti.resale.utils;

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
	private static final BigDecimal BUYER_CHARGE_RATE = new BigDecimal("0.05");
	private static final BigDecimal SELLER_CHARGE_RATE = new BigDecimal("0.05");
	private static final BigDecimal VAT_RATE = new BigDecimal("0.1");

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

	public FeeResult validateTransactionFee(Integer price) {

		Preconditions.validate(
			price != null && price > 0,
			ErrorCode.INVALID_LISTING_PRICE
		);

		BigDecimal buyerCharge = BigDecimal.valueOf(price).multiply(BUYER_CHARGE_RATE);
		BigDecimal sellerCharge = BigDecimal.valueOf(price).multiply(SELLER_CHARGE_RATE);
		BigDecimal buyerVAT = buyerCharge.multiply(VAT_RATE);
		BigDecimal sellerVAT = sellerCharge.multiply(VAT_RATE);
		int buyerFee = buyerCharge
			.add(buyerVAT)
			.setScale(0, RoundingMode.HALF_UP)
			.intValue();
		int sellerFee = sellerCharge
			.add(sellerVAT)
			.setScale(0, RoundingMode.HALF_UP)
			.intValue();
		int buyerTotal = (price + buyerFee);
		int sellerTotal = (price - sellerFee);

		Preconditions.validate(
			buyerTotal >= price, ErrorCode.INVALID_PRICE,
			"구매자 총 결제 금액에 오류가 발생했습니다."
		);

		Preconditions.validate(
			sellerTotal > 0, ErrorCode.INVALID_PRICE,
			"판매자 정산 금액이 0원 이하가 될 수 없습니다. 판매가를 올려주세요."
		);

		return new FeeResult(buyerFee, sellerFee, buyerTotal, sellerTotal);
	}

	public record FeeResult(
		Integer buyerFee,
		Integer sellerFee,
		Integer buyerTotal,
		Integer sellerTotal
	) {
	}
}