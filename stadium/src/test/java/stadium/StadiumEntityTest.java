package stadium;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;

import com.goti.stadium.domain.entity.stadium.StadiumEntity;
import com.goti.exception.FieldValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
public class StadiumEntityTest {

	@Test
	void 구장_생성_성공() {
		StadiumEntity stadium = StadiumEntity.create(
			"광주기아챔피언스필드",
			"광주",
			"광주광역시",
			"북구",
			"광주 북구 서림로 10",
			BigDecimal.valueOf(35.17),
			BigDecimal.valueOf(126.89),
			20500,
			null
		);
		assertThat(stadium).isNotNull();
		assertThat(stadium.getStadiumName()).isEqualTo("광주기아챔피언스필드");
		assertThat(stadium.getLocation()).isEqualTo("광주");
		assertThat(stadium.getTotalSeats()).isEqualTo(20500);
	}

	@NullAndEmptySource
	@ParameterizedTest
	void 구장_생성_실패_구장명_null_또는_공백(String stadiumName) {
		assertThatThrownBy(
			() -> StadiumEntity.create(
				stadiumName,
				"광주",
				"광주광역시",
				"북구",
				"광주 북구 서림로 10",
				BigDecimal.valueOf(35.17),
				BigDecimal.valueOf(126.89),
				20500,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("구장명은 비어 있을 수 없습니다.");
	}

	@NullAndEmptySource
	@ParameterizedTest
	void 구장_생성_실패_지역_null_또는_공백(String location) {
		assertThatThrownBy(
			() -> StadiumEntity.create(
				"광주기아챔피언스필드",
				location,
				"광주광역시",
				"북구",
				"광주 북구 서림로 10",
				BigDecimal.valueOf(35.17),
				BigDecimal.valueOf(126.89),
				20500,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("지역명은 비어 있을 수 없습니다.");
	}

	@NullAndEmptySource
	@ParameterizedTest
	void 구장_생성_실패_도시_null_또는_공백(String city) {
		assertThatThrownBy(
			() -> StadiumEntity.create(
				"광주기아챔피언스필드",
				"광주",
				city,
				"북구",
				"광주 북구 서림로 10",
				BigDecimal.valueOf(35.17),
				BigDecimal.valueOf(126.89),
				20500,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("시/도는 비어 있을 수 없습니다.");
	}

	@NullAndEmptySource
	@ParameterizedTest
	void 구장_생성_실패_시군구_null_또는_공백(String district) {
		assertThatThrownBy(
			() -> StadiumEntity.create(
				"광주기아챔피언스필드",
				"광주",
				"광주광역시",
				district,
				"광주 북구 서림로 10",
				BigDecimal.valueOf(35.17),
				BigDecimal.valueOf(126.89),
				20500,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("시/군/구는 비어 있을 수 없습니다.");
	}

	@NullAndEmptySource
	@ParameterizedTest
	void 구장_생성_실패_도로명주소_null_또는_공백(String roadAddress) {
		assertThatThrownBy(
			() -> StadiumEntity.create(
				"광주기아챔피언스필드",
				"광주",
				"광주광역시",
				"북구",
				roadAddress,
				BigDecimal.valueOf(35.17),
				BigDecimal.valueOf(126.89),
				20500,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("도로명 주소는 비어 있을 수 없습니다.");
	}

	@Test
	void 구장_생성_실패_위도_null_또는_공백() {
		assertThatThrownBy(
			() -> StadiumEntity.create(
				"광주기아챔피언스필드",
				"광주",
				"광주광역시",
				"북구",
				"광주 북구 서림로 10",
				null,
				BigDecimal.valueOf(126.89),
				20500,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("위도는 -90에서 90 사이의 값이어야 합니다.");
	}

	@ParameterizedTest
	@ValueSource(doubles = {-90.01, 90.01})
	void 구장_생성_실패_위도_범위_초과(double invalidLatitude) {
		assertThatThrownBy(
			() -> StadiumEntity.create(
				"광주기아챔피언스필드",
				"광주",
				"광주광역시",
				"북구",
				"광주 북구 서림로 10",
				BigDecimal.valueOf(invalidLatitude),
				BigDecimal.valueOf(126.89),
				20500,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("위도는 -90에서 90 사이의 값이어야 합니다.");
	}

	@ParameterizedTest
	@ValueSource(doubles = {-90.0, 90.0})
	void 구장_생성_성공_위도_경계값(double validLatitude) {
		StadiumEntity stadium = StadiumEntity.create(
			"광주기아챔피언스필드",
			"광주",
			"광주광역시",
			"북구",
			"광주 북구 서림로 10",
			BigDecimal.valueOf(validLatitude),
			BigDecimal.valueOf(126.89),
			20500,
			null
		);

		assertThat(stadium.getLatitude()).isEqualByComparingTo(BigDecimal.valueOf(validLatitude));
	}

	@Test
	void 구장_생성_실패_경도_null() {
		assertThatThrownBy(
			() -> StadiumEntity.create(
				"광주기아챔피언스필드",
				"광주",
				"광주광역시",
				"북구",
				"광주 북구 서림로 10",
				BigDecimal.valueOf(35.17),
				null,
				20500,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("경도는 -180에서 180 사이의 값이어야 합니다.");
	}

	@ParameterizedTest
	@ValueSource(doubles = {-180.01, 180.01})
	void 구장_생성_실패_경도_범위_초과(double invalidLongitude) {
		assertThatThrownBy(
			() -> StadiumEntity.create(
				"광주기아챔피언스필드",
				"광주",
				"광주광역시",
				"북구",
				"광주 북구 서림로 10",
				BigDecimal.valueOf(35.17),
				BigDecimal.valueOf(invalidLongitude),
				20500,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("경도는 -180에서 180 사이의 값이어야 합니다.");
	}

	@ParameterizedTest
	@ValueSource(doubles = {-180.0, 180.0})
	void 구장_생성_성공_경도_경계값(double validLongitude) {
		StadiumEntity stadium = StadiumEntity.create(
			"광주기아챔피언스필드",
			"광주",
			"광주광역시",
			"북구",
			"광주 북구 서림로 10",
			BigDecimal.valueOf(35.17),
			BigDecimal.valueOf(validLongitude),
			20500,
			null
		);

		assertThat(stadium).isNotNull();
		assertThat(stadium.getLongitude())
			.isEqualByComparingTo(BigDecimal.valueOf(validLongitude));
	}

	@Test
	void 구장_생성_실패_좌석수_null() {
		assertThatThrownBy(
			() -> StadiumEntity.create(
				"광주기아챔피언스필드",
				"광주",
				"광주광역시",
				"북구",
				"광주 북구 서림로 10",
				BigDecimal.valueOf(35.17),
				BigDecimal.valueOf(126.89),
				null,  // 좌석 수 null
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("총 좌석 수는 0보다 커야 합니다.");
	}

	@Test
	void 구장_생성_실패_좌석수_0() {
		assertThatThrownBy(
			() -> StadiumEntity.create(
				"광주기아챔피언스필드",
				"광주",
				"광주광역시",
				"북구",
				"광주 북구 서림로 10",
				BigDecimal.valueOf(35.17),
				BigDecimal.valueOf(126.89),
				0,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("총 좌석 수는 0보다 커야 합니다.");
	}

	@Test
	void 구장_생성_실패_좌석수_음수() {
		assertThatThrownBy(
			() -> StadiumEntity.create(
				"광주기아챔피언스필드",
				"광주",
				"광주광역시",
				"북구",
				"광주 북구 서림로 10",
				BigDecimal.valueOf(35.17),
				BigDecimal.valueOf(126.89),
				-1,  // 음수
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("총 좌석 수는 0보다 커야 합니다.");
	}

	@Test
	void 구장_생성_성공_좌석맵_설정_null() {
		StadiumEntity stadium = StadiumEntity.create(
			"광주기아챔피언스필드",
			"광주",
			"광주광역시",
			"북구",
			"광주 북구 서림로 10",
			BigDecimal.valueOf(35.17),
			BigDecimal.valueOf(126.89),
			20500,
			null  // seatMapConfig는 nullable
		);

		assertThat(stadium.getSeatMapConfig()).isNull();
	}

	@Test
	void 구장_생성_성공_좌석맵_설정_있음() {
		Map<String, Object> seatMapConfig = Map.of("sections", "sectionTest");

		StadiumEntity stadium = StadiumEntity.create(
			"광주기아챔피언스필드",
			"광주",
			"광주광역시",
			"북구",
			"광주 북구 서림로 10",
			BigDecimal.valueOf(35.17),
			BigDecimal.valueOf(126.89),
			20500,
			seatMapConfig
		);

		assertThat(stadium.getSeatMapConfig()).isEqualTo(seatMapConfig);
	}

}
