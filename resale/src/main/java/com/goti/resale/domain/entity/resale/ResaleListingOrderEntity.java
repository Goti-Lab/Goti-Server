package com.goti.resale.domain.entity.resale;

import static lombok.AccessLevel.*;

import java.util.UUID;

import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.global.validation.Preconditions;
import com.goti.resale.constants.ResaleListingOrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "resale_listing_orders",
	indexes = {
		@Index(name = "idx_listing_order_seller_id", columnList = "sellerId"),
		@Index(name = "idx_listing_order_grade_id", columnList = "gradeId"),
		@Index(name = "idx_listing_order_number", columnList = "orderNumber", unique = true)
	})
@NoArgsConstructor(access = PROTECTED)
public class ResaleListingOrderEntity extends ModificationTimestampEntity {

	@Column(nullable = false)
	private String orderNumber;

	@Column(nullable = false)
	private UUID sellerId;

	@Column(nullable = false)
	private UUID gradeId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ResaleListingOrderStatus orderStatus;

	private ResaleListingOrderEntity(String orderNumber, UUID sellerId, UUID gradeId) {
		this.orderNumber = orderNumber;
		this.sellerId = sellerId;
		this.gradeId = gradeId;
		this.orderStatus = ResaleListingOrderStatus.LISTING;
	}

	public static ResaleListingOrderEntity create(
		String orderNumber,
		UUID sellerId,
		UUID gradeId
	) {
		Preconditions.domainValidate(orderNumber != null, "주문 번호는 필수입니다.");
		Preconditions.domainValidate(sellerId != null, "판매자 ID는 필수입니다.");
		Preconditions.domainValidate(gradeId != null, "등급 ID는 필수입니다.");

		return new ResaleListingOrderEntity(orderNumber, sellerId, gradeId);
	}

	public void soldOut() {
		if (this.orderStatus == ResaleListingOrderStatus.PARTIAL
			|| this.orderStatus == ResaleListingOrderStatus.LISTING)
			this.orderStatus = ResaleListingOrderStatus.SOLD;
	}

	public void partial() {
		if (this.orderStatus == ResaleListingOrderStatus.LISTING
			|| this.orderStatus == ResaleListingOrderStatus.SOLD)
			this.orderStatus = ResaleListingOrderStatus.PARTIAL;
	}

	public void cancel() {
		this.orderStatus = ResaleListingOrderStatus.CANCELED;
	}

	public void settled() {
		if (this.orderStatus == ResaleListingOrderStatus.SOLD
			|| this.orderStatus == ResaleListingOrderStatus.PARTIAL)
			this.orderStatus = ResaleListingOrderStatus.SETTLED;
	}
}
