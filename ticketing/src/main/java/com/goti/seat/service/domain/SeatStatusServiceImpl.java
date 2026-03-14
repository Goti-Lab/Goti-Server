package com.goti.seat.service.domain;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.global.validation.Preconditions;
import com.goti.seat.dto.response.GameSeatStatusResponse;
import com.goti.seat.repository.SeatStatusRepository;

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
}
