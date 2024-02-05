package com.carlosarroyoam.rest.books.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.carlosarroyoam.rest.books.config.security.SecurityUser;
import com.carlosarroyoam.rest.books.dtos.UserResponse;
import com.carlosarroyoam.rest.books.entities.User;
import com.carlosarroyoam.rest.books.mappers.UserMapper;
import com.carlosarroyoam.rest.books.repositories.UserRepository;

@Service
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;

	public UserService(UserRepository userRepository, UserMapper userMapper) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByEmail(username).map(SecurityUser::new)
				.orElseThrow(() -> new UsernameNotFoundException("Username not found: " + username));
	}

	public List<UserResponse> findAll() {
		List<User> users = userRepository.findAll();
		return userMapper.toDtos(users);
	}

	public UserResponse findById(Long userId) {
		User userById = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		return userMapper.toDto(userById);
	}

}
