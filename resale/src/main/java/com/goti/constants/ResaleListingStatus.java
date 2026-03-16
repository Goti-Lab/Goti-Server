package com.goti.constants;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ResaleListingStatus {
	LISTING("판매중"),
	HOLD("점유상태"),
	SOLD("판매완료"),
	CANCELED("판매취소");

	private final String description;
}
