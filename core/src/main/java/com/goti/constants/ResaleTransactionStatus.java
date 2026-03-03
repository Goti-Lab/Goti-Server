package com.goti.constants;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ResaleTransactionStatus {
	PENDING("대기상태"),
	CONFIRMED("확인됨"),
	SETTLED("정산완료");

	private final String description;

}
