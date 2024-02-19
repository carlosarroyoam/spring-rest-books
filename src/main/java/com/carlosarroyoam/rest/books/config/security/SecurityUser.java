package com.carlosarroyoam.rest.books.config.security;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.carlosarroyoam.rest.books.entities.User;

public class SecurityUser implements UserDetails {

	private static final long serialVersionUID = -2504285061755069191L;
	private final transient User user;

	public SecurityUser(User user) {
		this.user = user;
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Arrays.asList(new SimpleGrantedAuthority(user.getRole().getTitle()));
	}

	@Override
	public boolean isAccountNonExpired() {
		return user.isActive();
	}

	@Override
	public boolean isAccountNonLocked() {
		return user.isActive();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return user.isActive();
	}

	@Override
	public boolean isEnabled() {
		return user.isActive();
	}

}