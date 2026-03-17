package payment;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;

import com.goti.exception.FieldValidationException;
import com.goti.payment.constants.EscrowStatus;
import com.goti.payment.domain.entity.payment.EscrowAccountEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
class EscrowAccountEntityTest {

	private static final UUID VALID_TRANSACTION_ID = UUID.randomUUID();
	private static final UUID VALID_BUYER_ID = UUID.randomUUID();
	private static final UUID VALID_SELLER_ID = UUID.randomUUID();
	private static final Integer VALID_ESCROW_AMOUNT = 52500;

	@Test
	void 에스크로_계좌_생성_성공() {
		EscrowAccountEntity entity = EscrowAccountEntity.create(
			VALID_TRANSACTION_ID,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			VALID_ESCROW_AMOUNT
		);

		assertAll(
			() -> assertThat(entity.getTransactionId()).isEqualTo(VALID_TRANSACTION_ID),
			() -> assertThat(entity.getBuyerId()).isEqualTo(VALID_BUYER_ID),
			() -> assertThat(entity.getSellerId()).isEqualTo(VALID_SELLER_ID),
			() -> assertThat(entity.getEscrowAmount()).isEqualTo(VALID_ESCROW_AMOUNT),
			() -> assertThat(entity.getEscrowStatus()).isEqualTo(EscrowStatus.HOLDING),
			() -> assertThat(entity.getReleasedAt()).isNull()
		);
	}

	@Test
	void 에스크로_금액이_0_성공() {
		EscrowAccountEntity entity = EscrowAccountEntity.create(
			VALID_TRANSACTION_ID,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			0
		);

		assertThat(entity.getEscrowAmount()).isZero();
	}

	@Test
	void 초기_상태가_HOLDING_성공() {
		EscrowAccountEntity entity = EscrowAccountEntity.create(
			VALID_TRANSACTION_ID,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			VALID_ESCROW_AMOUNT
		);

		assertThat(entity.getEscrowStatus()).isEqualTo(EscrowStatus.HOLDING);
	}

	@Test
	void 출금_일시가_null_성공() {
		EscrowAccountEntity entity = EscrowAccountEntity.create(
			VALID_TRANSACTION_ID,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			VALID_ESCROW_AMOUNT
		);

		assertThat(entity.getReleasedAt()).isNull();
	}

	@Test
	void 서로_다른_구매자와_판매자_성공() {
		UUID buyerId = UUID.randomUUID();
		UUID sellerId = UUID.randomUUID();

		EscrowAccountEntity entity = EscrowAccountEntity.create(
			VALID_TRANSACTION_ID,
			buyerId,
			sellerId,
			VALID_ESCROW_AMOUNT
		);

		assertThat(entity.getBuyerId()).isNotEqualTo(entity.getSellerId());
	}

	@Test
	void 거래_ID가_null_실패() {
		assertThatThrownBy(() -> EscrowAccountEntity.create(
			null,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			VALID_ESCROW_AMOUNT
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("거래 ID는 비어 있을 수 없습니다");
	}

	@Test
	void 구매자_ID가_null_실패() {
		assertThatThrownBy(() -> EscrowAccountEntity.create(
			VALID_TRANSACTION_ID,
			null,
			VALID_SELLER_ID,
			VALID_ESCROW_AMOUNT
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("구매자 ID는 비어 있을 수 없습니다");
	}

	@Test
	void 판매자_ID가_null_실패() {
		assertThatThrownBy(() -> EscrowAccountEntity.create(
			VALID_TRANSACTION_ID,
			VALID_BUYER_ID,
			null,
			VALID_ESCROW_AMOUNT
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("판매자 ID는 비어 있을 수 없습니다");
	}

	@ParameterizedTest
	@NullSource
	void 에스크로_금액이_null_실패(Integer escrowAmount) {
		assertThatThrownBy(() -> EscrowAccountEntity.create(
			VALID_TRANSACTION_ID,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			escrowAmount
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("에스크로 금액은 0 이상이어야 합니다");
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, -1000, -52500})
	void 에스크로_금액이_음수_실패(Integer escrowAmount) {
		assertThatThrownBy(() -> EscrowAccountEntity.create(
			VALID_TRANSACTION_ID,
			VALID_BUYER_ID,
			VALID_SELLER_ID,
			escrowAmount
		))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("에스크로 금액은 0 이상이어야 합니다");
	}
}