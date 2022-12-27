package com.carlosarroyoam.bookservice.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carlosarroyoam.bookservice.config.OpenApiConfig;
import com.carlosarroyoam.bookservice.entities.User;
import com.carlosarroyoam.bookservice.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/users")
@Tag(name = "User")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class UserController {
	private final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;

	@GetMapping(produces = "application/json")
	@Operation(summary = "Gets a list of users")
	public ResponseEntity<List<User>> findAll() {
		List<User> users = userService.findAll();

		return ResponseEntity.ok(users);
	}

	@GetMapping(path = "/{id}", produces = "application/json")
	@Operation(summary = "Gets a user by its id")
	public ResponseEntity<User> findById(@PathVariable Long id) {
		User userById = userService.findById(id);

		return ResponseEntity.ok(userById);
	}
}
