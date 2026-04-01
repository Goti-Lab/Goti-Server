package com.goti.user.service.domain.user;

import com.goti.constants.OAuthProvider;
import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.domain.entity.user.SocialProviderEntity;

import java.util.Optional;

public interface SocialProviderService {

	SocialProviderEntity save(
		MemberEntity member, OAuthProvider provider, String providerId, String email
	);

	Optional<SocialProviderEntity> findSocialProvider(String providerId, OAuthProvider provider);

	SocialProviderEntity getSocialProvider(String providerId, OAuthProvider provider);
}
