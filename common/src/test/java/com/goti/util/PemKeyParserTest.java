package com.goti.util;

import static org.assertj.core.api.Assertions.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PemKeyParserTest {

	private static String validPrivateKeyPem;
	private static String validPublicKeyPem;

	@BeforeAll
	static void generateKeys() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		KeyPair keyPair = generator.generateKeyPair();

		validPrivateKeyPem = "-----BEGIN PRIVATE KEY-----\n"
			+ Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(keyPair.getPrivate().getEncoded())
			+ "\n-----END PRIVATE KEY-----";

		validPublicKeyPem = "-----BEGIN PUBLIC KEY-----\n"
			+ Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(keyPair.getPublic().getEncoded())
			+ "\n-----END PUBLIC KEY-----";
	}

	@Test
	@DisplayName("유효한 PKCS#8 private key PEM을 파싱한다")
	void should_parsePrivateKey_when_validPkcs8() {
		// When
		RSAPrivateKey key = PemKeyParser.parsePrivateKey(validPrivateKeyPem);

		// Then
		assertThat(key).isNotNull();
		assertThat(key.getAlgorithm()).isEqualTo("RSA");
	}

	@Test
	@DisplayName("유효한 X.509 public key PEM을 파싱한다")
	void should_parsePublicKey_when_validX509() {
		// When
		RSAPublicKey key = PemKeyParser.parsePublicKey(validPublicKeyPem);

		// Then
		assertThat(key).isNotNull();
		assertThat(key.getAlgorithm()).isEqualTo("RSA");
	}

	@Test
	@DisplayName("PKCS#1 private key 형식이면 변환 안내와 함께 거부한다")
	void should_reject_when_pkcs1PrivateKey() {
		// Given
		String pkcs1Pem = "-----BEGIN RSA PRIVATE KEY-----\nMIIBogIBAAJ...\n-----END RSA PRIVATE KEY-----";

		// When & Then
		assertThatThrownBy(() -> PemKeyParser.parsePrivateKey(pkcs1Pem))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("PKCS#1 형식 미지원")
			.hasMessageContaining("openssl pkcs8 -topk8");
	}

	@Test
	@DisplayName("PKCS#1 public key 형식이면 변환 안내와 함께 거부한다")
	void should_reject_when_pkcs1PublicKey() {
		// Given
		String pkcs1PubPem = "-----BEGIN RSA PUBLIC KEY-----\nMIIBCgKCAQ...\n-----END RSA PUBLIC KEY-----";

		// When & Then
		assertThatThrownBy(() -> PemKeyParser.parsePublicKey(pkcs1PubPem))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("PKCS#1 public key 형식 미지원")
			.hasMessageContaining("openssl rsa");
	}

	@Test
	@DisplayName("잘못된 Base64 데이터면 파싱 실패한다")
	void should_throwException_when_invalidBase64() {
		// Given
		String invalidPem = "-----BEGIN PRIVATE KEY-----\n!!!invalid!!!\n-----END PRIVATE KEY-----";

		// When & Then
		assertThatThrownBy(() -> PemKeyParser.parsePrivateKey(invalidPem))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
