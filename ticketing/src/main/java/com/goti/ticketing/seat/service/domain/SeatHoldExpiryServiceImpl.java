package com.goti.ticketing.seat.service.domain;

import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;
import com.goti.ticketing.domain.entity.seat.SeatStatusEntity;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.seat.repository.SeatHoldRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatHoldExpiryServiceImpl implements SeatHoldExpiryService {
	private final SeatHoldRepository seatHoldRepository;

	@Override
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

		seatStatus.release();
		seatHold.release();
	}
}
