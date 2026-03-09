package com.goti.seat.dto.response;

import java.util.UUID;

public record BulkCreateSeatsResponse(
	UUID sectionId,
	String rowName,
	Integer startSeatNumber,
	Integer endSeatNumber
) {
	public static BulkCreateSeatsResponse from(
		UUID sectionId,
		String rowName,
		Integer startSeatNumber,
		Integer endSeatNumber
	) {
		return new BulkCreateSeatsResponse(
			sectionId,
			rowName,
			startSeatNumber,
			endSeatNumber
		);
	}
}
