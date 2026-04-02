package com.goti.resale.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResaleOrderSearchStatus {
	ALL("전체"),
	LISTING("판매중"),
	PENDING("정산대기"),
	SETTLED("정산완료"),
	CANCELED("취소");

	private final String description;
}
