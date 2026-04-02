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
		return findSocialProvider(providerId, provider).orElseThrow(
			() -> new CustomException(ErrorCode.SOCIAL_PROVIDER_NOT_FOUND)
		);
	}

	@Query("SELECT s " +
					 "FROM SocialProviderEntity s " +
		 "JOIN FETCH s.member " +
					"WHERE s.providerId = :providerId " +
		 				"AND s.provider = :provider")
	Optional<SocialProviderEntity> findSocialProvider(
		@Param("providerId") String providerId,
		@Param("provider") OAuthProvider provider
	);

}
