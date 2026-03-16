package com.goti.constants;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ResaleTransactionStatus {
	PENDING("결제대기"),
	COMPLETED("거래완료");

	private final String description;

}
