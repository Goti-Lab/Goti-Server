package com.goti.user.service.domain.user;

import com.goti.constants.Gender;
import com.goti.user.domain.entity.user.MemberEntity;

import java.time.LocalDate;
import java.util.Optional;

public interface MemberService {

  MemberEntity save(String name, String mobile, Gender gender, LocalDate birthDate);

  Optional<MemberEntity> findByMobile(String mobile);

}
