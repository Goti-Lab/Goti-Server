package com.goti.config.jwt;

import com.goti.config.properties.JwtProperties;

import com.goti.constants.OAuthProvider;
import com.goti.constants.UserRole;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.security.ExtendedUserDetailsService;

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
	static final String REGISTRATION_SUBJECT = "registration";

	public String create(UUID id, String mobile, UserRole role) {
		Date issuedAt = new Date();
		Date expireAt = new Date(issuedAt.getTime() + jwtProperties.accessValidTime().toMillis());
		String jwtId = getJwtId();
		return Jwts.builder()
			.subject(id.toString())
			.id(jwtId)
			.claim(ROLE_CLAIM_KEY, role.name())
			.claim(MOBILE_CLAIM_KEY, mobile)
			.issuedAt(issuedAt)
			.expiration(expireAt)
			.signWith(jwtProperties.secretKey())
			.compact();
	}

	// todo: Duration.ofMinutes(10) 하드코딩 기입 부분 -> user module application.yml 파일 읽지 못하는 부분 수정 예정
	// todo: 추가로 create method 도 같은 error 날것으로 예상됨
	public String createRegistrationToken(OAuthProvider provider, String providerId) {
		Date issuedAt = new Date();
		Date expireAt = new Date(issuedAt.getTime() + Duration.ofMinutes(10).toMillis());
		String jwtId = getJwtId();
		return Jwts.builder()
			.subject(REGISTRATION_SUBJECT)
			.id(jwtId)
			.claim(PROVIDER_TYPE_KEY, provider)
			.claim(PROVIDER_ID_KEY, providerId)
			.issuedAt(issuedAt)
			.expiration(expireAt)
			.signWith(jwtProperties.secretKey())
			.compact();
	}

	public void validateToken(String token) throws JwtException {
		Jws<Claims> claims = Jwts.parser()
			.verifyWith(jwtProperties.secretKey())
			.build().parseSignedClaims(token);
		log.info("ExpiredAt :: {}", claims.getPayload().getExpiration());
	}

	public Claims getRegistrationClaims(String token) {
		try {
			Claims claims = Jwts.parser()
				.verifyWith(jwtProperties.secretKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
			if (!REGISTRATION_SUBJECT.equals(claims.getSubject())) {
				throw new CustomException(ErrorCode.AUTH_INVALID);
			}
			return claims;
		} catch (ExpiredJwtException e) {
			throw new CustomException(ErrorCode.AUTH_REGISTRATION_EXPIRED);
		} catch (JwtException | IllegalArgumentException e) {
			throw new CustomException(ErrorCode.AUTH_INVALID);
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

	private Claims getClaims(String token) {
		return Jwts.parser()
			.verifyWith(jwtProperties.secretKey())
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	private String getJwtId() {
		return UUID.randomUUID().toString();
	}
}
