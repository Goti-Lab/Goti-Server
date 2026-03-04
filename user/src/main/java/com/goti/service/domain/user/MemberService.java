package com.goti.service.domain.user;

import com.goti.constants.Gender;
import com.goti.domain.entity.user.MemberEntity;

import java.time.LocalDate;

public interface MemberService {

	MemberEntity save(String name, String mobile, Gender gender, LocalDate birthDate);
}
