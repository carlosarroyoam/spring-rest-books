package com.carlosarroyoam.bookservice.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.carlosarroyoam.bookservice.configurations.security.SecurityUser;
import com.carlosarroyoam.bookservice.entities.User;
import com.carlosarroyoam.bookservice.repositories.UserRepository;

@Service
public class UserService implements UserDetailsService {
	private final Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByEmail(username).map(SecurityUser::new)
				.orElseThrow(() -> new UsernameNotFoundException("Username not found: " + username));
	}

	public List<User> findAll() {
		return userRepository.findAll();
	}

	public User findById(Long id) {
		return userRepository.findById(id).orElseThrow();
	}
}
