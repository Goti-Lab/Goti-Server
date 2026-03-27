package com.goti.user.config.jwt;

import com.goti.user.config.properties.JwtProperties;

import com.goti.constants.OAuthProvider;
import com.goti.user.constants.TokenType;
import com.goti.user.constants.UserRole;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.user.security.ExtendedUserDetailsService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
	private final JwtProperties jwtProperties;
	private final ExtendedUserDetailsService userDetailsService;

	private static final String TOKEN_PREFIX = "Bearer ";
	private static final String ROLE_CLAIM_KEY = "role";
	private static final String MOBILE_CLAIM_KEY = "mobile";

	private static final String PROVIDER_TYPE_KEY = "provider_type";
	private static final String PROVIDER_ID_KEY = "provider_id";
	private static final String PROVIDER_EMAIL_KEY = "provider_email";
	static final String SOCIAL_VERIFY_SUBJECT = "social_verify";
	public static final String RSA_KEY_ID = "goti-jwt-key-1";

	// @PostConstruct에서 초기화 — 매 요청마다 PEM 파싱/파서 재생성 방지
	private RSAPrivateKey rsaPrivateKey;
	private RSAPublicKey rsaPublicKey;
	private JwtParser rsaParser;
	private JwtParser hmacParser;
	private boolean rsaEnabled;

	@PostConstruct
	void initKeys() {
		hmacParser = Jwts.parser()
			.verifyWith(jwtProperties.secretKey())
			.build();

		if (jwtProperties.hasRsaKeys()) {
			rsaPrivateKey = jwtProperties.rsaPrivateKeyParsed();
			rsaPublicKey = jwtProperties.rsaPublicKeyParsed();
			rsaParser = Jwts.parser()
				.verifyWith(rsaPublicKey)
				.build();
			rsaEnabled = true;
			log.info("JWT RS256 키 초기화 완료");
		} else {
			rsaEnabled = false;
			log.info("JWT HS512 단독 모드 (RSA 키 미설정)");
		}
	}

	public String create(UUID id, String mobile, UserRole role, TokenType tokenType) {
		Date issuedAt = new Date();
		Duration validTime = tokenType == TokenType.ACCESS ?
			jwtProperties.accessValidTime() : jwtProperties.refreshValidTime();
		Date expireAt = new Date(issuedAt.getTime() + validTime.toMillis());
		String jwtId = createJwtId();

		var builder = Jwts.builder()
			.subject(id.toString())
			.id(jwtId)
			.issuer(jwtProperties.issuer())
			.claim(ROLE_CLAIM_KEY, role.name())
			.claim(MOBILE_CLAIM_KEY, mobile)
			.issuedAt(issuedAt)
			.expiration(expireAt);

		if (rsaEnabled) {
			builder.header().keyId(RSA_KEY_ID).and()
				.signWith(rsaPrivateKey);
		} else {
			builder.signWith(jwtProperties.secretKey());
		}

		return builder.compact();
	}

	public String createSocialVerifyToken(OAuthProvider provider, String providerId, String email) {
		Date issuedAt = new Date();
		Date expireAt = new Date(issuedAt.getTime() + Duration.ofMinutes(10).toMillis());
		String jwtId = createJwtId();

		var builder = Jwts.builder()
			.subject(SOCIAL_VERIFY_SUBJECT)
			.id(jwtId)
			.issuer(jwtProperties.issuer())
			.claim(PROVIDER_EMAIL_KEY, email)
			.claim(PROVIDER_TYPE_KEY, provider)
			.claim(PROVIDER_ID_KEY, providerId)
			.issuedAt(issuedAt)
			.expiration(expireAt);

		if (rsaEnabled) {
			builder.header().keyId(RSA_KEY_ID).and()
				.signWith(rsaPrivateKey);
		} else {
			builder.signWith(jwtProperties.secretKey());
		}

		return builder.compact();
	}

	public void validateToken(String token) {
		Jws<Claims> claims = parseClaimsDualVerify(token);
		log.debug("ExpiredAt :: {}", claims.getPayload().getExpiration());
	}

	public Claims getSocialVerifyClaims(String token) {
		try {
			Claims claims = parseClaimsDualVerify(token).getPayload();
			if (!SOCIAL_VERIFY_SUBJECT.equals(claims.getSubject())) {
				throw new CustomException(ErrorCode.AUTH_INVALID);
			}
			return claims;
		} catch (ExpiredJwtException e) {
			throw new CustomException(ErrorCode.AUTH_SIGNUP_EXPIRED);
		}
	}

	public String resolve(HttpServletRequest request) {
		String token = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (token != null && token.startsWith(TOKEN_PREFIX)) {
			return token.substring(TOKEN_PREFIX.length());
		}
		return token;
	}

	public Authentication getAuthentication(String token) {
		String userId = getClaims(token).getSubject();
		UserDetails userDetails = userDetailsService.loadUserById(userId);
		return UsernamePasswordAuthenticationToken.authenticated(
			userDetails,
			null,
			userDetails.getAuthorities()
		);
	}

	public long getExpiry(String token) {
		try {
			Date expiration = getClaims(token).getExpiration();
			long now = new Date().getTime();
			long diff = expiration.getTime() - now;

			return Math.max(0, diff);
		} catch (Exception e) {
			return 0;
		}
	}

	public String extractJti(String token) {
		return getClaims(token).getId();
	}

	/**
	 * JWKS 엔드포인트용 RSA public key 반환.
	 */
	public RSAPublicKey getRsaPublicKey() {
		return rsaPublicKey;
	}

	public String extractSubject(String token) {
		return getClaims(token).getSubject();
	}

	private Claims getClaims(String token) {
		return parseClaimsDualVerify(token).getPayload();
	}

	/**
	 * RS256 우선 검증, 서명/알고리즘 불일치 시에만 HS512 fallback (전환기 호환).
	 * ExpiredJwtException 등 서명 외 오류는 fallback 없이 즉시 throw.
	 * RSA 키가 설정되지 않은 경우 HS512만 사용.
	 *
	 * <p>TODO: RS256 전환 완료 후 (기존 HS512 토큰 만료 이후) fallback 제거 예정.
	 * access token TTL 기준 전환 후 최소 1시간 경과 시 안전하게 제거 가능.</p>
	 */
	private Jws<Claims> parseClaimsDualVerify(String token) {
		if (rsaEnabled) {
			try {
				return rsaParser.parseSignedClaims(token);
			} catch (SignatureException | UnsupportedJwtException e) {
				// SignatureException: RS256 서명 불일치
				// UnsupportedJwtException: HMAC 토큰이 RSA 파서에 진입 (alg 헤더 불일치)
				// 두 경우 모두 전환기 HS512 fallback 대상
				log.warn("RS256 검증 실패({}), HS512 fallback 시도", e.getClass().getSimpleName());
				return hmacParser.parseSignedClaims(token);
			}
		}
		return hmacParser.parseSignedClaims(token);
	}

	private String createJwtId() {
		return UUID.randomUUID().toString();
	}
}
