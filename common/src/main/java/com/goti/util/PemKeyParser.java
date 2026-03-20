package com.goti.util;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * PEM 형식 문자열 → RSA Key 객체 변환 유틸리티.
 * PKCS#8 private key, X.509 public key 형식만 지원한다.
 */
public final class PemKeyParser {

	private PemKeyParser() {
	}

	public static RSAPrivateKey parsePrivateKey(String pem) {
		String stripped = stripPemHeaders(pem,
			"-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");
		byte[] decoded = Base64.getDecoder().decode(stripped);
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(decoded));
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new IllegalArgumentException("RSA private key 파싱 실패", e);
		}
	}

	public static RSAPublicKey parsePublicKey(String pem) {
		String stripped = stripPemHeaders(pem,
			"-----BEGIN PUBLIC KEY-----", "-----END PUBLIC KEY-----");
		byte[] decoded = Base64.getDecoder().decode(stripped);
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(decoded));
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new IllegalArgumentException("RSA public key 파싱 실패", e);
		}
	}

	private static String stripPemHeaders(String pem, String beginMarker, String endMarker) {
		return pem
			.replace(beginMarker, "")
			.replace(endMarker, "")
			.replaceAll("\\s", "");
	}
}
