package com.goti.ticketing.seat.service.domain;

import java.util.List;
import java.util.UUID;

import com.goti.ticketing.seat.dto.response.SeatSectionResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.ticketing.domain.entity.seat.SeatGradeEntity;
import com.goti.ticketing.domain.entity.seat.SeatSectionEntity;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.seat.repository.SeatGradeRepository;
import com.goti.ticketing.seat.repository.SeatSectionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatSectionServiceImpl implements SeatSectionService {
	private final SeatGradeRepository seatGradeRepository;
	private final SeatSectionRepository seatSectionRepository;

	@Override
	@Transactional
	public SeatSectionResponse create(
		UUID gradeId,
		UUID stadiumId,
		String sectionCode,
		Integer capacity
	) {
		SeatGradeEntity seatGrade = seatGradeRepository.findById(gradeId)
			.orElseThrow(() -> new CustomException(ErrorCode.SEAT_GRADE_NOT_FOUND));

		Preconditions.validate(
			seatGrade.getStadiumId().equals(stadiumId),
			ErrorCode.SEAT_GRADE_STADIUM_MISMATCH
		);

		Preconditions.validate(
			!seatSectionRepository.existsByStadiumIdAndSectionCode(stadiumId, sectionCode),
			ErrorCode.SEAT_SECTION_ALREADY_EXISTS
		);

		SeatSectionEntity seatSection = SeatSectionEntity.create(seatGrade, stadiumId, sectionCode, capacity);
		seatSectionRepository.save(seatSection);

		return SeatSectionResponse.from(seatSection);
	}

	@Override
	@Transactional(readOnly = true)
	public List<SeatSectionResponse> get(UUID stadiumId, UUID userId) {
		Preconditions.validate(
			userId != null,
			ErrorCode.AUTH_INVALID
		);

		return seatSectionRepository.findAllByStadiumId(stadiumId).stream()
			.map(SeatSectionResponse::from)
			.toList();
	}
}
