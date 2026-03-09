package com.goti.dto.response;

import java.util.UUID;

public record ResaleHoldResponse(
	UUID holdId
) {
	public ResaleHoldResponse from(UUID holdId){
		return new ResaleHoldResponse(holdId);
	}
}
