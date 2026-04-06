package com.goti.user.security;

import com.goti.constants.OAuthProvider;
import com.goti.user.constants.UserRole;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import java.util.UUID;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ExtendedUserDetails extends User {
	private final UUID id;
	private final String providerId;
	private final OAuthProvider provider;

	public ExtendedUserDetails(UUID id, UserRole role, String providerId, OAuthProvider provider) {
		super(String.valueOf(id), "", AuthorityUtils.createAuthorityList("ROLE_" + role.name()));
		this.id = id;
		this.providerId = providerId;
		this.provider = provider;
	}

}

