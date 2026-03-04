package com.goti.service.domain.user;

import com.goti.constants.OAuthProvider;
import com.goti.domain.entity.user.MemberEntity;

import com.goti.domain.entity.user.SocialProviderEntity;
import com.goti.repository.SocialProviderRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SocialProviderServiceImpl implements SocialProviderService {

	private final SocialProviderRepository socialProviderRepository;

	@Override
	@Transactional
	public SocialProviderEntity save(
		MemberEntity member, OAuthProvider provider, String providerId, String email
	) {
		SocialProviderEntity socialProvider = SocialProviderEntity.create(
			member, provider, providerId, email
		);
		socialProviderRepository.save(socialProvider);
		return socialProvider;
	}

	@Override
	public Optional<MemberEntity> findMemberBySocialInfo(String providerId, OAuthProvider provider) {
		return findByProviderIdAndProvider(providerId, provider)
			.map(SocialProviderEntity::getMember);
	}

	@Override
	public Optional<SocialProviderEntity> findByProviderIdAndProvider(String providerId, OAuthProvider provider) {
		return socialProviderRepository.findByProviderIdAndProvider(
			providerId, provider
		);
	}
}
