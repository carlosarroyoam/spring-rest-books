package com.example.demospringrest.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demospringrest.dtos.LoginRequest;
import com.example.demospringrest.dtos.LoginResponse;
import com.example.demospringrest.services.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AuthService authService;

	@PostMapping(produces = "application/json")
	@Operation(summary = "Auths a user")
	public ResponseEntity<LoginResponse> auth(@RequestBody LoginRequest loginRequest) {
		LoginResponse loginResponse = authService.auth(loginRequest);

		return ResponseEntity.ok(loginResponse);
	}
}
