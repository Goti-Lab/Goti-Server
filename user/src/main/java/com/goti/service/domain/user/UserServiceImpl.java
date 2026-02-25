package com.goti.service.domain.user;

import com.goti.domain.entity.user.UserEntity;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	@Override
	public Optional<UserEntity> findUserById(UUID userId) {
		return Optional.empty();
	}

	@Override
	public Optional<UserEntity> findUserByMobile(String mobile) {
		return Optional.empty();
	}
}
