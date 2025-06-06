package com.carlosarroyoam.rest.books.service;

import com.carlosarroyoam.rest.books.constant.AppMessages;
import com.carlosarroyoam.rest.books.dto.AuthorDto;
import com.carlosarroyoam.rest.books.dto.AuthorDto.AuthorDtoMapper;
import com.carlosarroyoam.rest.books.dto.BookDto;
import com.carlosarroyoam.rest.books.dto.BookDto.BookDtoMapper;
import com.carlosarroyoam.rest.books.dto.CreateBookRequestDto;
import com.carlosarroyoam.rest.books.dto.UpdateBookRequestDto;
import com.carlosarroyoam.rest.books.entity.Book;
import com.carlosarroyoam.rest.books.repository.BookRepository;
import jakarta.transaction.Transactional;
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

@Service
public class BookService {
  private static final Logger log = LoggerFactory.getLogger(BookService.class);
  private final BookRepository bookRepository;

  public BookService(final BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public List<BookDto> findAll(Integer page, Integer size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Book> books = bookRepository.findAll(pageable);
    return BookDtoMapper.INSTANCE.toDtos(books.getContent());
  }

  public BookDto findById(Long bookId) {
    Book bookById = bookRepository.findById(bookId).orElseThrow(() -> {
      log.warn(AppMessages.USER_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.BOOK_NOT_FOUND_EXCEPTION);
    });

    return BookDtoMapper.INSTANCE.toDto(bookById);
  }

  @Transactional
  public BookDto create(CreateBookRequestDto requestDto) {
    if (bookRepository.existsByIsbn(requestDto.getIsbn())) {
      log.warn(AppMessages.ISBN_ALREADY_EXISTS_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          AppMessages.ISBN_ALREADY_EXISTS_EXCEPTION);
    }

    LocalDateTime now = LocalDateTime.now();
    Book book = BookDtoMapper.INSTANCE.createRequestToEntity(requestDto);
    book.setCreatedAt(now);
    book.setUpdatedAt(now);

    Book savedBook = bookRepository.save(book);
    return BookDtoMapper.INSTANCE.toDto(savedBook);
  }

  @Transactional
  public void update(Long bookId, UpdateBookRequestDto requestDto) {
    Book bookById = bookRepository.findById(bookId).orElseThrow(() -> {
      log.warn(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.BOOK_NOT_FOUND_EXCEPTION);
    });

    bookById.setTitle(requestDto.getTitle());
    bookById.setCoverUrl(requestDto.getCoverUrl());
    bookById.setPrice(requestDto.getPrice());
    bookById.setPublishedAt(requestDto.getPublishedAt());
    bookById.setIsAvailableOnline(requestDto.getIsAvailableOnline());
    bookById.setUpdatedAt(LocalDateTime.now());

    bookRepository.save(bookById);
  }

  @Transactional
  public void deleteById(Long bookId) {
    if (Boolean.FALSE.equals(bookRepository.existsById(bookId))) {
      log.warn(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.BOOK_NOT_FOUND_EXCEPTION);
    }

    bookRepository.deleteById(bookId);
  }

  public List<AuthorDto> findAuthorsByBookId(Long bookId) {
    Book bookById = bookRepository.findById(bookId).orElseThrow(() -> {
      log.warn(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.BOOK_NOT_FOUND_EXCEPTION);
    });

    return AuthorDtoMapper.INSTANCE.toDtos(bookById.getAuthors());
  }
}
