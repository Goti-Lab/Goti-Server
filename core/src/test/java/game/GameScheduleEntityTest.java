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
	LocalDate playDate;
	LocalTime startAt;
	LocalDateTime reservationOpenedAt;
	LocalDateTime reservationClosedAt;

	@BeforeEach
	void setup() {
		homeTeamId = UUID.randomUUID();
		awayTeamId = UUID.randomUUID();
		stadiumId = UUID.randomUUID();
		playDate = LocalDate.of(2026, 4, 1);
		startAt = LocalTime.of(18, 30);
		reservationOpenedAt = LocalDateTime.of(2026, 3, 25, 14, 0);
		reservationClosedAt = LocalDateTime.of(2026, 4, 1, 17, 0);
	}

	@Test
	void 경기_생성_성공() {
		GameScheduleEntity game = GameScheduleEntity.create(
			homeTeamId,
			awayTeamId,
			stadiumId,
			playDate,
			startAt
		);

		assertNotNull(game);
		assertThat(game.getHomeTeamId()).isEqualTo(homeTeamId);
		assertThat(game.getAwayTeamId()).isEqualTo(awayTeamId);
		assertThat(game.getStadiumId()).isEqualTo(stadiumId);
		assertThat(game.getPlayDate()).isEqualTo(playDate);
		assertThat(game.getStartAt()).isEqualTo(startAt);
		assertThat(game.getLeagueType()).isEqualTo(LeagueType.REGULAR);

		log.info("game playDate: {}", game.getPlayDate());
	}

	@Test
	void 경기_생성_성공_리그타입_기본값_적용() {
		GameScheduleEntity game = GameScheduleEntity.create(
			homeTeamId,
			awayTeamId,
			stadiumId,
			playDate,
			startAt
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
				playDate,
				startAt
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
				playDate,
				startAt
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
				playDate,
				startAt
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
				playDate,
				startAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("홈팀과 원정팀은 같을 수 없습니다.");
	}

	@Test
	void 경기_생성_실패_경기날짜_null() {
		assertThatThrownBy(
			() -> GameScheduleEntity.create(
				homeTeamId,
				awayTeamId,
				stadiumId,
				null,
				startAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("경기 날짜는 필수입니다.");
	}

	@Test
	void 경기_생성_실패_시작시간_null() {
		assertThatThrownBy(
			() -> GameScheduleEntity.create(
				homeTeamId,
				awayTeamId,
				stadiumId,
				playDate,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("경기 시작 시간은 필수입니다.");
	}

}
