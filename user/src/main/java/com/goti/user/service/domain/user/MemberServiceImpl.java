package com.goti.user.service.domain.user;

import com.goti.constants.Gender;
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
}
