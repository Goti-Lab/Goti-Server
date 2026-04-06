package com.goti.user.service.domain.user;

import com.goti.constants.Gender;
import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.infra.cache.RedisCache;
import com.goti.infra.constants.redis.RedisKey;
import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

	private final MemberRepository memberRepository;
	private final RedisCache redisCache;

	@Override
	@Transactional
	public MemberEntity save(String name, String mobile, Gender gender, LocalDate birthDate) {
		MemberEntity member = MemberEntity.create(
			mobile, name, gender, birthDate
		);
		memberRepository.save(member);
		return member;
	}

	@Override
	public Optional<MemberEntity> findByMobile(String mobile) {
		return memberRepository.findByMobile(mobile);
	}

	@Override
	public MemberEntity getMember(UUID memberId) {
		return memberRepository.findByIdOrThrow(memberId);
	}

	@Override
	@Transactional
	public MemberEntity update(
		UUID memberId,
		String mobile,
		String name,
		Gender gender,
		LocalDate birthDate,
		String authCode
	) {
		String key = RedisKey.MEMBER_IDENTITY_VERIFY.getKey(memberId);
		String cachedAuthCode = getCachedAuthCode(key);
		Preconditions.validate(
			cachedAuthCode.equals(authCode),
			ErrorCode.AUTH_CODE_INVALID
		);

		MemberEntity member = getMember(memberId);
		Preconditions.validate(
			member.verifyIdentity(gender, birthDate),
			ErrorCode.AUTH_IDENTITY_VERIFY_FAILED
		);
		verifyDuplicatedMobile(member, mobile);
		member.updateIdentity(mobile, name);
		Preconditions.validate(
			redisCache.consume(key),
			ErrorCode.AUTH_CODE_NOT_FOUND
		);
		return member;
	}

	private void verifyDuplicatedMobile(MemberEntity member, String mobile) {
		memberRepository.findByMobile(mobile).ifPresent(
			existingMember -> {
				if (!existingMember.getId().equals(member.getId())) {
					throw new CustomException(ErrorCode.AUTH_MOBILE_ALREADY_REGISTERED);
				}
			});
	}

	private String getCachedAuthCode(String key) {
		String cachedCode = redisCache.get(
			key, String.class
		);
		if (cachedCode == null) {
			throw new CustomException(ErrorCode.AUTH_CODE_NOT_FOUND);
		}
		return cachedCode;
	}
}
