package com.carlosarroyoam.rest.books.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.carlosarroyoam.rest.books.config.OpenApiConfig;
import com.carlosarroyoam.rest.books.dto.AuthorResponse;
import com.carlosarroyoam.rest.books.dto.CreateAuthorRequest;
import com.carlosarroyoam.rest.books.dto.UpdateAuthorRequest;
import com.carlosarroyoam.rest.books.service.AuthorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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
	public ResponseEntity<AuthorResponse> findById(@PathVariable("authorId") Long authorId) {
		AuthorResponse authorById = authorService.findById(authorId);
		return ResponseEntity.ok(authorById);
	}

	@PostMapping(consumes = "application/json")
	@Operation(summary = "Creates a new author")
	public ResponseEntity<Void> create(@Valid @RequestBody CreateAuthorRequest createAuthorRequest,
			UriComponentsBuilder builder) {
		AuthorResponse createdAuthor = authorService.create(createAuthorRequest);
		UriComponents uriComponents = builder.path("/authors/{authorId}").buildAndExpand(createdAuthor.getId());
		return ResponseEntity.created(uriComponents.toUri()).build();
	}

	@PutMapping(value = "/{authorId}", consumes = "application/json")
	@Operation(summary = "Updates a author by its id")
	public ResponseEntity<Void> update(@PathVariable("authorId") Long authorId,
			@Valid @RequestBody UpdateAuthorRequest updateAuthorRequest) {
		authorService.update(authorId, updateAuthorRequest);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{authorId}")
	@Operation(summary = "Deletes a author by its id")
	public ResponseEntity<Void> deleteById(@PathVariable("authorId") Long authorId) {
		authorService.deleteById(authorId);
		return ResponseEntity.noContent().build();
	}

}
