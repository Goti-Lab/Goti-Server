package com.goti.resale.dto.response;

import java.util.UUID;

public record ResaleReleaseResponse(
	UUID holdId
) {
	public static ResaleReleaseResponse from(UUID holdId) {
		return new ResaleReleaseResponse(holdId);
	}
}
