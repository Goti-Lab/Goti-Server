package seat;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.goti.domain.entity.seat.SeatEntity;
import com.goti.domain.entity.seat.SeatGradeEntity;
import com.goti.domain.entity.seat.SeatSectionEntity;
import com.goti.exception.FieldValidationException;

@ActiveProfiles("test")
public class SeatEntityTest {

	SeatSectionEntity seatSection;
	String rowName;
	Integer seatNum;

	@BeforeEach
	void setup() {
		SeatGradeEntity seatGrade = SeatGradeEntity.create(UUID.randomUUID(), "VIP", "#FFAA00");
		seatSection = SeatSectionEntity.create(seatGrade, UUID.randomUUID(), "101", 120);
		rowName = "A";
		seatNum = 1;
	}

	@Test
	void 좌석_생성_성공() {
		SeatEntity seat = SeatEntity.create(
			seatSection,
			rowName,
			seatNum
		);

		assertNotNull(seat);
		assertThat(seat.getSeatSection()).isEqualTo(seatSection);
		assertThat(seat.getRowName()).isEqualTo(rowName);
		assertThat(seat.getSeatNum()).isEqualTo(seatNum);
		assertThat(seat.isAvailable()).isTrue();
	}

	@Test
	void 좌석_생성_실패_행번호_null() {
		assertThatThrownBy(
			() -> SeatEntity.create(
				seatSection,
				null,
				seatNum
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("행 번호는 비어 있을 수 없습니다.");
	}

	@Test
	void 좌석_생성_실패_행번호_빈문자열() {
		assertThatThrownBy(
			() -> SeatEntity.create(
				seatSection,
				"",
				seatNum
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("행 번호는 비어 있을 수 없습니다.");
	}

	@Test
	void 좌석_생성_실패_행번호_공백() {
		assertThatThrownBy(
			() -> SeatEntity.create(
				seatSection,
				"   ",
				seatNum
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("행 번호는 비어 있을 수 없습니다.");
	}

	@Test
	void 좌석_생성_실패_좌석번호_null() {
		assertThatThrownBy(
			() -> SeatEntity.create(
				seatSection,
				rowName,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("좌석 번호는 0보다 커야 합니다.");
	}

	@Test
	void 좌석_생성_실패_좌석번호_0() {
		assertThatThrownBy(
			() -> SeatEntity.create(
				seatSection,
				rowName,
				0
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("좌석 번호는 0보다 커야 합니다.");
	}

	@Test
	void 좌석_생성_실패_좌석번호_음수() {
		assertThatThrownBy(
			() -> SeatEntity.create(
				seatSection,
				rowName,
				-1
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("좌석 번호는 0보다 커야 합니다.");
	}
}
