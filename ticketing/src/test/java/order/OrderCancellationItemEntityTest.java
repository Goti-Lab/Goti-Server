package order;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.domain.base.BaseUuidEntity;
import com.goti.ticketing.constants.LeagueType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.goti.ticketing.constants.OrderCancellationRequestType;
import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.order.OrderCancellationEntity;
import com.goti.ticketing.domain.entity.order.OrderCancellationItemEntity;
import com.goti.ticketing.domain.entity.order.OrderEntity;
import com.goti.ticketing.domain.entity.order.OrderItemEntity;
import com.goti.ticketing.domain.entity.seat.SeatEntity;
import com.goti.ticketing.domain.entity.seat.SeatGradeEntity;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;
import com.goti.ticketing.domain.entity.seat.SeatSectionEntity;
import com.goti.exception.FieldValidationException;

@ActiveProfiles("test")
class OrderCancellationItemEntityTest {

	private OrderCancellationEntity cancellation;
	private OrderItemEntity item;

	static final LocalDateTime START_AT = LocalDateTime.now().plusDays(3);
	static final LeagueType LEAGUE_TYPE = LeagueType.REGULAR;

	@BeforeEach
	void setup() {
		GameScheduleEntity gameSchedule = GameScheduleEntity.create(
			UUID.randomUUID(),
			UUID.randomUUID(),
			UUID.randomUUID(),
			START_AT,
			LEAGUE_TYPE
		);
		OrderEntity order = OrderEntity.create("ORD-20260309-0001", UUID.randomUUID(), gameSchedule, 2, 24000);
		cancellation = OrderCancellationEntity.create(
			order,
			OrderCancellationRequestType.USER_PARTIAL,
			UUID.randomUUID(),
			12000,
			1000,
			"cancel-idempotency-key"
		);

		SeatGradeEntity seatGrade = SeatGradeEntity.create(UUID.randomUUID(), "VIP", "#FFAA00");
		SeatSectionEntity seatSection = SeatSectionEntity.create(seatGrade, UUID.randomUUID(), "101", 120);
		SeatEntity seat = SeatEntity.create(seatSection, "A", 1);
		SeatHoldEntity seatHold = SeatHoldEntity.create(
			seat,
			gameSchedule,
			order.getMemberId(),
			"queue-token-jti",
			LocalDateTime.now().plusMinutes(10)
		);
		assignId(seatHold, UUID.randomUUID());
		item = OrderItemEntity.create(
			order,
			seat,
			seatHold.getId(),
			com.goti.ticketing.constants.TicketType.ADULT,
			12000
		);
	}

	@Test
	void 주문취소상세_생성_성공() {
		OrderCancellationItemEntity cancellationItem = OrderCancellationItemEntity.create(
			cancellation,
			item,
			11000,
			1000
		);

		assertThat(cancellationItem.getCancellation()).isEqualTo(cancellation);
		assertThat(cancellationItem.getItem()).isEqualTo(item);
		assertThat(cancellationItem.getRefundAmount()).isEqualTo(11000);
		assertThat(cancellationItem.getFeeAmount()).isEqualTo(1000);
		assertThat(cancellationItem.getCancelledAt()).isNull();
	}

	@Test
	void 주문취소상세_생성_실패_환불액_음수() {
		assertThatThrownBy(
			() -> OrderCancellationItemEntity.create(cancellation, item, -1, 1000)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("환불액은 0 이상이어야 합니다.");
	}

	@Test
	void 주문취소상세_생성_실패_수수료_음수() {
		assertThatThrownBy(
			() -> OrderCancellationItemEntity.create(cancellation, item, 11000, -1)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("수수료는 0 이상이어야 합니다.");
	}

	private void assignId(SeatHoldEntity seatHold, UUID holdId) {
		try {
			Field idField = BaseUuidEntity.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(seatHold, holdId);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("테스트용 SeatHold ID 설정에 실패했습니다.", e);
		}
	}
}
