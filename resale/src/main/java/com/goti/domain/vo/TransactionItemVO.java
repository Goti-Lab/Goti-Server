package com.goti.domain.vo;

import java.util.UUID;

import com.goti.domain.entity.resale.ResaleListingEntity;
import com.goti.utils.ResalePricePolicy;

public record TransactionItemVO(
	ResaleListingEntity listing,
	ResalePricePolicy.FeeResult feeResult
) {
	public UUID getSellerId() {
		return listing.getSellerId();
	}

	public Integer getListingPrice() {
		return listing.getListingPrice();
	}

	public Integer getBuyerFee() {
		return feeResult.buyerFee();
	}

	public Integer getSellerFee() {
		return feeResult.sellerFee();
	}

	public Integer getBuyerTotal() {
		return feeResult.buyerTotal();
	}

	public Integer getSellerTotal() {
		return feeResult.sellerTotal();
	}
}
