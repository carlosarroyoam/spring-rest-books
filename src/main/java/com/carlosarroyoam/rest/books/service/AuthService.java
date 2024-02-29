package com.carlosarroyoam.rest.books.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.carlosarroyoam.rest.books.dto.LoginRequest;
import com.carlosarroyoam.rest.books.dto.LoginResponse;
import com.carlosarroyoam.rest.books.entity.User;
import com.carlosarroyoam.rest.books.repository.UserRepository;

@Service
public class AuthService {

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
		User userByEmail = userRepository.findByEmail(loginRequest.getEmail())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

		LoginResponse loginResponse = new LoginResponse();
		loginResponse.setEmail(userByEmail.getEmail());
		loginResponse.setAccessToken(tokenService.generateToken(authentication));
		return loginResponse;
	}

}
