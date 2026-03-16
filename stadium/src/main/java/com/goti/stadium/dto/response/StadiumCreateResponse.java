package com.goti.stadium.dto.response;

import com.goti.stadium.domain.entity.stadium.StadiumEntity;

import java.util.UUID;

public record StadiumCreateResponse(
	UUID stadiumId,
	String stadiumName,
	String location,
	String city,
	String district,
	String roadAddress,
	int totalSeats
) {
	public static StadiumCreateResponse from(StadiumEntity stadium) {
		return new StadiumCreateResponse(
			stadium.getId(),
			stadium.getStadiumName(),
			stadium.getLocation(),
			stadium.getCity(),
			stadium.getDistrict(),
			stadium.getRoadAddress(),
			stadium.getTotalSeats()
		);
	}
}
