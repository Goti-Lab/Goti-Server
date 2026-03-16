package com.goti.domain.entity.resale;

import static lombok.AccessLevel.*;

import java.util.UUID;

import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "seller_accounts")
@NoArgsConstructor(access = PROTECTED)
public class SellerAccountEntity extends ModificationTimestampEntity {
	@Column(nullable = false)
	private UUID sellerId;

	@Column(nullable = false)
	private String bankName;

	@Column(nullable = false)
	private String accountNumber;

	@Column(nullable = false)
	private String accountHolder;

	private SellerAccountEntity(
		UUID sellerId,
		String bankName,
		String accountNumber,
		String accountHolder
	) {
		this.sellerId = sellerId;
		this.bankName = bankName;
		this.accountNumber = accountNumber;
		this.accountHolder = accountHolder;
	}

	public static SellerAccountEntity create(
		UUID sellerId,
		String bankName,
		String accountNumber,
		String accountHolder
	) {
		validate(sellerId, bankName, accountNumber, accountHolder);

		return new SellerAccountEntity(
			sellerId,
			bankName,
			accountNumber,
			accountHolder
		);
	}

	private static void validate(
		UUID userId,
		String bankName,
		String accountNumber,
		String accountHolder
	) {
		Preconditions.domainValidate(userId != null, "유저 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(bankName != null && !bankName.isBlank(), "은행 이름은 비어 있을 수 없습니다.");
		Preconditions.domainValidate(accountNumber != null && !accountNumber.isBlank(), "계좌 번호는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(accountHolder != null && !accountHolder.isBlank(), "예금주 성함은 비어 있을 수 없습니다.");
	}

}
