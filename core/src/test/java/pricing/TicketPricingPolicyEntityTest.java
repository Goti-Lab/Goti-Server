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

	private UUID teamId;
	private LocalDate policyStartAt;
	private LocalDate policyEndAt;
	private UUID createdBy;

	@BeforeEach
	void setup() {
		teamId = UUID.randomUUID();
		policyStartAt = LocalDate.of(2026, 3, 1);
		policyEndAt = LocalDate.of(2026, 10, 31);
		createdBy = UUID.randomUUID();
	}

	@Test
	void 가격정책_생성_성공() {
		TicketPricingPolicyEntity policy = TicketPricingPolicyEntity.create(
			teamId,
			policyStartAt,
			policyEndAt,
			createdBy
		);

		assertThat(policy.getTeamId()).isEqualTo(teamId);
		assertThat(policy.getPolicyStartAt()).isEqualTo(policyStartAt);
		assertThat(policy.getPolicyEndAt()).isEqualTo(policyEndAt);
		assertThat(policy.isActive()).isTrue();
		assertThat(policy.getCreatedBy()).isEqualTo(createdBy);
	}


	@Test
	void 가격정책_생성_실패_팀ID_null() {
		assertThatThrownBy(
			() -> TicketPricingPolicyEntity.create(null, policyStartAt, policyEndAt, createdBy)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("팀 ID는 필수입니다.");
	}

	@Test
	void 가격정책_생성_실패_시작일_null() {
		assertThatThrownBy(
			() -> TicketPricingPolicyEntity.create(teamId, null, policyEndAt, createdBy)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("정책 시작일은 필수입니다.");
	}

	@Test
	void 가격정책_생성_실패_종료일_null() {
		assertThatThrownBy(
			() -> TicketPricingPolicyEntity.create(teamId, policyStartAt, null, createdBy)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("정책 종료일은 필수입니다.");
	}

	@Test
	void 가격정책_생성_실패_종료일이_시작일보다_빠름() {
		assertThatThrownBy(
			() -> TicketPricingPolicyEntity.create(
				teamId,
				policyStartAt,
				policyStartAt.minusDays(1),
				createdBy
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("정책 종료일은 시작일보다 빠를 수 없습니다.");
	}

	@Test
	void 가격정책_생성_실패_생성자_null() {
		assertThatThrownBy(
			() -> TicketPricingPolicyEntity.create(teamId, policyStartAt, policyEndAt, null)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("생성자 ID는 필수입니다.");
	}
}
