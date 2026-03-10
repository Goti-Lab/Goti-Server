package com.goti.domain.entity.order;

import static lombok.AccessLevel.*;

import org.springframework.util.StringUtils;

import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "orderers",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_orderers_order_id", columnNames = "order_id")
	}
)
@NoArgsConstructor(access = PROTECTED)
public class OrderHistoryEntity extends ModificationTimestampEntity {

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private OrderEntity order;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String mobile;

	@Column(nullable = false)
	private String email;

	private OrderHistoryEntity(
		OrderEntity order,
		String name,
		String mobile,
		String email
	) {
		this.order = order;
		this.name = name;
		this.mobile = mobile;
		this.email = email;
	}

	public static OrderHistoryEntity create(
		OrderEntity order,
		String name,
		String mobile,
		String email
	) {
		validate(name, mobile, email);
		return new OrderHistoryEntity(order, name, mobile, email);
	}

	private static void validate(
		String name,
		String mobile,
		String email
	) {
		Preconditions.domainValidate(
			StringUtils.hasText(name),
			"구매자 이름은 비어 있을 수 없습니다."
		);
		Preconditions.domainValidate(
			StringUtils.hasText(mobile),
			"구매자 연락처는 비어 있을 수 없습니다."
		);
		Preconditions.domainValidate(
			StringUtils.hasText(email),
			"구매자 이메일은 비어 있을 수 없습니다."
		);
	}
}
