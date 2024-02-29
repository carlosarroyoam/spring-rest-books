package com.carlosarroyoam.rest.books.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carlosarroyoam.rest.books.dtos.LoginRequest;
import com.carlosarroyoam.rest.books.dtos.LoginResponse;
import com.carlosarroyoam.rest.books.services.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping(produces = "application/json")
	@Operation(summary = "Auths a user")
	public ResponseEntity<LoginResponse> auth(@Valid @RequestBody LoginRequest loginRequest) {
		LoginResponse loginResponse = authService.auth(loginRequest);
		return ResponseEntity.ok(loginResponse);
	}

}
