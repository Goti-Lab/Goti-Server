package com.goti.user.domain.entity.user;

import com.goti.domain.base.ModificationTimestampEntity;

import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.util.StringUtils;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "accounts")
@NoArgsConstructor(access = PROTECTED)
public class AccountEntity extends ModificationTimestampEntity {

	@Column(nullable = false)
	private String accountNumber;

	@Column(nullable = false)
	private String bankName;

	@Column(nullable = false)
	private String accountHolder;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", unique = true, nullable = false)
	private MemberEntity member;

	private AccountEntity(
		String accountNumber,
		String bankName,
		String accountHolder,
		MemberEntity member
	) {
		this.accountNumber = accountNumber;
		this.bankName = bankName;
		this.accountHolder = accountHolder;
		this.member = member;
	}

	public static AccountEntity create(
		final String accountNumber,
		final String bankName,
		final String accountHolder,
		final MemberEntity member
	) {
		validate(accountNumber, bankName, accountHolder, member);
		return new AccountEntity(
			accountNumber, bankName, accountHolder, member
		);
	}

	private static void validate(
		String accountNumber,
		String bankName,
		String accountHolder,
		MemberEntity member
	) {
		Preconditions.domainValidate(
			StringUtils.hasText(accountNumber), "계좌번호는 비어 있을 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(bankName), "계좌 은행은 비어 있을 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(accountHolder), "계좌 예금주는 비어 있을 수 없습니다."
		);

		Preconditions.domainValidate(
			member != null, "회원 정보는 비어 있을 수 없습니다."
		);
	}

}
