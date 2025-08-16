package com.carlosarroyoam.rest.books.book;

import com.carlosarroyoam.rest.books.author.dto.AuthorDto;
import com.carlosarroyoam.rest.books.author.dto.AuthorDto.AuthorDtoMapper;
import com.carlosarroyoam.rest.books.book.dto.BookDto;
import com.carlosarroyoam.rest.books.book.dto.BookDto.BookDtoMapper;
import com.carlosarroyoam.rest.books.book.dto.BookFilterDto;
import com.carlosarroyoam.rest.books.book.dto.CreateBookRequestDto;
import com.carlosarroyoam.rest.books.book.dto.UpdateBookRequestDto;
import com.carlosarroyoam.rest.books.book.entity.Book;
import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

  public List<BookDto> findAll(Pageable pageable, BookFilterDto filters) {
    Specification<Book> spec = Specification.unrestricted();
    spec = spec.and(BookSpecification.isbnEquals(filters.getIsbn()))
        .and(BookSpecification.titleContains(filters.getTitle()))
        .and(BookSpecification.authorIdIn(filters.getAuthorIds()))
        .and(BookSpecification.isAvailableOnline(filters.getIsAvailableOnline()));

    Page<Book> books = bookRepository.findAll(spec, pageable);
    return BookDtoMapper.INSTANCE.toDtos(books.getContent());
  }

  public BookDto findById(Long bookId) {
    Book bookById = bookRepository.findById(bookId).orElseThrow(() -> {
      log.warn(AppMessages.BOOK_NOT_FOUND_EXCEPTION);
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
    bookRepository.save(book);
    return BookDtoMapper.INSTANCE.toDto(book);
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
