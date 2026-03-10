package order;

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

import com.goti.constants.OrderStatus;
import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.order.OrderEntity;
import com.goti.exception.FieldValidationException;

@ActiveProfiles("test")
class OrderEntityTest {

	private String orderNumber;
	private UUID userId;
	private GameScheduleEntity gameSchedule;
	private Integer totalQuantity;
	private Integer totalAmount;

	@BeforeEach
	void setup() {
		orderNumber = "ORD-20260309-0001";
		userId = UUID.randomUUID();
		gameSchedule = GameScheduleEntity.create(
			UUID.randomUUID(),
			UUID.randomUUID(),
			UUID.randomUUID(),
			LocalDateTime.now().plusDays(3),
			LeagueType.REGULAR
		);
		totalQuantity = 2;
		totalAmount = 24000;
	}

	@Test
	void 주문_생성_성공() {
		OrderEntity order = OrderEntity.create(
			orderNumber,
			userId,
			gameSchedule,
			totalQuantity,
			totalAmount
		);

		assertThat(order.getOrderNumber()).isEqualTo(orderNumber);
		assertThat(order.getUserId()).isEqualTo(userId);
		assertThat(order.getGameSchedule()).isEqualTo(gameSchedule);
		assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
		assertThat(order.getTotalQuantity()).isEqualTo(totalQuantity);
		assertThat(order.getTotalAmount()).isEqualTo(totalAmount);
		assertThat(order.getConfirmedAt()).isNull();
		assertThat(order.getCanceledAt()).isNull();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "   "})
	void 주문_생성_실패_주문번호_빈값(String invalidOrderNumber) {
		assertThatThrownBy(
			() -> OrderEntity.create(
				invalidOrderNumber,
				userId,
				gameSchedule,
				totalQuantity,
				totalAmount
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("주문 번호는 비어 있을 수 없습니다.");
	}

	@Test
	void 주문_생성_실패_유저ID_null() {
		assertThatThrownBy(
			() -> OrderEntity.create(
				orderNumber,
				null,
				gameSchedule,
				totalQuantity,
				totalAmount
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("유저 ID는 필수입니다.");
	}

	@Test
	void 주문_생성_실패_총수량_0() {
		assertThatThrownBy(
			() -> OrderEntity.create(
				orderNumber,
				userId,
				gameSchedule,
				0,
				totalAmount
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("총 수량은 0보다 커야 합니다.");
	}

	@Test
	void 주문_생성_실패_총금액_음수() {
		assertThatThrownBy(
			() -> OrderEntity.create(
				orderNumber,
				userId,
				gameSchedule,
				totalQuantity,
				-1
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("총 금액은 0보다 커야 합니다.");
	}
}
