package com.goti.constants;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ResaleListingStatus {
	RESELL_AVAILABLE("리셀가능"),
	HOLD("홀딩중"),
	SOLD("판매됨"),
	CANCELED("취소"),
	FROZEN("동결");

	private final String description;
}
