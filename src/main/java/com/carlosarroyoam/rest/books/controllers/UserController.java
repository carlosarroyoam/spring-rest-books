package com.carlosarroyoam.rest.books.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carlosarroyoam.rest.books.config.OpenApiConfig;
import com.carlosarroyoam.rest.books.entities.User;
import com.carlosarroyoam.rest.books.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/users")
@Tag(name = "User")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping(produces = "application/json")
	@Operation(summary = "Gets the list of users")
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
