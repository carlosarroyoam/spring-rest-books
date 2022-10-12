package com.example.demospringrest.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.demospringrest.dtos.LoginRequest;
import com.example.demospringrest.dtos.LoginResponse;
import com.example.demospringrest.entities.User;
import com.example.demospringrest.repositories.UserRepository;

@Service
public class AuthService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private UserRepository userRepository;

	public LoginResponse auth(LoginRequest loginRequest) {
		User userByEmail = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

		LoginResponse loginResponse = new LoginResponse();

		loginResponse.setEmail(userByEmail.getEmail());
		loginResponse.setJwt(tokenService.generateToken(authentication));

		return loginResponse;
	}
}
