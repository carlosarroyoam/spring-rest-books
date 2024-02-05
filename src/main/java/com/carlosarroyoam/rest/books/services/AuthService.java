package com.carlosarroyoam.rest.books.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.carlosarroyoam.rest.books.dtos.LoginRequest;
import com.carlosarroyoam.rest.books.dtos.LoginResponse;
import com.carlosarroyoam.rest.books.entities.User;
import com.carlosarroyoam.rest.books.repositories.UserRepository;

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
		User userByEmail = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

		return new LoginResponse(userByEmail.getEmail(), tokenService.generateToken(authentication));
	}

}
