package com.goti.user.config.jwt;

import static org.assertj.core.api.Assertions.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.goti.user.config.properties.JwtProperties;
import com.goti.user.constants.TokenType;
import com.goti.user.constants.UserRole;
import com.goti.user.security.ExtendedUserDetailsService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class JwtTokenProviderRs256Test {

	private static String rsaPrivateKeyPem;
	private static String rsaPublicKeyPem;
	private static final String HMAC_SECRET = "ThisIsATestSecretKeyThatIsLongEnoughForHS512Algorithm1234567890";

	private JwtTokenProvider provider;

	@BeforeAll
	static void generateKeys() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		KeyPair keyPair = generator.generateKeyPair();

		rsaPrivateKeyPem = "-----BEGIN PRIVATE KEY-----\n"
			+ Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(keyPair.getPrivate().getEncoded())
			+ "\n-----END PRIVATE KEY-----";

		rsaPublicKeyPem = "-----BEGIN PUBLIC KEY-----\n"
			+ Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(keyPair.getPublic().getEncoded())
			+ "\n-----END PUBLIC KEY-----";
	}

	private JwtTokenProvider createProvider(String privateKey, String publicKey) {
		JwtProperties properties = new JwtProperties(
			HMAC_SECRET, privateKey, publicKey,
			Duration.ofHours(1), Duration.ofDays(7), Duration.ofMinutes(5)
		);
		ExtendedUserDetailsService mockService = new ExtendedUserDetailsService() {
			@Override
			public UserDetails loadUserById(String userId) throws UsernameNotFoundException {
				return null;
			}

			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				return null;
			}
		};
		JwtTokenProvider p = new JwtTokenProvider(properties, mockService);
		ReflectionTestUtils.setField(p, "applicationName", "goti-user");
		p.initKeys();
		return p;
	}

	@Nested
	@DisplayName("RS256 모드 (RSA 키 설정됨)")
	class Rs256Mode {

		@BeforeEach
		void setUp() {
			provider = createProvider(rsaPrivateKeyPem, rsaPublicKeyPem);
		}

		@Test
		@DisplayName("RS256으로 토큰 생성 후 검증에 성공한다")
		void should_createAndValidate_when_rs256Enabled() {
			// Given
			UUID userId = UUID.randomUUID();

			// When
			String token = provider.create(userId, "01012345678", UserRole.MEMBER, TokenType.ACCESS);

			// Then
			assertThatCode(() -> provider.validateToken(token))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("RS256 토큰의 claim을 올바르게 추출한다")
		void should_extractClaims_when_rs256Token() {
			// Given
			UUID userId = UUID.randomUUID();
			String token = provider.create(userId, "01012345678", UserRole.ADMIN, TokenType.ACCESS);

			// When
			String jti = provider.extractJti(token);

			// Then
			assertThat(jti).isNotNull().isNotBlank();
		}

		@Test
		@DisplayName("HS512로 서명된 기존 토큰도 dual-verify fallback으로 검증된다")
		void should_fallbackToHs512_when_tokenSignedWithHmac() {
			// Given: HS512로 직접 서명한 토큰
			String hmacToken = Jwts.builder()
				.subject(UUID.randomUUID().toString())
				.claim("role", "MEMBER")
				.claim("mobile", "01012345678")
				.signWith(Keys.hmacShaKeyFor(HMAC_SECRET.getBytes()))
				.compact();

			// When & Then: RS256 실패 후 HS512 fallback으로 검증 성공
			assertThatCode(() -> provider.validateToken(hmacToken))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("RSA public key를 반환한다")
		void should_returnRsaPublicKey() {
			// When & Then
			assertThat(provider.getRsaPublicKey()).isNotNull();
			assertThat(provider.getRsaPublicKey()).isInstanceOf(RSAPublicKey.class);
		}
	}

	@Nested
	@DisplayName("HS512 단독 모드 (RSA 키 미설정)")
	class Hs512OnlyMode {

		@BeforeEach
		void setUp() {
			provider = createProvider("", "");
		}

		@Test
		@DisplayName("HS512로 토큰 생성 후 검증에 성공한다")
		void should_createAndValidate_when_hs512Only() {
			// Given
			UUID userId = UUID.randomUUID();

			// When
			String token = provider.create(userId, "01012345678", UserRole.MEMBER, TokenType.ACCESS);

			// Then
			assertThatCode(() -> provider.validateToken(token))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("RSA public key는 null이다")
		void should_returnNull_when_noRsaKeys() {
			assertThat(provider.getRsaPublicKey()).isNull();
		}
	}

	@Nested
	@DisplayName("만료된 토큰")
	class ExpiredToken {

		@Test
		@DisplayName("만료된 토큰은 fallback 없이 즉시 실패한다")
		void should_throwExpired_when_tokenExpired() {
			// Given: 이미 만료된 토큰을 생성하기 위해 유효시간 0인 provider
			JwtProperties properties = new JwtProperties(
				HMAC_SECRET, rsaPrivateKeyPem, rsaPublicKeyPem,
				Duration.ZERO, Duration.ZERO, Duration.ZERO
			);
			ExtendedUserDetailsService mockService = new ExtendedUserDetailsService() {
				@Override
				public UserDetails loadUserById(String userId) throws UsernameNotFoundException {
					return null;
				}

				@Override
				public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
					return null;
				}
			};
			JwtTokenProvider expiredProvider = new JwtTokenProvider(properties, mockService);
			ReflectionTestUtils.setField(expiredProvider, "applicationName", "goti-user");
			expiredProvider.initKeys();

			UUID userId = UUID.randomUUID();
			String token = expiredProvider.create(userId, "01012345678", UserRole.MEMBER, TokenType.ACCESS);

			// When & Then
			assertThatThrownBy(() -> expiredProvider.validateToken(token))
				.isInstanceOf(ExpiredJwtException.class);
		}
	}
}
