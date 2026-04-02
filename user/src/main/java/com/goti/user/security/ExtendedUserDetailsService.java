package com.goti.user.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface ExtendedUserDetailsService extends UserDetailsService {

	UserDetails loadUserById(String userId, String providerId) throws UsernameNotFoundException;
}
