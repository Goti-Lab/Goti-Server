package com.goti.stadium.service.domain.stadium;

import com.goti.stadium.domain.entity.stadium.StadiumEntity;
import com.goti.stadium.dto.response.StadiumCreateResponse;
import com.goti.stadium.repository.StadiumRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StadiumServiceImpl implements StadiumService {

	private final StadiumRepository stadiumRepository;

	@Override
	@Transactional
	public StadiumCreateResponse create(
		String stadiumName,
		String location,
		String city,
		String district,
		String roadAddress,
		BigDecimal latitude,
		BigDecimal longitude,
		Integer totalSeats,
		Map<String, Object> seatMapConfig
	) {
		StadiumEntity stadium = StadiumEntity.create(
			stadiumName,
			location,
			city,
			district,
			roadAddress,
			latitude,
			longitude,
			totalSeats,
			seatMapConfig
		);
		stadiumRepository.save(stadium);

		return StadiumCreateResponse.from(stadium);
	}

	@Override
	public StadiumEntity getById(UUID stadiumId) {
		return stadiumRepository.findByIdOrThrow(stadiumId);
	}
}
