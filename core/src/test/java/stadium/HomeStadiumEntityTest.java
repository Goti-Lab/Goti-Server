package stadium;

import com.goti.constants.StadiumType;
import com.goti.constants.TeamCode;
import com.goti.domain.entity.resale.EscrowAccountEntity;
import com.goti.domain.entity.stadium.HomeStadiumEntity;
import com.goti.domain.entity.stadium.StadiumEntity;
import com.goti.domain.entity.team.BaseballTeamEntity;

import com.goti.exception.FieldValidationException;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ActiveProfiles("test")
public class HomeStadiumEntityTest {

	BaseballTeamEntity baseballTeam;
	StadiumEntity stadium;

	@BeforeEach
	void setup() {
		createBaseballTeam();
		createStadium();
	}

	@Test
	void 홈구장_생성_성공() {
		HomeStadiumEntity homeStadium = HomeStadiumEntity.create(
			baseballTeam, stadium, StadiumType.PRIMARY
		);
		assertNotNull(homeStadium);
		assertEquals(baseballTeam, homeStadium.getBaseballTeam());
		assertEquals(stadium, homeStadium.getStadium());
		assertEquals(StadiumType.PRIMARY, homeStadium.getType());
	}

	@Test
	void 홈구장_생성_실패_baseballTeam_null() {
		assertThatThrownBy(
			() -> HomeStadiumEntity.create(
					null, stadium, StadiumType.PRIMARY
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("도메인 필드 오류 : 야구구단 정보는 비어있을 수 없습니다.");
	}

	@Test
	void 홈구장_생성_실패_stadium_null() {
		assertThatThrownBy(
			() -> HomeStadiumEntity.create(
				baseballTeam,null, StadiumType.PRIMARY
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("도메인 필드 오류 : 구장 정보는 비어있을 수 없습니다.");
	}

	@Test
	void 홈구장_생성_실패_type_null() {
		assertThatThrownBy(
			() -> HomeStadiumEntity.create(
				baseballTeam, stadium, null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("도메인 필드 오류 : 구장 타입은 비어있을 수 없습니다.");
	}


	private void createStadium() {
		stadium = StadiumEntity.create(
			"대구 삼성 라이온즈 파크",
			"대구",
			"대구광역시",
			"수성구",
			"대구광역시 수성구 야구전설로 1",
			new BigDecimal("35.84112000"),
			new BigDecimal("128.68152000"),
			24000,
			Map.of(
				"shape", "octagon",
				"openedYear", 2016,
				"turf", "natural"
			)
		);
	}

	private void createBaseballTeam() {
		baseballTeam = BaseballTeamEntity.create(
			TeamCode.SS,
			"삼성라이온즈",
			"Samsung Lions",
			"삼성",
			"대구",
			1982,
			"대구광역시 수성구 야구전설로 1",
			"42250",
			"https://www.samsunglions.com/",
			"홍길동",
			"삼성그룹",
			"이재용",
			"이종열",
			"박진만",
			"https://image.url/logo.png"
		);
	}
}
