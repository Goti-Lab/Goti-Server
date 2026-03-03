package com.goti.constants;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ResaleListingStatus {
	RESELL_AVAILABLE("리셀가능"),
	HOLD("점유상태"),
	SOLD("판매완료"),
	CANCELED("취소"),
	FROZEN("동결");

	private final String description;
}
