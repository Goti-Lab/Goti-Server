package order;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import com.goti.ticketing.constants.LeagueType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;

import com.goti.ticketing.constants.OrderCancellationRequestType;
import com.goti.ticketing.constants.OrderCancellationStatus;
import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.order.OrderCancellationEntity;
import com.goti.ticketing.domain.entity.order.OrderEntity;
import com.goti.exception.FieldValidationException;

@ActiveProfiles("test")
class OrderCancellationEntityTest {

	private OrderEntity order;
	private UUID requestedBy;
	private Integer refundAmountTotal;
	private Integer feeAmountTotal;
	private String idempotencyKey;

	@BeforeEach
	void setup() {
		GameScheduleEntity gameSchedule = GameScheduleEntity.create(
			UUID.randomUUID(),
			UUID.randomUUID(),
			UUID.randomUUID(),
			LocalDateTime.now().plusDays(3),
			LeagueType.REGULAR
		);
		order = OrderEntity.create("ORD-20260309-0001", UUID.randomUUID(), gameSchedule, 2, 24000);
		requestedBy = UUID.randomUUID();
		refundAmountTotal = 12000;
		feeAmountTotal = 1000;
		idempotencyKey = "cancel-idempotency-key";
	}

	@Test
	void 주문취소_생성_성공() {
		OrderCancellationEntity cancellation = OrderCancellationEntity.create(
			order,
			OrderCancellationRequestType.USER_PARTIAL,
			requestedBy,
			refundAmountTotal,
			feeAmountTotal,
			idempotencyKey
		);

		assertThat(cancellation.getOrder()).isEqualTo(order);
		assertThat(cancellation.getStatus()).isEqualTo(OrderCancellationStatus.REQUESTED);
		assertThat(cancellation.getRequestType()).isEqualTo(OrderCancellationRequestType.USER_PARTIAL);
		assertThat(cancellation.getDenyReasonCode()).isNull();
		assertThat(cancellation.getRequestedBy()).isEqualTo(requestedBy);
		assertThat(cancellation.getRefundAmountTotal()).isEqualTo(refundAmountTotal);
		assertThat(cancellation.getFeeAmountTotal()).isEqualTo(feeAmountTotal);
		assertThat(cancellation.getIdempotencyKey()).isEqualTo(idempotencyKey);
		assertThat(cancellation.getApprovedBy()).isNull();
		assertThat(cancellation.getApprovedAt()).isNull();
		assertThat(cancellation.getCompletedAt()).isNull();
	}

	@Test
	void 주문취소_생성_실패_요청타입_null() {
		assertThatThrownBy(
			() -> OrderCancellationEntity.create(
				order,
				null,
				requestedBy,
				refundAmountTotal,
				feeAmountTotal,
				idempotencyKey
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("취소 요청 타입은 필수입니다.");
	}

	@Test
	void 주문취소_생성_실패_요청자_null() {
		assertThatThrownBy(
			() -> OrderCancellationEntity.create(
				order,
				OrderCancellationRequestType.USER_PARTIAL,
				null,
				refundAmountTotal,
				feeAmountTotal,
				idempotencyKey
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("요청자는 필수입니다.");
	}

	@Test
	void 주문취소_생성_실패_총환불액_음수() {
		assertThatThrownBy(
			() -> OrderCancellationEntity.create(
				order,
				OrderCancellationRequestType.USER_PARTIAL,
				requestedBy,
				-1,
				feeAmountTotal,
				idempotencyKey
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("총 환불액은 0 이상이어야 합니다.");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "   "})
	void 주문취소_생성_실패_멱등키_공백(String invalidIdempotencyKey) {
		assertThatThrownBy(
			() -> OrderCancellationEntity.create(
				order,
				OrderCancellationRequestType.USER_PARTIAL,
				requestedBy,
				refundAmountTotal,
				feeAmountTotal,
				invalidIdempotencyKey
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("멱등 키는 비어 있을 수 없습니다.");
	}
}
