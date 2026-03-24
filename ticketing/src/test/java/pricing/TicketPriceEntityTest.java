package pricing;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

import com.goti.ticketing.constants.LeagueType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.goti.ticketing.constants.TicketPricingDayType;
import com.goti.ticketing.constants.TicketType;
import com.goti.ticketing.domain.entity.pricing.TicketPriceEntity;
import com.goti.ticketing.domain.entity.pricing.TicketPricingPolicyEntity;
import com.goti.ticketing.domain.entity.seat.SeatGradeEntity;
import com.goti.exception.FieldValidationException;

@ActiveProfiles("test")
class TicketPriceEntityTest {

	SeatGradeEntity grade;
	TicketPricingPolicyEntity policy;

	@BeforeEach
	void setup() {
		grade = SeatGradeEntity.create(UUID.randomUUID(), "VIP", "#FFAA00");
		policy = TicketPricingPolicyEntity.create(
			UUID.randomUUID(),
			LocalDate.of(2026, 3, 1),
			LocalDate.of(2026, 10, 31)
		);
	}

	@Test
	void 티켓요금_생성_성공() {
		TicketPriceEntity ticketPrice = TicketPriceEntity.create(
			grade,
			policy,
			TicketType.ADULT,
			TicketPricingDayType.WEEKDAY,
			LeagueType.REGULAR,
			15000
		);

		assertThat(ticketPrice.getGrade()).isEqualTo(grade);
		assertThat(ticketPrice.getPolicy()).isEqualTo(policy);
		assertThat(ticketPrice.getTicketType()).isEqualTo(TicketType.ADULT);
		assertThat(ticketPrice.getDayType()).isEqualTo(TicketPricingDayType.WEEKDAY);
		assertThat(ticketPrice.getLeagueType()).isEqualTo(LeagueType.REGULAR);
		assertThat(ticketPrice.getPrice()).isEqualTo(15000);
	}

	@Test
	void 티켓요금_생성_실패_권종_null() {
		assertThatThrownBy(
			() -> TicketPriceEntity.create(
				grade,
				policy,
				null,
				TicketPricingDayType.WEEKDAY,
				LeagueType.REGULAR,
				15000
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("권종은 필수입니다.");
	}

	@Test
	void 티켓요금_생성_실패_요일유형_null() {
		assertThatThrownBy(
			() -> TicketPriceEntity.create(
				grade,
				policy,
				TicketType.ADULT,
				null,
				LeagueType.REGULAR,
				15000
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("요일 유형은 필수입니다.");
	}

	@Test
	void 티켓요금_생성_실패_리그유형_null() {
		assertThatThrownBy(
			() -> TicketPriceEntity.create(
				grade,
				policy,
				TicketType.ADULT,
				TicketPricingDayType.WEEKDAY,
				null,
				15000
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("리그 유형은 필수입니다.");
	}

	@Test
	void 티켓요금_생성_실패_가격_음수() {
		assertThatThrownBy(
			() -> TicketPriceEntity.create(
				grade,
				policy,
				TicketType.ADULT,
				TicketPricingDayType.WEEKDAY,
				LeagueType.REGULAR,
				-1
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("가격은 0 이상이어야 합니다.");
	}
}
