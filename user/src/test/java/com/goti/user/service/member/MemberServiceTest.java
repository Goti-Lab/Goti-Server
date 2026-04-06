package com.goti.user.service.member;

import com.goti.constants.Gender;
import com.goti.infra.cache.RedisCache;
import com.goti.infra.constants.redis.RedisKey;

import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.repository.MemberRepository;

import com.goti.user.service.domain.user.MemberServiceImpl;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class MemberServiceTest {

	@InjectMocks
	MemberServiceImpl memberService;
	@Mock MemberRepository memberRepository;
	@Mock RedisCache redisCache;

	private final UUID memberId = UUID.randomUUID();
	private MemberEntity member;

	@BeforeEach
	void setup() {
		member = MemberEntity.create(
			"01012345678", "홍길동", Gender.MALE, LocalDate.of(2000, 2, 3)
		);
	}

	@Test
	@Transactional
	void 회원_정보_변경_성공() {
		String newName = "테스트개명";
		String newMobile = "01099998888";
		String authCode = "123456";
		String redisKey = RedisKey.MEMBER_IDENTITY_VERIFY.getKey(memberId);

		when(memberRepository.findByIdOrThrow(memberId)).thenReturn(member);

		when(redisCache.get(redisKey, String.class)).thenReturn(authCode);
		when(redisCache.consume(redisKey)).thenReturn(true);

		memberService.update(
			memberId, newMobile, newName, Gender.MALE, LocalDate.of(2000, 2, 3), authCode
		);

		assertThat(member.getName()).isEqualTo(newName);
		assertThat(member.getMobile()).isEqualTo(newMobile);
		verify(redisCache).consume(redisKey);
		log.info("member mobile :: {}", member.getMobile());
		log.info("member name :: {}", member.getName());
	}
}
