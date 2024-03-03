package com.carlosarroyoam.rest.books.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.carlosarroyoam.rest.books.constant.AppMessages;
import com.carlosarroyoam.rest.books.dto.AuthorResponse;
import com.carlosarroyoam.rest.books.dto.BookResponse;
import com.carlosarroyoam.rest.books.dto.CreateBookRequest;
import com.carlosarroyoam.rest.books.dto.UpdateBookRequest;
import com.carlosarroyoam.rest.books.entity.Book;
import com.carlosarroyoam.rest.books.mapper.AuthorMapper;
import com.carlosarroyoam.rest.books.mapper.BookMapper;
import com.carlosarroyoam.rest.books.repository.BookRepository;

import jakarta.transaction.Transactional;

@Service
public class BookService {

	private static final Logger log = LoggerFactory.getLogger(BookService.class);
	private final BookRepository bookRepository;
	private final BookMapper bookMapper;
	private final AuthorMapper authorMapper;

	public BookService(BookRepository bookRepository, BookMapper bookMapper, AuthorMapper authorMapper) {
		this.bookRepository = bookRepository;
		this.bookMapper = bookMapper;
		this.authorMapper = authorMapper;
	}

	public List<BookResponse> findAll(Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Book> books = bookRepository.findAll(pageable);
		return bookMapper.toDtos(books.getContent());
	}

	public BookResponse findById(Long bookId) {
		Book bookById = bookRepository.findById(bookId).orElseThrow(() -> {
			log.warn(AppMessages.USER_NOT_FOUND_EXCEPTION);
			return new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.BOOK_NOT_FOUND_EXCEPTION);
		});

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
	public void update(Long bookId, UpdateBookRequest updateBookRequest) {
		Book bookById = bookRepository.findById(bookId).orElseThrow(() -> {
			log.warn(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
			return new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.BOOK_NOT_FOUND_EXCEPTION);
		});

		bookById.setTitle(updateBookRequest.getTitle());
		bookById.setPrice(updateBookRequest.getPrice());
		bookById.setPublishedAt(updateBookRequest.getPublishedAt());
		bookById.setIsAvailableOnline(updateBookRequest.getIsAvailableOnline());
		bookById.setUpdatedAt(LocalDateTime.now());

		bookRepository.save(bookById);
	}

	@Transactional
	public void deleteById(Long bookId) {
		boolean existsBookById = bookRepository.existsById(bookId);
		if (Boolean.FALSE.equals(existsBookById)) {
			log.warn(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.BOOK_NOT_FOUND_EXCEPTION);
		}

		bookRepository.deleteById(bookId);
	}

	public List<AuthorResponse> findAuthorsByBookId(Long bookId) {
		Book bookById = bookRepository.findById(bookId).orElseThrow(() -> {
			log.warn(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
			return new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.BOOK_NOT_FOUND_EXCEPTION);
		});

		return authorMapper.toDtos(bookById.getAuthors());
	}

}
