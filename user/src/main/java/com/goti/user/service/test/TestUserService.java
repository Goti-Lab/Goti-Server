package com.goti.user.service.test;

import java.time.LocalDate;

import com.goti.user.constants.TokenType;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.Gender;
import com.goti.constants.OAuthProvider;
import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.user.config.jwt.JwtTokenProvider;
import com.goti.user.constants.UserRole;
import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.dto.request.BulkCreateTestUserRequest;
import com.goti.user.dto.request.CreateTestUserRequest;
import com.goti.user.dto.request.TestLoginRequest;
import com.goti.user.dto.response.BulkTestUserResponse;
import com.goti.user.dto.response.TestUserResponse;
import com.goti.user.dto.response.TokenResponse;
import com.goti.user.service.domain.user.MemberService;
import com.goti.user.service.domain.user.SocialProviderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("!prod")
@Service
@RequiredArgsConstructor
public class TestUserService {

	private final MemberService memberService;
	private final SocialProviderService socialProviderService;
	private final TestUserPersistenceHelper persistenceHelper;
	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	public TestUserResponse createUser(CreateTestUserRequest request) {
		MemberEntity member = findOrCreateMember(request);

		String accessToken = jwtTokenProvider.create(
			member.getId(), member.getMobile(), UserRole.MEMBER, TokenType.ACCESS
		);

		return new TestUserResponse(
			member.getId(), member.getMobile(), member.getName(), accessToken
		);
	}

	private MemberEntity findOrCreateMember(CreateTestUserRequest request) {
		return memberService.findByMobile(request.mobile())
			.orElseGet(() -> {
				try {
					return persistenceHelper.createMember(
						request.name(), request.mobile(),
						request.gender(), request.birthDate()
					);
				} catch (DataIntegrityViolationException e) {
					// REQUIRES_NEW 덕분에 외부 트랜잭션은 깨끗한 상태
					log.debug("테스트 유저 이미 존재 (동시 생성): mobile={}", request.mobile());
					return memberService.findByMobile(request.mobile())
						.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
				}
			});
	}

	/**
	 * 테스트 유저 대량 생성.
	 * @Max(10_000)으로 단일 요청 상한을 제한하여 트랜잭션 부담을 완화.
	 * 10,000건 이상 필요 시 startIndex를 변경하여 반복 호출 (K6 setup에서 처리).
	 *
	 * <p>성능 참고: 건당 findByMobile SELECT가 발생하나, 테스트 데이터 세팅 용도이므로
	 * 배치 IN 쿼리 최적화는 적용하지 않음. 10,000건 기준 수 초 내 완료.</p>
	 */
	@Transactional
	public BulkTestUserResponse bulkCreateUsers(BulkCreateTestUserRequest request) {
		int created = 0;
		int skipped = 0;

		for (int i = request.startIndex(); i < request.startIndex() + request.count(); i++) {
			String mobile = String.format("000%08d", i);

			if (memberService.findByMobile(mobile).isPresent()) {
				skipped++;
				continue;
			}

			createMemberWithSocialProvider(
				"test-user-" + i, mobile, Gender.MALE, LocalDate.of(2000, 1, 1)
			);
			created++;
		}

		return new BulkTestUserResponse(created, skipped);
	}

	@Transactional(readOnly = true)
	public TokenResponse login(TestLoginRequest request) {
		MemberEntity member = memberService.findByMobile(request.mobile())
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		String accessToken = jwtTokenProvider.create(
			member.getId(), member.getMobile(), UserRole.MEMBER, TokenType.ACCESS
		);

		return new TokenResponse(accessToken);
	}

	private MemberEntity createMemberWithSocialProvider(
		String name, String mobile, Gender gender, LocalDate birthDate
	) {
		MemberEntity member = memberService.save(name, mobile, gender, birthDate);
		socialProviderService.save(
			member, OAuthProvider.NAVER,
			"test-provider-" + mobile,
			"test-" + mobile + "@test.com"
		);
		return member;
	}
}
