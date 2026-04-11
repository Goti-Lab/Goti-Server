package com.goti.stadium.dto.response.internal;

public record StadiumTotalSeatsResponse(
	int totalSeats
) {
	public static StadiumTotalSeatsResponse from(int totalSeats) {
		return new StadiumTotalSeatsResponse(totalSeats);
	}
}
