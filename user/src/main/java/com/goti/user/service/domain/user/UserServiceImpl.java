package com.goti.user.service.domain.user;

import com.goti.user.domain.entity.user.UserEntity;

import com.goti.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	@Override
	public Optional<UserEntity> findUserById(UUID userId) {
		return userRepository.findById(userId);
	}

	@Override
	public Optional<UserEntity> findUserByMobile(String mobile) {
		return userRepository.findByMobile(mobile);
	}
}
