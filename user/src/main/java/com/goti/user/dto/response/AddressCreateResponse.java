package com.goti.user.dto.response;

import com.goti.user.domain.entity.user.AddressEntity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "주소 생성 응답")
public record AddressCreateResponse(

	@Schema(description = "주소 ID", example = "5a484000-e39b-4a44-a716-41265c400000")
	UUID addressId,

	@Schema(description = "우편번호", example = "012932")
	String zipCode,

	@Schema(description = "기본주소", example = "서울특별시 강남구 학동로 343")
	String baseAddress,

	@Schema(description = "상세주소", example = "(논현동, 포바강남타워) 4층, 15층")
	String detailAddress
) {

	public static AddressCreateResponse from(AddressEntity address) {
		return new AddressCreateResponse(
			address.getId(),
			address.getZipCode(),
			address.getBaseAddress(),
			address.getDetailAddress()
		);
	}
}
