package com.goti.user.service.domain.account;

import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.dto.response.AccountRegisterResponse;

public interface AccountService {

	AccountRegisterResponse register(
		String accountNumber, String bankName, String accountHolder, MemberEntity member
	);
}
