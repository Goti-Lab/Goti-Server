package com.goti.resale.domain.entity.resale;

import static lombok.AccessLevel.*;

import java.util.UUID;

import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.global.validation.Preconditions;
import com.goti.resale.constants.ResaleOrderStatus;

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
@Table(name = "resale_orders",
	indexes = {
		@Index(name = "idx_resale_order_number", columnList = "order_number", unique = true),
		@Index(name = "idx_resale_order_buyer_id", columnList = "buyer_id")
	})
@NoArgsConstructor(access = PROTECTED)
public class ResaleOrderEntity extends ModificationTimestampEntity {

	@Column(nullable = false)
	private String orderNumber;

	@Column(nullable = false)
	private UUID buyerId;

	@Column(nullable = false)
	private Integer totalAmount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ResaleOrderStatus orderStatus;

	private ResaleOrderEntity(
		String orderNumber,
		UUID buyerId,
		Integer totalAmount
	) {
		this.orderNumber = orderNumber;
		this.buyerId = buyerId;
		this.totalAmount = totalAmount;
		this.orderStatus = ResaleOrderStatus.PENDING;
	}

	public static ResaleOrderEntity create(
		String orderNumber,
		UUID buyerId,
		Integer totalAmount
	) {
		Preconditions.domainValidate(orderNumber != null, "주문 번호는 필수입니다.");
		Preconditions.domainValidate(buyerId != null, "구매자 ID는 필수입니다.");
		Preconditions.domainValidate(totalAmount != null && totalAmount >= 0, "총 금액은 0 이상이어야 합니다.");
		return new ResaleOrderEntity(orderNumber, buyerId, totalAmount);
	}

	public void complete() {
		this.orderStatus = ResaleOrderStatus.COMPLETED;
	}
}
