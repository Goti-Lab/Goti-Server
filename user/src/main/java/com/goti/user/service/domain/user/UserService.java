package com.goti.user.service.domain.user;

import com.goti.user.domain.entity.user.UserEntity;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

	Optional<UserEntity> findUserById(UUID userId);

	Optional<UserEntity> findUserByMobile(String mobile);
}
