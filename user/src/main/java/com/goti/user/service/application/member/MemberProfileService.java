package com.goti.user.service.application.member;

import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.dto.response.AccountRegisterResponse;
import com.goti.user.dto.response.AddressRegisterResponse;
import com.goti.user.service.domain.account.AccountService;
import com.goti.user.service.domain.address.AddressService;
import com.goti.user.service.domain.user.MemberService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberProfileService {
	private final MemberService memberService;
	private final AccountService accountService;
	private final AddressService addressService;

	@Transactional
	public AccountRegisterResponse registerAccount(
		String accountNumber, String bankName, String accountHolder, UUID memberId
	) {
		MemberEntity member = memberService.getMember(memberId);
		return accountService.register(
			accountNumber, bankName, accountHolder, member
		);
	}

	@Transactional
	public AddressRegisterResponse registerAddress(
		UUID memberId, String zipCode, String baseAddress, String detailAddress
	) {
		MemberEntity member = memberService.getMember(memberId);
		return addressService.register(
			member, zipCode, baseAddress, detailAddress
		);
	}

}
