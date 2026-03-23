package com.goti.user.controller;

import static org.assertj.core.api.Assertions.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.goti.user.config.jwt.JwtTokenProvider;

class JwksControllerTest {

	@Test
	@DisplayName("RSA 키 설정 시 JWKS 응답에 kid, kty, alg, n, e가 포함된다")
	@SuppressWarnings("unchecked")
	void should_returnJwkWithKid_when_rsaKeyConfigured() throws Exception {
		// Given
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		KeyPair keyPair = generator.generateKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

		JwtTokenProvider mockProvider = createMockProvider(publicKey);
		JwksController controller = new JwksController(mockProvider);

		// When
		ResponseEntity<Map<String, Object>> response = controller.jwks();

		// Then
		assertThat(response.getStatusCode().value()).isEqualTo(200);

		Map<String, Object> body = response.getBody();
		assertThat(body).containsKey("keys");

		List<Map<String, Object>> keys = (List<Map<String, Object>>) body.get("keys");
		assertThat(keys).hasSize(1);

		Map<String, Object> jwk = keys.get(0);
		assertThat(jwk.get("kty")).isEqualTo("RSA");
		assertThat(jwk.get("alg")).isEqualTo("RS256");
		assertThat(jwk.get("use")).isEqualTo("sig");
		assertThat(jwk.get("kid")).isEqualTo("goti-jwt-key-1");
		assertThat(jwk.get("n")).isNotNull();
		assertThat(jwk.get("e")).isNotNull();
	}

	@Test
	@DisplayName("RSA 키 미설정 시 빈 keys 배열을 반환한다")
	@SuppressWarnings("unchecked")
	void should_returnEmptyKeys_when_noRsaKey() {
		// Given
		JwtTokenProvider mockProvider = createMockProvider(null);
		JwksController controller = new JwksController(mockProvider);

		// When
		ResponseEntity<Map<String, Object>> response = controller.jwks();

		// Then
		Map<String, Object> body = response.getBody();
		List<Map<String, Object>> keys = (List<Map<String, Object>>) body.get("keys");
		assertThat(keys).isEmpty();
	}

	@Test
	@DisplayName("두 번째 호출은 캐싱된 응답을 반환한다")
	void should_returnCachedResponse_when_calledTwice() throws Exception {
		// Given
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		RSAPublicKey publicKey = (RSAPublicKey) generator.generateKeyPair().getPublic();

		JwtTokenProvider mockProvider = createMockProvider(publicKey);
		JwksController controller = new JwksController(mockProvider);

		// When
		ResponseEntity<Map<String, Object>> first = controller.jwks();
		ResponseEntity<Map<String, Object>> second = controller.jwks();

		// Then
		assertThat(first).isSameAs(second);
	}

	private JwtTokenProvider createMockProvider(RSAPublicKey publicKey) {
		JwtTokenProvider provider = new JwtTokenProvider(null, null);
		ReflectionTestUtils.setField(provider, "rsaPublicKey", publicKey);
		return provider;
	}
}
