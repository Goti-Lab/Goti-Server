package pricing;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.goti.domain.entity.pricing.TicketPricingPolicyEntity;
import com.goti.exception.FieldValidationException;

@ActiveProfiles("test")
class TicketPricingPolicyEntityTest {

	UUID teamId;
	LocalDate policyStartAt;
	LocalDate policyEndAt;

	@BeforeEach
	void setup() {
		teamId = UUID.randomUUID();
		policyStartAt = LocalDate.of(2026, 3, 1);
		policyEndAt = LocalDate.of(2026, 10, 31);
	}

	@Test
	void 가격정책_생성_성공() {
		TicketPricingPolicyEntity policy = TicketPricingPolicyEntity.create(
			teamId,
			policyStartAt,
			policyEndAt
		);

		assertThat(policy.getTeamId()).isEqualTo(teamId);
		assertThat(policy.getPolicyStartAt()).isEqualTo(policyStartAt);
		assertThat(policy.getPolicyEndAt()).isEqualTo(policyEndAt);
		assertThat(policy.isActive()).isTrue();
	}


	@Test
	void 가격정책_생성_실패_팀ID_null() {
		assertThatThrownBy(
			() -> TicketPricingPolicyEntity.create(null, policyStartAt, policyEndAt)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("팀 ID는 필수입니다.");
	}

	@Test
	void 가격정책_생성_실패_시작일_null() {
		assertThatThrownBy(
			() -> TicketPricingPolicyEntity.create(teamId, null, policyEndAt)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("정책 시작일은 필수입니다.");
	}

	@Test
	void 가격정책_생성_실패_종료일_null() {
		assertThatThrownBy(
			() -> TicketPricingPolicyEntity.create(teamId, policyStartAt, null)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("정책 종료일은 필수입니다.");
	}

	@Test
	void 가격정책_생성_실패_종료일이_시작일보다_빠름() {
		assertThatThrownBy(
			() -> TicketPricingPolicyEntity.create(
				teamId,
				policyStartAt,
				policyStartAt.minusDays(1)
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("정책 종료일은 시작일보다 빠를 수 없습니다.");
	}
}
