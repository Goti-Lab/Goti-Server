package payment;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;

import com.goti.constants.PaymentMethod;
import com.goti.constants.PaymentStatus;
import com.goti.constants.PaymentType;
import com.goti.domain.entity.payment.PaymentEntity;
import com.goti.exception.FieldValidationException;

@ActiveProfiles("test")
class PaymentEntityTest {

	UUID orderId;
	Integer paymentAmount;
	String idempotencyKey;

	@BeforeEach
	void setup() {
		orderId = UUID.randomUUID();
		paymentAmount = 24000;
		idempotencyKey = "payment-idempotency-key";
	}

	@Test
	void 결제_생성_성공() {
		PaymentEntity payment = PaymentEntity.create(
			orderId,
			null,
			PaymentType.PAYMENT,
			PaymentMethod.CARD,
			paymentAmount,
			"MOCK",
			"pg-tid-001",
			idempotencyKey
		);

		assertThat(payment.getOrderId()).isEqualTo(orderId);
		assertThat(payment.getCancellationId()).isNull();
		assertThat(payment.getPaymentType()).isEqualTo(PaymentType.PAYMENT);
		assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
		assertThat(payment.getPaymentAmount()).isEqualTo(paymentAmount);
		assertThat(payment.getPgProvider()).isEqualTo("MOCK");
		assertThat(payment.getPgTid()).isEqualTo("pg-tid-001");
		assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
		assertThat(payment.getPaidAt()).isNull();
		assertThat(payment.getFailedReason()).isNull();
		assertThat(payment.getIdempotencyKey()).isEqualTo(idempotencyKey);
	}

	@Test
	void 결제_성공_처리() {
		PaymentEntity payment = PaymentEntity.create(
			orderId,
			null,
			PaymentType.PAYMENT,
			PaymentMethod.CARD,
			paymentAmount,
			"MOCK",
			null,
			idempotencyKey
		);

		payment.succeed("mock-tid-001");

		assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
		assertThat(payment.getPgTid()).isEqualTo("mock-tid-001");
		assertThat(payment.getPaidAt()).isNotNull();
		assertThat(payment.getFailedReason()).isNull();
	}

	@Test
	void 결제_실패_처리() {
		PaymentEntity payment = PaymentEntity.create(
			orderId,
			null,
			PaymentType.PAYMENT,
			PaymentMethod.CARD,
			paymentAmount,
			"MOCK",
			null,
			idempotencyKey
		);

		payment.fail("mock 결제 실패");

		assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
		assertThat(payment.getPaidAt()).isNull();
		assertThat(payment.getFailedReason()).isEqualTo("mock 결제 실패");
	}

	@Test
	void 결제_생성_실패_결제처리유형_null() {
		assertThatThrownBy(
			() -> PaymentEntity.create(
				orderId,
				null,
				null,
				PaymentMethod.CARD,
				paymentAmount,
				"MOCK",
				"pg-tid-001",
				idempotencyKey
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("결제 처리 유형은 필수입니다.");
	}

	@Test
	void 결제_생성_실패_결제금액_0이하() {
		assertThatThrownBy(
			() -> PaymentEntity.create(
				orderId,
				null,
				PaymentType.PAYMENT,
				PaymentMethod.CARD,
				0,
				"MOCK",
				"pg-tid-001",
				idempotencyKey
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("결제 금액은 0원보다 커야 합니다.");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "   "})
	void 결제_생성_실패_멱등키_공백(String invalidIdempotencyKey) {
		assertThatThrownBy(
			() -> PaymentEntity.create(
				orderId,
				null,
				PaymentType.PAYMENT,
				PaymentMethod.CARD,
				paymentAmount,
				"MOCK",
				"pg-tid-001",
				invalidIdempotencyKey
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("멱등 키는 비어 있을 수 없습니다.");
	}

	@Test
	void 결제_생성_실패_주문아이디_null() {
		assertThatThrownBy(
			() -> PaymentEntity.create(
				null,
				null,
				PaymentType.PAYMENT,
				PaymentMethod.CARD,
				paymentAmount,
				"MOCK",
				"pg-tid-001",
				idempotencyKey
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("주문 ID는 필수입니다.");
	}
}
