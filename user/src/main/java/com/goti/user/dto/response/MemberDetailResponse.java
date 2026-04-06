package com.goti.user.dto.response;

import com.goti.constants.OAuthProvider;
import com.goti.user.domain.entity.user.AccountEntity;
import com.goti.user.domain.entity.user.AddressEntity;
import com.goti.user.domain.entity.user.MemberEntity;

import com.goti.user.domain.entity.user.SocialProviderEntity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = "회원 본인 상세조회 응답")
public record MemberDetailResponse(

	@Schema(description = "이메일", example = "email@google.com")
	String email,

	@Schema(description = "소셜 제공자 타입", example = "GOOGLE")
	OAuthProvider oAuthProvider,

	@Schema(description = "이름", example = "홍길동")
	String name,

	@Schema(description = "휴대전화번호", example = "010-1234-5678")
	String mobile,

	@Schema(description = "계좌 정보")
	BankAccount bankAccount,

	@Schema(description = "주소 정보")
	Address address,

	@Schema(description = "소셜 계정 연결 정보")
	SocialConnection socialConnection

) {

	@Schema(description = "은행 계좌 정보")
	public record BankAccount(
		@Schema(description = "은행명", example = "카카오뱅크")
		String bankName,
		@Schema(description = "계좌번호", example = "3333-67-8765445")
		String accountNumber,
		@Schema(description = "예금주", example = "김고티")
		String accountHolder
	) {
	}

	@Schema(description = "주소 정보")
	public record Address(
		@Schema(description = "우편번호", example = "12345")
		String zipCode,
		@Schema(description = "기본 주소", example = "서울특별시 강남구 테헤란로 123")
		String baseAddress,
		@Schema(description = "상세 주소", example = "101호")
		String detailAddress
	) {
	}

	@Schema(description = "소셜 연결 상태")
	public record SocialConnection(
		@Schema(description = "구글 연결 여부", example = "true")
		boolean isGoogleConnected,
		@Schema(description = "카카오 연결 여부", example = "true")
		boolean isKakaoConnected,
		@Schema(description = "네이버 연결 여부", example = "false")
		boolean isNaverConnected
	) {
		public static SocialConnection of(List<SocialProviderEntity> connections, OAuthProvider currentProvider) {

			Set<OAuthProvider> connectedProviders = connections.stream()
				.map(SocialProviderEntity::getProvider)
				.collect(Collectors.toSet());

			connectedProviders.add(currentProvider);

			return new SocialConnection(
				connectedProviders.contains(OAuthProvider.GOOGLE),
				connectedProviders.contains(OAuthProvider.KAKAO),
				connectedProviders.contains(OAuthProvider.NAVER)
			);
		}
	}

	public static MemberDetailResponse from(
		String email,
		OAuthProvider provider,
		MemberEntity member,
		AccountEntity account,
		AddressEntity address,
		SocialConnection socialConnection
	) {
		return new MemberDetailResponse(
			email,
			provider,
			member.getName(),
			member.getMobile(),
			account != null ?
				new BankAccount(
					account.getBankName(),
					account.getAccountNumber(),
					account.getAccountHolder()
				) :
				null,
			address != null ?
				new Address(
					address.getZipCode(),
					address.getBaseAddress(),
					address.getDetailAddress()
				) :
				null,
			socialConnection
		);
	}

}
