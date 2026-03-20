package com.goti.resale.domain.entity.resale;

import static lombok.AccessLevel.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.global.validation.Preconditions;
import com.goti.resale.constants.ResaleTransactionStatus;

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
@Table(name = "resale_transactions",
	indexes = {
		@Index(name = "unique_idx_resale_ticket_number", columnList = "resale_ticket_number", unique = true),
		@Index(name = "idx_listing_id", columnList = "listing_id"),
		@Index(name = "idx_resale_order_id", columnList = "resale_order_id"),
		@Index(name = "idx_buyer_id", columnList = "buyer_id"),
		@Index(name = "idx_seller_id", columnList = "seller_id")
	})
@NoArgsConstructor(access = PROTECTED)
public class ResaleTransactionEntity extends ModificationTimestampEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "resale_order_id", nullable = false)
	private ResaleOrderEntity resaleOrder;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "listing_id", nullable = false)
	private ResaleListingEntity listing;

	@Column(nullable = false)
	private String resaleTicketNumber;

	@Column(nullable = false)
	private UUID buyerId;

	@Column(nullable = false)
	private UUID sellerId;

	@Column(nullable = false)
	private Integer transactionPrice;

	@Column(nullable = false)
	private Integer buyerFee;

	@Column(nullable = false)
	private Integer sellerFee;

	@Column(nullable = false)
	private Integer buyerTotal;

	@Column(nullable = false)
	private Integer sellerTotal;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ResaleTransactionStatus transactionStatus;

	private LocalDateTime confirmedAt;

	private UUID escrowId;

	private ResaleTransactionEntity(
		ResaleOrderEntity resaleOrder,
		ResaleListingEntity listing,
		String resaleTicketNumber,
		UUID buyerId,
		UUID sellerId,
		Integer transactionPrice,
		Integer buyerFee,
		Integer sellerFee,
		Integer buyerTotal,
		Integer sellerTotal
	) {
		this.resaleOrder = resaleOrder;
		this.listing = listing;
		this.resaleTicketNumber = resaleTicketNumber;
		this.buyerId = buyerId;
		this.sellerId = sellerId;
		this.transactionPrice = transactionPrice;
		this.buyerFee = buyerFee;
		this.sellerFee = sellerFee;
		this.buyerTotal = buyerTotal;
		this.sellerTotal = sellerTotal;
		this.transactionStatus = ResaleTransactionStatus.PENDING;
		this.confirmedAt = null;
		this.escrowId = null;
	}

	public static ResaleTransactionEntity create(
		ResaleOrderEntity resaleOrder,
		ResaleListingEntity listing,
		String resaleTicketNumber,
		UUID buyerId,
		UUID sellerId,
		Integer transactionPrice,
		Integer buyerFee,
		Integer sellerFee,
		Integer buyerTotal,
		Integer sellerTotal
	) {
		validate(
			resaleTicketNumber,
			buyerId, sellerId,
			transactionPrice,
			buyerFee, sellerFee,
			buyerTotal, sellerTotal
		);

		return new ResaleTransactionEntity(
			resaleOrder,
			listing,
			resaleTicketNumber,
			buyerId,
			sellerId,
			transactionPrice,
			buyerFee,
			sellerFee,
			buyerTotal,
			sellerTotal
		);
	}

	private static void validate(
		String resaleTicketNumber,
		UUID buyerId,
		UUID sellerId,
		Integer transactionPrice,
		Integer buyerFee,
		Integer sellerFee,
		Integer buyerTotal,
		Integer sellerTotal
	) {
		Preconditions.domainValidate(resaleTicketNumber != null, "리셀 티켓 번호는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(buyerId != null, "구매자 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(sellerId != null, "판매자 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(!buyerId.equals(sellerId), "구매자와 판매자는 같을 수 없습니다.");
		Preconditions.domainValidate(transactionPrice != null && transactionPrice >= 0, "거래 가격은 0 이상이어야 합니다.");
		Preconditions.domainValidate(buyerFee != null && buyerFee >= 0, "구매자 수수료는 0 이상이어야 합니다.");
		Preconditions.domainValidate(sellerFee != null && sellerFee >= 0, "판매자 수수료는 0 이상이어야 합니다.");
		Preconditions.domainValidate(buyerTotal != null && buyerTotal >= 0, "구매자 총액은 0 이상이어야 합니다.");
		Preconditions.domainValidate(sellerTotal != null && sellerTotal >= 0, "판매자 총액은 0 이상이어야 합니다.");
		Preconditions.domainValidate(buyerTotal.equals(transactionPrice + buyerFee), "구매자 총액이 올바르지 않습니다.");
		Preconditions.domainValidate(sellerTotal.equals(transactionPrice - sellerFee), "판매자 총액이 올바르지 않습니다.");
	}

	public void complete(UUID escrowId) {
		Preconditions.domainValidate(
			this.transactionStatus == ResaleTransactionStatus.PENDING,
			"결제 대기 상태에서만 할 수 있습니다."
		);
		Preconditions.domainValidate(escrowId != null && !escrowId.toString().isBlank(), "에스크로 ID는 비어 있을 수 없습니다."
		);

		this.transactionStatus = ResaleTransactionStatus.COMPLETED;
		this.confirmedAt = LocalDateTime.now();
		this.escrowId = escrowId;
	}

}
