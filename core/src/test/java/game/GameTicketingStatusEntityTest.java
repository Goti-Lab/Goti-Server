package game;

import com.goti.constants.LeagueType;
import com.goti.domain.entity.game.GameScheduleEntity;

import com.goti.domain.entity.game.GameTicketingStatusEntity;

import com.goti.exception.FieldValidationException;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@ActiveProfiles("test")
public class GameTicketingStatusEntityTest {

	GameScheduleEntity gameSchedule;

	static final LocalDateTime START_AT = LocalDateTime.now().plusDays(3);
	static final LeagueType LEAGUE_TYPE = LeagueType.REGULAR;
	static final LocalDateTime OPENED_AT = LocalDateTime.now().plusDays(3);
	static final LocalDateTime CLOSED_AT = LocalDateTime.now().plusDays(5);

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
	void 게임_티켓팅_상태_생성_성공() {
		GameTicketingStatusEntity gameTicketingStatus = GameTicketingStatusEntity.create(
			gameSchedule, OPENED_AT, CLOSED_AT
		);

		assertNotNull(gameTicketingStatus);
		log.info("gameTicketingStatus :: {}", gameTicketingStatus);
	}

	@Test
	void 게임_티켓팅_상태_생성_실패_gameSchedule_null() {
		assertThatThrownBy(
			() -> GameTicketingStatusEntity.create(
				null, OPENED_AT, CLOSED_AT
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("게임일정 값은 비어있을 수 없습니다.");
	}

	@Test
	void 게임_티켓팅_상태_생성_실패_예매시작_시간_null() {
		assertThatThrownBy(
			() -> GameTicketingStatusEntity.create(
				gameSchedule, null, CLOSED_AT
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("예매 시작 시점은 현재보다 미래여야 합니다.");
	}

	@Test
	void 게임_티켓팅_상태_생성_실패_예매시작_시간_과거() {
		LocalDateTime openedAt = LocalDateTime.now().minusDays(2);

		assertThatThrownBy(
			() -> GameTicketingStatusEntity.create(
				gameSchedule, openedAt , CLOSED_AT
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("예매 시작 시점은 현재보다 미래여야 합니다.");
	}

	@Test
	void 게임_티켓팅_상태_생성_실패_예매종료_시간_null() {
		assertThatThrownBy(
			() -> GameTicketingStatusEntity.create(
				gameSchedule, OPENED_AT, null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("예매 종료 시점은 현재보다 미래여야 합니다.");
	}

	@Test
	void 게임_티켓팅_상태_생성_실패_예매종료_시간_과거() {
		LocalDateTime closeAt = LocalDateTime.now().minusDays(2);

		assertThatThrownBy(
			() -> GameTicketingStatusEntity.create(
				gameSchedule, OPENED_AT, closeAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("예매 종료 시점은 현재보다 미래여야 합니다.");
	}

	@Test
	void 게임_티켓팅_상태_생성_실패_예매시작시간_예매종료시간_이후() {
		LocalDateTime openedAt = LocalDateTime.now().plusDays(3);
		LocalDateTime closeAt = LocalDateTime.now().plusDays(1);

		assertThatThrownBy(
			() -> GameTicketingStatusEntity.create(
				gameSchedule, openedAt, closeAt
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("예매 종료 시점은 시작 시점보다 이후여야 합니다.");
	}

}
