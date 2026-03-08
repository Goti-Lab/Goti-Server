package com.goti.domain.entity.resale;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;

import com.goti.constants.ResaleTransactionStatus;
import com.goti.exception.FieldValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
class ResaleTransactionEntityTest {

	private static final UUID VALID_BUYER_ID = UUID.randomUUID();
	private static final UUID VALID_SELLER_ID = UUID.randomUUID();
	private static final Integer VALID_TRANSACTION_PRICE = 50000;
	private static final Integer VALID_BUYER_FEE = 2500;
	private static final Integer VALID_SELLER_FEE = 2000;
	private static final Integer VALID_BUYER_TOTAL = 52500; // 50000 + 2500
	private static final Integer VALID_SELLER_TOTAL = 48000; // 50000 - 2000

	private ResaleListingEntity validListing;

	@BeforeEach
	void setUp() {
		validListing = ResaleListingEntity.create(
			UUID.randomUUID(),
			VALID_SELLER_ID,
			UUID.randomUUID(),
			UUID.randomUUID(),
			UUID.randomUUID(),
			"A구역 10열 5번",
			50000,
			50000
		);
	}

	@Test
	void 리셀_거래_생성_성공() {
		ResaleTransactionEntity entity = ResaleTransactionEntity.create(
			validListing,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			VALID_TRANSACTION_PRICE,
			VALID_BUYER_FEE,
			VALID_SELLER_FEE,
			VALID_BUYER_TOTAL,
			VALID_SELLER_TOTAL
		);

		assertAll(
			() -> assertThat(entity.getListing()).isEqualTo(validListing),
			() -> assertThat(entity.getBuyerId()).isEqualTo(VALID_BUYER_ID),
			() -> assertThat(entity.getSellerId()).isEqualTo(VALID_SELLER_ID),
			() -> assertThat(entity.getTransactionPrice()).isEqualTo(VALID_TRANSACTION_PRICE),
			() -> assertThat(entity.getBuyerFee()).isEqualTo(VALID_BUYER_FEE),
			() -> assertThat(entity.getSellerFee()).isEqualTo(VALID_SELLER_FEE),
			() -> assertThat(entity.getBuyerTotal()).isEqualTo(VALID_BUYER_TOTAL),
			() -> assertThat(entity.getSellerTotal()).isEqualTo(VALID_SELLER_TOTAL),
			() -> assertThat(entity.getTransactionStatus()).isEqualTo(ResaleTransactionStatus.PENDING),
			() -> assertThat(entity.getConfirmedAt()).isNull()
		);
	}

	@Test
	void 수수료가_0_성공() {
		Integer buyerTotal = VALID_TRANSACTION_PRICE; // 수수료 0
		Integer sellerTotal = VALID_TRANSACTION_PRICE; // 수수료 0

		ResaleTransactionEntity entity = ResaleTransactionEntity.create(
			validListing,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			VALID_TRANSACTION_PRICE,
			0,
			0,
			buyerTotal,
			sellerTotal
		);

		assertAll(
			() -> assertThat(entity.getBuyerFee()).isZero(),
			() -> assertThat(entity.getSellerFee()).isZero(),
			() -> assertThat(entity.getBuyerTotal()).isEqualTo(VALID_TRANSACTION_PRICE),
			() -> assertThat(entity.getSellerTotal()).isEqualTo(VALID_TRANSACTION_PRICE)
		);
	}

	@Test
	void 거래_가격이_0_성공() {
		ResaleTransactionEntity entity = ResaleTransactionEntity.create(
			validListing,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			0,
			0,
			0,
			0,
			0
		);

		assertAll(
			() -> assertThat(entity.getTransactionPrice()).isZero(),
			() -> assertThat(entity.getBuyerTotal()).isZero(),
			() -> assertThat(entity.getSellerTotal()).isZero()
		);
	}

	// ===== 실패 케이스 - UUID 필드 =====

