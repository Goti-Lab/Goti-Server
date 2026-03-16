package com.goti.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResaleListingStatus {
	LISTING("판매등록"),
	HOLD("점유상태"),
	SOLD("판매완료(정산 대기)"),
	SETTLED("판매완료(정산 완료)"),
	CANCELED("판매취소");

	private final String description;
}
