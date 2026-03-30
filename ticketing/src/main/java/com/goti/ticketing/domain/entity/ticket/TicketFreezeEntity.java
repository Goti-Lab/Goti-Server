package com.goti.ticketing.domain.entity.ticket;

import static lombok.AccessLevel.*;

import java.time.LocalDateTime;

import org.springframework.util.StringUtils;

import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.constants.TicketFreezeReason;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "ticket_freezes",
	indexes = {
		@Index(name = "idx_ticket_freezes_frozen_until", columnList = "frozen_until")
	}
)
@NoArgsConstructor(access = PROTECTED)
public class TicketFreezeEntity extends ModificationTimestampEntity {

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ticket_id", nullable = false, unique = true)
	private TicketEntity ticket;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TicketFreezeReason freezeReason;

	@Column(nullable = false)
	private LocalDateTime frozenUntil;

	private TicketFreezeEntity(
		TicketEntity ticket,
		TicketFreezeReason freezeReason,
		LocalDateTime frozenUntil
	) {
		this.ticket = ticket;
		this.freezeReason = freezeReason;
		this.frozenUntil = frozenUntil;
	}

	public static TicketFreezeEntity create(
		TicketEntity ticket,
		TicketFreezeReason freezeReason,
		LocalDateTime frozenUntil
	) {
		validate(ticket, freezeReason, frozenUntil);
		return new TicketFreezeEntity(ticket, freezeReason, frozenUntil);
	}

	public boolean isActive() {
		return LocalDateTime.now().isBefore(this.frozenUntil);
	}

	public void refreeze(
		TicketFreezeReason freezeReason,
		LocalDateTime frozenUntil
	) {
		validate(this.ticket, freezeReason, frozenUntil);
		this.freezeReason = freezeReason;
		this.frozenUntil = frozenUntil;
	}

	private static void validate(
		TicketEntity ticket,
		TicketFreezeReason freezeReason,
		LocalDateTime frozenUntil
	) {
		Preconditions.domainValidate(ticket != null, "동결 대상 티켓은 필수입니다.");
		Preconditions.domainValidate(freezeReason != null, "동결 사유는 필수입니다.");
		Preconditions.domainValidate(frozenUntil != null, "동결 종료 시각은 필수입니다.");
		Preconditions.domainValidate(
			frozenUntil.isAfter(LocalDateTime.now()),
			"동결 종료 시각은 현재보다 이후여야 합니다."
		);
	}
}
