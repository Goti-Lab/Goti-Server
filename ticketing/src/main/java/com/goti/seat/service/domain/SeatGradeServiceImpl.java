package com.goti.seat.service.domain;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.seat.SeatGradeEntity;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.seat.dto.response.SeatGradeResponse;
import com.goti.seat.repository.SeatGradeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatGradeServiceImpl implements SeatGradeService {
	private final SeatGradeRepository seatGradeRepository;

	@Override
	@Transactional
	public SeatGradeResponse create(UUID stadiumId, String name, String displayColorHex) {
		Preconditions.validate(
			!seatGradeRepository.existsByStadiumIdAndName(stadiumId, name),
			ErrorCode.SEAT_GRADE_ALREADY_EXISTS
		);

		SeatGradeEntity seatGrade = SeatGradeEntity.create(stadiumId, name, displayColorHex);
		try {
			SeatGradeEntity savedSeatGrade = seatGradeRepository.save(seatGrade);
			return SeatGradeResponse.from(savedSeatGrade);
		} catch (DataIntegrityViolationException e) {
			throw new CustomException(ErrorCode.SEAT_GRADE_ALREADY_EXISTS);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<SeatGradeResponse> get(UUID stadiumId) {
		return seatGradeRepository.findAllByStadiumId(stadiumId).stream()
			.map(SeatGradeResponse::from)
			.toList();
	}
}
