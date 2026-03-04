package com.goti.domain.entity.resale;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;

import com.goti.constants.ResaleAvailableStatus;
import com.goti.constants.ResaleListingStatus;
import com.goti.exception.FieldValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
class ResaleListingEntityTest {

	private static final UUID VALID_TICKET_ID = UUID.randomUUID();
	private static final UUID VALID_SELLER_ID = UUID.randomUUID();
	private static final UUID VALID_GAME_ID = UUID.randomUUID();
	private static final String VALID_SEAT_INFO = "A구역 10열 5번";
	private static final Integer VALID_DAILY_BASE_PRICE = 50000;
	private static final Integer VALID_LISTING_PRICE = 55000;

	@Test
	void 리셀_생성_성공() {
		ResaleListingEntity entity = ResaleListingEntity.create(
			VALID_TICKET_ID,
			VALID_SELLER_ID,
			VALID_GAME_ID,
			VALID_SEAT_INFO,
			VALID_DAILY_BASE_PRICE,
			VALID_LISTING_PRICE
		);

		assertAll(
			() -> assertThat(entity.getTicketId()).isEqualTo(VALID_TICKET_ID),
			() -> assertThat(entity.getSellerId()).isEqualTo(VALID_SELLER_ID),
			() -> assertThat(entity.getGameId()).isEqualTo(VALID_GAME_ID),
			() -> assertThat(entity.getSeatInfo()).isEqualTo(VALID_SEAT_INFO),
			() -> assertThat(entity.getDailyBasePrice()).isEqualTo(VALID_DAILY_BASE_PRICE),
			() -> assertThat(entity.getListingPrice()).isEqualTo(VALID_LISTING_PRICE),
			() -> assertThat(entity.getListingStatus()).isEqualTo(ResaleListingStatus.LISTING),
			() -> assertThat(entity.getAvailableStatus()).isEqualTo(ResaleAvailableStatus.ENABLED),
			() -> assertThat(entity.getLastTransactionPrice()).isNull(),
			() -> assertThat(entity.getSoldAt()).isNull(),
			() -> assertThat(entity.getCanceledAt()).isNull()
		);
	}

	@Test
	void 원가와_판매가가_0_성공() {
		ResaleListingEntity entity = ResaleListingEntity.create(
			VALID_TICKET_ID,
			VALID_SELLER_ID,
			VALID_GAME_ID,
			VALID_SEAT_INFO,
			0,
			0
		);

		assertAll(
			() -> assertThat(entity.getDailyBasePrice()).isZero(),
			() -> assertThat(entity.getListingPrice()).isZero()
		);
	}

	@Test
	void 티켓_ID가_null_실패() {
		assertThatThrownBy(() -> ResaleListingEntity.create(
			null,
			VALID_SELLER_ID,
			VALID_GAME_ID,
			VALID_SEAT_INFO,
			VALID_DAILY_BASE_PRICE,
			VALID_LISTING_PRICE
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("티켓 ID는 비어 있을 수 없습니다");
	}

	@Test
	void 판매자_ID가_null_실패() {
		assertThatThrownBy(() -> ResaleListingEntity.create(
			VALID_TICKET_ID,
			null,
			VALID_GAME_ID,
			VALID_SEAT_INFO,
			VALID_DAILY_BASE_PRICE,
			VALID_LISTING_PRICE
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("판매자 ID는 비어 있을 수 없습니다");
	}

	@Test
	void 게임_ID가_null_실패() {
		assertThatThrownBy(() -> ResaleListingEntity.create(
			VALID_TICKET_ID,
			VALID_SELLER_ID,
			null,
			VALID_SEAT_INFO,
			VALID_DAILY_BASE_PRICE,
			VALID_LISTING_PRICE
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("게임 ID는 비어 있을 수 없습니다");
	}

	@Test
	void 좌석_정보가_null_실패() {
		assertThatThrownBy(() -> ResaleListingEntity.create(
			VALID_TICKET_ID,
			VALID_SELLER_ID,
			VALID_GAME_ID,
			null,
			VALID_DAILY_BASE_PRICE,
			VALID_LISTING_PRICE
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("좌석 정보는 비어 있을 수 없습니다");
	}

	@ParameterizedTest
	@NullSource
	void 시작가가_null_실패(Integer dailyPrice) {
		assertThatThrownBy(() -> ResaleListingEntity.create(
			VALID_TICKET_ID,
			VALID_SELLER_ID,
			VALID_GAME_ID,
			VALID_SEAT_INFO,
			dailyPrice,
			VALID_LISTING_PRICE
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("일일 기준가는 0 이상이어야 합니다");
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, -50000})
	void 시작가가_음수_실패(Integer dailyPrice) {
		assertThatThrownBy(() -> ResaleListingEntity.create(
			VALID_TICKET_ID,
			VALID_SELLER_ID,
			VALID_GAME_ID,
			VALID_SEAT_INFO,
			dailyPrice,
			VALID_LISTING_PRICE
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("일일 기준가는 0 이상이어야 합니다");
	}

	@ParameterizedTest
	@NullSource
	void 판매가가_null_실패(Integer listingPrice) {
		assertThatThrownBy(() -> ResaleListingEntity.create(
			VALID_TICKET_ID,
			VALID_SELLER_ID,
			VALID_GAME_ID,
			VALID_SEAT_INFO,
			VALID_DAILY_BASE_PRICE,
			listingPrice
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("판매가는 0 이상이어야 합니다");
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, -55000})
	void 판매가가_음수_실패(Integer listingPrice) {
		assertThatThrownBy(() -> ResaleListingEntity.create(
			VALID_TICKET_ID,
			VALID_SELLER_ID,
			VALID_GAME_ID,
			VALID_SEAT_INFO,
			VALID_DAILY_BASE_PRICE,
			listingPrice
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("판매가는 0 이상이어야 합니다");
	}
}