package com.carlosarroyoam.rest.books.services;

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

	public BookResponse save(CreateBookRequest createBookRequest) {
		Book book = bookMapper.createRequestToEntity(createBookRequest);
		Book savedBook = bookRepository.save(book);
		return bookMapper.toDto(savedBook);
	}

	public BookResponse update(Long id, UpdateBookRequest updateBookRequest) {
		Book findById = bookRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

		findById.setTitle(updateBookRequest.getTitle());
		findById.setAuthor(updateBookRequest.getAuthor());
		findById.setPrice(updateBookRequest.getPrice());
		findById.setPublishedAt(updateBookRequest.getPublishedAt());
		findById.setAvailableOnline(updateBookRequest.isAvailableOnline());

		Book updatedBook = bookRepository.save(findById);
		return bookMapper.toDto(updatedBook);
	}

	public void deleteById(Long id) {
		Book findById = bookRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

		bookRepository.delete(findById);
	}

}
