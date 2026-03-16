package com.goti.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TicketIssueStatus {

	ISSUED("발행 완료"),
	USED("사용 완료"),
	INVALID("사용 불가"),
	RESALE_ISSUED("리셀 발행");

	private final String description;
}
