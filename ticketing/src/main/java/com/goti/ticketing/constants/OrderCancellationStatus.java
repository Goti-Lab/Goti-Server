package com.goti.ticketing.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderCancellationStatus {

	REQUESTED("요청 접수"),
	VALIDATED("요청 검증 통과"),
	APPROVAL_PENDING("운영자 승인 대기"),
	REFUNDING("PG 환불 중"),
	COMPLETED("취소/환불 완료"),
	FAILED("실패");

	private final String description;
}
