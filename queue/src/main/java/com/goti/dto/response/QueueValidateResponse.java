package com.goti.dto.response;

import java.util.UUID;

public record QueueValidateResponse(
	UUID gameId,
	boolean isPassed,
	Long rank,
	String token
) {
}
