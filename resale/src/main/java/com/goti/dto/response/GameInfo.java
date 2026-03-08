package com.goti.dto.response;

import java.util.UUID;

public record GameInfo(
	UUID gameId,
	UUID gradeId
) {
	public static GameInfo of(UUID gameId, UUID gradeId) {
		return new GameInfo(gameId, gradeId);
	}
}