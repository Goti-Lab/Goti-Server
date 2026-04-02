package com.goti.user.dto.response;

import com.goti.user.domain.entity.user.AccountEntity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "계좌 등록 응답")
public record AccountRegisterResponse(

	@Schema(description = "계좌번호 Id", example = "5a484000-e39b-4a44-a716-41265c400000")
	UUID accountId,

	@Schema(description = "계좌번호", example = "1002-876-543210")
	String accountNumber,

	@Schema(description = "계좌 은행", example = "우리은행")
	String bankName,

	@Schema(description = "계좌 예금주", example = "홍길동")
	String accountHolder
) {
	public static AccountRegisterResponse from(AccountEntity account) {
		return new AccountRegisterResponse(
			account.getId(),
			account.getAccountNumber(),
			account.getBankName(),
			account.getAccountHolder()
		);
	}
}
