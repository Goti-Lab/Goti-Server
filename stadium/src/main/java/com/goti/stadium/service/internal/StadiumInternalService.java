package com.goti.stadium.service.internal;

import com.goti.stadium.domain.entity.stadium.StadiumEntity;
import com.goti.stadium.dto.response.internal.StadiumTotalSeatsResponse;
import com.goti.stadium.repository.StadiumRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StadiumInternalService {

	private final StadiumRepository stadiumRepository;

	public StadiumTotalSeatsResponse getTotalSeats(UUID stadiumId) {
		StadiumEntity stadium = stadiumRepository.findByIdOrThrow(stadiumId);
		return StadiumTotalSeatsResponse.from(stadium.getTotalSeats());
	}

}
