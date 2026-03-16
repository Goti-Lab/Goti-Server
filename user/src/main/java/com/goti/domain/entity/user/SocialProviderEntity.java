package com.goti.domain.entity.user;

import com.goti.global.validation.Preconditions;
import com.goti.constants.OAuthProvider;
import com.goti.domain.base.ModificationTimestampEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.util.StringUtils;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "social_providers")
@NoArgsConstructor(access = PROTECTED)
public class SocialProviderEntity extends ModificationTimestampEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private MemberEntity member;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OAuthProvider provider;

	@Column(nullable = false)
	private String providerId;

	@Column(nullable = false)
	private String email;

	private SocialProviderEntity(
		MemberEntity member,
		OAuthProvider provider,
		String providerId,
		String email
	) {
		this.member = member;
		this.provider = provider;
		this.providerId = providerId;
		this.email = email;
	}

	public static SocialProviderEntity create(
		final MemberEntity member,
		final OAuthProvider provider,
		final String providerId,
		final String email
	) {
		validate(provider, providerId, email);
		return new SocialProviderEntity(member, provider, providerId, email);
	}

	private static void validate(
		OAuthProvider provider, String providerId, String email
	) {
		Preconditions.domainValidate(
			provider != null,
			"소셜 제공자(kakao, naver, google)는 비어 있을 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(providerId), "사용자 소셜 고유 ID 값은 비어있을 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(email), "이메일값은 비어있을 수 없습니다."
		);
	}
}
