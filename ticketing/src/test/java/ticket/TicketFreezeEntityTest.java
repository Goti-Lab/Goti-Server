package ticket;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.goti.exception.FieldValidationException;
import com.goti.ticketing.constants.TicketFreezeReason;
import com.goti.ticketing.domain.entity.ticket.TicketEntity;
import com.goti.ticketing.domain.entity.ticket.TicketFreezeEntity;

@ActiveProfiles("test")
class TicketFreezeEntityTest {

	@Test
	void 티켓_동결_생성_성공() {
		TicketEntity ticket = createTicket();
		LocalDateTime frozenUntil = LocalDateTime.now().plusHours(12);

		TicketFreezeEntity freeze = TicketFreezeEntity.create(
			ticket,
			TicketFreezeReason.POLICY_VIOLATION,
			frozenUntil
		);

		assertThat(freeze.getTicket()).isEqualTo(ticket);
		assertThat(freeze.getFreezeReason()).isEqualTo(TicketFreezeReason.POLICY_VIOLATION);
		assertThat(freeze.getFrozenUntil()).isEqualTo(frozenUntil);
		assertThat(freeze.isActive()).isTrue();
	}

	@Test
	void 티켓_동결_생성_실패_종료시각_과거() {
		TicketEntity ticket = createTicket();

		assertThatThrownBy(() -> TicketFreezeEntity.create(
			ticket,
			TicketFreezeReason.RESALE_CANCEL_AFTER_ONE_HOUR,
			LocalDateTime.now().minusMinutes(1)
		)).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("동결 종료 시각은 현재보다 이후여야 합니다.");
	}

	@Test
	void 티켓_동결_갱신_성공() {
		TicketFreezeEntity freeze = TicketFreezeEntity.create(
			createTicket(),
			TicketFreezeReason.RESALE_CANCEL_AFTER_ONE_HOUR,
			LocalDateTime.now().plusHours(12)
		);

		LocalDateTime newFrozenUntil = LocalDateTime.now().plusHours(6);
		freeze.refreeze(
			TicketFreezeReason.POLICY_VIOLATION,
			newFrozenUntil
		);

		assertThat(freeze.getFreezeReason()).isEqualTo(TicketFreezeReason.POLICY_VIOLATION);
		assertThat(freeze.getFrozenUntil()).isEqualTo(newFrozenUntil);
	}

	private TicketEntity createTicket() {
		return TicketEntity.create(
			"TKT-20260215-XXXXX",
			UUID.randomUUID(),
			null,
			UUID.randomUUID(),
			UUID.randomUUID(),
			"goti-user",
			"user@goti.com",
			"01012345678",
			"삼성 vs 두산",
			LocalDateTime.of(2026, 4, 1, 18, 30),
			"VIP A구역 3열 15번",
			12000,
			null
		);
	}
}
