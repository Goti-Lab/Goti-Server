package com.goti.ticketing.seat.service.domain;

import com.goti.constants.messages.ErrorCode;
import com.goti.ticketing.constants.SeatStatus;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;
import com.goti.ticketing.domain.entity.seat.SeatStatusEntity;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.seat.repository.SeatHoldRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeatHoldServiceImpl implements SeatHoldService {
	private final SeatHoldRepository seatHoldRepository;

	@Override
	@Transactional(readOnly = true)
	public SeatHoldEntity findSeatHold(UUID holdId) {
		return seatHoldRepository.findById(holdId)
			.orElseThrow(() -> new CustomException(ErrorCode.SEAT_HOLD_NOT_FOUND));
	}

	@Override
	@Transactional(readOnly = true)
	public List<SeatHoldEntity> getHoldingSeats(UUID gameId, UUID userId) {
		return seatHoldRepository.findAllHoldingSeats(gameId, userId);
	}

	@Override
	@Transactional(readOnly = true)
	public Map<UUID, SeatHoldEntity> getByIds(List<UUID> holdIds) {
		return seatHoldRepository.findAllWithDetailsByIdIn(holdIds).stream()
			.collect(Collectors.toMap(SeatHoldEntity::getId, seatHold -> seatHold));
	}

	@Override
	public void expire(
		SeatStatusEntity seatStatus,
		SeatHoldEntity seatHold,
		LocalDateTime now
	) {
		Preconditions.domainValidate(seatStatus != null, "좌석 상태는 필수입니다.");
		Preconditions.domainValidate(seatHold != null, "좌석 점유 정보는 필수입니다.");
		Preconditions.domainValidate(now != null, "만료 처리 시각은 필수입니다.");
		Preconditions.domainValidate(
			!seatHold.getExpiredAt().isAfter(now),
			"만료되지 않은 점유는 해제할 수 없습니다."
		);

		if (seatStatus.getStatus() == SeatStatus.HELD) {
			seatStatus.release();
		}
		seatHold.release();
	}
}
