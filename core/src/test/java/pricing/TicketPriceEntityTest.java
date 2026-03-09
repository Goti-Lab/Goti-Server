package pricing;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.goti.constants.TicketPricingDayType;
import com.goti.constants.TicketPricingMatchType;
import com.goti.constants.TicketType;
import com.goti.domain.entity.pricing.TicketPriceEntity;
import com.goti.domain.entity.pricing.TicketPricingPolicyEntity;
import com.goti.domain.entity.seat.SeatGradeEntity;
import com.goti.exception.FieldValidationException;

@ActiveProfiles("test")
class TicketPriceEntityTest {

	private SeatGradeEntity grade;
	private TicketPricingPolicyEntity policy;
	private UUID createdBy;

	@BeforeEach
	void setup() {
		grade = SeatGradeEntity.create(UUID.randomUUID(), "VIP", "#FFAA00");
		policy = TicketPricingPolicyEntity.create(
			UUID.randomUUID(),
			LocalDate.of(2026, 3, 1),
			LocalDate.of(2026, 10, 31),
			UUID.randomUUID()
		);
		createdBy = UUID.randomUUID();
	}

	@Test
	void 티켓요금_생성_성공() {
		TicketPriceEntity ticketPrice = TicketPriceEntity.create(
			grade,
			policy,
			TicketType.ADULT,
			TicketPricingDayType.WEEKDAY,
			TicketPricingMatchType.REGULAR,
			15000,
			createdBy
		);

		assertThat(ticketPrice.getGrade()).isEqualTo(grade);
		assertThat(ticketPrice.getPolicy()).isEqualTo(policy);
		assertThat(ticketPrice.getTicketType()).isEqualTo(TicketType.ADULT);
		assertThat(ticketPrice.getDayType()).isEqualTo(TicketPricingDayType.WEEKDAY);
		assertThat(ticketPrice.getMatchType()).isEqualTo(TicketPricingMatchType.REGULAR);
		assertThat(ticketPrice.getPrice()).isEqualTo(15000);
		assertThat(ticketPrice.getCreatedBy()).isEqualTo(createdBy);
	}

	@Test
	void 티켓요금_생성_실패_권종_null() {
		assertThatThrownBy(
			() -> TicketPriceEntity.create(
				grade,
				policy,
				null,
				TicketPricingDayType.WEEKDAY,
				TicketPricingMatchType.REGULAR,
				15000,
				createdBy
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
				TicketPricingMatchType.REGULAR,
				15000,
				createdBy
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("요일 유형은 필수입니다.");
	}

	@Test
	void 티켓요금_생성_실패_매치유형_null() {
		assertThatThrownBy(
			() -> TicketPriceEntity.create(
				grade,
				policy,
				TicketType.ADULT,
				TicketPricingDayType.WEEKDAY,
				null,
				15000,
				createdBy
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("매치 유형은 필수입니다.");
	}

	@Test
	void 티켓요금_생성_실패_가격_음수() {
		assertThatThrownBy(
			() -> TicketPriceEntity.create(
				grade,
				policy,
				TicketType.ADULT,
				TicketPricingDayType.WEEKDAY,
				TicketPricingMatchType.REGULAR,
				-1,
				createdBy
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("가격은 0 이상이어야 합니다.");
	}

	@Test
	void 티켓요금_생성_실패_생성자_null() {
		assertThatThrownBy(
			() -> TicketPriceEntity.create(
				grade,
				policy,
				TicketType.ADULT,
				TicketPricingDayType.WEEKDAY,
				TicketPricingMatchType.REGULAR,
				15000,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("생성자 ID는 필수입니다.");
	}
}
