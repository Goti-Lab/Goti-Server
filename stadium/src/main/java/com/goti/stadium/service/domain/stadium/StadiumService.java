package com.goti.stadium.service.domain.stadium;

import com.goti.stadium.domain.entity.stadium.StadiumEntity;
import com.goti.stadium.dto.response.StadiumCreateResponse;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

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

	StadiumEntity getById(UUID stadiumId);

}
