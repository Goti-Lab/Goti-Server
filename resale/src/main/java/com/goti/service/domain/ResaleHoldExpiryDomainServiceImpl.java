package com.goti.service.domain;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.goti.domain.entity.resale.ResaleHoldEntity;
import com.goti.domain.entity.resale.ResaleListingEntity;
import com.goti.global.validation.Preconditions;

@Service
public class ResaleHoldExpiryDomainServiceImpl implements ResaleHoldExpiryDomainService {

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
}