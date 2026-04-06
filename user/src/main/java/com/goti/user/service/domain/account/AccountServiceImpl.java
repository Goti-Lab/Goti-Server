package com.goti.user.service.domain.account;

import com.goti.user.domain.entity.user.AccountEntity;
import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.dto.response.AccountRegisterResponse;
import com.goti.user.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

	private final AccountRepository accountRepository;

	@Override
	@Transactional
	public AccountRegisterResponse register(
		String accountNumber, String bankName, String accountHolder, MemberEntity member
	) {
		AccountEntity account = findAccount(member)
			.map(existingAccount -> {
				existingAccount.updateDetails(accountNumber, bankName, accountHolder);
				return existingAccount;
			})
			.orElseGet(() -> AccountEntity.create(
				accountNumber, bankName, accountHolder, member
			));

		accountRepository.save(account);

		return AccountRegisterResponse.from(account);
	}

	@Override
	public Optional<AccountEntity> findAccount(MemberEntity member) {
		return accountRepository.findByMember(member);
	}
}
