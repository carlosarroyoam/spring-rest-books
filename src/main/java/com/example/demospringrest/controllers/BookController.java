package com.example.demospringrest.controllers;

import java.util.List;

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

import com.example.demospringrest.entities.Book;
import com.example.demospringrest.repositories.BookRepository;

@RestController
@RequestMapping("/books")
public class BookController {
	@Autowired
	private BookRepository bookRepository;

	@GetMapping
	public ResponseEntity<List<Book>> findAll() {
		List<Book> books = bookRepository.findAll();

		return ResponseEntity.ok(books);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Book> findById(@PathVariable Long id) {
		Book book = bookRepository.findById(id).orElseThrow();

		return ResponseEntity.ok(book);
	}

	@PostMapping
	public ResponseEntity<Book> store(@RequestBody Book book, UriComponentsBuilder b) {
		Book createdBook = bookRepository.save(book);

		UriComponents uriComponents = b.path("/books/{id}").buildAndExpand(createdBook.getId());

		return ResponseEntity.created(uriComponents.toUri()).body(createdBook);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Book> update(@PathVariable Long id, @RequestBody Book book) {
		Book findById = bookRepository.findById(id).orElseThrow();

		findById.setTitle(book.getTitle());
		findById.setAuthor(book.getAuthor());
		findById.setPrice(book.getPrice());
		findById.setPublishedAt(book.getPublishedAt());
		findById.setAvailableOnline(book.isAvailableOnline());

		Book updatedBook = bookRepository.save(findById);

		return ResponseEntity.ok(updatedBook);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> destroy(@PathVariable Long id) {
		bookRepository.deleteById(id);

		return ResponseEntity.noContent().build();
	}
}
