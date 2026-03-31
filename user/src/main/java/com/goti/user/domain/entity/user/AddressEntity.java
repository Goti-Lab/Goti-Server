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

import static lombok.AccessLevel.*;

@Getter
@Entity
@Table(name = "addresses")
@NoArgsConstructor(access = PROTECTED)
public class AddressEntity extends ModificationTimestampEntity {

	@Column(nullable = false, length = 10)
	private String zipCode;

	@Column(nullable = false)
	private String baseAddress;

	@Column(nullable = false)
	private String detailAddress;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", unique = true, nullable = false)
	private MemberEntity member;

	public void updateDetails(
		final String zipCode,
		final String baseAddress,
		final String detailAddress
	) {
		validate(zipCode, baseAddress, detailAddress, member);
		this.zipCode = zipCode;
		this.baseAddress = baseAddress;
		this.detailAddress = detailAddress;
	}

	private AddressEntity(
		String zipCode, String baseAddress, String detailAddress, MemberEntity member
	) {
		this.zipCode = zipCode;
		this.baseAddress = baseAddress;
		this.detailAddress = detailAddress;
		this.member = member;
	}

	public static AddressEntity create(
		final String zipCode,
		final String baseAddress,
		final String detailAddress,
		final MemberEntity member
	) {
		validate(zipCode, baseAddress, detailAddress, member);
		return new AddressEntity(
			zipCode, baseAddress, detailAddress, member
		);
	}

	private static void validate(
		String zipCode, String baseAddress, String detailAddress, MemberEntity member
	) {
		Preconditions.domainValidate(
			StringUtils.hasText(zipCode),
			"우편번호는 비어있을 수 없습니다."
		);
		Preconditions.domainValidate(
			StringUtils.hasText(baseAddress),
			"기본 주소는 비어있을 수 없습니다."
		);
		Preconditions.domainValidate(
			StringUtils.hasText(detailAddress),
			"상세 주소는 비어있을 수 없습니다."
		);
		Preconditions.domainValidate(
			member != null,
			"회원 정보는 비어있을 수 없습니다."
		);
	}
}
