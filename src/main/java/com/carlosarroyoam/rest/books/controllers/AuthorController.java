package com.carlosarroyoam.rest.books.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carlosarroyoam.rest.books.config.OpenApiConfig;
import com.carlosarroyoam.rest.books.dtos.AuthorResponse;
import com.carlosarroyoam.rest.books.services.AuthorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/authors")
@Tag(name = "Author")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class AuthorController {

	private final AuthorService authorService;

	public AuthorController(AuthorService authorService) {
		this.authorService = authorService;
	}

	@GetMapping(produces = "application/json")
	@Operation(summary = "Gets the list of authors")
	public ResponseEntity<List<AuthorResponse>> findAll() {
		List<AuthorResponse> authors = authorService.findAll();
		return ResponseEntity.ok(authors);
	}

	@GetMapping(path = "/{authorId}", produces = "application/json")
	@Operation(summary = "Gets an author by its id")
	public ResponseEntity<AuthorResponse> findById(@PathVariable Long authorId) {
		AuthorResponse authorById = authorService.findById(authorId);
		return ResponseEntity.ok(authorById);
	}

}
