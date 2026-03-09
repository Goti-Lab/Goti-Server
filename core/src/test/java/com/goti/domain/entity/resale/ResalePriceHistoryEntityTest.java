package com.goti.domain.entity.resale;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;

import com.goti.exception.FieldValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
class ResalePriceHistoryEntityTest {

	private static final UUID VALID_GAME_ID = UUID.randomUUID();
	private static final UUID VALID_SEAT_ID = UUID.randomUUID();
	private static final UUID VALID_GRADE_ID = UUID.randomUUID();
	private static final Integer VALID_TRANSACTION_PRICE = 50000;
	private static final LocalDate VALID_TRANSACTION_DATE = LocalDate.now().minusDays(1);
	private static final LocalDateTime VALID_TRANSACTION_TIME = LocalDateTime.now().minusHours(1);

	@Test
	void 가격_히스토리_생성_성공() {
		ResalePriceHistoryEntity entity = ResalePriceHistoryEntity.create(
			VALID_GAME_ID,
			VALID_SEAT_ID,
			VALID_GRADE_ID,
			VALID_TRANSACTION_PRICE,
			VALID_TRANSACTION_DATE,
			VALID_TRANSACTION_TIME
		);

		assertAll(
			() -> assertThat(entity.getGameId()).isEqualTo(VALID_GAME_ID),
			() -> assertThat(entity.getSeatId()).isEqualTo(VALID_SEAT_ID),
			() -> assertThat(entity.getGradeId()).isEqualTo(VALID_GRADE_ID),
			() -> assertThat(entity.getTransactionPrice()).isEqualTo(VALID_TRANSACTION_PRICE),
			() -> assertThat(entity.getTransactionDate()).isEqualTo(VALID_TRANSACTION_DATE),
			() -> assertThat(entity.getTransactionTime()).isEqualTo(VALID_TRANSACTION_TIME)
		);
	}

	@Test
	void 거래가격이_0_성공() {
		ResalePriceHistoryEntity entity = ResalePriceHistoryEntity.create(
			VALID_GAME_ID,
			VALID_SEAT_ID,
			VALID_GRADE_ID,
			0,
			VALID_TRANSACTION_DATE,
			VALID_TRANSACTION_TIME
		);

		assertThat(entity.getTransactionPrice()).isZero();
	}

	@Test
	void 과거_날짜_성공() {
		LocalDate pastDate = LocalDate.now().minusYears(1);
		LocalDateTime pastDateTime = LocalDateTime.now().minusMonths(6);

		ResalePriceHistoryEntity entity = ResalePriceHistoryEntity.create(
			VALID_GAME_ID,
			VALID_SEAT_ID,
			VALID_GRADE_ID,
			VALID_TRANSACTION_PRICE,
			pastDate,
			pastDateTime
		);

		assertAll(
			() -> assertThat(entity.getTransactionDate()).isEqualTo(pastDate),
			() -> assertThat(entity.getTransactionTime()).isEqualTo(pastDateTime)
		);
	}

	@Test
	void 게임_ID가_null_실패() {
		// when & then
		assertThatThrownBy(() -> ResalePriceHistoryEntity.create(
			null,
			VALID_SEAT_ID,
			VALID_GRADE_ID,
			VALID_TRANSACTION_PRICE,
			VALID_TRANSACTION_DATE,
			VALID_TRANSACTION_TIME
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("게임 ID는 비어 있을 수 없습니다");
	}

	@Test
	void 좌석_ID가_null_실패() {
		assertThatThrownBy(() -> ResalePriceHistoryEntity.create(
			VALID_GAME_ID,
			null,
			VALID_GRADE_ID,
			VALID_TRANSACTION_PRICE,
			VALID_TRANSACTION_DATE,
			VALID_TRANSACTION_TIME
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("좌석 ID는 비어 있을 수 없습니다");
	}

	@Test
	void 등급_ID가_null_실패() {
		assertThatThrownBy(() -> ResalePriceHistoryEntity.create(
			VALID_GAME_ID,
			VALID_SEAT_ID,
			null,
			VALID_TRANSACTION_PRICE,
			VALID_TRANSACTION_DATE,
			VALID_TRANSACTION_TIME
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("등급 ID는 비어 있을 수 없습니다");
	}

	@ParameterizedTest
	@NullSource
	void 거래가격이_null_실패(Integer transactionPrice) {
		assertThatThrownBy(() -> ResalePriceHistoryEntity.create(
			VALID_GAME_ID,
			VALID_SEAT_ID,
			VALID_GRADE_ID,
			transactionPrice,
			VALID_TRANSACTION_DATE,
			VALID_TRANSACTION_TIME
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("거래 가격은 0 이상이어야 합니다");
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, -50000})
	void 거래가격이_음수_실패(Integer transactionPrice) {
		assertThatThrownBy(() -> ResalePriceHistoryEntity.create(
			VALID_GAME_ID,
			VALID_SEAT_ID,
			VALID_GRADE_ID,
			transactionPrice,
			VALID_TRANSACTION_DATE,
			VALID_TRANSACTION_TIME
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("거래 가격은 0 이상이어야 합니다");
	}

	@Test
	void 체결_날짜가_null_실패() {
		assertThatThrownBy(() -> ResalePriceHistoryEntity.create(
			VALID_GAME_ID,
			VALID_SEAT_ID,
			VALID_GRADE_ID,
			VALID_TRANSACTION_PRICE,
			null,
			VALID_TRANSACTION_TIME
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("체결 날짜는 비어 있을 수 없습니다");
	}

	@Test
	void 체결_날짜가_미래_실패() {
		LocalDate futureDate = LocalDate.now().plusDays(1);

		assertThatThrownBy(() -> ResalePriceHistoryEntity.create(
			VALID_GAME_ID,
			VALID_SEAT_ID,
			VALID_GRADE_ID,
			VALID_TRANSACTION_PRICE,
			futureDate,
			VALID_TRANSACTION_TIME
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("체결 날짜는 과거 날짜여야 합니다");
	}

	@Test
	void 체결_시각이_null_실패() {
		assertThatThrownBy(() -> ResalePriceHistoryEntity.create(
			VALID_GAME_ID,
			VALID_SEAT_ID,
			VALID_GRADE_ID,
			VALID_TRANSACTION_PRICE,
			VALID_TRANSACTION_DATE,
			null
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("체결 시각은 비어 있을 수 없습니다");
	}

	@Test
	void 체결_시각이_미래_실패() {
		LocalDateTime futureTime = LocalDateTime.now().plusHours(1);

		assertThatThrownBy(() -> ResalePriceHistoryEntity.create(
			VALID_GAME_ID,
			VALID_SEAT_ID,
			VALID_GRADE_ID,
			VALID_TRANSACTION_PRICE,
			VALID_TRANSACTION_DATE,
			futureTime
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("체결 시각은 과거여야합니다");
	}
}