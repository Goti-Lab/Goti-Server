package com.goti.resale.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResaleTransactionStatus {
	PENDING("결제대기"),
	COMPLETED("거래완료");

	private final String description;

}
