package order;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.goti.exception.FieldValidationException;
import com.goti.ticketing.constants.OrderCancellationRequestType;
import com.goti.ticketing.order.service.domain.OrderCancellationRefundPolicy;

@SpringBootTest(classes = OrderCancellationRefundPolicy.class)
@ActiveProfiles("test")
class OrderCancellationRefundPolicyTest {

	@Autowired
	private OrderCancellationRefundPolicy refundPolicy;

	@Test
	void 예매당일_취소면_수수료포함_전액환불() {
		LocalDateTime paidAt = LocalDateTime.of(2026, 3, 22, 10, 0);
		LocalDateTime canceledAt = LocalDateTime.of(2026, 3, 22, 18, 0);
		LocalDateTime gameStartAt = LocalDateTime.of(2026, 3, 25, 18, 30);

		OrderCancellationRefundPolicy.RefundAmount refundAmount = refundPolicy.calculate(
			paidAt,
			gameStartAt,
			canceledAt,
			20000,
			1000,
			OrderCancellationRequestType.ORDER_FULL,
			false
		);

		assertThat(refundAmount.refundAmount()).isEqualTo(21000);
		assertThat(refundAmount.cancellationFeeAmount()).isZero();
		assertThat(refundAmount.refundedBookingFeeAmount()).isEqualTo(1000);
	}

	@Test
	void 예매익일이후_취소면_티켓금액의_10퍼센트_수수료부과() {
		LocalDateTime paidAt = LocalDateTime.of(2026, 3, 22, 10, 0);
		LocalDateTime canceledAt = LocalDateTime.of(2026, 3, 23, 10, 0);
		LocalDateTime gameStartAt = LocalDateTime.of(2026, 3, 25, 18, 30);

		OrderCancellationRefundPolicy.RefundAmount refundAmount = refundPolicy.calculate(
			paidAt,
			gameStartAt,
			canceledAt,
			20000,
			1000,
			OrderCancellationRequestType.ORDER_FULL,
			false
		);

		assertThat(refundAmount.refundAmount()).isEqualTo(18000);
		assertThat(refundAmount.cancellationFeeAmount()).isEqualTo(2000);
		assertThat(refundAmount.refundedBookingFeeAmount()).isZero();
	}

	@Test
	void 경기취소면_전액환불() {
		LocalDateTime paidAt = LocalDateTime.of(2026, 3, 22, 10, 0);
		LocalDateTime canceledAt = LocalDateTime.of(2026, 3, 25, 17, 0);
		LocalDateTime gameStartAt = LocalDateTime.of(2026, 3, 25, 18, 30);

		OrderCancellationRefundPolicy.RefundAmount refundAmount = refundPolicy.calculate(
			paidAt,
			gameStartAt,
			canceledAt,
			20000,
			1000,
			OrderCancellationRequestType.ORDER_FULL,
			true
		);

		assertThat(refundAmount.refundAmount()).isEqualTo(21000);
		assertThat(refundAmount.cancellationFeeAmount()).isZero();
		assertThat(refundAmount.refundedBookingFeeAmount()).isEqualTo(1000);
	}

	@Test
	void 경기시작_4시간_이내_취소_불가() {
		LocalDateTime paidAt = LocalDateTime.of(2026, 3, 22, 10, 0);
		LocalDateTime canceledAt = LocalDateTime.of(2026, 3, 25, 14, 31);
		LocalDateTime gameStartAt = LocalDateTime.of(2026, 3, 25, 18, 30);

		assertThatThrownBy(
			() -> refundPolicy.calculate(
				paidAt,
				gameStartAt,
				canceledAt,
				20000,
				1000,
				OrderCancellationRequestType.ORDER_FULL,
				false
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("경기 시작 4시간 전까지만 취소할 수 있습니다.");
	}

	@Test
	void 예매당일_부분취소면_예매수수료는_환불되지않는다() {
		LocalDateTime paidAt = LocalDateTime.of(2026, 3, 22, 10, 0);
		LocalDateTime canceledAt = LocalDateTime.of(2026, 3, 22, 18, 0);
		LocalDateTime gameStartAt = LocalDateTime.of(2026, 3, 25, 18, 30);

		OrderCancellationRefundPolicy.RefundAmount refundAmount = refundPolicy.calculate(
			paidAt,
			gameStartAt,
			canceledAt,
			20000,
			1000,
			OrderCancellationRequestType.ORDER_PARTIAL,
			false
		);

		assertThat(refundAmount.refundAmount()).isEqualTo(20000);
		assertThat(refundAmount.cancellationFeeAmount()).isZero();
		assertThat(refundAmount.refundedBookingFeeAmount()).isZero();
	}
}
