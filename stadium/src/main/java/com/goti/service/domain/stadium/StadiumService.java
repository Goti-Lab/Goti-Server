package com.goti.service.domain.stadium;

import com.goti.dto.response.StadiumCreateResponse;

import java.math.BigDecimal;
import java.util.Map;

public interface StadiumService {

	StadiumCreateResponse create(
		String stadiumName,
		String location,
		String city,
		String district,
		String roadAddress,
		BigDecimal latitude,
		BigDecimal longitude,
		Integer totalSeats,
		Map<String, Object> seatMapConfig
	);
}
