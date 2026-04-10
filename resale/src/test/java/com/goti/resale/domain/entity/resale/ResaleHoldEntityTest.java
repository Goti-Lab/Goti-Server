package com.goti.resale.domain.entity.resale;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.goti.exception.FieldValidationException;
import com.goti.resale.constants.ResaleHoldStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
class ResaleHoldEntityTest {

	private static final UUID VALID_SELLER_ID = UUID.randomUUID();
	private static final UUID VALID_GRADE_ID = UUID.randomUUID();

	ResaleListingEntity validListing;
	UUID validBuyerId;
	String queueTokenJti;
	LocalDateTime expiredAt;
	ResaleListingOrderEntity listingOrder;

	@BeforeEach
	void setUp() {
		listingOrder = ResaleListingOrderEntity.create(
			"ORD-12345678",
			VALID_SELLER_ID,
			VALID_GRADE_ID
		);

		validListing = ResaleListingEntity.create(
			listingOrder,
			UUID.randomUUID(),
			VALID_SELLER_ID,
			UUID.randomUUID(),
			UUID.randomUUID(),
			UUID.randomUUID(),
			VALID_GRADE_ID,
			"A구역 10열 5번",
			50000,
			50000
		);
		queueTokenJti = "queue-token-jti-123";
		expiredAt = LocalDateTime.now().plusSeconds(600);
		validBuyerId = UUID.fromString("8df84c70-833e-4374-85ad-fa52f92f939e");

	}

	@Test
	void 리셀_점유_생성_성공() {
		ResaleHoldEntity resaleHold = ResaleHoldEntity.create(
			validListing,
			validBuyerId,
			queueTokenJti,
			expiredAt
		);

		assertNotNull(resaleHold);
		assertThat(resaleHold.getResaleListing()).isEqualTo(validListing);
		assertThat(resaleHold.getUserId()).isEqualTo(validBuyerId);
		assertThat(resaleHold.getQueueTokenJti()).isEqualTo(queueTokenJti);
		assertThat(resaleHold.getExpiredAt()).isEqualTo(expiredAt);
		assertThat(resaleHold.getStatus()).isEqualTo(ResaleHoldStatus.HOLDING);
		assertThat(resaleHold.getReleasedAt()).isNull();
	}

	@Test
	void 리셀_점유_생성_실패_리셀_등록_NULL() {
		assertThatThrownBy(
			() -> ResaleHoldEntity.create(
				null,
				validBuyerId,
				queueTokenJti,
				expiredAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("리셀 등록은 필수입니다.");
	}

	@Test
	void 리셀_점유_생성_실패_유저_NULL() {
		assertThatThrownBy(
			() -> ResaleHoldEntity.create(
				validListing,
				null,
				queueTokenJti,
				expiredAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("유저 ID는 필수입니다.");
	}

	@Test
	void 리셀_점유_생성_실패_큐토큰_NULL() {
		assertThatThrownBy(
			() -> ResaleHoldEntity.create(
				validListing,
				validBuyerId,
				null,
				expiredAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("큐 토큰 식별자는 비어 있을 수 없습니다.");
	}

	@Test
	void 리셀_점유_생성_실패_큐토큰_빈문자열() {
		assertThatThrownBy(
			() -> ResaleHoldEntity.create(
				validListing,
				validBuyerId,
				"",
				expiredAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("큐 토큰 식별자는 비어 있을 수 없습니다.");
	}

	@Test
	void 리셀_점유_생성_실패_큐토큰_공백() {
		assertThatThrownBy(
			() -> ResaleHoldEntity.create(
				validListing,
				validBuyerId,
				"   ",
				expiredAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("큐 토큰 식별자는 비어 있을 수 없습니다.");
	}

	@Test
	void 리셀_점유_생성_실패_만료시간_NULL() {
		assertThatThrownBy(
			() -> ResaleHoldEntity.create(
				validListing,
				validBuyerId,
				queueTokenJti,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("만료 시각은 필수입니다.");
	}
}