package com.goti.user.security;

import com.goti.constants.OAuthProvider;
import com.goti.user.domain.entity.user.UserEntity;

import com.goti.user.service.domain.user.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Primary
@RequiredArgsConstructor
public class ExtendedUserDetailsServiceImpl implements ExtendedUserDetailsService {

	private final UserService userService;

	@Override
	public UserDetails loadUserByUsername(String mobile) throws UsernameNotFoundException {
		UserEntity user = userService.findUserByMobile(mobile)
			.orElseThrow(
				() -> new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다.")
			);
		return convert(user, null, null);
	}

	@Override
	public UserDetails loadUserById(
		String userId, String providerId, OAuthProvider provider
	) throws UsernameNotFoundException {
		UserEntity user = userService.findUserById(UUID.fromString(userId))
			.orElseThrow(
				() -> new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다.")
			);
		return convert(user, providerId, provider);
	}

	private UserDetails convert(UserEntity user, String providerId, OAuthProvider provider) {
		return new ExtendedUserDetails(
			user.getId(),
			user.getRole(),
			providerId,
			provider
		);
	}
}

