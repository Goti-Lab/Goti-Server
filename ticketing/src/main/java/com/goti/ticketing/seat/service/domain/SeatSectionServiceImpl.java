package com.goti.ticketing.seat.service.domain;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.goti.ticketing.seat.dto.response.SeatSectionRegisterResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.ticketing.constants.SeatStatus;
import com.goti.ticketing.domain.entity.seat.SeatGradeEntity;
import com.goti.ticketing.domain.entity.seat.SeatSectionEntity;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.seat.dto.response.SeatSectionSearchResponse;
import com.goti.ticketing.seat.repository.SeatGradeRepository;
import com.goti.ticketing.seat.repository.SeatSectionRepository;
import com.goti.ticketing.seat.repository.SeatStatusRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatSectionServiceImpl implements SeatSectionService {
	private final SeatGradeRepository seatGradeRepository;
	private final SeatSectionRepository seatSectionRepository;
	private final SeatStatusRepository seatStatusRepository;

	@Override
	@Transactional
	public SeatSectionRegisterResponse create(
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

		return SeatSectionRegisterResponse.from(seatSection);
	}

	@Override
	@Transactional(readOnly = true)
	public List<SeatSectionSearchResponse> get(UUID stadiumId, UUID userId, UUID gameId) {
		Preconditions.validate(
			userId != null,
			ErrorCode.AUTH_INVALID
		);

		List<SeatSectionEntity> seatSections = seatSectionRepository.findAllByStadiumId(stadiumId);

		List<UUID> sectionIds = seatSections.stream()
			.map(SeatSectionEntity::getId)
			.toList();

		Map<UUID, Integer> availableSeatCounts = seatStatusRepository
			.countSectionAvailableSeats(gameId, sectionIds, SeatStatus.AVAILABLE)
			.stream()
			.collect(Collectors.toMap(
				SeatStatusRepository.SectionAvailableSeatCountProjection::getSectionId,
				count -> Math.toIntExact(count.getAvailableSeatCount())
			));

		return seatSections.stream()
			.map(section -> SeatSectionSearchResponse.from(
				section,
				availableSeatCounts.getOrDefault(section.getId(), 0)
			))
			.toList();
	}
}
