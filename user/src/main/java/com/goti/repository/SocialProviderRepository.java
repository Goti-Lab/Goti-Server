package com.goti.repository;

import com.goti.constants.OAuthProvider;
import com.goti.domain.entity.user.SocialProviderEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SocialProviderRepository extends JpaRepository<SocialProviderEntity, UUID> {

	@Query("SELECT s " +
					 "FROM SocialProviderEntity s " +
		 "JOIN FETCH s.member " +
					"WHERE s.providerId = :providerId " +
		 				"AND s.provider = :provider")
	Optional<SocialProviderEntity> findByProviderIdAndProviderWithUser(
		@Param("providerId") String providerId,
		@Param("provider") OAuthProvider provider
	);
}
