package seat;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.goti.domain.entity.seat.SeatGradeEntity;
import com.goti.domain.entity.seat.SeatSectionEntity;
import com.goti.exception.FieldValidationException;

@ActiveProfiles("test")
public class SeatSectionEntityTest {

	SeatGradeEntity seatGrade;
	UUID stadiumId;
	String sectionCode;
	Integer capacity;

	@BeforeEach
	void setup() {
		seatGrade = SeatGradeEntity.create(UUID.randomUUID(), "VIP", "#FFAA00");
		stadiumId = UUID.randomUUID();
		sectionCode = "101";
		capacity = 120;
	}

	@Test
	void 좌석구역_생성_성공() {
		SeatSectionEntity seatSection = SeatSectionEntity.create(
			seatGrade,
			stadiumId,
			sectionCode,
			capacity
		);

		assertNotNull(seatSection);
		assertThat(seatSection.getSeatGrade()).isEqualTo(seatGrade);
		assertThat(seatSection.getStadiumId()).isEqualTo(stadiumId);
		assertThat(seatSection.getSectionCode()).isEqualTo(sectionCode);
		assertThat(seatSection.getCapacity()).isEqualTo(capacity);
	}

	@Test
	void 좌석구역_생성_실패_구장_ID_null() {
		assertThatThrownBy(
			() -> SeatSectionEntity.create(
				seatGrade,
				null,
				sectionCode,
				capacity
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("구장 ID는 필수입니다.");
	}

	@Test
	void 좌석구역_생성_실패_구역코드_null() {
		assertThatThrownBy(
			() -> SeatSectionEntity.create(
				seatGrade,
				stadiumId,
				null,
				capacity
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("구역 코드는 비어 있을 수 없습니다.");
	}

	@Test
	void 좌석구역_생성_실패_구역코드_빈문자열() {
		assertThatThrownBy(
			() -> SeatSectionEntity.create(
				seatGrade,
				stadiumId,
				"",
				capacity
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("구역 코드는 비어 있을 수 없습니다.");
	}

	@Test
	void 좌석구역_생성_실패_구역코드_공백() {
		assertThatThrownBy(
			() -> SeatSectionEntity.create(
				seatGrade,
				stadiumId,
				"   ",
				capacity
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("구역 코드는 비어 있을 수 없습니다.");
	}

	@Test
	void 좌석구역_생성_실패_수용인원_null() {
		assertThatThrownBy(
			() -> SeatSectionEntity.create(
				seatGrade,
				stadiumId,
				sectionCode,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("수용 인원은 0보다 커야 합니다.");
	}

	@Test
	void 좌석구역_생성_실패_수용인원_0() {
		assertThatThrownBy(
			() -> SeatSectionEntity.create(
				seatGrade,
				stadiumId,
				sectionCode,
				0
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("수용 인원은 0보다 커야 합니다.");
	}

	@Test
	void 좌석구역_생성_실패_수용인원_음수() {
		assertThatThrownBy(
			() -> SeatSectionEntity.create(
				seatGrade,
				stadiumId,
				sectionCode,
				-1
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("수용 인원은 0보다 커야 합니다.");
	}
}
