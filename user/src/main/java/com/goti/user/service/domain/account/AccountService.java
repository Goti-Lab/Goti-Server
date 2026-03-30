package com.goti.user.service.domain.account;

import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.dto.response.AccountCreateResponse;

public interface AccountService {

	AccountCreateResponse create(
		String accountNumber, String bankName, String accountHolder, MemberEntity member
	);
}
