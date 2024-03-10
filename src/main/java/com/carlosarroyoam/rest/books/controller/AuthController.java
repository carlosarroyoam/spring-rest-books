package com.carlosarroyoam.rest.books.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carlosarroyoam.rest.books.dto.LoginRequest;
import com.carlosarroyoam.rest.books.dto.LoginResponse;
import com.carlosarroyoam.rest.books.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Operations about auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping(consumes = "application/json", produces = "application/json")
	@Operation(summary = "Auths a user")
	public ResponseEntity<LoginResponse> auth(@Valid @RequestBody LoginRequest loginRequest) {
		LoginResponse loginResponse = authService.auth(loginRequest);
		return ResponseEntity.ok(loginResponse);
	}

}
