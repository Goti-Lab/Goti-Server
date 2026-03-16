package seat;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.constants.LeagueType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.goti.constants.SeatStatus;
import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.seat.SeatEntity;
import com.goti.domain.entity.seat.SeatGradeEntity;
import com.goti.domain.entity.seat.SeatSectionEntity;
import com.goti.domain.entity.seat.SeatStatusEntity;
import com.goti.exception.FieldValidationException;

@ActiveProfiles("test")
public class SeatStatusEntityTest {

	GameScheduleEntity game;
	SeatEntity seat;

	static final LocalDateTime START_AT = LocalDateTime.now().plusDays(3);
	static final LeagueType LEAGUE_TYPE = LeagueType.REGULAR;

	@BeforeEach
	void setup() {
		game = GameScheduleEntity.create(
			UUID.randomUUID(),
			UUID.randomUUID(),
			UUID.randomUUID(),
			START_AT,
			LEAGUE_TYPE
		);

		SeatGradeEntity seatGrade = SeatGradeEntity.create(UUID.randomUUID(), "VIP", "#FFAA00");
		SeatSectionEntity seatSection = SeatSectionEntity.create(seatGrade, UUID.randomUUID(), "101", 120);
		seat = SeatEntity.create(seatSection, "A", 1);
	}

	@Test
	void 좌석상태_생성_성공() {
		SeatStatusEntity seatStatus = SeatStatusEntity.create(game, seat);

		assertNotNull(seatStatus);
		assertThat(seatStatus.getGame()).isEqualTo(game);
		assertThat(seatStatus.getSeat()).isEqualTo(seat);
		assertThat(seatStatus.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
	}

	@Test
	void 좌석상태_점유_성공_AVAILABLE_to_HELD() {
		SeatStatusEntity seatStatus = SeatStatusEntity.create(game, seat);

		seatStatus.hold();

		assertThat(seatStatus.getStatus()).isEqualTo(SeatStatus.HELD);
	}

	@Test
	void 좌석상태_해제_성공_HELD_to_AVAILABLE() {
		SeatStatusEntity seatStatus = SeatStatusEntity.create(game, seat);
		seatStatus.hold();

		seatStatus.release();

		assertThat(seatStatus.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
	}

	@Test
	void 좌석상태_판매_성공_HELD_to_SOLD() {
		SeatStatusEntity seatStatus = SeatStatusEntity.create(game, seat);
		seatStatus.hold();

		seatStatus.sell();

		assertThat(seatStatus.getStatus()).isEqualTo(SeatStatus.SOLD);
	}

	@Test
	void 좌석상태_점유_실패_HELD에서_재점유() {
		SeatStatusEntity seatStatus = SeatStatusEntity.create(game, seat);
		seatStatus.hold();

		assertThatThrownBy(seatStatus::hold)
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("좌석 점유 가능 상태에서만 점유 상태로 변경할 수 있습니다.");
	}

	@Test
	void 좌석상태_판매_실패_AVAILABLE에서_직접판매() {
		SeatStatusEntity seatStatus = SeatStatusEntity.create(game, seat);

		assertThatThrownBy(seatStatus::sell)
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("점유 상태에서만 판매 완료 상태로 변경할 수 있습니다.");
	}

	@Test
	void 좌석상태_해제_실패_SOLD에서_해제() {
		SeatStatusEntity seatStatus = SeatStatusEntity.create(game, seat);
		seatStatus.hold();
		seatStatus.sell();

		assertThatThrownBy(seatStatus::release)
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("점유 상태에서만 좌석 점유 가능 상태로 변경할 수 있습니다.");
	}
}
