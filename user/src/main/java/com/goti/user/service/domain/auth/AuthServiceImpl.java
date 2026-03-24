package com.goti.user.service.domain.auth;

import com.goti.global.validation.Preconditions;
import com.goti.user.config.jwt.JwtTokenProvider;
import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.infra.cache.RedisCache;

import com.goti.infra.constants.redis.RedisKey;
import com.goti.infra.sms.SmsProvider;

import com.goti.user.constants.TokenType;
import com.goti.user.domain.entity.user.MemberEntity;
import org.springframework.data.util.Pair;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final JwtTokenProvider jwtTokenProvider;
	private final SmsProvider smsProvider;
	private final RedisCache redisCache;

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	@Override
	public void sendSmsCode(String socialVerifyToken, String mobile) {
		jwtTokenProvider.getSocialVerifyClaims(socialVerifyToken);
		String authCode = createAuthCode();
		smsProvider.send(mobile, authCode);
		saveAuthCode(mobile, authCode);
	}

	@Override
	public void verifySmsCode(String mobile, String authCode) {
		String redisKey = RedisKey.SMS_AUTH_CODE.getKey(mobile);

		String cachedAuthCode = getCachedAuthCode(redisKey);

		if (!cachedAuthCode.equals(authCode))
			throw new CustomException(ErrorCode.AUTH_CODE_INVALID);

		if (!redisCache.consume(redisKey))
			throw new CustomException(ErrorCode.AUTH_CODE_NOT_FOUND);
	}

	@Override
	public UUID validateTokenAndGetMemberId(String token) {
		UUID memberId = parseMemberId(token);
		validateJti(token, memberId);
		return memberId;
	}

	@Override
	public Pair<String, String> issueTokens(MemberEntity member) {
		deleteCachedTokenId(member.getId());
		String accessToken = createToken(member, TokenType.ACCESS);
		String refreshToken = createToken(member, TokenType.REFRESH);
		saveTokenJti(member.getId(), refreshToken);
		return Pair.of(accessToken, refreshToken);
	}

	private void saveTokenJti(UUID memberId, String token) {
		String jti = jwtTokenProvider.extractJti(token);
		redisCache.set(RedisKey.REFRESH_TOKEN, memberId, jti);
	}

	private String createToken(MemberEntity member, TokenType tokenType) {
		return jwtTokenProvider.create(
			member.getId(),
			member.getMobile(),
			member.getRole(),
			tokenType
		);
	}

	private UUID parseMemberId(String token) {
		jwtTokenProvider.validateToken(token);
		return UUID.fromString(jwtTokenProvider.extractSubject(token));
	}

	private String createAuthCode() {
		int code = SECURE_RANDOM.nextInt(900000) + 100000;
		return String.valueOf(code);
	}

	private void saveAuthCode(String mobile, String authCode) {
		redisCache.set(RedisKey.SMS_AUTH_CODE, mobile, authCode);
	}

	private String getCachedAuthCode(String key) {
		String cachedCode = redisCache.get(
			key, String.class
		);
		if (cachedCode == null)
			throw new CustomException(ErrorCode.AUTH_CODE_NOT_FOUND);
		return cachedCode;
	}

	private void validateJti(String token, UUID memberId) {
		String cachedJti = redisCache.get(
			RedisKey.REFRESH_TOKEN.getKey(memberId),
			String.class
		);

		Preconditions.validate(
			StringUtils.hasText(cachedJti),
			ErrorCode.AUTH_REFRESH_EXPIRED
		);

		String requestedJti = jwtTokenProvider.extractJti(token);

		Preconditions.validate(
			cachedJti.equals(requestedJti),
			ErrorCode.AUTH_INVALID
		);
	}

	private void deleteCachedTokenId(UUID memberId) {
		redisCache.delete(
			RedisKey.REFRESH_TOKEN.getKey(memberId)
		);
	}

}
