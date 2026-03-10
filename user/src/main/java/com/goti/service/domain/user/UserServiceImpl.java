package com.goti.service.domain.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.goti.domain.entity.user.UserEntity;
import com.goti.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final MemberRepository memberRepository;

	@Override
	public Optional<UserEntity> findUserById(UUID userId) {
		return memberRepository.findById(userId).map(member -> (UserEntity)member);
	}

	@Override
	public Optional<UserEntity> findUserByMobile(String mobile) {
		return Optional.empty();
	}
}
