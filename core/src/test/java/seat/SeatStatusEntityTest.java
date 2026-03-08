package seat;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.goti.constants.SeatStatus;
import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.seat.SeatEntity;
import com.goti.domain.entity.seat.SeatGradeEntity;
import com.goti.domain.entity.seat.SeatSectionEntity;
import com.goti.domain.entity.seat.SeatStatusEntity;

@ActiveProfiles("test")
public class SeatStatusEntityTest {

	GameScheduleEntity game;
	SeatEntity seat;

	@BeforeEach
	void setup() {
		game = GameScheduleEntity.create(
			UUID.randomUUID(),
			UUID.randomUUID(),
			UUID.randomUUID(),
			LocalDate.of(2026, 4, 1),
			LocalTime.of(18, 30)
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
}
