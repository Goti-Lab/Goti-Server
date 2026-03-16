package com.goti.resale.domain.entity.resale;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;

import com.goti.resale.constants.ResaleSettlementStatus;
import com.goti.exception.FieldValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
class ResaleSettlementEntityTest {

	private static final UUID VALID_ESCROW_ID = UUID.randomUUID();
	private static final UUID VALID_SELLER_ID = UUID.randomUUID();
	private static final Integer VALID_SETTLEMENT_AMOUNT = 48000;
	
	@Test
	void 리셀_정산_생성_성공() {
		ResaleSettlementEntity entity = ResaleSettlementEntity.create(
			VALID_ESCROW_ID,
			VALID_SELLER_ID,
			VALID_SETTLEMENT_AMOUNT
		);

		assertAll(
			() -> assertThat(entity.getEscrowId()).isEqualTo(VALID_ESCROW_ID),
			() -> assertThat(entity.getSellerId()).isEqualTo(VALID_SELLER_ID),
			() -> assertThat(entity.getSettlementAmount()).isEqualTo(VALID_SETTLEMENT_AMOUNT),
			() -> assertThat(entity.getSettlementStatus()).isEqualTo(ResaleSettlementStatus.PENDING),
			() -> assertThat(entity.getBankName()).isNull(),
			() -> assertThat(entity.getAccountNumber()).isNull(),
			() -> assertThat(entity.getAccountHolder()).isNull(),
			() -> assertThat(entity.getSettledAt()).isNull(),
			() -> assertThat(entity.getFailedReason()).isNull()
		);
	}

	@Test
	void 정산_금액이_0_성공() {
		ResaleSettlementEntity entity = ResaleSettlementEntity.create(
			VALID_ESCROW_ID,
			VALID_SELLER_ID,
			0
		);

		assertThat(entity.getSettlementAmount()).isZero();
	}

	@Test
	void 초기_상태가_PENDING_성공() {
		ResaleSettlementEntity entity = ResaleSettlementEntity.create(
			VALID_ESCROW_ID,
			VALID_SELLER_ID,
			VALID_SETTLEMENT_AMOUNT
		);

		assertThat(entity.getSettlementStatus()).isEqualTo(ResaleSettlementStatus.PENDING);
	}

	@Test
	void 계좌_정보가_모두_null_성공() {
		ResaleSettlementEntity entity = ResaleSettlementEntity.create(
			VALID_ESCROW_ID,
			VALID_SELLER_ID,
			VALID_SETTLEMENT_AMOUNT
		);

		assertAll(
			() -> assertThat(entity.getBankName()).isNull(),
			() -> assertThat(entity.getAccountNumber()).isNull(),
			() -> assertThat(entity.getAccountHolder()).isNull()
		);
	}

	@Test
	void 정산_관련_필드가_모두_null_성공() {
		ResaleSettlementEntity entity = ResaleSettlementEntity.create(
			VALID_ESCROW_ID,
			VALID_SELLER_ID,
			VALID_SETTLEMENT_AMOUNT
		);

		assertAll(
			() -> assertThat(entity.getSettledAt()).isNull(),
			() -> assertThat(entity.getFailedReason()).isNull()
		);
	}

	@Test
	void 에스크로_ID가_null_실패() {
		assertThatThrownBy(() -> ResaleSettlementEntity.create(
			null,
			VALID_SELLER_ID,
			VALID_SETTLEMENT_AMOUNT
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("에스크로 ID는 비어 있을 수 없습니다");
	}

	@Test
	void 판매자_ID가_null_실패() {
		assertThatThrownBy(() -> ResaleSettlementEntity.create(
			VALID_ESCROW_ID,
			null,
			VALID_SETTLEMENT_AMOUNT
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("판매자 ID는 비어 있을 수 없습니다");
	}

	@ParameterizedTest
	@NullSource
	void 정산_금액이_null_실패(Integer settlementAmount) {
		assertThatThrownBy(() -> ResaleSettlementEntity.create(
			VALID_ESCROW_ID,
			VALID_SELLER_ID,
			settlementAmount
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("정산 금액은 0 이상이어야 합니다");
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, -1000, -48000})
	void 정산_금액이_음수_실패(Integer settlementAmount) {
		assertThatThrownBy(() -> ResaleSettlementEntity.create(
			VALID_ESCROW_ID,
			VALID_SELLER_ID,
			settlementAmount
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("정산 금액은 0 이상이어야 합니다");
	}
}