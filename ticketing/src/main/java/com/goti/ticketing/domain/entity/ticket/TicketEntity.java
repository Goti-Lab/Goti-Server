package com.goti.ticketing.domain.entity.ticket;

import static lombok.AccessLevel.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.util.StringUtils;

import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.constants.ResaleEnabledStatus;
import com.goti.ticketing.constants.TicketStatus;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@AttributeOverride(name = "createdAt", column = @Column(name = "issued_at", nullable = false, updatable = false))
@Table(
	name = "tickets",
	indexes = {
		@Index(name = "idx_tickets_game_id", columnList = "game_id"),
		@Index(name = "idx_tickets_user_id", columnList = "user_id")
	}
)
@NoArgsConstructor(access = PROTECTED)
public class TicketEntity extends ModificationTimestampEntity {

	@Column(nullable = false, unique = true)
	private String ticketNumber;

	@Column(nullable = false)
	private UUID orderItemId;

	@Column
	private UUID resaleTransactionId;

	@Column(nullable = false)
	private UUID gameId;

	@Column(nullable = false)
	private UUID userId;

	@Column
	private String userNickname;

	@Column
	private String userEmail;

	@Column
	private String userPhone;

	@Column
	private String gameTitle;

	@Column
	private LocalDateTime gameDate;

	@Column(nullable = false)
	private String seatInfo;

	@Column
	private Integer ticketPrice;

	@Column
	private Integer resalePrice;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TicketStatus ticketStatus;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ResaleEnabledStatus resaleEnabledStatus;

	@Column
	private LocalDateTime usedAt;

	@OneToOne(mappedBy = "ticket")
	private TicketFreezeEntity freeze;

	private TicketEntity(
		String ticketNumber,
		UUID orderItemId,
		UUID resaleTransactionId,
		UUID gameId,
		UUID userId,
		String userNickname,
		String userEmail,
		String userPhone,
		String gameTitle,
		LocalDateTime gameDate,
		String seatInfo,
		Integer ticketPrice,
		Integer resalePrice
	) {
		this.ticketNumber = ticketNumber;
		this.orderItemId = orderItemId;
		this.resaleTransactionId = resaleTransactionId;
		this.gameId = gameId;
		this.userId = userId;
		this.userNickname = userNickname;
		this.userEmail = userEmail;
		this.userPhone = userPhone;
		this.gameTitle = gameTitle;
		this.gameDate = gameDate;
		this.seatInfo = seatInfo;
		this.ticketPrice = ticketPrice;
		this.resalePrice = resalePrice;
		this.ticketStatus = TicketStatus.ISSUED;
		this.resaleEnabledStatus = ResaleEnabledStatus.DISABLED;
		this.usedAt = null;
	}

	public static TicketEntity create(
		String ticketNumber,
		UUID orderItemId,
		UUID resaleTransactionId,
		UUID gameId,
		UUID userId,
		String userNickname,
		String userEmail,
		String userPhone,
		String gameTitle,
		LocalDateTime gameDate,
		String seatInfo,
		Integer ticketPrice,
		Integer resalePrice
	) {
		validate(
			ticketNumber,
			orderItemId,
			gameId,
			userId,
			seatInfo,
			ticketPrice
		);
		return new TicketEntity(
			ticketNumber,
			orderItemId,
			resaleTransactionId,
			gameId,
			userId,
			userNickname,
			userEmail,
			userPhone,
			gameTitle,
			gameDate,
			seatInfo,
			ticketPrice,
			resalePrice
		);
	}

	private static void validate(
		String ticketNumber,
		UUID orderItemId,
		UUID gameId,
		UUID userId,
		String seatInfo,
		Integer ticketPrice
	) {
		Preconditions.domainValidate(
			StringUtils.hasText(ticketNumber),
			"티켓 번호는 비어 있을 수 없습니다."
		);
		Preconditions.domainValidate(
			orderItemId != null,
			"주문 상세 ID는 필수입니다."
		);
		Preconditions.domainValidate(
			gameId != null,
			"경기 ID는 필수입니다."
		);
		Preconditions.domainValidate(
			userId != null,
			"유저 ID는 필수입니다."
		);
		Preconditions.domainValidate(
			StringUtils.hasText(seatInfo),
			"좌석 정보는 비어 있을 수 없습니다."
		);
		Preconditions.domainValidate(
			ticketPrice != null && ticketPrice > 0,
			"티켓 가격은 0원보다 커야합니다."
		);
	}

	public void invalidate() {
		Preconditions.domainValidate(
			this.ticketStatus == TicketStatus.ISSUED || this.ticketStatus == TicketStatus.RESALE_ISSUED,
			"발행 완료 또는 리셀 발행 상태의 티켓만 무효화할 수 있습니다."
		);
		this.ticketStatus = TicketStatus.INVALID;
	}

	public void markAsResaleListing() {
		Preconditions.domainValidate(
			this.ticketStatus == TicketStatus.ISSUED,
			"발행 완료 상태의 티켓만 리셀 등록이 가능합니다."
		);
		this.ticketStatus = TicketStatus.RESALE_ISSUED;
	}

	public void restoreFromResale() {
		Preconditions.domainValidate(
			this.ticketStatus == TicketStatus.RESALE_ISSUED,
			"리셀 발행 상태의 티켓만 일반 상태로 복구할 수 있습니다."
		);
		this.ticketStatus = TicketStatus.ISSUED;
	}

	public void resaleEnable() {
		Preconditions.domainValidate(
			this.resaleEnabledStatus == ResaleEnabledStatus.DISABLED,
			"리셀 불가능 상태의 티켓만 가능 상태로 변경할 수 있습니다."
		);
		this.resaleEnabledStatus = ResaleEnabledStatus.ENABLED;
	}

	public boolean isFrozen() {
		return freeze != null && freeze.isActive();
	}
}
