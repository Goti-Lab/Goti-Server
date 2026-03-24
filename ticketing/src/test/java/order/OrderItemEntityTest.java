package order;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.ticketing.constants.LeagueType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.goti.ticketing.constants.OrderItemStatus;
import com.goti.ticketing.constants.TicketType;
import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.order.OrderEntity;
import com.goti.ticketing.domain.entity.order.OrderItemEntity;
import com.goti.ticketing.domain.entity.seat.SeatEntity;
import com.goti.ticketing.domain.entity.seat.SeatGradeEntity;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;
import com.goti.ticketing.domain.entity.seat.SeatSectionEntity;
import com.goti.domain.base.BaseUuidEntity;
import com.goti.exception.FieldValidationException;

@ActiveProfiles("test")
class OrderItemEntityTest {

	private OrderEntity order;
	private SeatEntity seat;
	private UUID holdId;

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

		SeatGradeEntity seatGrade = SeatGradeEntity.create(UUID.randomUUID(), "VIP", "#FFAA00");
		SeatSectionEntity seatSection = SeatSectionEntity.create(seatGrade, UUID.randomUUID(), "101", 120);
		seat = SeatEntity.create(seatSection, "A", 1);
		SeatHoldEntity seatHold = SeatHoldEntity.create(
			seat,
			gameSchedule,
			order.getMemberId(),
			"queue-token-jti",
			LocalDateTime.now().plusMinutes(10)
		);
		assignId(seatHold, UUID.randomUUID());
		holdId = seatHold.getId();
	}

	@Test
	void 주문상세_생성_성공() {
		OrderItemEntity item = OrderItemEntity.create(order, seat, holdId, TicketType.ADULT, 12000);

		assertThat(item.getOrder()).isEqualTo(order);
		assertThat(item.getSeat()).isEqualTo(seat);
		assertThat(item.getHoldId()).isEqualTo(holdId);
		assertThat(item.getTicketType()).isEqualTo(TicketType.ADULT);
		assertThat(item.getTicketPrice()).isEqualTo(12000);
		assertThat(item.getItemStatus()).isEqualTo(OrderItemStatus.RESERVED);
	}

	@Test
	void 주문상세_결제완료_성공() {
		OrderItemEntity item = OrderItemEntity.create(order, seat, holdId, TicketType.ADULT, 12000);

		item.pay();

		assertThat(item.getItemStatus()).isEqualTo(OrderItemStatus.PAID);
	}

	@Test
	void 주문상세_만료처리_성공() {
		OrderItemEntity item = OrderItemEntity.create(order, seat, holdId, TicketType.ADULT, 12000);

		item.expire();

		assertThat(item.getItemStatus()).isEqualTo(OrderItemStatus.CANCELED);
	}

	@Test
	void 주문상세_만료처리_실패_결제완료후만료처리() {
		OrderItemEntity item = OrderItemEntity.create(order, seat, holdId, TicketType.ADULT, 12000);
		item.pay();

		assertThatThrownBy(item::expire)
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("예약 상태에서만 주문 상세 만료 처리가 가능합니다.");
	}

	@Test
	void 주문상세_취소_성공() {
		OrderItemEntity item = OrderItemEntity.create(order, seat, holdId, TicketType.ADULT, 12000);
		item.pay();

		item.cancel();

		assertThat(item.getItemStatus()).isEqualTo(OrderItemStatus.CANCELED);
	}

	@Test
	void 주문상세_생성_실패_권종_null() {
		assertThatThrownBy(
			() -> OrderItemEntity.create(order, seat, holdId, null, 12000)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("권종은 필수입니다.");
	}

	@Test
	void 주문상세_생성_실패_판매금액_음수() {
		assertThatThrownBy(
			() -> OrderItemEntity.create(order, seat, holdId, TicketType.ADULT, -1)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("티켓 가격은 0원 보다 커야합니다");
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
