package com.goti.service.domain.user;

import com.goti.constants.OAuthProvider;
import com.goti.domain.entity.user.MemberEntity;

import java.util.Optional;

public interface SocialProviderService {

	Optional<MemberEntity> findMemberBySocialInfo(String providerId, OAuthProvider provider);
}
