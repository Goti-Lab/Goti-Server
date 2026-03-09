package seat;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.goti.constants.SeatHoldStatus;
import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.seat.SeatEntity;
import com.goti.domain.entity.seat.SeatGradeEntity;
import com.goti.domain.entity.seat.SeatHoldEntity;
import com.goti.domain.entity.seat.SeatSectionEntity;
import com.goti.exception.FieldValidationException;

@ActiveProfiles("test")
public class SeatHoldEntityTest {

	SeatEntity seat;
	GameScheduleEntity game;
	UUID userId;
	String queueTokenJti;
	LocalDateTime expiredAt;

	@BeforeEach
	void setup() {
		SeatGradeEntity seatGrade = SeatGradeEntity.create(UUID.randomUUID(), "VIP", "#FFAA00");
		SeatSectionEntity seatSection = SeatSectionEntity.create(seatGrade, UUID.randomUUID(), "101", 120);
		seat = SeatEntity.create(seatSection, "A", 1);

		game = GameScheduleEntity.create(
			UUID.randomUUID(),
			UUID.randomUUID(),
			UUID.randomUUID(),
			LocalDate.of(2026, 4, 1),
			LocalTime.of(18, 30)
		);

		userId = UUID.randomUUID();
		queueTokenJti = "queue-token-jti-123";
		expiredAt = LocalDateTime.now().plusSeconds(600);
	}

	@Test
	void 좌석임시점유_생성_성공() {
		SeatHoldEntity seatHold = SeatHoldEntity.create(
			seat,
			game,
			userId,
			queueTokenJti,
			expiredAt
		);

		assertNotNull(seatHold);
		assertThat(seatHold.getSeat()).isEqualTo(seat);
		assertThat(seatHold.getGameSchedule()).isEqualTo(game);
		assertThat(seatHold.getUserId()).isEqualTo(userId);
		assertThat(seatHold.getQueueTokenJti()).isEqualTo(queueTokenJti);
		assertThat(seatHold.getStatus()).isEqualTo(SeatHoldStatus.HOLDING);
		assertThat(seatHold.getExpiredAt()).isEqualTo(expiredAt);
		assertThat(seatHold.getReleasedAt()).isNull();
	}

	@Test
	void 좌석임시점유_생성_실패_유저ID_null() {
		assertThatThrownBy(
			() -> SeatHoldEntity.create(
				seat,
				game,
				null,
				queueTokenJti,
				expiredAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("유저 ID는 필수입니다.");
	}

	@Test
	void 좌석임시점유_생성_실패_큐토큰_null() {
		assertThatThrownBy(
			() -> SeatHoldEntity.create(
				seat,
				game,
				userId,
				null,
				expiredAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("큐 토큰 식별자는 비어 있을 수 없습니다.");
	}

	@Test
	void 좌석임시점유_생성_실패_큐토큰_빈문자열() {
		assertThatThrownBy(
			() -> SeatHoldEntity.create(
				seat,
				game,
				userId,
				"",
				expiredAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("큐 토큰 식별자는 비어 있을 수 없습니다.");
	}

	@Test
	void 좌석임시점유_생성_실패_큐토큰_공백() {
		assertThatThrownBy(
			() -> SeatHoldEntity.create(
				seat,
				game,
				userId,
				"   ",
				expiredAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("큐 토큰 식별자는 비어 있을 수 없습니다.");
	}

	@Test
	void 좌석임시점유_생성_실패_만료시각_null() {
		assertThatThrownBy(
			() -> SeatHoldEntity.create(
				seat,
				game,
				userId,
				queueTokenJti,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("만료 시각은 필수입니다.");
	}
}
