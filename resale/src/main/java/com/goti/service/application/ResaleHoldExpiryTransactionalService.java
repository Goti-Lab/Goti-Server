package com.goti.service.application;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.resale.ResaleHoldEntity;
import com.goti.domain.entity.resale.ResaleListingEntity;
import com.goti.exception.CustomException;
import com.goti.repository.hold.ResaleHoldRepository;
import com.goti.repository.listing.ResaleListingRepository;
import com.goti.service.domain.ResaleHoldExpiryDomainService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResaleHoldExpiryTransactionalService {
	private final ResaleHoldRepository resaleHoldRepository;
	private final ResaleListingRepository resaleListingRepository;
	private final ResaleHoldExpiryDomainService holdExpiryDomainService;

	@Transactional
	public void expire(UUID holdId, LocalDateTime now) {
		ResaleHoldEntity resaleHold = resaleHoldRepository.findById(holdId)
			.orElseThrow(
				() -> new CustomException(ErrorCode.RESALE_HOLD_NOT_FOUND)
			);

		ResaleListingEntity resaleListing = resaleHold.getResaleListing();

		holdExpiryDomainService.expire(resaleListing, resaleHold, now);

		resaleHoldRepository.save(resaleHold);
		resaleListingRepository.save(resaleListing);
	}
}
