package com.goti.service.domain.user;

import com.goti.constants.OAuthProvider;
import com.goti.domain.entity.user.MemberEntity;
import com.goti.domain.entity.user.SocialProviderEntity;

import java.util.Optional;

public interface SocialProviderService {

	SocialProviderEntity save(
		MemberEntity member, OAuthProvider provider, String providerId, String email
	);

	Optional<MemberEntity> findMemberBySocialInfo(String providerId, OAuthProvider provider);

	Optional<SocialProviderEntity> findByProviderIdAndProvider(String providerId, OAuthProvider provider);
}
