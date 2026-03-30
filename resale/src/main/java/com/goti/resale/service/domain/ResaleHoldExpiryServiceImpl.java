package com.goti.resale.service.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.resale.domain.entity.resale.ResaleHoldEntity;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.repository.hold.ResaleHoldRepository;
import com.goti.resale.repository.listing.ResaleListingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResaleHoldExpiryServiceImpl implements ResaleHoldExpiryService {

	private final ResaleListingRepository resaleListingRepository;
	private final ResaleHoldRepository resaleHoldRepository;

	@Override
	public void expire(
		ResaleListingEntity resaleListing,
		ResaleHoldEntity resaleHold,
		LocalDateTime now
	) {
		Preconditions.domainValidate(resaleListing != null, "리셀 상태는 필수입니다.");
		Preconditions.domainValidate(resaleHold != null, "리셀 점유 정보는 필수입니다.");
		Preconditions.domainValidate(now != null, "만료 처리 시각은 필수입니다.");
		Preconditions.domainValidate(
			!resaleHold.getExpiredAt().isAfter(now),
			"만료되지 않은 점유는 해제할 수 없습니다."
		);

		resaleListing.releaseHold();
		resaleHold.release();
	}

	@Override
	@Transactional
	public void expire(UUID holdId, LocalDateTime now) {
		ResaleHoldEntity resaleHold = resaleHoldRepository.findById(holdId)
			.orElseThrow(
				() -> new CustomException(ErrorCode.RESALE_HOLD_NOT_FOUND)
			);

		ResaleListingEntity resaleListing = resaleHold.getResaleListing();

		expire(resaleListing, resaleHold, now);

		resaleHoldRepository.save(resaleHold);
		resaleListingRepository.save(resaleListing);
	}
}
