package com.goti.security;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import java.util.UUID;

/**
 * DB 조회 없이 Istio 헤더(X-User-Id, X-User-Role)만으로 생성하는 경량 UserDetails.
 * {@code @AuthenticationPrincipal(expression = "id")} 호환 — 기존 Controller 수정 불필요.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class SimpleUserDetails extends User {
	private final UUID id;

	public SimpleUserDetails(UUID id, String role) {
		super(id.toString(), "", AuthorityUtils.createAuthorityList("ROLE_" + role));
		this.id = id;
	}
}
