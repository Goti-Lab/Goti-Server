package com.goti.user.service.member;

import com.goti.constants.Gender;
import com.goti.constants.OAuthProvider;
import com.goti.user.domain.entity.user.AccountEntity;
import com.goti.user.domain.entity.user.AddressEntity;
import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.domain.entity.user.SocialProviderEntity;
import com.goti.user.dto.response.MemberDetailResponse;
import com.goti.user.dto.response.MemberSummaryResponse;
import com.goti.user.service.application.member.MemberProfileService;
import com.goti.user.service.domain.account.AccountService;
import com.goti.user.service.domain.address.AddressService;

import com.goti.user.service.domain.user.SocialProviderService;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class MemberProfileServiceTest {

	@InjectMocks
	private MemberProfileService memberProfileService;

	@Mock
	private SocialProviderService socialProviderService;

	@Mock
	private AccountService accountService;

	@Mock
	private AddressService addressService;

	@Test
	@DisplayName("회원 상세 프로필 조회 성공 테스트 (마이페이지)")
	void 회원_본인_상세_프로필_조회_성공() {
		String providerId = "google_12345";
		OAuthProvider provider = OAuthProvider.GOOGLE;

		MemberEntity mockMember = spy(MemberEntity.create(
			"010-1234-5678", "김고티", Gender.MALE, LocalDate.of(2000, 10, 21)
		));

		SocialProviderEntity mockSocial = mock(SocialProviderEntity.class);
		given(mockSocial.getMember()).willReturn(mockMember);
		given(mockSocial.getEmail()).willReturn("goti1234@google.com");

		given(socialProviderService.getSocialProvider(
			providerId, provider
		)).willReturn(mockSocial);

		AccountEntity mockAccount = mock(AccountEntity.class);
		given(mockAccount.getBankName()).willReturn("카카오뱅크");
		given(mockAccount.getAccountNumber()).willReturn("3333-67-8765445");
		given(mockAccount.getAccountHolder()).willReturn("테스트예금주");

		AddressEntity mockAddress = mock(AddressEntity.class);
		given(mockAddress.getZipCode()).willReturn("12345");
		given(mockAddress.getBaseAddress()).willReturn("서울특별시 강남구 학동로 343");
		given(mockAddress.getDetailAddress()).willReturn("(논현동, 포바강남타워) 4층, 15층");

		given(accountService.findAccount(mockMember)).willReturn(Optional.of(mockAccount));
		given(addressService.findAddress(mockMember)).willReturn(Optional.of(mockAddress));

		MemberDetailResponse response =
			memberProfileService.getProfileDetail(providerId, provider);

		assertThat(response.name()).isEqualTo("김고티");
		assertThat(response.oAuthProvider()).isEqualTo(OAuthProvider.GOOGLE);
		assertThat(response.bankAccount().bankName()).isEqualTo("카카오뱅크");
		assertThat(response.bankAccount().accountHolder()).isEqualTo("테스트예금주");
		assertThat(response.address().zipCode()).isEqualTo("12345");
		assertThat(response.address().baseAddress()).isEqualTo("서울특별시 강남구 학동로 343");

		assertThat(response.socialConnection().isGoogleConnected()).isTrue();
		log.info("response :: {}", response);
		verify(socialProviderService, times(1)).getSocialProvider(anyString() , any());
	}

	@Test
	@DisplayName("회원 요약 정보 조회 성공 테스트")
	void 회원_요약_정보_조회_성공() {
		// given
		String providerId = "google_12345";
		OAuthProvider provider = OAuthProvider.GOOGLE;
		String email = "goti1234@google.com";
		String name = "김고티";
		String mobile = "01012345678";

		// 1. Member 엔티티 생성
		MemberEntity mockMember = MemberEntity.create(
			mobile, name, Gender.MALE, LocalDate.of(2000, 10, 21)
		);

		// 2. SocialProvider 모킹 및 연관관계 설정
		SocialProviderEntity mockSocial = mock(SocialProviderEntity.class);
		given(mockSocial.getMember()).willReturn(mockMember);
		given(mockSocial.getEmail()).willReturn(email);

		given(socialProviderService.getSocialProvider(providerId, provider))
			.willReturn(mockSocial);

		// when
		MemberSummaryResponse response = memberProfileService.getProfileSummary(providerId, provider);

		// then
		assertThat(response.name()).isEqualTo(name);
		assertThat(response.mobile()).isEqualTo(mobile);
		assertThat(response.email()).isEqualTo(email);

		log.info("summary response :: {}", response);

		// 검증
		verify(socialProviderService, times(1)).getSocialProvider(eq(providerId), eq(provider));
	}

}
