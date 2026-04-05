package com.goti.user.repository;

import com.goti.constants.OAuthProvider;
import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.user.domain.entity.user.SocialProviderEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SocialProviderRepository extends JpaRepository<SocialProviderEntity, UUID> {

	default SocialProviderEntity findSocialProviderOrThrow(String providerId, OAuthProvider provider) {
		return findByProviderIdAndProvider(
			providerId, provider
		).orElseThrow(
			() -> new CustomException(ErrorCode.SOCIAL_PROVIDER_NOT_FOUND)
		);
	}

	Optional<SocialProviderEntity> findByProviderIdAndProvider(String providerId, OAuthProvider provider);

}
