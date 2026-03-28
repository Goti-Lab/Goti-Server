package com.goti.ticketing.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TicketFreezeReason {

	POLICY_VIOLATION("정책 위반"),
	RESALE_CANCEL_AFTER_ONE_HOUR("리셀 등록 1시간 이후 취소");

	private final String description;
}
