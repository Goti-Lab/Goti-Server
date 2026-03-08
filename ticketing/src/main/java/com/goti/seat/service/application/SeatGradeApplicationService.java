package com.goti.seat.service.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.seat.SeatGradeEntity;
import com.goti.global.validation.Preconditions;
import com.goti.seat.dto.response.SeatGradeResponse;
import com.goti.seat.repository.SeatGradeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatGradeApplicationService {
	private final SeatGradeRepository seatGradeRepository;

	@Transactional
	public SeatGradeResponse create(
		UUID stadiumId,
		String name,
		String displayColorHex
	) {
		Preconditions.validate(
			!seatGradeRepository.existsByStadiumIdAndName(stadiumId, name),
			ErrorCode.SEAT_GRADE_ALREADY_EXISTS
		);

		SeatGradeEntity seatGrade = SeatGradeEntity.create(stadiumId, name, displayColorHex);
		SeatGradeEntity savedSeatGrade = seatGradeRepository.save(seatGrade);

		return SeatGradeResponse.from(savedSeatGrade);
	}
}
