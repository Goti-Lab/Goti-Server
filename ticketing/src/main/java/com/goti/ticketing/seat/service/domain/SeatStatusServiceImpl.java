package com.goti.ticketing.seat.service.domain;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.domain.entity.seat.SeatStatusEntity;
import com.goti.ticketing.seat.dto.response.GameSeatStatusResponse;
import com.goti.ticketing.seat.repository.SeatStatusRepository;

import lombok.RequiredArgsConstructor;

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

		return seatStatusRepository.findSeatStatuses(gameId, sectionId).stream()
			.map(GameSeatStatusResponse::from)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Map<UUID, SeatStatusEntity> getByGameIdAndSeatIds(UUID gameId, List<UUID> seatIds) {
		return seatStatusRepository.findAllByGameAndSeatIds(gameId, seatIds)
			.stream()
			.collect(Collectors.toMap(seatStatus -> seatStatus.getSeat().getId(), seatStatus -> seatStatus));
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
