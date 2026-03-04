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
import com.goti.constants.ReservationAvailableStatus;
import com.goti.domain.entity.game.BaseballGameEntity;
import com.goti.exception.FieldValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
public class BaseballGameEntityTest {

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
		BaseballGameEntity game = BaseballGameEntity.create(
			homeTeamId,
			awayTeamId,
			stadiumId,
			playDate,
			startAt,
			reservationOpenedAt,
			reservationClosedAt
		);

		assertNotNull(game);
		assertThat(game.getHomeTeamId()).isEqualTo(homeTeamId);
		assertThat(game.getAwayTeamId()).isEqualTo(awayTeamId);
		assertThat(game.getStadiumId()).isEqualTo(stadiumId);
		assertThat(game.getPlayDate()).isEqualTo(playDate);
		assertThat(game.getStartAt()).isEqualTo(startAt);
		assertThat(game.getLeagueType()).isEqualTo(LeagueType.REGULAR);
		assertThat(game.getReservationAvailableStatus()).isEqualTo(ReservationAvailableStatus.PENDING);

		log.info("game playDate: {}", game.getPlayDate());
		log.info("reservation status: {}", game.getReservationAvailableStatus());
	}

	@Test
	void 경기_생성_성공_리그타입_기본값_적용() {
		BaseballGameEntity game = BaseballGameEntity.create(
			homeTeamId,
			awayTeamId,
			stadiumId,
			playDate,
			startAt,
			reservationOpenedAt,
			reservationClosedAt
		);

		assertThat(game.getLeagueType()).isEqualTo(LeagueType.REGULAR);
		assertThat(game.getReservationAvailableStatus()).isEqualTo(ReservationAvailableStatus.PENDING);
	}

	@Test
	void 경기_생성_실패_홈팀_ID_null() {
		assertThatThrownBy(
			() -> BaseballGameEntity.create(
				null,
				awayTeamId,
				stadiumId,
				playDate,
				startAt,
				reservationOpenedAt,
				reservationClosedAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("홈팀 ID는 필수입니다.");
	}

	@Test
	void 경기_생성_실패_원정팀_ID_null() {
		assertThatThrownBy(
			() -> BaseballGameEntity.create(
				homeTeamId,
				null,
				stadiumId,
				playDate,
				startAt,
				reservationOpenedAt,
				reservationClosedAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("원정팀 ID는 필수입니다.");
	}

	@Test
	void 경기_생성_실패_구장_ID_null() {
		assertThatThrownBy(
			() -> BaseballGameEntity.create(
				homeTeamId,
				awayTeamId,
				null,
				playDate,
				startAt,
				reservationOpenedAt,
				reservationClosedAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("구장 ID는 필수입니다.");
	}

	@Test
	void 경기_생성_실패_홈팀_원정팀_동일() {
		assertThatThrownBy(
			() -> BaseballGameEntity.create(
				homeTeamId,
				homeTeamId,
				stadiumId,
				playDate,
				startAt,
				reservationOpenedAt,
				reservationClosedAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("홈팀과 원정팀은 같을 수 없습니다.");
	}

	@Test
	void 경기_생성_실패_경기날짜_null() {
		assertThatThrownBy(
			() -> BaseballGameEntity.create(
				homeTeamId,
				awayTeamId,
				stadiumId,
				null,
				startAt,
				reservationOpenedAt,
				reservationClosedAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("경기 날짜는 필수입니다.");
	}

	@Test
	void 경기_생성_실패_시작시간_null() {
		assertThatThrownBy(
			() -> BaseballGameEntity.create(
				homeTeamId,
				awayTeamId,
				stadiumId,
				playDate,
				null,
				reservationOpenedAt,
				reservationClosedAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("경기 시작 시간은 필수입니다.");
	}

	@Test
	void 경기_생성_실패_예매오픈일시_null() {
		assertThatThrownBy(
			() -> BaseballGameEntity.create(
				homeTeamId,
				awayTeamId,
				stadiumId,
				playDate,
				startAt,
				null,
				reservationClosedAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("예매 오픈 일시는 필수입니다.");
	}

	@Test
	void 경기_생성_실패_예매종료일시_null() {
		assertThatThrownBy(
			() -> BaseballGameEntity.create(
				homeTeamId,
				awayTeamId,
				stadiumId,
				playDate,
				startAt,
				reservationOpenedAt,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("예매 종료 일시는 필수입니다.");
	}

	@Test
	void 경기_생성_실패_예매오픈이_종료보다_늦음() {
		LocalDateTime invalidOpen = LocalDateTime.of(2026, 4, 1, 18, 0);
		LocalDateTime invalidClose = LocalDateTime.of(2026, 4, 1, 17, 0);

		assertThatThrownBy(
			() -> BaseballGameEntity.create(
				homeTeamId,
				awayTeamId,
				stadiumId,
				playDate,
				startAt,
				invalidOpen,
				invalidClose
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("예매 오픈 일시는 종료 일시보다 늦을 수 없습니다.");
	}
}
