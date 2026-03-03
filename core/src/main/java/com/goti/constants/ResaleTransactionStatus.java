package com.goti.constants;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ResaleTransactionStatus {
	PENDING("대기중"),
	CONFIRMED("확인됨"),
	SETTLED("정산됨");

	private final String description;

}
