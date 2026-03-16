package order;

import static org.assertj.core.api.Assertions.*;

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
import com.goti.ticketing.domain.entity.seat.SeatSectionEntity;
import com.goti.exception.FieldValidationException;

@ActiveProfiles("test")
class OrderItemEntityTest {

	OrderEntity order;
	SeatEntity seat;

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
	}

	@Test
	void 주문상세_생성_성공() {
		OrderItemEntity item = OrderItemEntity.create(order, seat, TicketType.ADULT, 12000);

		assertThat(item.getOrder()).isEqualTo(order);
		assertThat(item.getSeat()).isEqualTo(seat);
		assertThat(item.getTicketType()).isEqualTo(TicketType.ADULT);
		assertThat(item.getTicketPrice()).isEqualTo(12000);
		assertThat(item.getItemStatus()).isEqualTo(OrderItemStatus.RESERVED);
	}

	@Test
	void 주문상세_결제완료_성공() {
		OrderItemEntity item = OrderItemEntity.create(order, seat, TicketType.ADULT, 12000);

		item.pay();

		assertThat(item.getItemStatus()).isEqualTo(OrderItemStatus.PAID);
	}

	@Test
	void 주문상세_생성_실패_권종_null() {
		assertThatThrownBy(
			() -> OrderItemEntity.create(order, seat, null, 12000)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("권종은 필수입니다.");
	}

	@Test
	void 주문상세_생성_실패_판매금액_음수() {
		assertThatThrownBy(
			() -> OrderItemEntity.create(order, seat, TicketType.ADULT, -1)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("티켓 가격은 0원 보다 커야합니다");
	}
}
