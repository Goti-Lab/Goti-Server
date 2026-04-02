package com.goti.ticketing.seat.service.domain;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import com.goti.exception.CustomException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.seat.SeatEntity;
import com.goti.ticketing.domain.entity.seat.SeatStatusEntity;
import com.goti.ticketing.constants.SeatStatus;
import com.goti.ticketing.seat.dto.response.GameSeatStatusResponse;
import com.goti.ticketing.seat.repository.SeatStatusRepository;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatStatusServiceImpl implements SeatStatusService {
	private final SeatStatusRepository seatStatusRepository;

	@Override
	@Transactional(readOnly = true)
	public List<GameSeatStatusResponse> get(
		UUID gameId,
		UUID sectionId,
		UUID userId
	) {
		Preconditions.validate(
			userId != null,
			ErrorCode.AUTH_INVALID
		);

		List<GameSeatStatusResponse> seatStatuses = seatStatusRepository.findSeatStatuses(gameId, sectionId).stream()
			.map(GameSeatStatusResponse::from)
			.toList();

		Map<SeatStatus, Long> counts = seatStatuses.stream()
			.collect(Collectors.groupingBy(GameSeatStatusResponse::status, Collectors.counting()));

		log.info(
			"action=SEAT_STATUS gameId={} userId={} sectionId={} total={} available={} held={} sold={} blocked={}",
			gameId,
			userId,
			sectionId,
			seatStatuses.size(),
			counts.getOrDefault(SeatStatus.AVAILABLE, 0L),
			counts.getOrDefault(SeatStatus.HELD, 0L),
			counts.getOrDefault(SeatStatus.SOLD, 0L),
			counts.getOrDefault(SeatStatus.BLOCKED, 0L)
		);

		return seatStatuses;
	}

	@Override
	@Transactional(readOnly = true)
	public Map<UUID, SeatStatusEntity> getByGameIdAndSeatIds(UUID gameId, List<UUID> seatIds) {
		return seatStatusRepository.findAllByGameAndSeatIds(gameId, seatIds)
			.stream()
			.collect(Collectors.toMap(seatStatus -> seatStatus.getSeat().getId(), seatStatus -> seatStatus));
	}

	@Override
	@Transactional(readOnly = true)
	public Set<UUID> getSeatIdsByGameId(UUID gameId) {
		return Set.copyOf(seatStatusRepository.findSeatIdsByGameId(gameId));
	}

	@Override
	@Transactional
	public List<SeatStatusEntity> createMissingStatuses(
		GameScheduleEntity game,
		List<SeatEntity> seats,
		Set<UUID> existingSeatIds
	) {
		List<SeatStatusEntity> newSeatStatuses = seats.stream()
			.filter(seat -> !existingSeatIds.contains(seat.getId()))
			.map(seat -> SeatStatusEntity.create(game, seat))
			.toList();

		return seatStatusRepository.saveAll(newSeatStatuses);
	}

	@Override
	@Transactional(readOnly = true)
	public SeatStatusEntity get(GameScheduleEntity game, SeatEntity seat) {
		return seatStatusRepository.findByGameAndSeat(game, seat)
			.orElseThrow(() -> new CustomException(ErrorCode.SEAT_STATUS_NOT_FOUND));
	}

	@Override
	@Transactional(readOnly = true)
	public long countAvailableSeats(GameScheduleEntity gameSchedule) {
		return seatStatusRepository.countByGameAndStatus(gameSchedule, SeatStatus.AVAILABLE);
	}

	@Override
	@Transactional
	public void cancelSale(SeatStatusEntity seatStatus) {
		Preconditions.domainValidate(
			seatStatus != null,
			"좌석 상태는 필수입니다."
		);
		seatStatus.cancelSale();
	}

	@Override
	public void release(SeatStatusEntity seatStatus) {
		Preconditions.domainValidate(
			seatStatus != null,
			"좌석 상태는 필수입니다."
		);
		seatStatus.release();
	}
}
