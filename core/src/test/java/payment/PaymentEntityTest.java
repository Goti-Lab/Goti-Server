package payment;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.constants.LeagueType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;

import com.goti.constants.PaymentMethod;
import com.goti.constants.PaymentStatus;
import com.goti.constants.PaymentType;
import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.order.OrderEntity;
import com.goti.domain.entity.payment.PaymentEntity;
import com.goti.exception.FieldValidationException;

@ActiveProfiles("test")
class PaymentEntityTest {

	private OrderEntity order;
	private Integer paymentAmount;
	private String idempotencyKey;

	@BeforeEach
	void setup() {
		GameScheduleEntity gameSchedule = GameScheduleEntity.create(
			UUID.randomUUID(),
			UUID.randomUUID(),
			UUID.randomUUID(),
			LocalDateTime.of(2026, 4, 1, 18, 30),
			LeagueType.REGULAR
		);
		order = OrderEntity.create("ORD-20260310-0001", UUID.randomUUID(), gameSchedule, 2, 24000);
		paymentAmount = 24000;
		idempotencyKey = "payment-idempotency-key";
	}

	@Test
	void 결제_생성_성공() {
		PaymentEntity payment = PaymentEntity.create(
			order,
			null,
			PaymentType.PAYMENT,
			PaymentMethod.CARD,
			paymentAmount,
			"MOCK",
			"pg-tid-001",
			idempotencyKey
		);

		assertThat(payment.getOrder()).isEqualTo(order);
		assertThat(payment.getCancellation()).isNull();
		assertThat(payment.getPaymentType()).isEqualTo(PaymentType.PAYMENT);
		assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
		assertThat(payment.getPaymentAmount()).isEqualTo(paymentAmount);
		assertThat(payment.getPgProvider()).isEqualTo("MOCK");
		assertThat(payment.getPgTid()).isEqualTo("pg-tid-001");
		assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
		assertThat(payment.getPaidAt()).isNull();
		assertThat(payment.getIdempotencyKey()).isEqualTo(idempotencyKey);
	}

	@Test
	void 결제_생성_실패_결제처리유형_null() {
		assertThatThrownBy(
			() -> PaymentEntity.create(
				order,
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
				order,
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
				order,
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
}
