package game;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.goti.constants.LeagueType;
import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.exception.FieldValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
public class GameScheduleEntityTest {

	UUID homeTeamId;
	UUID awayTeamId;
	UUID stadiumId;

	static final LocalDateTime START_AT = LocalDateTime.now().plusDays(3);
	static final LeagueType LEAGUE_TYPE = LeagueType.REGULAR;

	@BeforeEach
	void setup() {
		homeTeamId = UUID.randomUUID();
		awayTeamId = UUID.randomUUID();
		stadiumId = UUID.randomUUID();
	}

	@Test
	void 경기_생성_성공() {
		GameScheduleEntity game = GameScheduleEntity.create(
			homeTeamId,
			awayTeamId,
			stadiumId,
			START_AT,
			LEAGUE_TYPE
		);

		assertNotNull(game);
		assertThat(game.getHomeTeamId()).isEqualTo(homeTeamId);
		assertThat(game.getAwayTeamId()).isEqualTo(awayTeamId);
		assertThat(game.getStadiumId()).isEqualTo(stadiumId);
		assertThat(game.getStartAt()).isEqualTo(START_AT);
		assertThat(game.getLeagueType()).isEqualTo(LeagueType.REGULAR);

		log.info("game startAt: {}", game.getStartAt());
	}

	@Test
	void 경기_생성_성공_리그타입_기본값_적용() {
		GameScheduleEntity game = GameScheduleEntity.create(
			homeTeamId,
			awayTeamId,
			stadiumId,
			START_AT,
			LEAGUE_TYPE
		);

		assertThat(game.getLeagueType()).isEqualTo(LeagueType.REGULAR);
	}

	@Test
	void 경기_생성_실패_홈팀_ID_null() {
		assertThatThrownBy(
			() -> GameScheduleEntity.create(
				null,
				awayTeamId,
				stadiumId,
				START_AT,
				LEAGUE_TYPE
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("홈팀 ID는 필수입니다.");
	}

	@Test
	void 경기_생성_실패_원정팀_ID_null() {
		assertThatThrownBy(
			() -> GameScheduleEntity.create(
				homeTeamId,
				null,
				stadiumId,
				START_AT,
				LEAGUE_TYPE
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("원정팀 ID는 필수입니다.");
	}

	@Test
	void 경기_생성_실패_구장_ID_null() {
		assertThatThrownBy(
			() -> GameScheduleEntity.create(
				homeTeamId,
				awayTeamId,
				null,
				START_AT,
				LEAGUE_TYPE
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("구장 ID는 필수입니다.");
	}

	@Test
	void 경기_생성_실패_홈팀_원정팀_동일() {
		assertThatThrownBy(
			() -> GameScheduleEntity.create(
				homeTeamId,
				homeTeamId,
				stadiumId,
				START_AT,
				LEAGUE_TYPE
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("홈팀과 원정팀은 같을 수 없습니다.");
	}

	@Test
	void 경기_생성_실패_시작시간_null() {
		assertThatThrownBy(
			() -> GameScheduleEntity.create(
				homeTeamId,
				awayTeamId,
				stadiumId,
				null,
				LEAGUE_TYPE
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("경기 시작 시간은 필수입니다.");
	}

	@Test
	void 경기_생성_실패_경기타입_null() {
		assertThatThrownBy(
			() -> GameScheduleEntity.create(
				homeTeamId,
				awayTeamId,
				stadiumId,
				START_AT,
				null
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("경기생성실패_경기타입 null : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 경기 타입은 필수입니다.", ex.getMessage());
			}
		);
	}

}
