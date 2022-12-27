package com.carlosarroyoam.bookservice.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.carlosarroyoam.bookservice.config.OpenApiConfig;
import com.carlosarroyoam.bookservice.entities.Book;
import com.carlosarroyoam.bookservice.services.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/books")
@Tag(name = "Books")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class BookController {
	private final Logger logger = LoggerFactory.getLogger(BookController.class);

	@Autowired
	private BookService bookService;

	@GetMapping(produces = "application/json")
	@Operation(summary = "Get a list of books")
	public ResponseEntity<List<Book>> findAll() {
		List<Book> books = bookService.findAll();

		return ResponseEntity.ok(books);
	}

	@GetMapping(value = "/{bookId}", produces = "application/json")
	@Operation(summary = "Get a book by its id")
	public ResponseEntity<Book> findById(@PathVariable Long bookId) {
		Book book = bookService.findById(bookId);

		return ResponseEntity.ok(book);
	}

	@PostMapping(consumes = "application/json", produces = "application/json")
	@Operation(summary = "Stores a book")
	public ResponseEntity<Book> store(@RequestBody Book book, UriComponentsBuilder b) {
		Book createdBook = bookService.save(book);

		UriComponents uriComponents = b.path("/books/{id}").buildAndExpand(createdBook.getId());

		logger.info("Stored new book: {}", createdBook);

		return ResponseEntity.created(uriComponents.toUri()).body(createdBook);
	}

	@PutMapping(value = "/{bookId}", consumes = "application/json", produces = "application/json")
	@Operation(summary = "Updates a book by its id")
	public ResponseEntity<Book> update(@PathVariable Long bookId, @RequestBody Book book) throws Exception {
		Book updatedBook = bookService.update(bookId, book);

		logger.info("Updated book: {}", updatedBook);

		return ResponseEntity.ok(updatedBook);
	}

	@DeleteMapping("/{bookId}")
	@Operation(summary = "Deletes a book by its id")
	public ResponseEntity<?> destroy(@PathVariable Long bookId) {
		bookService.deleteById(bookId);

		logger.info("Deleted book with id: {}", bookId);

		return ResponseEntity.noContent().build();
	}
}
