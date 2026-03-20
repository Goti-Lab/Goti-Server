package com.goti.user.controller;

import com.goti.user.config.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * JWKS (JSON Web Key Set) 엔드포인트.
 * Istio RequestAuthentication이 이 엔드포인트에서 public key를 가져와 JWT를 검증한다.
 */
@RestController
@RequiredArgsConstructor
public class JwksController {

	private final JwtTokenProvider jwtTokenProvider;

	@GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> jwks() {
		RSAPublicKey publicKey = jwtTokenProvider.getRsaPublicKey();
		if (publicKey == null) {
			return ResponseEntity.ok(Map.of("keys", List.of()));
		}

		Map<String, Object> jwk = Map.of(
			"kty", "RSA",
			"alg", "RS256",
			"use", "sig",
			"kid", "goti-jwt-key-1",
			"n", base64UrlEncode(publicKey.getModulus().toByteArray()),
			"e", base64UrlEncode(publicKey.getPublicExponent().toByteArray())
		);

		return ResponseEntity.ok(Map.of("keys", List.of(jwk)));
	}

	private String base64UrlEncode(byte[] bytes) {
		// BigInteger는 양수에도 선행 0x00을 포함할 수 있으므로 제거
		if (bytes.length > 1 && bytes[0] == 0) {
			byte[] trimmed = new byte[bytes.length - 1];
			System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
			bytes = trimmed;
		}
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
