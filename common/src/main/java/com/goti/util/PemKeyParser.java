package com.goti.util;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public final class PemKeyParser {

	private PemKeyParser() {
	}

	public static RSAPrivateKey parsePrivateKey(String pem) {
		rejectPkcs1Format(pem);
		String stripped = stripPemHeaders(pem,
			"-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");
		byte[] decoded = Base64.getDecoder().decode(stripped);
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(decoded));
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			log.error("RSA private key 파싱 실패", e);
			throw new IllegalArgumentException("RSA private key 파싱 실패", e);
		}
	}

	public static RSAPublicKey parsePublicKey(String pem) {
		rejectPkcs1Format(pem);
		String stripped = stripPemHeaders(pem,
			"-----BEGIN PUBLIC KEY-----", "-----END PUBLIC KEY-----");
		byte[] decoded = Base64.getDecoder().decode(stripped);
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(decoded));
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			log.error("RSA public key 파싱 실패", e);
			throw new IllegalArgumentException("RSA public key 파싱 실패", e);
		}
	}

	private static void rejectPkcs1Format(String pem) {
		if (pem.contains("BEGIN RSA PRIVATE KEY")) {
			log.error("PKCS#1 형식 private key 감지. PKCS#8 변환 필요: "
				+ "openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in key.pem -out key-pkcs8.pem");
			throw new IllegalArgumentException("PKCS#1 형식 미지원. PKCS#8로 변환 필요");
		}
		if (pem.contains("BEGIN RSA PUBLIC KEY")) {
			log.error("PKCS#1 형식 public key 감지. X.509 변환 필요: "
				+ "openssl rsa -RSAPublicKey_in -pubout -in pub.pem -out pub-x509.pem");
			throw new IllegalArgumentException("PKCS#1 public key 형식 미지원. X.509 형식 필요");
		}
	}

	private static String stripPemHeaders(String pem, String beginMarker, String endMarker) {
		return pem
			.replace(beginMarker, "")
			.replace(endMarker, "")
			.replaceAll("\\s", "");
	}
}
