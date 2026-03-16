package order;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import com.goti.domain.entity.order.OrderHistoryEntity;
import com.goti.constants.LeagueType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;

import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.order.OrderEntity;
import com.goti.exception.FieldValidationException;

@ActiveProfiles("test")
class OrderHistoryEntityTest {

	OrderEntity order;
	String name;
	String mobile;
	String email;
	static final LocalDateTime START_AT = LocalDateTime.now().plusDays(3);
	static final LeagueType LEAGUE_TYPE = LeagueType.REGULAR;

	@BeforeEach
	void setup() {
		GameScheduleEntity gameSchedule = GameScheduleEntity.create(
			UUID.randomUUID(),
			UUID.randomUUID(),
			UUID.randomUUID(),
			START_AT,
			LEAGUE_TYPE
		);

		order = OrderEntity.create(
			"ORD-20260309-0001",
			UUID.randomUUID(),
			gameSchedule,
			2,
			24000
		);
		name = "홍길동";
		mobile = "01012345678";
		email = "orderer@goti.com";
	}

	@Test
	void 구매자정보_생성_성공() {
		OrderHistoryEntity orderer = OrderHistoryEntity.create(order, name, mobile, email);

		assertThat(orderer.getOrder()).isEqualTo(order);
		assertThat(orderer.getName()).isEqualTo(name);
		assertThat(orderer.getMobile()).isEqualTo(mobile);
		assertThat(orderer.getEmail()).isEqualTo(email);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "   "})
	void 구매자정보_생성_실패_이름_빈값(String invalidName) {
		assertThatThrownBy(
			() -> OrderHistoryEntity.create(order, invalidName, mobile, email)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("구매자 이름은 비어 있을 수 없습니다.");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "   "})
	void 구매자정보_생성_실패_연락처_빈값(String invalidMobile) {
		assertThatThrownBy(
			() -> OrderHistoryEntity.create(order, name, invalidMobile, email)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("구매자 연락처는 비어 있을 수 없습니다.");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "   "})
	void 구매자정보_생성_실패_이메일_빈값(String invalidEmail) {
		assertThatThrownBy(
			() -> OrderHistoryEntity.create(order, name, mobile, invalidEmail)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("구매자 이메일은 비어 있을 수 없습니다.");
	}
}
