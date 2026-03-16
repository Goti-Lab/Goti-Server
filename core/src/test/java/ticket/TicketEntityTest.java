package ticket;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.constants.LeagueType;

import com.goti.constants.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;

import com.goti.constants.ResaleEnabledStatus;
import com.goti.constants.TicketType;
import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.order.OrderEntity;
import com.goti.domain.entity.order.OrderItemEntity;
import com.goti.domain.entity.seat.SeatEntity;
import com.goti.domain.entity.seat.SeatGradeEntity;
import com.goti.domain.entity.seat.SeatSectionEntity;
import com.goti.domain.entity.ticket.TicketEntity;
import com.goti.exception.FieldValidationException;

@ActiveProfiles("test")
class TicketEntityTest {

	OrderItemEntity orderItem;
	UUID orderItemId;
	UUID gameId;
	UUID userId;

	@BeforeEach
	void setup() {
		GameScheduleEntity gameSchedule = GameScheduleEntity.create(
			UUID.randomUUID(),
			UUID.randomUUID(),
			UUID.randomUUID(),
			LocalDateTime.now().plusDays(3),
			LeagueType.REGULAR
		);
		OrderEntity order = OrderEntity.create("ORD-20260310-0001", UUID.randomUUID(), gameSchedule, 1, 12000);
		SeatGradeEntity seatGrade = SeatGradeEntity.create(UUID.randomUUID(), "VIP", "#FFAA00");
		SeatSectionEntity seatSection = SeatSectionEntity.create(seatGrade, UUID.randomUUID(), "101", 120);
		SeatEntity seat = SeatEntity.create(seatSection, "A", 1);
		orderItem = OrderItemEntity.create(order, seat, TicketType.ADULT, 12000);
		orderItemId = UUID.randomUUID();
		gameId = UUID.randomUUID();
		userId = UUID.randomUUID();
	}

	@Test
	void 티켓_생성_성공() {
		TicketEntity ticket = TicketEntity.create(
			"TKT-20260215-XXXXX",
			orderItemId,
			null,
			gameId,
			userId,
			"goti-user",
			"user@goti.com",
			"01012345678",
			"삼성 vs 두산",
			LocalDateTime.of(2026, 4, 1, 18, 30),
			"VIP A구역 3열 15번",
			12000,
			null
		);

		assertThat(ticket.getOrderItemId()).isEqualTo(orderItemId);
		assertThat(ticket.getTicketNumber()).isEqualTo("TKT-20260215-XXXXX");
		assertThat(ticket.getGameId()).isEqualTo(gameId);
		assertThat(ticket.getUserId()).isEqualTo(userId);
		assertThat(ticket.getSeatInfo()).isEqualTo("VIP A구역 3열 15번");
		assertThat(ticket.getTicketPrice()).isEqualTo(12000);
		assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.ISSUED);
		assertThat(ticket.getResaleEnabledStatus()).isEqualTo(ResaleEnabledStatus.DISABLED);
		assertThat(ticket.getUsedAt()).isNull();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "   "})
	void 티켓_생성_실패_티켓번호_빈값(String invalidTicketNumber) {
		assertThatThrownBy(
			() -> TicketEntity.create(
				invalidTicketNumber,
				orderItemId,
				null,
				gameId,
				userId,
				null,
				null,
				null,
				null,
				null,
				"VIP A구역 3열 15번",
				12000,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("티켓 번호는 비어 있을 수 없습니다.");
	}

	@Test
	void 티켓_생성_실패_경기ID_null() {
		assertThatThrownBy(
			() -> TicketEntity.create(
				"TKT-20260215-XXXXX",
				orderItemId,
				null,
				null,
				userId,
				null,
				null,
				null,
				null,
				null,
				"VIP A구역 3열 15번",
				12000,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("경기 ID는 필수입니다.");
	}

	@Test
	void 티켓_생성_실패_좌석정보_빈값() {
		assertThatThrownBy(
			() -> TicketEntity.create(
				"TKT-20260215-XXXXX",
				orderItemId,
				null,
				gameId,
				userId,
				null,
				null,
				null,
				null,
				null,
				" ",
				12000,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("좌석 정보는 비어 있을 수 없습니다.");
	}

	@Test
	void 티켓_생성_실패_티켓가격_음수() {
		assertThatThrownBy(
			() -> TicketEntity.create(
				"TKT-20260215-XXXXX",
				orderItemId,
				null,
				gameId,
				userId,
				null,
				null,
				null,
				null,
				null,
				"VIP A구역 3열 15번",
				-1,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("티켓 가격은 0원보다 커야합니다.");
	}
}
