package com.goti.seat.service.domain;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
		UUID sectionId
	) {
		return seatStatusRepository.findSeatStatuses(gameId, sectionId).stream()
			.map(GameSeatStatusResponse::from)
			.toList();
	}
}
