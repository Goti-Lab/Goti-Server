package com.goti.service.domain.user;

import com.goti.constants.OAuthProvider;
import com.goti.domain.entity.user.MemberEntity;

import com.goti.domain.entity.user.SocialProviderEntity;
import com.goti.repository.SocialProviderRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SocialProviderServiceImpl implements SocialProviderService {

	private final SocialProviderRepository socialProviderRepository;

	@Override
	public Optional<MemberEntity> findMemberBySocialInfo(String providerId, OAuthProvider provider) {
		return socialProviderRepository.findByProviderIdAndProviderWithUser(
			providerId, provider
		).map(SocialProviderEntity::getMember);
	}
}
