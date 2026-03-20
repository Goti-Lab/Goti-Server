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
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

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

	static final String ISSUER = "goti-user-service";

	public String create(UUID id, String mobile, UserRole role, TokenType tokenType) {
		Date issuedAt = new Date();
		Duration validTime = tokenType == TokenType.ACCESS ?
			jwtProperties.accessValidTime() : jwtProperties.refreshValidTime();
		Date expireAt = new Date(issuedAt.getTime() + validTime.toMillis());
		String jwtId = createJwtId();

		var builder = Jwts.builder()
			.subject(id.toString())
			.id(jwtId)
			.issuer(ISSUER)
			.claim(ROLE_CLAIM_KEY, role.name())
			.claim(MOBILE_CLAIM_KEY, mobile)
			.issuedAt(issuedAt)
			.expiration(expireAt);

		if (jwtProperties.hasRsaKeys()) {
			builder.signWith(jwtProperties.rsaPrivateKeyParsed());
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
			.issuer(ISSUER)
			.claim(PROVIDER_EMAIL_KEY, email)
			.claim(PROVIDER_TYPE_KEY, provider)
			.claim(PROVIDER_ID_KEY, providerId)
			.issuedAt(issuedAt)
			.expiration(expireAt);

		if (jwtProperties.hasRsaKeys()) {
			builder.signWith(jwtProperties.rsaPrivateKeyParsed());
		} else {
			builder.signWith(jwtProperties.secretKey());
		}

		return builder.compact();
	}

	public void validateToken(String token) throws JwtException {
		Jws<Claims> claims = parseClaimsDualVerify(token);
		log.info("ExpiredAt :: {}", claims.getPayload().getExpiration());
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

	public String extractJti(String token) {
		return getClaims(token).getId();
	}

	/**
	 * JWKS 엔드포인트용 RSA public key 반환.
	 */
	public RSAPublicKey getRsaPublicKey() {
		if (!jwtProperties.hasRsaKeys()) {
			return null;
		}
		return jwtProperties.rsaPublicKeyParsed();
	}

	private Claims getClaims(String token) {
		return parseClaimsDualVerify(token).getPayload();
	}

	/**
	 * RS256 우선 검증, 실패 시 HS512 fallback (전환기 호환).
	 * RSA 키가 설정되지 않은 경우 HS512만 사용.
	 */
	private Jws<Claims> parseClaimsDualVerify(String token) {
		if (jwtProperties.hasRsaKeys()) {
			try {
				return Jwts.parser()
					.verifyWith(jwtProperties.rsaPublicKeyParsed())
					.build()
					.parseSignedClaims(token);
			} catch (JwtException e) {
				// RS256 실패 → HS512 fallback (기존 토큰 호환)
				log.debug("RS256 검증 실패, HS512 fallback 시도");
				return Jwts.parser()
					.verifyWith(jwtProperties.secretKey())
					.build()
					.parseSignedClaims(token);
			}
		}
		return Jwts.parser()
			.verifyWith(jwtProperties.secretKey())
			.build()
			.parseSignedClaims(token);
	}

	private String createJwtId() {
		return UUID.randomUUID().toString();
	}
}
