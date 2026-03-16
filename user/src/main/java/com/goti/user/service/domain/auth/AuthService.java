package com.goti.user.service.domain.auth;

public interface AuthService {
	void sendSmsCode(String socialVerifyToken, String mobile);

	void verifySmsCode(String mobile, String authCode);
}
