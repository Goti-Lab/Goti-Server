package com.goti.queue.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QueueStatus {
	WAITING("대기"),
	ADMITTED("입장 허용"),
	LEFT("이탈"),
	EXPIRED("시간 만료");
	private final String description;
}
