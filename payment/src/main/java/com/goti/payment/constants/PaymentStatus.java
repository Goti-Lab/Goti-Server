package com.goti.payment.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

	PENDING("대기"),
	SUCCESS("성공"),
	FAILED("실패"),
	CANCELED("취소");

	private final String description;
}
