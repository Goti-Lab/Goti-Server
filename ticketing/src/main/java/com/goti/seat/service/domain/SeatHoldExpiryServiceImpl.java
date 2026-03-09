package com.goti.seat.service.domain;

import com.goti.domain.entity.seat.SeatHoldEntity;
import com.goti.domain.entity.seat.SeatStatusEntity;
import com.goti.global.validation.Preconditions;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SeatHoldExpiryServiceImpl implements SeatHoldExpiryService {

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
