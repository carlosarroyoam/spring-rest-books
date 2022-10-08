package com.example.demospringrest.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demospringrest.dtos.LoginResponse;
import com.example.demospringrest.services.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TokenService tokenService;

	@PostMapping(produces = "application/json")
	@Operation(summary = "Auths a user")
	public ResponseEntity<LoginResponse> auth(Authentication authentication) {
		logger.info("Token requested for: {}", authentication.getName());

		LoginResponse loginResponse = new LoginResponse();

		loginResponse.setEmail(authentication.getName());
		loginResponse.setJwt(tokenService.generateToken(authentication));

		logger.info("Token granted: {}", loginResponse.getJwt());

		return ResponseEntity.ok(loginResponse);
	}
}
