package com.carlosarroyoam.rest.books.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.carlosarroyoam.rest.books.config.security.SecurityUser;
import com.carlosarroyoam.rest.books.constant.AppMessages;
import com.carlosarroyoam.rest.books.dto.ChangePasswordRequest;
import com.carlosarroyoam.rest.books.dto.CreateUserRequest;
import com.carlosarroyoam.rest.books.dto.UpdateUserRequest;
import com.carlosarroyoam.rest.books.dto.UserResponse;
import com.carlosarroyoam.rest.books.entity.User;
import com.carlosarroyoam.rest.books.mapper.UserMapper;
import com.carlosarroyoam.rest.books.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService implements UserDetailsService {

	private static final Logger log = LoggerFactory.getLogger(UserService.class);
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByEmail(username).map(SecurityUser::new).orElseThrow(() -> {
			log.warn(AppMessages.USER_NOT_FOUND_EXCEPTION);
			return new UsernameNotFoundException("Username not found: " + username);
		});
	}

	public List<UserResponse> findAll(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<User> users = userRepository.findAll(pageable);
		return userMapper.toDtos(users.getContent());
	}

	public UserResponse findById(Long userId) {
		User userById = userRepository.findById(userId).orElseThrow(() -> {
			log.warn(AppMessages.USER_NOT_FOUND_EXCEPTION);
			return new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.USER_NOT_FOUND_EXCEPTION);
		});

		return userMapper.toDto(userById);
	}

	@Transactional
	public UserResponse create(CreateUserRequest createUserRequest) {
		if (userRepository.existsByUsername(createUserRequest.getUsername())) {
			log.warn(AppMessages.USERNAME_ALREADY_EXISTS_EXCEPTION);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.USERNAME_ALREADY_EXISTS_EXCEPTION);
		}

		if (userRepository.existsByEmail(createUserRequest.getEmail())) {
			log.warn(AppMessages.EMAIL_ALREADY_EXISTS_EXCEPTION);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.EMAIL_ALREADY_EXISTS_EXCEPTION);
		}

		LocalDateTime now = LocalDateTime.now();
		User user = userMapper.toEntity(createUserRequest);
		user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
		user.setIsActive(Boolean.FALSE);
		user.setCreatedAt(now);
		user.setUpdatedAt(now);

		User savedUser = userRepository.save(user);
		return userMapper.toDto(savedUser);
	}

	@Transactional
	public void update(Long userId, UpdateUserRequest updateUserRequest) {
		User userById = userRepository.findById(userId).orElseThrow(() -> {
			log.warn(AppMessages.USER_NOT_FOUND_EXCEPTION);
			return new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.USER_NOT_FOUND_EXCEPTION);
		});

		if (updateUserRequest.getName() != null)
			userById.setName(updateUserRequest.getName());

		if (updateUserRequest.getAge() != null)
			userById.setAge(updateUserRequest.getAge());

		userById.setUpdatedAt(LocalDateTime.now());
		userRepository.save(userById);
	}

	@Transactional
	public void deleteById(Long userId) {
		User userById = userRepository.findById(userId).orElseThrow(() -> {
			log.warn(AppMessages.USER_NOT_FOUND_EXCEPTION);
			return new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.USER_NOT_FOUND_EXCEPTION);
		});

		userById.setIsActive(Boolean.FALSE);
		userById.setUpdatedAt(LocalDateTime.now());

		userRepository.save(userById);
	}

	@Transactional
	public void changePassword(Long userId, ChangePasswordRequest changePasswordRequest) {
		User userById = userRepository.findById(userId).orElseThrow(() -> {
			log.warn(AppMessages.USER_NOT_FOUND_EXCEPTION);
			return new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.USER_NOT_FOUND_EXCEPTION);
		});

		if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), userById.getPassword())) {
			log.warn(AppMessages.UNAUTHORIZED_CREDENTIALS_EXCEPTION);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.UNAUTHORIZED_CREDENTIALS_EXCEPTION);
		}

		if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
			log.warn(AppMessages.PASSWORDS_NOT_MATCH_EXCEPTION);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PASSWORDS_NOT_MATCH_EXCEPTION);
		}

		userById.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
		userById.setUpdatedAt(LocalDateTime.now());

		userRepository.save(userById);
	}

}
