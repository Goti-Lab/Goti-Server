package com.goti.service.domain.auth;

public interface AuthService {
	void sendSmsCode(String socialVerifyToken, String mobile);
}
