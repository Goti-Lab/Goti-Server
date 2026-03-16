package seat;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.goti.ticketing.domain.entity.seat.SeatGradeEntity;
import com.goti.exception.FieldValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
public class SeatGradeEntityTest {

	UUID stadiumId;
	String name;
	String displayColorHex;

	@BeforeEach
	void setup() {
		stadiumId = UUID.randomUUID();
		name = "VIP";
		displayColorHex = "#FFAA00";
	}

	@Test
	void 좌석등급_생성_성공() {
		SeatGradeEntity seatGrade = SeatGradeEntity.create(
			stadiumId,
			name,
			displayColorHex
		);

		assertNotNull(seatGrade);
		assertThat(seatGrade.getStadiumId()).isEqualTo(stadiumId);
		assertThat(seatGrade.getName()).isEqualTo(name);
		assertThat(seatGrade.getDisplayColorHex()).isEqualTo(displayColorHex);

		log.info("seat grade name: {}", seatGrade.getName());
		log.info("seat grade color: {}", seatGrade.getDisplayColorHex());
	}

	@Test
	void 좌석등급_생성_성공_표시색상_null() {
		SeatGradeEntity seatGrade = SeatGradeEntity.create(
			stadiumId,
			name,
			null
		);

		assertThat(seatGrade.getStadiumId()).isEqualTo(stadiumId);
		assertThat(seatGrade.getName()).isEqualTo(name);
		assertThat(seatGrade.getDisplayColorHex()).isNull();
	}

	@Test
	void 좌석등급_생성_실패_구장_ID_null() {
		assertThatThrownBy(
			() -> SeatGradeEntity.create(
				null,
				name,
				displayColorHex
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("구장 ID는 필수입니다.");
	}

	@Test
	void 좌석등급_생성_실패_등급명_null() {
		assertThatThrownBy(
			() -> SeatGradeEntity.create(
				stadiumId,
				null,
				displayColorHex
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("좌석 등급명은 비어 있을 수 없습니다.");
	}

	@Test
	void 좌석등급_생성_실패_등급명_빈문자열() {
		assertThatThrownBy(
			() -> SeatGradeEntity.create(
				stadiumId,
				"",
				displayColorHex
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("좌석 등급명은 비어 있을 수 없습니다.");
	}

	@Test
	void 좌석등급_생성_실패_등급명_공백() {
		assertThatThrownBy(
			() -> SeatGradeEntity.create(
				stadiumId,
				"   ",
				displayColorHex
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("좌석 등급명은 비어 있을 수 없습니다.");
	}
}
