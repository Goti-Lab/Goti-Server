package com.goti.user.service.test;

import java.time.LocalDate;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.Gender;
import com.goti.constants.OAuthProvider;
import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.service.domain.user.MemberService;
import com.goti.user.service.domain.user.SocialProviderService;

import lombok.RequiredArgsConstructor;

/**
 * 테스트 유저 생성 시 REQUIRES_NEW 트랜잭션 격리를 위한 헬퍼.
 * Self-invocation 문제를 회피하기 위해 별도 Bean으로 분리.
 *
 * <p>중복 키(DataIntegrityViolationException) 발생 시
 * 이 트랜잭션만 롤백되고 호출자 트랜잭션은 영향 없음.</p>
 */
@Profile("!prod")
@Component
@RequiredArgsConstructor
public class TestUserPersistenceHelper {

	private final MemberService memberService;
	private final SocialProviderService socialProviderService;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public MemberEntity createMember(
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
