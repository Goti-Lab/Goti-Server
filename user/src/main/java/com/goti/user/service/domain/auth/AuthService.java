package com.goti.user.service.domain.auth;

import com.goti.constants.OAuthProvider;
import com.goti.user.domain.entity.user.MemberEntity;
import org.springframework.data.util.Pair;

import java.util.UUID;

public interface AuthService {
	void sendSmsCode(String socialVerifyToken, String mobile);

	void verifySmsCode(String mobile, String authCode);

	UUID validateTokenAndGetMemberId(String token);

	Pair<String, String> issueTokens(
		MemberEntity member, String providerId, OAuthProvider provider
	);

}
