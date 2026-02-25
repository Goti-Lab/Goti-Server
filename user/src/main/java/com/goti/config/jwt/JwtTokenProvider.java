package com.goti.config.jwt;

import com.goti.config.properties.JwtProperties;

import com.goti.constants.UserRole;

import com.goti.security.ExtendedUserDetailsService;

import io.jsonwebtoken.Claims;
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
	private static final String JWT_ID_KEY = "jti";

	public String create(UUID id, String mobile, UserRole role) {
		Date issuedAt = new Date();
		Date expireAt = new Date(issuedAt.getTime() + jwtProperties.accessValidTime().toMillis());
		String randomUUID = UUID.randomUUID().toString();
		return Jwts.builder()
			.subject(id.toString())
			.claim(JWT_ID_KEY, randomUUID)
			.claim(ROLE_CLAIM_KEY, role.name())
			.claim(MOBILE_CLAIM_KEY, mobile)
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
}
