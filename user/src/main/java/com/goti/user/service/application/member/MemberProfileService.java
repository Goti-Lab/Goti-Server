package com.goti.user.service.application.member;

import com.goti.constants.OAuthProvider;
import com.goti.user.domain.entity.user.AccountEntity;
import com.goti.user.domain.entity.user.AddressEntity;
import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.domain.entity.user.SocialProviderEntity;
import com.goti.user.dto.response.AccountRegisterResponse;
import com.goti.user.dto.response.AddressRegisterResponse;
import com.goti.user.dto.response.MemberDetailResponse;
import com.goti.user.service.domain.account.AccountService;
import com.goti.user.service.domain.address.AddressService;
import com.goti.user.service.domain.user.MemberService;

import com.goti.user.service.domain.user.SocialProviderService;

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
	private final SocialProviderService socialProviderService;

	@Transactional(readOnly = true)
	public MemberDetailResponse getProfileDetail(String providerId, OAuthProvider provider) {
		SocialProviderEntity socialProvider = socialProviderService.getSocialProvider(providerId, provider);
		MemberEntity member = socialProvider.getMember();
		AccountEntity account = accountService.findAccount(member).orElse(null);
		AddressEntity address = addressService.findAddress(member).orElse(null);
		var socialConnection = MemberDetailResponse.SocialConnection.of(
			member.getSocialProviders(), provider
		);
		return MemberDetailResponse.from(
			socialProvider.getEmail(),
			provider,
			member,
			account,
			address,
			socialConnection
		);
	}

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
