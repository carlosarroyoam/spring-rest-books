package com.carlosarroyoam.bookservice.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.carlosarroyoam.bookservice.entities.Book;
import com.carlosarroyoam.bookservice.repositories.BookRepository;

@Service
public class BookService {
	private final Logger logger = LoggerFactory.getLogger(BookService.class);

	@Autowired
	private BookRepository bookRepository;

	public List<Book> findAll() {
		return bookRepository.findAll();
	}

	public Book findById(Long id) {
		return bookRepository.findById(id).orElseThrow();
	}

	public Book save(Book book) {
		return bookRepository.save(book);
	}

	public Book update(Long id, Book book) {
		Book findById = bookRepository.findById(id).orElseThrow();

		findById.setTitle(book.getTitle());
		findById.setAuthor(book.getAuthor());
		findById.setPrice(book.getPrice());
		findById.setPublishedAt(book.getPublishedAt());
		findById.setAvailableOnline(book.isAvailableOnline());

		return bookRepository.save(findById);
	}

	public void deleteById(Long id) {
		Book findById = bookRepository.findById(id).orElseThrow();

		bookRepository.delete(findById);
	}
}
