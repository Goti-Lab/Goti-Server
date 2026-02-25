package com.goti.security;

import com.goti.constants.UserRole;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import java.util.UUID;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ExtendedUserDetails extends User {
	private final UUID id;

	public ExtendedUserDetails(UUID id, String mobile, UserRole role) {
		super(mobile, "", AuthorityUtils.createAuthorityList("ROLE_" + role.name()));
		this.id = id;
	}

}

