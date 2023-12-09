package com.carlosarroyoam.bookservice.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carlosarroyoam.bookservice.dtos.LoginRequest;
import com.carlosarroyoam.bookservice.dtos.LoginResponse;
import com.carlosarroyoam.bookservice.services.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping(produces = "application/json")
	@Operation(summary = "Auths a user")
	public ResponseEntity<LoginResponse> auth(@RequestBody LoginRequest loginRequest) {
		log.info("authenticating user");
		return ResponseEntity.ok(authService.auth(loginRequest));
	}

}
