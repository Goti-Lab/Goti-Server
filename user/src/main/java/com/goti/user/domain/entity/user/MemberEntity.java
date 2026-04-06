package com.goti.user.domain.entity.user;

import static lombok.AccessLevel.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.springframework.util.StringUtils;

import com.goti.global.validation.Preconditions;
import com.goti.constants.Gender;
import com.goti.user.constants.UserRole;

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

	@OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
	private List<SocialProviderEntity> socialProviders = new ArrayList<>();

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

	public boolean verifyIdentity(Gender gender, LocalDate birthDate) {
		if (gender == null || birthDate == null) return false;
		return this.getGender() == gender && this.getBirthDate().equals(birthDate);
	}

	public void updateIdentity(
		final String mobile,
		final String name
	) {
		validateIdentityInput(mobile, name);
		updateName(name);
		updateMobile(mobile);
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

	private static void validateIdentityInput(String mobile, String name) {
		Preconditions.domainValidate(
			StringUtils.hasText(mobile), "회원 휴대전화 번호는 비어 있을 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(name), "회원 이름은 비어 있을 수 없습니다."
		);
	}
}

