package com.goti.user.service.application.auth;

import com.goti.user.config.jwt.JwtTokenProvider;
import com.goti.constants.Gender;
import com.goti.constants.OAuthProvider;
import com.goti.constants.messages.ErrorCode;
import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.domain.entity.user.SocialProviderEntity;
import com.goti.user.dto.response.SocialVerifyResponse;
import com.goti.exception.CustomException;
import com.goti.infra.api.client.SocialApiClient;
import com.goti.infra.api.client.SocialClientProvider;
import com.goti.infra.api.dto.response.common.SocialStateResponse;
import com.goti.infra.api.dto.response.common.SocialUserInfoResponse;
import com.goti.infra.cache.RedisCache;
import com.goti.infra.constants.redis.RedisKey;

import com.goti.user.service.domain.auth.AuthService;
import com.goti.user.service.domain.user.MemberService;
import com.goti.user.service.domain.user.SocialProviderService;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialAuthService {

	private final SocialClientProvider socialClientProvider;
	private final SocialProviderService socialProviderService;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedisCache redisCache;
	private final MemberService memberService;
	private final AuthService authService;

	private static final String KEY_SEPARATOR = ":";
	private static final String PROVIDER_ID_KEY = "provider_id";
	private static final String PROVIDER_TYPE_KEY = "provider_type";
	private static final String PROVIDER_EMAIL_KEY = "provider_email";

	public SocialStateResponse issueState(OAuthProvider provider) {
		if (provider == OAuthProvider.KAKAO) {
			throw new CustomException(ErrorCode.BAD_REQUEST);
		}
		String state = UUID.randomUUID().toString();
		String keyParam = provider + KEY_SEPARATOR + state;

		redisCache.set(RedisKey.OAUTH_STATE, keyParam, true);

		return SocialStateResponse.of(state);
	}

	public SocialVerifyResponse verify(OAuthProvider provider, String authCode, String state) {
		validateState(provider, state);
		SocialApiClient apiClient = socialClientProvider.getClient(provider);
		String socialAccessToken = apiClient.getAccessToken(authCode, state);
		SocialUserInfoResponse socialUserInfo = apiClient.getSocialUserInfo(socialAccessToken);
		String email = socialUserInfo.email();
		String providerId = socialUserInfo.providerId();
		boolean isRegistered = socialProviderService.findSocialProvider(
			providerId, provider
		).isPresent();

		String socialVerifyToken = jwtTokenProvider.createSocialVerifyToken(
			provider, providerId, email
		);
		return SocialVerifyResponse.of(isRegistered, socialVerifyToken);
	}

	@Transactional
	public Pair<String, String> login(String socialVerifyToken) {
		SocialInfo verifiedSocialInfo = getSocialInfo(socialVerifyToken);
		SocialProviderEntity socialProvider = socialProviderService.getSocialProvider(
			verifiedSocialInfo.providerId(), verifiedSocialInfo.provider()
		);
		MemberEntity member = socialProvider.getMember();
		return authService.issueTokens(member, verifiedSocialInfo.providerId());
	}

	@Transactional
	public Pair<String, String> signup(
		String socialVerifyToken,
		String name,
		String mobile,
		Gender gender,
		LocalDate birthDate,
		String authCode
	) {
		SocialInfo verifiedSocialInfo = getSocialInfo(socialVerifyToken);

		authService.verifySmsCode(mobile, authCode);
		MemberEntity member = getOrCreateMember(name, mobile, gender, birthDate);

		createSocialProvider(member, verifiedSocialInfo);
		return authService.issueTokens(member, verifiedSocialInfo.providerId());
	}

	public void sendSignupSmsCode(String socialVerifyToken, String mobile) {
		authService.sendSmsCode(socialVerifyToken, mobile);
	}

	@Transactional
	public Pair<String, String> reissueToken(String refreshToken) {
		UUID memberId = authService.validateTokenAndGetMemberId(refreshToken);
		MemberEntity member = memberService.getMember(memberId);
		String providerId = jwtTokenProvider.extractProviderId(refreshToken);
		return authService.issueTokens(member, providerId);
	}

	private void validateState(OAuthProvider provider, String state) {
		if (provider == OAuthProvider.KAKAO) return;

		if (state == null || state.isBlank()) {
			throw new CustomException(ErrorCode.MISSING_PARAMETER, "state");
		}
		String keyParam = provider + KEY_SEPARATOR + state;
		String stateKey = RedisKey.OAUTH_STATE.getKey(keyParam);
		if (!redisCache.consume(stateKey)) {
			throw new CustomException(ErrorCode.INVALID_STATE);
		}
	}

	private record SocialInfo(String providerId, OAuthProvider provider, String email) {}

	private SocialInfo getSocialInfo(String socialVerifyToken) {
		Claims claims = jwtTokenProvider.getSocialVerifyClaims(socialVerifyToken);

		String providerId = claims.get(PROVIDER_ID_KEY, String.class);

		OAuthProvider provider = OAuthProvider.valueOf(
			claims.get(PROVIDER_TYPE_KEY, String.class)
		);
		String email = claims.get(PROVIDER_EMAIL_KEY, String.class);

		return new SocialInfo(providerId, provider, email);
	}

	private MemberEntity getOrCreateMember(
		String name, String mobile, Gender gender, LocalDate birthDate
	) {
		return memberService.findByMobile(mobile)
			.orElseGet(() -> {
				try {
					return memberService.save(name, mobile, gender, birthDate);
				} catch (DataIntegrityViolationException e) {
					return memberService.findByMobile(mobile).orElseThrow(
						() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)
					);
				}
			});
	}

	private void createSocialProvider(
		MemberEntity member, SocialInfo socialInfo
	) {
		socialProviderService.findSocialProvider(
			socialInfo.providerId(),
			socialInfo.provider
		).ifPresentOrElse(
			existingProvider -> {
				if (!existingProvider.getMember().getId().equals(member.getId())) {
					log.error(
						"소셜 계정 연동 충돌 발생: 소셜ID: {}, 기존회원ID: {}, 신규요청회원ID: {}",
						socialInfo.providerId(), existingProvider.getMember().getId(), member.getId()
					);
					throw new CustomException(ErrorCode.SOCIAL_PROVIDER_ALREADY_LINKED);
				}
			},
			() -> {
				try {
					socialProviderService.save(
						member, socialInfo.provider(), socialInfo.providerId(), socialInfo.email()
					);
				} catch(DataIntegrityViolationException e) {
					log.warn(
						"소셜 정보 중복 생성 시도 skip - 소셜ID: {}",
						socialInfo.providerId()
					);
				}
			}
		);
	}


}
