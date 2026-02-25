package com.goti.domain.entity.user;

import static lombok.AccessLevel.*;

import java.time.LocalDate;

import jakarta.persistence.Table;

import org.springframework.util.StringUtils;

import com.goti.global.validation.Preconditions;
import com.goti.constants.Gender;
import com.goti.constants.UserRole;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = PROTECTED)
@DiscriminatorValue("MEMBER")
public class MemberEntity extends UserEntity {
	private MemberEntity(
		String mobile,
		String name,
		Gender gender,
		LocalDate birthDate
	) {
		super(
			mobile, name, gender, birthDate, UserRole.MEMBER
		);
	}

	public static MemberEntity create(
		final String mobile,
		final String name,
		final Gender gender,
		final LocalDate birthDate
	) {
		validate(mobile, name, gender, birthDate);
		return new MemberEntity(mobile, name, gender, birthDate);
	}

	private static void validate(
		String mobile,
		String name,
		Gender gender,
		LocalDate birthDate
	) {

		Preconditions.domainValidate(
			StringUtils.hasText(mobile), "회원 휴대전화 번호는 비어 있을 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(name), "회원 이름은 비어 있을 수 없습니다."
		);

		Preconditions.domainValidate(
			gender != null, "회원 성별은 비어 있을 수 없습니다."
		);

		Preconditions.domainValidate(
			birthDate != null, "회원 생년월일은 비어 있을 수 없습니다."
		);

		Preconditions.domainValidate(
			birthDate.isBefore(LocalDate.now()), "회원 생년월일은 과거 날짜여야 합니다."
		);
	}
}

