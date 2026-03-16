package game;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.constants.LeagueType;
import com.goti.exception.FieldValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.goti.constants.GameResult;
import com.goti.constants.GameStatus;
import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.game.GameStatusEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
public class GameStatusEntityTest {

	GameScheduleEntity gameSchedule;

	static final LocalDateTime START_AT = LocalDateTime.now().plusDays(3);
	static final LeagueType LEAGUE_TYPE = LeagueType.REGULAR;

	@BeforeEach
	void setup() {
		gameSchedule = GameScheduleEntity.create(
			UUID.randomUUID(),
			UUID.randomUUID(),
			UUID.randomUUID(),
			START_AT,
			LEAGUE_TYPE
		);
	}

	@Test
	void 경기상태_생성_성공() {
		GameStatusEntity gameStatus = GameStatusEntity.create(gameSchedule);

		assertNotNull(gameStatus);
		assertThat(gameStatus.getGameSchedule()).isEqualTo(gameSchedule);
		assertThat(gameStatus.getGameStatus()).isEqualTo(GameStatus.SCHEDULED);
		assertThat(gameStatus.getHomeTeamScore()).isEqualTo(0);
		assertThat(gameStatus.getAwayTeamScore()).isEqualTo(0);
		assertThat(gameStatus.getGameResult()).isEqualTo(GameResult.NONE);

		log.info("gameStatus : {}", gameStatus.getGameStatus());
		log.info("score : {}:{}", gameStatus.getHomeTeamScore(), gameStatus.getAwayTeamScore());
	}

	@Test
	void 경기상태_생성_실패_gameSchedule_null() {

		assertThatThrownBy(
			() -> GameStatusEntity.create(null)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("경기상태 엔티티 : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 게임일정 값은 비어있을 수 없습니다.", ex.getMessage());
			}
		);
	}
}
