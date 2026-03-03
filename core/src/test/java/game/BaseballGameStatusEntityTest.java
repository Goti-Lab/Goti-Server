package game;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.goti.constants.GameResult;
import com.goti.constants.GameStatus;
import com.goti.constants.LeagueType;
import com.goti.constants.ReservationAvailableStatus;
import com.goti.domain.entity.game.BaseballGameEntity;
import com.goti.domain.entity.game.BaseballGameStatusEntity;
import com.goti.exception.FieldValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
public class BaseballGameStatusEntityTest {

	BaseballGameEntity baseballGame;

	@BeforeEach
	void setup() {
		baseballGame = BaseballGameEntity.create(
			UUID.randomUUID(),
			UUID.randomUUID(),
			UUID.randomUUID(),
			LocalDate.of(2026, 4, 1),
			LocalTime.of(18, 30),
			GameStatus.SCHEDULED,
			LeagueType.REGULAR,
			ReservationAvailableStatus.PENDING,
			LocalDateTime.of(2026, 3, 25, 14, 0),
			LocalDateTime.of(2026, 4, 1, 17, 0)
		);
	}

	@Test
	void 경기상태_생성_성공() {
		BaseballGameStatusEntity gameStatus = BaseballGameStatusEntity.create(
			baseballGame,
			GameStatus.IN_PROGRESS,
			3,
			1,
			GameResult.PENDING
		);

		assertNotNull(gameStatus);
		assertThat(gameStatus.getBaseballGameId()).isEqualTo(baseballGame);
		assertThat(gameStatus.getGameStatus()).isEqualTo(GameStatus.IN_PROGRESS);
		assertThat(gameStatus.getHomeTeamScore()).isEqualTo(0);
		assertThat(gameStatus.getAwayTeamScore()).isEqualTo(0);
		assertThat(gameStatus.getGameResult()).isEqualTo(GameResult.PENDING);

		log.info("gameStatus : {}", gameStatus.getGameStatus());
		log.info("score : {}:{}", gameStatus.getHomeTeamScore(), gameStatus.getAwayTeamScore());
	}

	@Test
	void 경기상태_생성_성공_결과_기본값_적용() {
		BaseballGameStatusEntity gameStatus = BaseballGameStatusEntity.create(
			baseballGame,
			GameStatus.SCHEDULED,
			0,
			0,
			null
		);

		assertThat(gameStatus.getGameResult()).isEqualTo(GameResult.PENDING);
	}

	@Test
	void 경기상태_생성_실패_경기상태_null() {
		assertThatThrownBy(
			() -> BaseballGameStatusEntity.create(
				baseballGame,
				null,
				0,
				0,
				GameResult.PENDING
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("경기 상태는 필수입니다.");
	}
}