	@Test
	void 구매자_ID가_null_실패() {
		assertThatThrownBy(() -> ResaleTransactionEntity.create(
			validListing,
			null,
			VALID_SELLER_ID,
			VALID_TRANSACTION_PRICE,
			VALID_BUYER_FEE,
			VALID_SELLER_FEE,
			VALID_BUYER_TOTAL,
			VALID_SELLER_TOTAL
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("구매자 ID는 비어 있을 수 없습니다");
	}

	@Test
	void 판매자_ID가_null_실패() {
		assertThatThrownBy(() -> ResaleTransactionEntity.create(
			validListing,
			VALID_BUYER_ID,
			null,
			VALID_TRANSACTION_PRICE,
			VALID_BUYER_FEE,
			VALID_SELLER_FEE,
			VALID_BUYER_TOTAL,
			VALID_SELLER_TOTAL
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("판매자 ID는 비어 있을 수 없습니다");
	}

	@Test
	void 구매자와_판매자가_같음_실패() {
		UUID sameUserId = UUID.randomUUID();

		assertThatThrownBy(() -> ResaleTransactionEntity.create(
			validListing,
			sameUserId,
			sameUserId,
			VALID_TRANSACTION_PRICE,
			VALID_BUYER_FEE,
			VALID_SELLER_FEE,
			VALID_BUYER_TOTAL,
			VALID_SELLER_TOTAL
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("구매자와 판매자는 같을 수 없습니다");
	}

	@ParameterizedTest
	@NullSource
	void 거래_가격이_null_실패(Integer transactionPrice) {
		assertThatThrownBy(() -> ResaleTransactionEntity.create(
			validListing,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			transactionPrice,
			VALID_BUYER_FEE,
			VALID_SELLER_FEE,
			VALID_BUYER_TOTAL,
			VALID_SELLER_TOTAL
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("거래 가격은 0 이상이어야 합니다");
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, -10000})
	void 거래_가격이_음수_실패(Integer transactionPrice) {
		assertThatThrownBy(() -> ResaleTransactionEntity.create(
			validListing,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			transactionPrice,
			VALID_BUYER_FEE,
			VALID_SELLER_FEE,
			VALID_BUYER_TOTAL,
			VALID_SELLER_TOTAL
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("거래 가격은 0 이상이어야 합니다");
	}

	@ParameterizedTest
	@NullSource
	void 구매자_수수료가_null_실패(Integer buyerFee) {
		assertThatThrownBy(() -> ResaleTransactionEntity.create(
			validListing,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			VALID_TRANSACTION_PRICE,
			buyerFee,
			VALID_SELLER_FEE,
			VALID_BUYER_TOTAL,
			VALID_SELLER_TOTAL
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("구매자 수수료는 0 이상이어야 합니다");
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, -1000})
	void 구매자_수수료가_음수_실패(Integer buyerFee) {
		assertThatThrownBy(() -> ResaleTransactionEntity.create(
			validListing,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			VALID_TRANSACTION_PRICE,
			buyerFee,
			VALID_SELLER_FEE,
			VALID_BUYER_TOTAL,
			VALID_SELLER_TOTAL
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("구매자 수수료는 0 이상이어야 합니다");
	}

	@ParameterizedTest
	@NullSource
	void 판매자_수수료가_null_실패(Integer sellerFee) {
		assertThatThrownBy(() -> ResaleTransactionEntity.create(
			validListing,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			VALID_TRANSACTION_PRICE,
			VALID_BUYER_FEE,
			sellerFee,
			VALID_BUYER_TOTAL,
			VALID_SELLER_TOTAL
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("판매자 수수료는 0 이상이어야 합니다");
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, -1000})
	void 판매자_수수료가_음수_실패(Integer sellerFee) {
		assertThatThrownBy(() -> ResaleTransactionEntity.create(
			validListing,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			VALID_TRANSACTION_PRICE,
			VALID_BUYER_FEE,
			sellerFee,
			VALID_BUYER_TOTAL,
			VALID_SELLER_TOTAL
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("판매자 수수료는 0 이상이어야 합니다");
	}

	@ParameterizedTest
	@NullSource
	void 구매자_총액이_null_실패(Integer buyerTotal) {
		assertThatThrownBy(() -> ResaleTransactionEntity.create(
			validListing,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			VALID_TRANSACTION_PRICE,
			VALID_BUYER_FEE,
			VALID_SELLER_FEE,
			buyerTotal,
			VALID_SELLER_TOTAL
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("구매자 총액은 0 이상이어야 합니다");
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, -10000})
	void 구매자_총액이_음수_실패(Integer buyerTotal) {
		assertThatThrownBy(() -> ResaleTransactionEntity.create(
			validListing,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			VALID_TRANSACTION_PRICE,
			VALID_BUYER_FEE,
			VALID_SELLER_FEE,
			buyerTotal,
			VALID_SELLER_TOTAL
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("구매자 총액은 0 이상이어야 합니다");
	}

	@Test
	void 구매자_총액_계산_오류_실패() {
		Integer wrongBuyerTotal = 99999;

		assertThatThrownBy(() -> ResaleTransactionEntity.create(
			validListing,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			VALID_TRANSACTION_PRICE,
			VALID_BUYER_FEE,
			VALID_SELLER_FEE,
			wrongBuyerTotal,
			VALID_SELLER_TOTAL
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("구매자 총액이 올바르지 않습니다");
	}

	@ParameterizedTest
	@NullSource
	void 판매자_총액이_null_실패(Integer sellerTotal) {
		assertThatThrownBy(() -> ResaleTransactionEntity.create(
			validListing,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			VALID_TRANSACTION_PRICE,
			VALID_BUYER_FEE,
			VALID_SELLER_FEE,
			VALID_BUYER_TOTAL,
			sellerTotal
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("판매자 총액은 0 이상이어야 합니다");
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, -10000})
	void 판매자_총액이_음수_실패(Integer sellerTotal) {
		assertThatThrownBy(() -> ResaleTransactionEntity.create(
			validListing,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			VALID_TRANSACTION_PRICE,
			VALID_BUYER_FEE,
			VALID_SELLER_FEE,
			VALID_BUYER_TOTAL,
			sellerTotal
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("판매자 총액은 0 이상이어야 합니다");
	}

	@Test
	void 판매자_총액_계산_오류_실패() {
		Integer wrongSellerTotal = 99999; // 50000 - 2000 ≠ 99999

		assertThatThrownBy(() -> ResaleTransactionEntity.create(
			validListing,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			VALID_TRANSACTION_PRICE,
			VALID_BUYER_FEE,
			VALID_SELLER_FEE,
			VALID_BUYER_TOTAL,
			wrongSellerTotal
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("판매자 총액이 올바르지 않습니다");
	}
}