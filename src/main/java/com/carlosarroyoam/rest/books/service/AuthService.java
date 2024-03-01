package com.carlosarroyoam.rest.books.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.carlosarroyoam.rest.books.constant.AppMessages;
import com.carlosarroyoam.rest.books.dto.LoginRequest;
import com.carlosarroyoam.rest.books.dto.LoginResponse;
import com.carlosarroyoam.rest.books.entity.User;
import com.carlosarroyoam.rest.books.repository.UserRepository;

@Service
public class AuthService {

	private static final Logger log = LoggerFactory.getLogger(AuthService.class);
	private final AuthenticationManager authenticationManager;
	private final TokenService tokenService;
	private final UserRepository userRepository;

	public AuthService(AuthenticationManager authenticationManager, TokenService tokenService,
			UserRepository userRepository) {
		this.authenticationManager = authenticationManager;
		this.tokenService = tokenService;
		this.userRepository = userRepository;
	}

	public LoginResponse auth(LoginRequest loginRequest) {
		User userByEmail = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> {
			log.warn(AppMessages.USER_NOT_FOUND_EXCEPTION);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.USER_NOT_FOUND_EXCEPTION);
		});

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

		LoginResponse loginResponse = new LoginResponse();
		loginResponse.setEmail(userByEmail.getEmail());
		loginResponse.setAccessToken(tokenService.generateToken(authentication));
		return loginResponse;
	}

}
