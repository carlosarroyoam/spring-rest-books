package com.carlosarroyoam.rest.books.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.carlosarroyoam.rest.books.entities.Book;
import com.carlosarroyoam.rest.books.repositories.BookRepository;

@Service
public class BookService {

	private final BookRepository bookRepository;

	public BookService(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}

	public List<Book> findAll() {
		return bookRepository.findAll();
	}

	public Book findById(Long id) {
		return bookRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
	}

	public Book save(Book book) {
		book.setPublishedAt(LocalDate.now());
		return bookRepository.save(book);
	}

	public Book update(Long id, Book book) {
		Book findById = bookRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

		findById.setTitle(book.getTitle());
		findById.setAuthor(book.getAuthor());
		findById.setPrice(book.getPrice());
		findById.setPublishedAt(book.getPublishedAt());
		findById.setAvailableOnline(book.isAvailableOnline());

		return bookRepository.save(findById);
	}

	public void deleteById(Long id) {
		Book findById = bookRepository.findById(id)
				.orElseThrow(() 
						-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

		bookRepository.delete(findById);
	}

}
