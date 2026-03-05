package com.goti.service.auth.application;

import com.goti.config.jwt.JwtTokenProvider;
import com.goti.constants.Gender;
import com.goti.constants.OAuthProvider;
import com.goti.domain.entity.user.MemberEntity;
import com.goti.domain.entity.user.SocialProviderEntity;
import com.goti.dto.response.LoginResponse;
import com.goti.repository.MemberRepository;

import com.goti.service.domain.user.MemberService;

import com.goti.service.domain.user.SocialProviderService;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SignupService {

	private final MemberRepository memberRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final MemberService memberService;
	private final SocialProviderService socialProviderService;

	static final String PROVIDER_ID_KEY = "provider_id";
	static final String REGISTRATION_SUBJECT = "registration";

	public LoginResponse signup(
		String registrationToken,
		String email,
		String name,
		String mobile,
		Gender gender,
		LocalDate birthDate
	) {
		Claims claims = jwtTokenProvider.getSocialVerifyClaims(registrationToken);
		String providerId = claims.get(PROVIDER_ID_KEY, String.class);
		OAuthProvider provider = OAuthProvider.valueOf(claims.get(REGISTRATION_SUBJECT, String.class));

		MemberEntity member = memberRepository.findByMobile(mobile).orElseGet(
			() -> createMember(name, mobile, gender, birthDate)
		);

		socialProviderService.findByProviderIdAndProvider(providerId, provider)
			.orElseGet(
				() -> createSocialProvider(member, provider, providerId, email)
			);

		String accessToken = jwtTokenProvider.create(
			member.getId(), member.getMobile(), member.getRole()
		);
		return LoginResponse.authenticated(accessToken);
	}

	private MemberEntity createMember(String name, String mobile, Gender gender, LocalDate birthDate) {
		return memberService.save(name, mobile, gender, birthDate);
	}

	private SocialProviderEntity createSocialProvider(
		MemberEntity member, OAuthProvider provider, String providerId, String email
	) {
		return socialProviderService.save(member, provider, providerId, email);
	}


}
