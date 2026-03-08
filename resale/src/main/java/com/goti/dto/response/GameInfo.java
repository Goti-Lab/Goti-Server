package com.goti.dto.response;

import java.util.UUID;

public record GameInfo(
	UUID gameId,
	UUID gradeId
) {
}