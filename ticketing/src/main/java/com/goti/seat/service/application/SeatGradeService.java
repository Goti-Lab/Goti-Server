package com.goti.seat.service.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import com.goti.seat.dto.response.SeatGradeResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatGradeService {
	private final com.goti.seat.service.domain.SeatGradeService seatGradeService;

	public SeatGradeResponse create(
		UUID stadiumId,
		String name,
		String displayColorHex
	) {
		return seatGradeService.create(stadiumId, name, displayColorHex);
	}

	public List<SeatGradeResponse> get(UUID stadiumId) {
		return seatGradeService.get(stadiumId);
	}
}
