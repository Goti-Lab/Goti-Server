package com.goti.ticketing.seat.service.domain;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.global.validation.Preconditions;
import com.goti.infra.queue.QueueAccessReader;
import com.goti.ticketing.constants.SeatStatus;
import com.goti.ticketing.domain.entity.seat.SeatGradeEntity;
import com.goti.ticketing.session.model.ReservationSessionCache;
import com.goti.ticketing.session.service.application.ReservationSessionService;
import com.goti.ticketing.seat.dto.response.SeatGradeRegisterResponse;
import com.goti.ticketing.seat.dto.response.SeatGradeSearchResponse;
import com.goti.ticketing.seat.dto.response.SeatGradeSearchResultResponse;
import com.goti.ticketing.seat.repository.SeatGradeRepository;
import com.goti.ticketing.seat.repository.SeatStatusRepository;
import com.goti.ticketing.seat.repository.dto.SeatGradeAvailableSeatCount;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatGradeServiceImpl implements SeatGradeService {
	private final SeatGradeRepository seatGradeRepository;
	private final SeatStatusRepository seatStatusRepository;
	private final QueueAccessReader queueAccessReader;
	private final ReservationSessionService reservationSessionService;

	@Override
	@Transactional
	public SeatGradeRegisterResponse create(UUID stadiumId, String name, String displayColorHex) {
		Preconditions.validate(
			!seatGradeRepository.existsByStadiumIdAndName(stadiumId, name),
			ErrorCode.SEAT_GRADE_ALREADY_EXISTS
		);

		SeatGradeEntity seatGrade = SeatGradeEntity.create(stadiumId, name, displayColorHex);
		seatGradeRepository.save(seatGrade);
		return SeatGradeRegisterResponse.from(seatGrade);
	}

	@Override
	@Transactional(readOnly = true)
	public SeatGradeSearchResultResponse get(UUID stadiumId, UUID gameId, UUID userId) {
		Preconditions.validate(
			userId != null,
			ErrorCode.AUTH_INVALID
		);
		Preconditions.validate(
			queueAccessReader.isAdmitted(gameId, userId),
			ErrorCode.QUEUE_ADMISSION_REQUIRED
		);

		ReservationSessionCache reservationSession = reservationSessionService.getOrCreate(userId, gameId);

		List<SeatGradeEntity> seatGrades = seatGradeRepository.findAllByStadiumId(stadiumId);
		List<UUID> seatGradeIds = seatGrades.stream()
			.map(SeatGradeEntity::getId)
			.toList();

		Map<UUID, Integer> availableSeatCounts = seatStatusRepository
			.countSeatGradeAvailableSeats(gameId, seatGradeIds, SeatStatus.AVAILABLE)
			.stream()
			.collect(Collectors.toMap(
				SeatGradeAvailableSeatCount::seatGradeId,
				count -> Math.toIntExact(count.availableSeatCount())
			));

		List<SeatGradeSearchResponse> responses = seatGrades.stream()
			.map(seatGrade -> SeatGradeSearchResponse.from(
				seatGrade,
				availableSeatCounts.getOrDefault(seatGrade.getId(), 0)
			))
			.toList();

		return SeatGradeSearchResultResponse.from(reservationSession, responses);
	}
}
