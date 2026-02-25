package com.goti.domain.entity.user;

import static lombok.AccessLevel.*;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.goti.constants.Gender;
import com.goti.constants.UserRole;
import com.goti.constants.UserStatus;
import com.goti.domain.base.ModificationTimestampEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_users_mobile", columnNames = {"mobile"}),
		@UniqueConstraint(name = "uk_users_email", columnNames = {"email"})
	})
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor(access = PROTECTED)
public class UserEntity extends ModificationTimestampEntity {

	@Column(nullable = false)
	private String mobile;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Gender gender;

	@Column(nullable = false)
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate birthDate;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private UserStatus status;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private UserRole role;

	protected UserEntity(
		String mobile,
		String name,
		Gender gender,
		LocalDate birthDate,
		UserRole role
	) {
		this.mobile = mobile;
		this.name = name;
		this.gender = gender;
		this.birthDate = birthDate;
		this.status = UserStatus.ACTIVATED;
		this.role = role;
	}
}
