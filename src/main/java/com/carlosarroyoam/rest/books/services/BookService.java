package com.carlosarroyoam.rest.books.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.carlosarroyoam.rest.books.dtos.BookResponse;
import com.carlosarroyoam.rest.books.dtos.CreateBookRequest;
import com.carlosarroyoam.rest.books.dtos.UpdateBookRequest;
import com.carlosarroyoam.rest.books.entities.Book;
import com.carlosarroyoam.rest.books.mappers.BookMapper;
import com.carlosarroyoam.rest.books.repositories.BookRepository;

import jakarta.transaction.Transactional;

@Service
public class BookService {

	private final BookRepository bookRepository;
	private final BookMapper bookMapper;

	public BookService(BookRepository bookRepository, BookMapper bookMapper) {
		this.bookRepository = bookRepository;
		this.bookMapper = bookMapper;
	}

	public List<BookResponse> findAll() {
		List<Book> books = bookRepository.findAll();
		return bookMapper.toDtos(books);
	}

	public BookResponse findById(Long id) {
		Book bookById = bookRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

		return bookMapper.toDto(bookById);
	}

	@Transactional
	public BookResponse create(CreateBookRequest createBookRequest) {
		LocalDateTime now = LocalDateTime.now();
		Book book = bookMapper.createRequestToEntity(createBookRequest);
		book.setCreatedAt(now);
		book.setUpdatedAt(now);

		Book savedBook = bookRepository.save(book);
		return bookMapper.toDto(savedBook);
	}

	@Transactional
	public BookResponse update(Long id, UpdateBookRequest updateBookRequest) {
		Book findById = bookRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

		findById.setTitle(updateBookRequest.getTitle());
		findById.setPrice(updateBookRequest.getPrice());
		findById.setPublishedAt(updateBookRequest.getPublishedAt());
		findById.setAvailableOnline(updateBookRequest.isAvailableOnline());
		findById.setUpdatedAt(LocalDateTime.now());

		Book updatedBook = bookRepository.save(findById);
		return bookMapper.toDto(updatedBook);
	}

	@Transactional
	public void deleteById(Long id) {
		Book findById = bookRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

		bookRepository.delete(findById);
	}

}
