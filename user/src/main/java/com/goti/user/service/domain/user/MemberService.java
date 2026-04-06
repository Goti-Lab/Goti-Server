package com.goti.user.service.domain.user;

import com.goti.constants.Gender;
import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.dto.response.MemberUpdateResponse;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface MemberService {

  MemberEntity save(String name, String mobile, Gender gender, LocalDate birthDate);

  Optional<MemberEntity> findByMobile(String mobile);

	MemberEntity getMember(UUID memberId);

	MemberUpdateResponse update(
		UUID memberId, String mobile, String name, Gender gender, LocalDate birthDate, String authCode
	);
}
