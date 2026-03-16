package com.goti.resale.domain.entity.resale;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;

import com.goti.exception.FieldValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
class SellerAccountEntityTest {

	private static final UUID VALID_SELLER_ID = UUID.randomUUID();
	private static final String VALID_BANK_NAME = "국민은행";
	private static final String VALID_ACCOUNT_NUMBER = "123456789012";
	private static final String VALID_ACCOUNT_HOLDER = "홍길동";

	@Test
	void 판매자_계좌_생성_성공() {
		SellerAccountEntity entity = SellerAccountEntity.create(
			VALID_SELLER_ID,
			VALID_BANK_NAME,
			VALID_ACCOUNT_NUMBER,
			VALID_ACCOUNT_HOLDER
		);

		assertAll(
			() -> assertThat(entity.getSellerId()).isEqualTo(VALID_SELLER_ID),
			() -> assertThat(entity.getBankName()).isEqualTo(VALID_BANK_NAME),
			() -> assertThat(entity.getAccountNumber()).isEqualTo(VALID_ACCOUNT_NUMBER),
			() -> assertThat(entity.getAccountHolder()).isEqualTo(VALID_ACCOUNT_HOLDER)
		);
	}

	@Test
	void 다양한_은행_이름_성공() {
		String[] bankNames = {"신한은행", "우리은행", "하나은행", "KB국민은행", "NH농협은행"};

		for (String bankName : bankNames) {
			SellerAccountEntity entity = SellerAccountEntity.create(
				VALID_SELLER_ID,
				bankName,
				VALID_ACCOUNT_NUMBER,
				VALID_ACCOUNT_HOLDER
			);

			assertThat(entity.getBankName()).isEqualTo(bankName);
		}
	}

	@Test
	void 다양한_계좌_번호_형식_성공() {
		String[] accountNumbers = {
			"123456789012",
			"1234-5678-9012",
			"123-456-789012",
			"12345678901234"
		};

		for (String accountNumber : accountNumbers) {
			SellerAccountEntity entity = SellerAccountEntity.create(
				VALID_SELLER_ID,
				VALID_BANK_NAME,
				accountNumber,
				VALID_ACCOUNT_HOLDER
			);

			assertThat(entity.getAccountNumber()).isEqualTo(accountNumber);
		}
	}

	@Test
	void 다양한_예금주_이름_성공() {
		String[] accountHolders = {"홍길동", "김철수", "이영희", "박민수"};

		for (String accountHolder : accountHolders) {
			SellerAccountEntity entity = SellerAccountEntity.create(
				VALID_SELLER_ID,
				VALID_BANK_NAME,
				VALID_ACCOUNT_NUMBER,
				accountHolder
			);

			assertThat(entity.getAccountHolder()).isEqualTo(accountHolder);
		}
	}

	@Test
	void 판매자_ID가_null_실패() {
		assertThatThrownBy(() -> SellerAccountEntity.create(
			null,
			VALID_BANK_NAME,
			VALID_ACCOUNT_NUMBER,
			VALID_ACCOUNT_HOLDER
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("유저 ID는 비어 있을 수 없습니다");
	}

	@ParameterizedTest
	@NullAndEmptySource
	void 은행_이름이_null_또는_빈값_실패(String bankName) {
		assertThatThrownBy(() -> SellerAccountEntity.create(
			VALID_SELLER_ID,
			bankName,
			VALID_ACCOUNT_NUMBER,
			VALID_ACCOUNT_HOLDER
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("은행 이름은 비어 있을 수 없습니다");
	}

	@ParameterizedTest
	@ValueSource(strings = {" ", "  ", "\t", "\n", "   \t   "})
	void 은행_이름이_공백_실패(String bankName) {
		assertThatThrownBy(() -> SellerAccountEntity.create(
			VALID_SELLER_ID,
			bankName,
			VALID_ACCOUNT_NUMBER,
			VALID_ACCOUNT_HOLDER
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("은행 이름은 비어 있을 수 없습니다");
	}

	@ParameterizedTest
	@NullAndEmptySource
	void 계좌_번호가_null_또는_빈값_실패(String accountNumber) {
		assertThatThrownBy(() -> SellerAccountEntity.create(
			VALID_SELLER_ID,
			VALID_BANK_NAME,
			accountNumber,
			VALID_ACCOUNT_HOLDER
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("계좌 번호는 비어 있을 수 없습니다");
	}

	@ParameterizedTest
	@ValueSource(strings = {" ", "  ", "\t", "\n", "   \t   "})
	void 계좌_번호가_공백_실패(String accountNumber) {
		assertThatThrownBy(() -> SellerAccountEntity.create(
			VALID_SELLER_ID,
			VALID_BANK_NAME,
			accountNumber,
			VALID_ACCOUNT_HOLDER
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("계좌 번호는 비어 있을 수 없습니다");
	}

	@ParameterizedTest
	@NullAndEmptySource
	void 예금주가_null_또는_빈값_실패(String accountHolder) {
		assertThatThrownBy(() -> SellerAccountEntity.create(
			VALID_SELLER_ID,
			VALID_BANK_NAME,
			VALID_ACCOUNT_NUMBER,
			accountHolder
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("예금주 성함은 비어 있을 수 없습니다");
	}

	@ParameterizedTest
	@ValueSource(strings = {" ", "  ", "\t", "\n", "   \t   "})
	void 예금주가_공백_실패(String accountHolder) {
		assertThatThrownBy(() -> SellerAccountEntity.create(
			VALID_SELLER_ID,
			VALID_BANK_NAME,
			VALID_ACCOUNT_NUMBER,
			accountHolder
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("예금주 성함은 비어 있을 수 없습니다");
	}
}