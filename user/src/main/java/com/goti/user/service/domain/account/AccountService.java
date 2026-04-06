package com.goti.user.service.domain.account;

import com.goti.user.domain.entity.user.AccountEntity;
import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.dto.response.AccountRegisterResponse;

import java.util.Optional;

public interface AccountService {

	AccountRegisterResponse register(
		String accountNumber, String bankName, String accountHolder, MemberEntity member
	);

	Optional<AccountEntity> findAccount(MemberEntity member);
}
