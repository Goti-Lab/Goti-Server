package com.goti.ticketing.domain.entity.seat;

import static lombok.AccessLevel.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.util.StringUtils;

import com.goti.ticketing.constants.SeatHoldStatus;
import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "seat_holds",
	indexes = {
		@Index(name = "idx_user_status", columnList = "user_id, status"),
		@Index(name = "idx_expires", columnList = "status, expired_at")
	}
)
@NoArgsConstructor(access = PROTECTED)
public class SeatHoldEntity extends ModificationTimestampEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seat_id", nullable = false)
	private SeatEntity seat;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "game_schedule_id", nullable = false)
	private GameScheduleEntity gameSchedule;

	@Column(nullable = false)
	private UUID userId;

	@Column(nullable = false)
	private String queueTokenJti;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SeatHoldStatus status;

	@Column(nullable = false)
	private LocalDateTime expiredAt;

	private LocalDateTime releasedAt;

	private SeatHoldEntity(
		SeatEntity seat,
		GameScheduleEntity gameSchedule,
		UUID userId,
		String queueTokenJti,
		LocalDateTime expiredAt
	) {
		this.seat = seat;
		this.gameSchedule = gameSchedule;
		this.userId = userId;
		this.queueTokenJti = queueTokenJti;
		this.status = SeatHoldStatus.HOLDING;
		this.expiredAt = expiredAt;
		this.releasedAt = null;
	}

	public static SeatHoldEntity create(
		SeatEntity seat,
		GameScheduleEntity game,
		UUID userId,
		String queueTokenJti,
		LocalDateTime expiredAt
	) {
		validate(userId, queueTokenJti, expiredAt);
		return new SeatHoldEntity(seat, game, userId, queueTokenJti, expiredAt);
	}

	private static void validate(
		UUID userId,
		String queueTokenJti,
		LocalDateTime expiredAt
	) {
		Preconditions.domainValidate(
			userId != null,
			"유저 ID는 필수입니다."
		);
		Preconditions.domainValidate(
			StringUtils.hasText(queueTokenJti),
			"큐 토큰 식별자는 비어 있을 수 없습니다."
		);
		Preconditions.domainValidate(
			expiredAt != null,
			"만료 시각은 필수입니다."
		);
	}

	public void release() {
		Preconditions.domainValidate(
			this.status == SeatHoldStatus.HOLDING,
			"HOLDING 상태에서만 RELEASED 상태로 변경할 수 있습니다."
		);
		this.status = SeatHoldStatus.RELEASED;
		this.releasedAt = LocalDateTime.now();
	}
}
