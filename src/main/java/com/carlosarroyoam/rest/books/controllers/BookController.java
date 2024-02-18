package com.carlosarroyoam.rest.books.controllers;

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
import com.carlosarroyoam.rest.books.dtos.BookResponse;
import com.carlosarroyoam.rest.books.dtos.CreateBookRequest;
import com.carlosarroyoam.rest.books.dtos.UpdateBookRequest;
import com.carlosarroyoam.rest.books.services.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/books")
@Tag(name = "Books")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class BookController {

	private final BookService bookService;

	public BookController(BookService bookService) {
		this.bookService = bookService;
	}

	@GetMapping(produces = "application/json")
	@Operation(summary = "Gets the list of books")
	public ResponseEntity<List<BookResponse>> findAll() {
		List<BookResponse> books = bookService.findAll();
		return ResponseEntity.ok(books);
	}

	@GetMapping(value = "/{bookId}", produces = "application/json")
	@Operation(summary = "Gets a book by its id")
	public ResponseEntity<BookResponse> findById(@PathVariable Long bookId) {
		BookResponse bookById = bookService.findById(bookId);
		return ResponseEntity.ok(bookById);
	}

	@PostMapping(consumes = "application/json", produces = "application/json")
	@Operation(summary = "Stores a new book")
	public ResponseEntity<BookResponse> create(@Valid @RequestBody CreateBookRequest createBookRequest,
			UriComponentsBuilder builder) {
		BookResponse createdBook = bookService.create(createBookRequest);
		UriComponents uriComponents = builder.path("/books/{id}").buildAndExpand(createdBook.getId());
		return ResponseEntity.created(uriComponents.toUri()).body(createdBook);
	}

	@PutMapping(value = "/{bookId}", consumes = "application/json", produces = "application/json")
	@Operation(summary = "Updates a book by its id")
	public ResponseEntity<BookResponse> update(@PathVariable Long bookId,
			@Valid @RequestBody UpdateBookRequest updateBookRequest) {
		BookResponse updatedBook = bookService.update(bookId, updateBookRequest);
		return ResponseEntity.ok(updatedBook);
	}

	@DeleteMapping("/{bookId}")
	@Operation(summary = "Deletes a book by its id")
	public ResponseEntity<Void> deleteById(@PathVariable Long bookId) {
		bookService.deleteById(bookId);
		return ResponseEntity.noContent().build();
	}

}
