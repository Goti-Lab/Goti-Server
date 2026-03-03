package com.goti.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GameResult {

	PENDING("경기 미종료"),
	HOME_WIN("홈팀 승리"),
	AWAY_WIN("원정팀 승리"),
	DRAW("무승부"),
	CANCELLED("경기 취소");

	private final String description;
}