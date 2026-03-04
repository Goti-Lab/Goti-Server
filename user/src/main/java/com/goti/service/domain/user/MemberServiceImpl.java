package com.goti.service.domain.user;

import com.goti.constants.Gender;
import com.goti.domain.entity.user.MemberEntity;
import com.goti.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

	private final MemberRepository memberRepository;

	@Transactional
	public MemberEntity save(String name, String mobile, Gender gender, LocalDate birthDate) {
		MemberEntity member = MemberEntity.create(
			mobile, name, gender, birthDate
		);
		memberRepository.save(member);
		return member;
	}
}
